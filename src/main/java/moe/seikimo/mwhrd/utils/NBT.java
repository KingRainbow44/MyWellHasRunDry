package moe.seikimo.mwhrd.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

public interface NBT {
    /**
     * Reads a block position from an NBT element.
     *
     * @param element The NBT element to read from.
     * @return The block position.
     */
    static BlockPos readBlockPos(NbtElement element) {
        if (!(element instanceof NbtCompound compound)) {
            throw new IllegalArgumentException("Expected a compound tag");
        }

        // Pull the coordinates from the NBT.
        var x = compound.getInt("x");
        var y = compound.getInt("y");
        var z = compound.getInt("z");

        return new BlockPos(x, y, z);
    }
}
