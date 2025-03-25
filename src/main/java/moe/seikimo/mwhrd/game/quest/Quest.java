package moe.seikimo.mwhrd.game.quest;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.PostLoad;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import moe.seikimo.mwhrd.game.quest.data.QuestData;
import moe.seikimo.mwhrd.managers.GlobalQuestManager;

/**
 * A quest that can be started by a player.
 */
@Data
@Embedded
public final class Quest {
    /**
     * Creates a new quest with the given ID.
     *
     * @param data The quest's raw data.
     * @return The new quest instance.
     */
    public static Quest fromData(QuestData data) {
        var quest = new Quest();
        quest.setId(data.getId());
        quest.setData(data);

        return quest;
    }

    private int id;

    /** The status of the quest. */
    private State state = State.STARTED;
    /** A map of variables defined for the quest. */
    private Int2ObjectMap<String> variables = new Int2ObjectOpenHashMap<>();

    /** The specific data of the quest. */
    private transient QuestData data;

    /**
     * An event listener invoked after the object is loaded.
     */
    @PostLoad
    public void postLoad() {
        this.data = GlobalQuestManager.getQuestData(this.id);
    }

    /**
     * The state of the quest.
     */
    public enum State {
        NOT_STARTED,
        STARTED,
        COMPLETED,
        FAILED
    }
}
