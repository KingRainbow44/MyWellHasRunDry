package moe.seikimo.mwhrd.utils.items;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

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
     * @return The component-form of the NBT.
     */
    public NbtComponent build() {
        return NbtComponent.of(this.compound);
    }
}
