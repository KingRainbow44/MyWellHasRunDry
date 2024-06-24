package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.managers.PartyManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.awt.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Slf4j
public final class PartyCommand {
    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var party = dispatcher.register(literal("party")
            .requires(ServerCommandSource::isExecutedByPlayer)
            .then(literal("create")
                .executes(PartyCommand::createParty))
            .then(literal("accept")
                .executes(PartyCommand::acceptInvite))
            .then(literal("leave")
                .executes(PartyCommand::leaveParty))
            .then(literal("kick")
                .then(argument("username", StringArgumentType.word())
                    .executes(PartyCommand::kickPlayer))
                .executes(PartyCommand::usage))
            .then(literal("decline")
                .executes(PartyCommand::declineInvite))
            .then(literal("disband")
                .executes(PartyCommand::disbandParty))
            .then(literal("warp")
                .executes(PartyCommand::warpParty))
            .then(literal("return")
                .executes(PartyCommand::returnPlayer))
            .then(argument("username", StringArgumentType.word())
                .executes(PartyCommand::invitePlayer))
            .executes(PartyCommand::usage));
        dispatcher.register(literal("p").redirect(party));
    }

    private static int usage(CommandContext<ServerCommandSource> context) {
        context.getSource().sendError(Text.literal("Usage: /party [username|create|accept|leave|kick|decline|disband|warp] [username]"));
        return 1;
    }

    private static int createParty(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return -1;

        // Check if the player is in a party.
        if (PartyManager.findParty(player) != null) {
            context.getSource().sendError(Text.literal("You are already in a party."));
            return 1;
        }

        PartyManager.createParty(player);
        context.getSource().sendMessage(Text.literal("You have created a party.")
            .withColor(Color.GREEN.getRGB()));

        return 1;
    }

    private static int disbandParty(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return -1;

        var party = PartyManager.findParty(player);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party."));
            return 1;
        }

        // Check if the player is the leader.
        var leader = PartyManager.findLeader(party);
        if (!leader.equals(player.getUuid())) {
            context.getSource().sendError(Text.literal("You are not the leader of the party."));
            return 1;
        }

        PartyManager.disbandParty(player);
        context.getSource().sendMessage(Text.literal("You have disbanded the party.")
            .withColor(Color.RED.getRGB()));

        return 1;
    }

    private static int invitePlayer(CommandContext<ServerCommandSource> context) {
        var leader = context.getSource().getPlayer();
        if (leader == null) return 1;

        // Check if the player has a party.
        var party = PartyManager.findParty(leader);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party."));
            return 1;
        }

        var username = StringArgumentType.getString(context, "username");

        // Check if the player is online.
        var target = context.getSource().getServer()
            .getPlayerManager()
            .getPlayer(username);
        if (target == null) {
            context.getSource().sendError(Text.literal("The player is not online."));
            return 1;
        }

        // Invite the player.
        if (!PartyManager.invitePlayer(leader, target)) {
            context.getSource().sendError(Text.literal("Unable to invite the player."));
        } else {
            context.getSource().sendMessage(Text.literal("Player invited!")
                .withColor(Color.GREEN.getRGB()));
        }

        return 1;
    }

    private static int acceptInvite(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return -1;

        if (!PartyManager.acceptInvite(player)) {
            context.getSource().sendError(Text.literal("Unable to accept the invite."));
        }

        return 1;
    }

    private static int leaveParty(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return -1;

        var party = PartyManager.findParty(player);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party."));
            return 1;
        }

        PartyManager.leaveParty(player);
        context.getSource().sendMessage(Text.literal("You have left the party.")
            .withColor(Color.RED.getRGB()));

        return 1;
    }

    private static int kickPlayer(CommandContext<ServerCommandSource> context) {
        var leader = context.getSource().getPlayer();
        if (leader == null) return -1;

        var username = StringArgumentType.getString(context, "username");

        // Check if the player is online.
        var target = context.getSource().getServer()
            .getPlayerManager()
            .getPlayer(username);
        if (target == null) {
            context.getSource().sendError(Text.literal("The player is not online."));
            return 1;
        }

        var party = PartyManager.findParty(leader);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party."));
            return 1;
        }

        // Check if the player is the leader.
        var leaderId = PartyManager.findLeader(party);
        if (!leaderId.equals(leader.getUuid())) {
            context.getSource().sendError(Text.literal("You are not the leader of the party."));
            return 1;
        }

        // Check if the player is in the party.
        if (!party.contains(target)) {
            context.getSource().sendError(Text.literal("The player is not in the party."));
            return 1;
        }

        PartyManager.kickPlayer(leader, target);

        return 1;
    }

    private static int declineInvite(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return -1;

        if (!PartyManager.declineInvite(player)) {
            context.getSource().sendError(Text.literal("There was no invite to decline."));
        } else {
            context.getSource().sendMessage(Text.literal("Invite declined.")
                .withColor(Color.YELLOW.getRGB()));
        }

        return 1;
    }

    private static int warpParty(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return -1;

        var party = PartyManager.findParty(player);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party."));
            return 1;
        }

        // Check the party size.
        if (party.size() < 2) {
            context.getSource().sendError(Text.literal("The party must have at least 2 players."));
            return 1;
        }

        // Check if the player is the leader.
        var leader = PartyManager.findLeader(party);
        if (!leader.equals(player.getUuid())) {
            context.getSource().sendError(Text.literal("You are not the leader of the party."));
            return 1;
        }

        context.getSource().sendMessage(Text.literal("Warping the party...")
            .withColor(Color.GREEN.getRGB()));
        if (!PartyManager.warpParty(player)) {
            context.getSource().sendError(Text.literal("Unable to warp the party."));
        }

        return 1;
    }

    private static int returnPlayer(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return -1;

        var party = PartyManager.findParty(player);
        if (party != null) {
            context.getSource().sendError(Text.literal("You cannot run this in an active party."));
            return 1;
        }

        if (!PartyManager.returnPlayer(player)) {
            context.getSource().sendError(Text.literal("You did not join a party."));
        } else {
            context.getSource().sendMessage(Text.literal("You have been returned to your last location.")
                .withColor(Color.GREEN.getRGB()));
        }

        return 1;
    }
}
