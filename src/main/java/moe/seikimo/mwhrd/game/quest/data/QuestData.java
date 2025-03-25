package moe.seikimo.mwhrd.game.quest.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Raw quest data, as defined in a Lua table.
 * <p>
 * To be deserialized from a Lua table, the following is required:
 * - a no-arguments constructor must exist
 * - every deserializable field must have a setter method
 */
@Data
public final class QuestData {
    public int id;
    public boolean hidden = false;
    public List<Condition> conditions = new ArrayList<>();

    /** The data of a quest condition. */
    @Data
    public static final class Condition {
        public ConditionType type;

        /**
         * Used in {@link ConditionType#QUEST_COMPLETED}.
         */
        public int questId;
    }

    /** Types of quest conditions. */
    public enum ConditionType {
        /**
         * This quest will always be assigned to the player.
         */
        ALWAYS,

        /**
         * The player has the specified quest completed.
         */
        QUEST_COMPLETED
    }
}
