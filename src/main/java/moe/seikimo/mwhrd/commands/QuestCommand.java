package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class QuestCommand {
    private static final SimpleCommandExceptionType NOT_INVOLVED = new SimpleCommandExceptionType(Text.translatable("commands.quest.not_involved"));

    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("quest")
                .requires(s -> s.hasPermissionLevel(2))
                .then(reset())
        );
    }

    /**
     * Sub-command: <code>reset</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> reset() {
        return literal("reset")
            .requires(ServerCommandSource::isExecutedByPlayer)
            .executes(ctx -> {
                var player = ctx.getSource().getPlayer();
                var questData = Players.getQuestData(player);

                // Check if the player has started a quest chain before.
                if (!questData.isStarted()) {
                    throw NOT_INVOLVED.create();
                }

                // Reset the player's quest data.
                questData.reset();

                ctx.getSource().sendFeedback(() -> Text.translatable("commands.quest.reset.success"), true);

                return Command.SINGLE_SUCCESS;
            });
    }
}
