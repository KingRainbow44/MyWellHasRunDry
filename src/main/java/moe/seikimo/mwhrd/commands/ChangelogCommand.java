package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static moe.seikimo.mwhrd.MyWellHasRunDry.CHANGELOG;
import static net.minecraft.server.command.CommandManager.literal;

public final class ChangelogCommand {
    private static final byte[] UPDATE_KEY = new byte[] {
        0x4d, 0x57, 0x48, 0x52, 0x44, 0x2d, 0x55, 0x50, 0x44, 0x41, 0x54, 0x45, 0x2d, 0x4b, 0x45, 0x59
    };

    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("changelog")
            .then(literal("update")
                .executes(ChangelogCommand::checkUpdate))
            .executes(ChangelogCommand::changelog));
    }

    private static int checkUpdate(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().sendMessage(Text.literal("Checking for updates..."));
        } catch (Exception exception) {
            context.getSource().sendError(Text.literal("Failed to check for updates!"));
        }
        return 1;
    }

    private static int changelog(CommandContext<ServerCommandSource> context) {
        CHANGELOG.forEach(context.getSource()::sendMessage);
        return 1;
    }
}
