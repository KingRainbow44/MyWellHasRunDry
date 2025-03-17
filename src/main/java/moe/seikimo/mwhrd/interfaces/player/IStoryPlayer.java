package moe.seikimo.mwhrd.interfaces.player;

import moe.seikimo.mwhrd.game.quest.PlayerQuestManager;

public interface IStoryPlayer {
    /**
     * Gets the quest manager for this player.
     *
     * @return The quest manager.
     */
    PlayerQuestManager mwhrd$getQuestManager();
}
