package moe.seikimo.mwhrd.interfaces;

import net.minecraft.block.Portal;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

public interface ITimeTraveler {
    /**
     * Sets the player's queued portal.
     *
     * @param portal The portal to queue.
     */
    void mwhrd$setQueuedPortal(Pair<Portal, BlockPos> portal);

    /**
     * @return The player's queued portal.
     */
    Pair<Portal, BlockPos> mwhrd$getQueuedPortal();

    /**
     * Stores a player's inventory in the database.
     *
     * @param clear Whether to clear the player's inventory after storing it.
     */
    void mwhrd$storeInventory(boolean clear);

    /**
     * Restores a player's lost inventory.
     */
    void mwhrd$restoreInventory();
}
