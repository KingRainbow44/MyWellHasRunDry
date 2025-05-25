package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import lombok.experimental.ExtensionMethod;
import moe.seikimo.mwhrd.game.quest.Quest;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@ExtensionMethod(Players.class)
public final class QuestCommand {
    private static final SimpleCommandExceptionType NOT_INVOLVED = new SimpleCommandExceptionType(Text.translatable("commands.quest.not_involved"));
    private static final SimpleCommandExceptionType DIALOGUE_RUNNING = new SimpleCommandExceptionType(Text.translatable("commands.quest.dialogue.running"));
    private static final SimpleCommandExceptionType DIALOGUE_NOT_RUNNING = new SimpleCommandExceptionType(Text.translatable("commands.quest.dialogue.not_running"));
    private static final SimpleCommandExceptionType DIALOGUE_NOT_COMPLETE = new SimpleCommandExceptionType(Text.translatable("commands.quest.dialogue.reset.incomplete"));
    private static final SimpleCommandExceptionType STATE_INVALID = new SimpleCommandExceptionType(Text.translatable("commands.quest.state.set.invalid_state"));

    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("quest")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .requires(s -> s.hasPermissionLevel(2))
                .then(reset())
                .then(dialogue())
                .then(state())
        );
    }

    /**
     * Sub-command: <code>reset</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> reset() {
        return literal("reset")
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

    /**
     * Sub-command: <code>dialogue</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> dialogue() {
        return literal("dialogue")
            .then(dialogueStart())
            .then(dialogueEnd())
            .then(dialogueReset());
    }

    /**
     * Sub-command: <code>dialogue start</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> dialogueStart() {
        return literal("start")
            .then(argument("id", IntegerArgumentType.integer())
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    var questManager = Players.getQuestManager(player);

                    // Check if dialogue is already playing.
                    if (questManager.isInConversation()) {
                        throw DIALOGUE_RUNNING.create();
                    }

                    // Start the dialogue.
                    var dialogueId = IntegerArgumentType.getInteger(ctx, "id");
                    questManager.startDialogue(dialogueId);

                    ctx.getSource().sendFeedback(() -> Text.translatable("commands.quest.dialogue.start.success", dialogueId), true);

                    return Command.SINGLE_SUCCESS;
                }));
    }

    /**
     * Sub-command: <code>dialogue end</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> dialogueEnd() {
        return literal("end")
            .executes(ctx -> {
                var player = ctx.getSource().getPlayer();
                var questManager = Players.getQuestManager(player);

                // Check if dialogue is not playing.
                if (!questManager.isInConversation()) {
                    throw DIALOGUE_NOT_RUNNING.create();
                }

                // End the dialogue.
                questManager.endDialogue();

                ctx.getSource().sendFeedback(() -> Text.translatable("commands.quest.dialogue.end.success"), true);

                return Command.SINGLE_SUCCESS;
            });
    }

    /**
     * Sub-command: <code>dialogue reset</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> dialogueReset() {
        return literal("reset")
            .then(argument("id", IntegerArgumentType.integer())
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    var dialogueId = IntegerArgumentType.getInteger(ctx, "id");

                    var questManager = player.getQuestManager();
                    var data = questManager.getData();

                    // Check if the dialogue exists.
                    if (!data.hasCompletedDialogue(dialogueId)) {
                        throw DIALOGUE_NOT_COMPLETE.create();
                    }

                    // Reset the dialogue state.
                    data.setDialogueState(dialogueId, false);

                    ctx.getSource().sendFeedback(() -> Text.translatable("commands.quest.dialogue.reset.success"), true);

                    return Command.SINGLE_SUCCESS;
                }))
            .executes(ctx -> {
                var player = ctx.getSource().getPlayer();

                var questManager = player.getQuestManager();
                var data = questManager.getData();

                // Reset all dialogue states.
                data.getDialogues().clear();

                ctx.getSource().sendFeedback(() -> Text.translatable("commands.quest.dialogue.reset.success"), true);

                return Command.SINGLE_SUCCESS;
            });
    }

    /**
     * Sub-command: <code>state</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> state() {
        return literal("state")
            .then(argument("id", IntegerArgumentType.integer())
                .then(stateSet())
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    var questId = IntegerArgumentType.getInteger(ctx, "id");

                    // Get the state of the quest.
                    var questData = Players.getQuestData(player);
                    var state = questData.getQuestState(questId);

                    // Send the state to the player.
                    ctx.getSource().sendMessage(Text.translatable("commands.quest.state.get", questId, state));

                    return Command.SINGLE_SUCCESS;
                }));
    }

    /**
     * Sub-command: <code>state set</code>
     */
    private static ArgumentBuilder<ServerCommandSource, ?> stateSet() {
        return literal("set")
            .then(argument("value", StringArgumentType.word())
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    var questId = IntegerArgumentType.getInteger(ctx, "id");
                    var value = StringArgumentType.getString(ctx, "value");

                    try {
                        // Try parsing the value.
                        var newState = Quest.State.valueOf(value.toUpperCase());

                        // Set the state.
                        var questData = Players.getQuestData(player);
                        questData.setQuestState(questId, newState);

                        ctx.getSource().sendFeedback(() -> Text.translatable("commands.quest.state.set.success", questId, newState), true);
                    } catch (IllegalArgumentException ignored) {
                        throw STATE_INVALID.create();
                    }

                    return Command.SINGLE_SUCCESS;
                }));
    }
}
