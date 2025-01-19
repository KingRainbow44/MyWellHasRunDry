package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.game.beacon.BeaconManager;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.interfaces.ITimeTraveler;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.utils.BorderHelper;
import moe.seikimo.mwhrd.utils.Debug;
import moe.seikimo.mwhrd.utils.PlayerList;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
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
            .executes(DebugCommand::usage));
    }

    private static int usage(CommandContext<ServerCommandSource> context) {
        context.getSource().sendError(Text.literal(
            "Usage: /debug <property> <value>"
        ));
        return 1;
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
                return 1;
            }

            field.setAccessible(false);

            context.getSource().sendMessage(Text.literal(
                "Set " + property + " to " + value));
        } catch (Exception exception) {
            context.getSource().sendError(Text.literal(
                "Unknown property: " + property
            ));
            return 1;
        }

        return 1;
    }

    private static int unsetHardcore(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (!(player instanceof IDBObject<?> dbObject)) {
            context.getSource().sendError(Text.literal(
                "Player is not an IDBObject"
            ));
            return 1;
        }

        var data = dbObject.mwhrd$getData();
        if (data instanceof PlayerModel model) {
            model.unsetHardcore(true);
        } else {
            context.getSource().sendError(Text.literal(
                "Player data is not a PlayerModel"
            ));
            return 1;
        }

        return 1;
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

        return 1;
    }

    private static int unban(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (!(player instanceof IDBObject<?> dbObject)) {
            context.getSource().sendError(Text.literal(
                "Player is not an IDBObject"
            ));
            return 1;
        }

        var data = dbObject.mwhrd$getData();
        if (data instanceof PlayerModel model) {
            model.unbanPlayer();
        } else {
            context.getSource().sendError(Text.literal(
                "Player data is not a PlayerModel"
            ));
            return 1;
        }

        return 1;
    }

    private static int fuel(CommandContext<ServerCommandSource> context) {
        var value = LongArgumentType.getLong(context, "value");
        BeaconManager.FUEL_TIME = value;
        context.getSource().sendMessage(Text.literal(
            "Set fuel time to " + value));
        return 1;
    }

    private static int restoreInv(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Must be ran as a player"));
            return 1;
        }

        var target = getString(context, "player");
        var targetPlayer = Objects.requireNonNull(player.getServer())
            .getPlayerManager()
            .getPlayer(target);
        if (targetPlayer == null) {
            context.getSource().sendError(Text.literal("Player not found"));
            return 1;
        }

        if (!(targetPlayer instanceof ITimeTraveler traveler)) {
            context.getSource().sendError(Text.literal("Player is not an ITimeTraveler"));
            return 1;
        }

        try {
            traveler.mwhrd$restoreInventory();
            context.getSource().sendMessage(Text.literal("Restored inventory for " + target));
        } catch (Exception exception) {
            log.error("Failed to restore inventory for {}", target, exception);
        }

        return 1;
    }

    private static int border(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Must be ran as a player"));
            return 1;
        }

        var center = player.getBlockPos();
        var radius = 1000;

        try {
            BorderHelper.setWorldBorder(player, center, radius);
            context.getSource().sendMessage(Text.literal("Set world border"));
        } catch (Exception exception) {
            log.error("Failed to set world border", exception);
        }

        return 1;
    }

    private static int tablist(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Must be ran as a player"));
            return 1;
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

        return 1;
    }
}
