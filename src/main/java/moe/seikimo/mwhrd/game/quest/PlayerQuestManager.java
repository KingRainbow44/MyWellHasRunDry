package moe.seikimo.mwhrd.game.quest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.game.quest.data.DialogueOption;
import moe.seikimo.mwhrd.impl.script.ScriptContext;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.models.PlayerQuestData;
import moe.seikimo.mwhrd.script.ScriptLoader;
import moe.seikimo.mwhrd.script.ScriptObject;
import moe.seikimo.mwhrd.script.ScriptSerializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Independent state manager for player quests.
 */
@Data
@Slf4j
public final class PlayerQuestManager implements ScriptObject {
    private final ServerPlayerEntity player;

    private PlayerQuestData data;

    /** Current dialogue instance. */
    private Dialogue currentDialogue = null;
    /** Amount of ticks until the next dialogue should be shown. */
    private int nextDialogue = 0;
    /** The selectable options for dialogue. */
    private Map<String, LuaFunction> options = new HashMap<>();

    public PlayerQuestManager(ServerPlayerEntity player) {
        this.player = player;

        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
    }

    /**
     * Invoked when the player model is loaded.
     *
     * @param model The player model.
     */
    public void initialize(PlayerModel model) {
        this.data = model.getQuestData();
    }

    /**
     * Invoked when the Minecraft server ticks.
     *
     * @param server The Minecraft server.
     */
    private void onTick(MinecraftServer server) {
        var dialogue = this.currentDialogue;
        if (dialogue == null) {
            return;
        }

        // Check if the next dialogue should be shown.
        if (this.nextDialogue-- > 0 || dialogue.isFinished()) {
            return;
        }

        // Show the next dialogue.
        var line = dialogue.next();
        if (line != null) {
            // Otherwise, we need to show the dialogue.
            this.showDialogue(line.resolve());
            this.nextDialogue = line.getDelay();
        } else {
            // The dialogue is done.
            this.currentDialogue.finishReading(this.player);
        }
    }

    /// <editor-fold desc="Dialogue">

    /**
     * @return True if the player is in dialogue.
     */
    public boolean isInConversation() {
        return this.getCurrentDialogue() != null;
    }

    /**
     * Shows a dialogue to the player.
     *
     * @param text The text to show.
     */
    public void showDialogue(Text text) {
        var toDisplay = Text.literal("<%s> "
            .formatted(this.currentDialogue.getDisplayName()))
            .formatted(Formatting.YELLOW)
            .append(text);

        this.player.sendMessage(toDisplay);
    }

    /**
     * Initiates a dialogue.
     *
     * @param dialogueId The dialogue ID. This is also just the quest ID.
     */
    public boolean startDialogue(int dialogueId) {
        return this.startDialogue(dialogueId, true);
    }

    /**
     * Initiates a dialogue.
     *
     * @param dialogueId The dialogue ID.
     * @param quest Should the dialogue be looked for in the 'quest' folder?
     * @return Whether the dialogue was started or not.
     */
    public boolean startDialogue(int dialogueId, boolean quest) {
        if (
            this.currentDialogue != null &&
            this.currentDialogue.getId() == dialogueId
        ) {
            // We shouldn't overwrite the current dialogue.
            return false;
        }

        // Create the quest instance.
        var path = quest ?
            "quest/q_%s.lua".formatted(dialogueId) :
            "dialogue/d_%s.lua".formatted(dialogueId);
        this.currentDialogue = new Dialogue(dialogueId, path);

        // If the dialogue has been completed before, continue.
        if (this.data.hasCompletedDialogue(dialogueId)) {
            // Invoke script handler for this scenario.
            this.currentDialogue.alreadyRead(this.player);
            this.currentDialogue = null;

            return false;
        }

        // Invoke the start event.
        this.currentDialogue.startReading(this.player);

        return true;
    }

    /**
     * Ends the current dialogue.
     */
    public void endDialogue() {
        if (!this.isInConversation()) {
            throw new RuntimeException("No dialogue is currently running.");
        }

        this.currentDialogue.finishReading(this.player);

        // Check conditions.
        this.data.checkConditions();
    }

    /**
     * Prompts the player with dialogue options.
     *
     * @param context The script's execution context.
     * @param options The prompt options.
     */
    public void prompt(
        ScriptContext context,
        LuaTable... options
    ) {
        // Get the Lua script.
        var bindings = context.getBindings();
        Objects.requireNonNull(bindings);

        // Interact with each option.
        for (int i = 0; i < options.length; i++) {
            var data = options[i];
            var option = ScriptSerializer.toObject(data, DialogueOption.class);

            // Fetch the function from the context.
            var func = bindings.get(option.getCallback());
            if (!(func instanceof LuaFunction function)) {
                continue;
            }
            this.options.put(option.getCallback(), function);

            // Send the player the option.
            this.player.sendMessage(
                Text.literal(
                    "%d. "
                        .formatted(i + 1)
                )
                    .formatted(Formatting.DARK_GRAY)
                    .append(Text.translatable(option.getLocale()))
                    .styled(style -> style.withClickEvent(
                        new ClickEvent.RunCommand(
                            "/interact %s".formatted(option.getCallback())
                        )
                    ))
                    .formatted(Formatting.GRAY)
            );
        }
    }

    /**
     * Marks the dialogue as finished.
     * Sends the translated message to the player.
     *
     * @param key The language key (from the language file) to send.
     */
    public void finishDialogue(String key) {
        // Send the player the message.
        this.showDialogue(Text.translatable(key));

        // Mark the dialogue as finished.
        var current = this.getCurrentDialogue();
        this.data.setDialogueState(current.getId(), true);

        // Check conditions.
        this.data.checkConditions();

        // Clear the current dialogue.
        this.currentDialogue = null;
    }

    /**
     * Stops the current dialogue.
     * Marks it as incomplete.
     */
    public void stopDialogue() {
        // Mark the dialogue as incomplete.
        var current = this.getCurrentDialogue();
        this.data.setDialogueState(current.getId(), false);

        // Check conditions.
        this.data.checkConditions();

        // Clear the current dialogue.
        this.currentDialogue = null;
    }

    /**
     * Acts as a "reply" from the given actor.
     * Sends the translated message to the player.
     *
     * @param key The language key (from the language file) to send.
     */
    public void reply(String key) {
        // Send the player the message.
        this.showDialogue(Text.translatable(key));
    }

    /**
     * Invokes a dialogue option.
     *
     * @param option The option to invoke.
     */
    public void invokeOption(String option) {
        // Check if the player is in dialogue.
        if (this.currentDialogue == null) {
            return;
        }

        var func = this.options.get(option);
        if (func == null) {
            return;
        }

        try {
            // Invoke the function.
            var context = ScriptContext.dialogue(this.player, this.currentDialogue);
            ScriptLoader.call(func, context);
        } catch (Exception ex) {
            log.warn("Unable to invoke dialogue option.", ex);
        }

        // Clear the current dialogue.
        this.currentDialogue = null;
    }

    /// </editor-fold>

    /// <editor-fold desc="Quests">

    /**
     * Marks a quest as complete for the player.
     *
     * @param questId The ID of the quest to complete.
     */
    public void complete(int questId) {
        // Check if the quest is running.
        if (!this.data.hasStarted(questId)) {
            throw new RuntimeException("The quest is not running.");
        }

        // Mark the quest as completed.
        this.data.setQuestState(questId, Quest.State.COMPLETED);

        // Re-check conditions.
        this.data.checkConditions();
    }

    /// </editor-fold>
}
