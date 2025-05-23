package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.game.beacon.BeaconManager;
import moe.seikimo.mwhrd.impl.PlayerNpcElement;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.interfaces.ITimeTraveler;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.utils.*;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Slf4j
public final class DebugCommand {
    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("debug")
            .requires(source -> source.hasPermissionLevel(3))
            .then(argument("property", StringArgumentType.word())
                .then(argument("value", StringArgumentType.word())
                    .executes(DebugCommand::set)))
            .then(literal("hardcore")
                .then(literal("v2")
                    .executes(DebugCommand::unsetHardcoreV2))
                .executes(DebugCommand::unsetHardcore))
            .then(literal("unban")
                .executes(DebugCommand::unban))
            .then(literal("fuel")
                .then(argument("value", LongArgumentType.longArg(0))
                    .executes(DebugCommand::fuel)))
            .then(literal("custom")
                .then(literal("tablist")
                    .executes(DebugCommand::tablist))
                .executes(DebugCommand::usage))
            .then(literal("inv")
                .then(literal("restore")
                    .then(argument("player", StringArgumentType.word())
                        .executes(DebugCommand::restoreInv))
                    .executes(DebugCommand::usage))
                .executes(DebugCommand::usage))
            .then(literal("border")
                .executes(DebugCommand::border))
            .then(literal("player")
                .executes(DebugCommand::newPlayer))
            .then(literal("text")
                .then(argument("input", greedyString())
                    .executes(DebugCommand::text)))
            .then(literal("schem")
                .then(argument("path", greedyString())
                    .executes(DebugCommand::paste)))
            .executes(DebugCommand::usage));
    }

    private static int usage(CommandContext<ServerCommandSource> context) {
        context.getSource().sendError(Text.literal(
            "Usage: /debug <property> <value>"
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<ServerCommandSource> context) {
        var property = getString(context, "property");
        var value = getString(context, "value");

        try {
            var field = Debug.class.getDeclaredField(property);
            field.setAccessible(true);

            if (field.getType() == boolean.class) {
                field.set(null, Boolean.parseBoolean(value));
            } else if (field.getType() == int.class) {
                field.set(null, Integer.parseInt(value));
            } else if (field.getType() == String.class) {
                field.set(null, value);
            } else {
                context.getSource().sendError(Text.literal(
                    "Unsupported property type: " + field.getType().getName()
                ));
                return Command.SINGLE_SUCCESS;
            }

            field.setAccessible(false);

            context.getSource().sendMessage(Text.literal(
                "Set " + property + " to " + value));
        } catch (Exception exception) {
            context.getSource().sendError(Text.literal(
                "Unknown property: " + property
            ));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int unsetHardcore(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (!(player instanceof IDBObject<?> dbObject)) {
            context.getSource().sendError(Text.literal(
                "Player is not an IDBObject"
            ));
            return Command.SINGLE_SUCCESS;
        }

        var data = dbObject.mwhrd$getData();
        if (data instanceof PlayerModel model) {
            model.unsetHardcore(true);
        } else {
            context.getSource().sendError(Text.literal(
                "Player data is not a PlayerModel"
            ));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int unsetHardcoreV2(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        var model = Players.getModel(player);

        if (!model.isHardcoreV2()) {
            context.getSource().sendError(Text.literal("not in hardcore v2"));
        } else {
            model.setAliveTicks(2_399_900);
            model.save();

            context.getSource().sendMessage(Text.literal("added 2,399,900 ticks"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int unban(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (!(player instanceof IDBObject<?> dbObject)) {
            context.getSource().sendError(Text.literal(
                "Player is not an IDBObject"
            ));
            return Command.SINGLE_SUCCESS;
        }

        var data = dbObject.mwhrd$getData();
        if (data instanceof PlayerModel model) {
            model.unbanPlayer();
        } else {
            context.getSource().sendError(Text.literal(
                "Player data is not a PlayerModel"
            ));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int fuel(CommandContext<ServerCommandSource> context) {
        var value = LongArgumentType.getLong(context, "value");
        BeaconManager.FUEL_TIME = value;
        context.getSource().sendMessage(Text.literal(
            "Set fuel time to " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int restoreInv(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Must be ran as a player"));
            return Command.SINGLE_SUCCESS;
        }

        var target = getString(context, "player");
        var targetPlayer = Objects.requireNonNull(player.getServer())
            .getPlayerManager()
            .getPlayer(target);
        if (targetPlayer == null) {
            context.getSource().sendError(Text.literal("Player not found"));
            return Command.SINGLE_SUCCESS;
        }

        if (!(targetPlayer instanceof ITimeTraveler traveler)) {
            context.getSource().sendError(Text.literal("Player is not an ITimeTraveler"));
            return Command.SINGLE_SUCCESS;
        }

        try {
            traveler.mwhrd$restoreInventory();
            context.getSource().sendMessage(Text.literal("Restored inventory for " + target));
        } catch (Exception exception) {
            log.error("Failed to restore inventory for {}", target, exception);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int border(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Must be ran as a player"));
            return Command.SINGLE_SUCCESS;
        }

        var center = player.getBlockPos();
        var radius = 1000;

        try {
            BorderHelper.setWorldBorder(player, center, radius);
            context.getSource().sendMessage(Text.literal("Set world border"));
        } catch (Exception exception) {
            log.error("Failed to set world border", exception);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int tablist(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Must be ran as a player"));
            return Command.SINGLE_SUCCESS;
        }

        try {
            PlayerList.removeAllPlayers(player.networkHandler);

            player.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(
                Text.literal("top text"),
                Text.literal("bottom text")
            ));

            var entries = new ArrayList<PlayerListS2CPacket.Entry>();
            for (var i = 0; i < 10; i++) {
                entries.add(PlayerList.fakePlayer(
                    "Player" + i,
                    Text.literal("Player " + i)
                        .formatted(i % 2 == 0 ? Formatting.BOLD : Formatting.GOLD, Formatting.YELLOW),
                    10 - i,
                    PlayerList.DARK_GRAY_HEAD, PlayerList.DARK_GRAY_SIGN
                ));
            }

            player.networkHandler.sendPacket(PlayerList.create(
                PlayerList.NEW_ACTIONS, entries
            ));

            context.getSource().sendMessage(Text.literal("Sent tab list update"));
        } catch (Exception exception) {
            log.error("Failed to send tab list update", exception);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int newPlayer(CommandContext<ServerCommandSource> context) {
        try {
            var player = Objects.requireNonNull(context.getSource().getPlayer());

            var element = new PlayerNpcElement();
            element.setGlowing(true);
            element.setOffset(new Vec3d(0, 3, 0));
            element.setSkin(PlayerList.DARK_GRAY_HEAD, PlayerList.DARK_GRAY_SIGN);

            var text = new TextDisplayElement();
            text.setText(Text.literal("hello world"));
            text.setOffset(new Vec3d(0, 2, 0));

            var holder = new ElementHolder();
            holder.addElement(element);
            holder.addElement(text);

            EntityAttachment.ofTicking(holder, player);
            holder.startWatching(player);

            context.getSource().sendMessage(Text.literal("Created player NPC"));

            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            log.error("Failed to create player NPC", ex);
            return 0;
        }
    }

    private static int text(CommandContext<ServerCommandSource> context) {
        var text = getString(context, "input");
        context.getSource().sendMessage(Utils.fromLegacy(text));

        return Command.SINGLE_SUCCESS;
    }

    private static int paste(CommandContext<ServerCommandSource> context) {
        var path = getString(context, "path");
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Must be ran as a player"));
            return Command.SINGLE_SUCCESS;
        }

        try {
            var schematic = IO.readSchematic(path);
            Objects.requireNonNull(schematic);

            var placement = schematic.sample();
            placement.place(
                player.getWorld(),
                player.getBlockPos().add(0, -1, 0)
            );

            context.getSource().sendMessage(Text.literal("Pasted schematic"));
        } catch (Exception exception) {
            log.error("Failed to paste schematic", exception);
        }

        return Command.SINGLE_SUCCESS;
    }
}
