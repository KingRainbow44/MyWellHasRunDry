package moe.seikimo.mwhrd.utils.items;

import moe.seikimo.mwhrd.utils.NBT;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;

public final class ItemNbt {
    /**
     * Wraps an item stack into an ItemNbt instance.
     *
     * @param stack The item stack to wrap.
     * @return The ItemNbt instance.
     */
    public static ItemNbt wrap(ItemStack stack) {
        return new ItemNbt(stack);
    }

    private final ItemStack stack;
    private final NbtCompound compound;

    /**
     * Creates a new item NBT instance from an item stack.
     *
     * @param stack The item stack to create the NBT from.
     */
    public ItemNbt(ItemStack stack) {
        this.stack = stack;
        this.compound = stack
            .getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
            .copyNbt();
    }

    /**
     * Checks if the NBT contains a key.
     *
     * @param key The key to check.
     * @return Whether the key exists.
     */
    public boolean contains(String key) {
        return this.compound.contains(key);
    }

    /**
     * Gets a string from the NBT.
     *
     * @param key The key to get.
     * @return The string value.
     */
    public String getString(String key) {
        return this.compound.getString(key);
    }

    /**
     * Gets an integer from the NBT.
     *
     * @param key The key to get.
     * @return The integer value.
     */
    public int getInt(String key) {
        return this.compound.getInt(key);
    }

    /**
     * Gets a double from the NBT.
     *
     * @param key The key to get.
     * @return The double value.
     */
    public double getDouble(String key) {
        return this.compound.getDouble(key);
    }

    /**
     * Gets a boolean from the NBT.
     *
     * @param key The key to get.
     * @return The boolean value.
     */
    public boolean getBoolean(String key) {
        return this.compound.getBoolean(key);
    }

    /**
     * Gets a byte array from the NBT.
     *
     * @param key The key to get.
     * @return The byte array value.
     */
    public byte[] getByteArray(String key) {
        return this.compound.getByteArray(key);
    }

    /**
     * Gets an integer array from the NBT.
     *
     * @param key The key to get.
     * @return The integer array value.
     */
    public int[] getIntArray(String key) {
        return this.compound.getIntArray(key);
    }

    /**
     * Gets a long array from the NBT.
     *
     * @param key The key to get.
     * @return The long array value.
     */
    public long[] getLongArray(String key) {
        return this.compound.getLongArray(key);
    }

    /**
     * Gets a block position from the NBT.
     *
     * @param key The key to get.
     * @return The block position value.
     */
    public GlobalPos getGlobalPos(String key) {
        if (!(this.compound.get(key) instanceof NbtCompound posNbt)) {
            throw new IllegalArgumentException("Expected a compound tag");
        }

        // Read the dimension registry key.
        var dimensionId = posNbt.getString("dimension");
        var dimensionKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionId));

        // Read the block position.
        var blockPos = NBT.readBlockPos(posNbt.getCompound("position"));

        return GlobalPos.create(dimensionKey, blockPos);
    }

    /**
     * Gets an NBT element from the NBT.
     *
     * @param key The key to get.
     * @return The NBT element value.
     */
    public NbtElement get(String key) {
        return this.compound.get(key);
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, String value) {
        this.compound.putString(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, int value) {
        this.compound.putInt(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, double value) {
        this.compound.putDouble(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, boolean value) {
        this.compound.putBoolean(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, byte[] value) {
        this.compound.putByteArray(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, int[] value) {
        this.compound.putIntArray(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, long[] value) {
        this.compound.putLongArray(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, NbtElement value) {
        this.compound.put(key, value);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }

    /**
     * Sets a key-value pair in the NBT.
     *
     * @param key The key to set.
     * @param value The value to set.
     * @return The ItemNbt instance.
     */
    public ItemNbt set(String key, GlobalPos value) {
        return this.set(key, NbtBuilder.of()
            .set("dimension", value.dimension())
            .set("position", value.pos())
            .asElement());
    }

    /**
     * Removes a key from the NBT.
     *
     * @param key The key to remove.
     * @return The ItemNbt instance.
     */
    public ItemNbt remove(String key) {
        this.compound.remove(key);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, this.stack, this.compound);
        return this;
    }
}
