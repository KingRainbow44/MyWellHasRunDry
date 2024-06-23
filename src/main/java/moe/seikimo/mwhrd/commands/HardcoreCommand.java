package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.PlayerModel;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Duration;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public final class HardcoreCommand {
    private static final List<Text> DESCRIPTION = List.of(
        Text.empty(),
        Text.literal("----- HARDCORE MODE -----")
            .formatted(Formatting.BOLD, Formatting.DARK_RED),
        Text.empty(),
        Text.literal("Hardcore mode is a special mode which will 100x the difficulty of Minecraft.")
            .formatted(Formatting.GRAY),
        Text.literal("In this mode, you will be banned for 24 hours if you die.")
            .formatted(Formatting.GRAY),
        Text.literal("Upon death, you also lose your ")
            .formatted(Formatting.GRAY)
            .append(Text.literal("inventory, ender chest, and experience")
                .formatted(Formatting.YELLOW))
            .append(Text.literal(".")
                .formatted(Formatting.GRAY)),
        Text.empty(),
        Text.literal("Hardcode mode is enabled for 100 real-world hours.")
            .formatted(Formatting.GRAY),
        Text.literal("Once enabled, it cannot be turned off.")
            .formatted(Formatting.GRAY),
        Text.empty(),
        Text.literal("Hardcore mode changes the following:")
            .formatted(Formatting.GRAY),
        Text.literal(" - Natural regeneration is disabled.")
            .formatted(Formatting.RED),
        Text.literal(" - Environmental damage taken is tripled.")
            .formatted(Formatting.RED),
        Text.literal(" - Health is increased to 40.")
            .formatted(Formatting.GREEN),
        Text.literal(" - Special reward drops are enabled.")
            .formatted(Formatting.GREEN),
        Text.literal(" - Player debuffs are disabled.")
            .formatted(Formatting.GREEN),
        Text.empty(),
        Text.literal("To enable hardcore mode, type ")
            .formatted(Formatting.GRAY)
            .append(Text.literal("/hardcore enable")
                .formatted(Formatting.YELLOW))
            .append(Text.literal(".")
                .formatted(Formatting.GRAY))
    );

    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("hardcore")
            .then(literal("enable")
                .executes(HardcoreCommand::enable))
            .executes(HardcoreCommand::usage));
    }

    @SuppressWarnings("unchecked")
    private static int enable(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getPlayer() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("You cannot enable hardcore."));
        } else {
            var dataPlayer = (IDBObject<PlayerModel>) player;
            var data = dataPlayer.mwhrd$getData();

            if (data.isBanned() || data.isHardcore()) {
                context.getSource().sendError(Text.literal("You cannot toggle hardcore mode."));
            } else {
                data.setHardcore(Duration.ofHours(100));
            }
        }

        return 1;
    }

    private static int usage(CommandContext<ServerCommandSource> context) {
        DESCRIPTION.forEach(context.getSource()::sendMessage);
        return 1;
    }
}
