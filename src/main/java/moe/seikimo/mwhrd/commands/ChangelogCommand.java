package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import static moe.seikimo.mwhrd.MyWellHasRunDry.CHANGELOG;
import static net.minecraft.server.command.CommandManager.literal;

public final class ChangelogCommand {
    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("changelog").executes(ChangelogCommand::changelog));
    }

    private static int changelog(CommandContext<ServerCommandSource> context) {
        CHANGELOG.forEach(context.getSource()::sendMessage);
        return 1;
    }
}
