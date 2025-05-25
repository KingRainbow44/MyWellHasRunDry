package moe.seikimo.mwhrd.game.quest;

import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Quests {
    /* MCA (Minecraft Comes Alive) quests. */
    int MCA_QUEST_ENTRYPOINT = 700_000;

    /**
     * Checks if the player has started MCA.
     *
     * @param player The player to check.
     * @return {@code true} if the player has started MCA, {@code false} otherwise.
     */
    static boolean hasStartedMca(ServerPlayerEntity player) {
        return Players.getQuestData(player)
            .getQuestState(MCA_QUEST_ENTRYPOINT) == Quest.State.COMPLETED;
    }
}
