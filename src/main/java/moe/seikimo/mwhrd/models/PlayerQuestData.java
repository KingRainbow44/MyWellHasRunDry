package moe.seikimo.mwhrd.models;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.PostLoad;
import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import moe.seikimo.mwhrd.game.quest.Quest;
import moe.seikimo.mwhrd.game.quest.data.QuestData;
import moe.seikimo.mwhrd.managers.GlobalQuestManager;

import java.util.Map;

@Data
@Embedded
public final class PlayerQuestData {
    /**
     * Marker of if a player has started any quest chains before.
     * <p>
     * This flag is used in {@link GlobalQuestManager} for checking BOTW.
     */
    private boolean started = false;

    /** Data for quests started/completed by the player. */
    private Map<Integer, Quest> quests = new Int2ObjectOpenHashMap<>();

    /** Finished state of dialogues. */
    private Map<Integer, Boolean> dialogues = new Int2BooleanArrayMap();

    /**
     * Event listener invoked after the object is loaded.
     */
    @PostLoad
    public void postLoad() {
        // Check all quest conditions.
        this.checkConditions();
    }

    /**
     * Deletes all previous quest data.
     */
    public void reset() {
        this.setStarted(false);
        this.quests.clear();

        // Re-check all quest conditions.
        this.checkConditions();
    }

    /**
     * Checks all quests to see if conditions are fulfilled.
     */
    public void checkConditions() {
        // Check new quests' assign conditions.
        for (var questData : GlobalQuestManager.getKnownQuests().values()) {
            // If the quest is already started, skip it.
            if (this.hasStarted(questData.getId())) {
                continue;
            }

            // Check to see if the conditions are fulfilled.
            var conditions = questData.getConditions();
            if (conditions.stream().allMatch(this::isFulfilled)) {
                // If all conditions are fulfilled, start the quest.
                this.assignQuest(questData);
            }
        }
    }

    /**
     * Checks to see if the given condition is fulfilled.
     *
     * @param condition The condition to check.
     * @return {@code true} if the condition is fulfilled, {@code false} otherwise.
     */
    private boolean isFulfilled(QuestData.Condition condition) {
        return switch (condition.getType()) {
            case ALWAYS -> true;
            case QUEST_COMPLETED -> this.getQuestState(condition.getQuestId()) == Quest.State.COMPLETED;
        };
    }

    /**
     * Assigns a quest to the player.
     *
     * @param data The quest data to assign.
     */
    public void assignQuest(QuestData data) {
        this.quests.put(data.getId(), Quest.fromData(data));
    }

    /**
     * Checks if the player has started the given quest.
     *
     * @param questId The ID of the quest to check.
     * @return {@code true} if the player has started the quest, {@code false} otherwise.
     */
    public boolean hasStarted(int questId) {
        return this.quests.containsKey(questId);
    }

    /**
     * Returns the full quest data for the given quest ID.
     *
     * @param questId The ID of the quest to retrieve.
     * @return The quest data.
     */
    public Quest getQuest(int questId) {
        return this.quests.get(questId);
    }

    /**
     * Retrieves the state of the quest with the given ID.
     *
     * @param questId The ID of the quest to check.
     * @return The state of the quest.
     */
    public Quest.State getQuestState(int questId) {
        if (!this.hasStarted(questId)) {
            return Quest.State.NOT_STARTED;
        }

        return this.quests.get(questId).getState();
    }

    /**
     * Sets the state of the quest with the given ID.
     *
     * @param questId The ID of the quest to set the state of.
     * @param state The state to set the quest to.
     */
    public void setQuestState(int questId, Quest.State state) {
        if (!this.hasStarted(questId)) {
            return;
        }

        this.quests.get(questId).setState(state);
    }

    /**
     * Sets the state of the dialogue with the given ID.
     *
     * @param dialogueId The ID of the dialogue to set the state of.
     * @param completed {@code true} if the dialogue is completed, {@code false} otherwise.
     */
    public void setDialogueState(int dialogueId, boolean completed) {
        this.dialogues.put(dialogueId, completed);
    }

    /**
     * Checks if the player has completed the given dialogue.
     *
     * @param dialogueId The ID of the dialogue to check.
     * @return {@code true} if the player has completed the dialogue, {@code false} otherwise.
     */
    public boolean hasCompletedDialogue(int dialogueId) {
        return this.dialogues.getOrDefault(dialogueId, false);
    }
}
