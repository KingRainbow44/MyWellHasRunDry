package moe.seikimo.mwhrd.utils.items;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;

public final class NbtBuilder {
    /**
     * Creates a new NbtBuilder instance
     */
    public static NbtBuilder of() {
        return new NbtBuilder();
    }

    private final NbtCompound compound = new NbtCompound();

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, String value) {
        this.compound.putString(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, int value) {
        this.compound.putInt(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, double value) {
        this.compound.putDouble(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, boolean value) {
        this.compound.putBoolean(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, byte[] value) {
        this.compound.putByteArray(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, int[] value) {
        this.compound.putIntArray(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, long[] value) {
        this.compound.putLongArray(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, NbtElement value) {
        this.compound.put(key, value);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param registryKey The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, RegistryKey<?> registryKey) {
        this.compound.putString(key, registryKey.getValue().toString());
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param position The value to set.
     * @return The NbtBuilder instance.
     */
    public NbtBuilder set(String key, BlockPos position) {
        var compound = new NbtCompound();
        compound.putInt("x", position.getX());
        compound.putInt("y", position.getY());
        compound.putInt("z", position.getZ());

        this.compound.put(key, compound);
        return this;
    }

    /**
     * @return The NBT as a NbtElement.
     */
    public NbtElement asElement() {
        return this.compound;
    }

    /**
     * @return The component-form of the NBT.
     */
    public NbtComponent build() {
        return NbtComponent.of(this.compound);
    }
}
