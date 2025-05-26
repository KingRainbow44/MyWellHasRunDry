package moe.seikimo.mwhrd.game.quest.data;

import lombok.Data;
import moe.seikimo.mwhrd.game.mca.GuildProgress;

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

    public List<Condition> accept_conditions = new ArrayList<>();
    public List<Condition> complete_conditions = new ArrayList<>();

    /** The data of a quest condition. */
    @Data
    public static final class Condition {
        public ConditionType type;

        /**
         * Used in {@link ConditionType#QUEST_COMPLETED}.
         */
        public int quest_id;

        /**
         * Used in {@link ConditionType#DIALOGUE_COMPLETED}.
         */
        public int dialogue_id;

        /**
         * Used in {@link ConditionType#GUILD_FLAG_SET}.
         */
        public String flag;

        /**
         * Used in {@link ConditionType#GUILD_PROGRESS}.
         */
        public GuildProgress progress;
    }

    /** Types of quest conditions. */
    public enum ConditionType {
        /**
         * This condition will always pass.
         */
        ALWAYS,

        /**
         * This condition will always fail.
         */
        NEVER,

        /**
         * The player has the specified quest completed.
         */
        QUEST_COMPLETED,

        /**
         * The player has the specified dialogue completed.
         */
        DIALOGUE_COMPLETED,

        /**
         * The player's guild has reached a certain progress level.
         * This will satisfy if the guild is at or above the specified level.
         * <p>
         * If the player is not in a guild, this condition fails.
         */
        GUILD_PROGRESS,

        /**
         * The player's guild has a flag set.
         * <p>
         * If the player is not in a guild, this condition fails.
         */
        GUILD_FLAG_SET
    }
}
