package moe.seikimo.mwhrd.game.quest;

import lombok.Data;
import moe.seikimo.mwhrd.script.ScriptObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Independent state manager for player quests.
 */
@Data
public final class PlayerQuestManager implements ScriptObject {
    private final ServerPlayerEntity player;

    /** Current dialogue instance. */
    private Dialogue currentDialogue = null;

    /** Amount of ticks until the next dialogue should be shown. */
    private int nextDialogue = 0;

    public PlayerQuestManager(ServerPlayerEntity player) {
        this.player = player;

        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
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
        if (this.nextDialogue-- > 0) {
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
            // End the dialogue.
            this.currentDialogue = null;
        }
    }

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
    public void startDialogue(int dialogueId) {
        this.startDialogue(dialogueId, true);
    }

    /**
     * Initiates a dialogue.
     *
     * @param dialogueId The dialogue ID.
     * @param quest Should the dialogue be looked for in the 'quest' folder?
     */
    public void startDialogue(int dialogueId, boolean quest) {
        if (this.currentDialogue != null) {
            // We shouldn't overwrite the current dialogue.
            return;
        }

        // Create the quest instance.
        var path = quest ?
            "quest/q_%s.lua".formatted(dialogueId) :
            "dialogue/d_%s.lua".formatted(dialogueId);
        this.currentDialogue = new Dialogue(path);

        // Invoke the start event.
        this.currentDialogue.startReading(this.player);
    }

    /**
     * Ends the current dialogue.
     */
    public void endDialogue() {
        if (!this.isInConversation()) {
            throw new RuntimeException("No dialogue is currently running.");
        }

        this.currentDialogue.finishReading(this.player);
        this.currentDialogue = null;
    }
}
