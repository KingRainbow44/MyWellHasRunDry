package moe.seikimo.mwhrd.models;

import dev.morphia.annotations.Embedded;
import lombok.Data;

@Data
@Embedded
public final class PlayerQuestData {
    /** Marker of if a player has started any quest chains before. */
    private boolean started = false;

    /**
     * Deletes all previous quest data.
     */
    public void reset() {
        this.setStarted(false);
    }
}
