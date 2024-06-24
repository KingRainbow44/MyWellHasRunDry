package moe.seikimo.mwhrd.interfaces;

import net.minecraft.util.math.BlockPos;

/**
 * Used in world manipulation beacon powers.
 */
public interface ISelectionPlayer {
    void mwhrd$setPos1(BlockPos pos);
    void mwhrd$setPos2(BlockPos pos);

    BlockPos mwhrd$getPos1();
    BlockPos mwhrd$getPos2();

    /**
     * @return Whether the player has a selection.
     */
    default boolean hasSelection() {
        return mwhrd$getPos1() != null && mwhrd$getPos2() != null;
    }

    /**
     * @return The estimated block count of the selection.
     */
    default int selectionSize() {
        if (!hasSelection()) {
            return 0;
        }

        var pos1 = mwhrd$getPos1();
        var pos2 = mwhrd$getPos2();

        return Math.abs(pos1.getX() - pos2.getX()) *
            Math.abs(pos1.getY() - pos2.getY()) *
            Math.abs(pos1.getZ() - pos2.getZ());
    }
}
