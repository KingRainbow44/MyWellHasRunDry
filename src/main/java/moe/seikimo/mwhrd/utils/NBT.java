package moe.seikimo.mwhrd.utils;

import com.mojang.datafixers.util.Pair;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface NBT {
    Logger LOGGER = LoggerFactory.getLogger("NBT");

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
        var x = compound.getInt("x", 0);
        var y = compound.getInt("y", 0);
        var z = compound.getInt("z", 0);

        return new BlockPos(x, y, z);
    }

    /**
     * Serializes an item stack to an NBT element.
     */
    static NbtElement writeItemStack(ItemStack stack) {
        return ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).getOrThrow();
    }

    /**
     * Optionally reads an item stack from an NBT element.
     */
    static Optional<ItemStack> readItemStack(NbtElement element) {
        return ItemStack.CODEC.decode(NbtOps.INSTANCE, element)
            .result()
            .map(Pair::getFirst);
    }

    /**
     * Creates an NBT write view.
     */
    static NbtWriteView write() {
        try (var logging = new ErrorReporter.Logging(LOGGER)) {
            return NbtWriteView.create(logging);
        } catch (Exception e) {
            LOGGER.error("Failed to create NBT write view", e);
            throw new RuntimeException("Failed to create NBT write view", e);
        }
    }

    /**
     * Creates an NBT read view for the given NBT compound.
     */
    static ReadView read(NbtCompound tag) {
        try (var logging = new ErrorReporter.Logging(LOGGER)) {
            var registry = MyWellHasRunDry.getRegistry();
            return NbtReadView.create(logging, registry, tag);
        } catch (Exception e) {
            LOGGER.error("Failed to create NBT read view", e);
            throw new RuntimeException("Failed to create NBT read view", e);
        }
    }
}
