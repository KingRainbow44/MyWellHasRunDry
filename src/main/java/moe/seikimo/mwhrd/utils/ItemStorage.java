package moe.seikimo.mwhrd.utils;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import moe.seikimo.general.EncodingUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Serializable, infinite item storage.
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public final class ItemStorage {
    /**
     * This is where all unidentified items go to die.
     */
    public static final ItemStorage EMPTY = new ItemStorage();

    @Getter
    private final List<ItemStack> backing
        = Collections.synchronizedList(new ArrayList<>());

    /**
     * @return The size of the storage.
     */
    public int size() {
        return this.backing.size();
    }

    /**
     * Clears the storage.
     */
    public void clear() {
        this.backing.clear();
    }

    /**
     * Fetches an item from the storage.
     *
     * @param index The index of the item.
     * @return The item at the index.
     */
    @Nullable
    public ItemStack get(int index) {
        return this.backing.get(index);
    }

    /**
     * Removes an item from the storage.
     *
     * @param stack The item to remove.
     * @return The removed item.
     */
    public ItemStack remove(ItemStack stack) {
        return this.backing.remove(stack) ? stack : null;
    }

    /**
     * Removes an item from the storage.
     *
     * @param index The index of the item.
     * @return The removed item.
     */
    public ItemStack remove(int index) {
        return this.backing.remove(index);
    }

    /**
     * Counts the quantity of a specific item in the storage.
     *
     * @param itemType The item to count.
     * @return The quantity of the item in the storage.
     */
    public long count(Item itemType) {
        return this.backing.stream()
            .filter(stack -> stack.getItem() == itemType)
            .flatMapToInt(stack -> IntStream.of(stack.getCount()))
            .sum();
    }

    /**
     * Adds an item to the storage.
     *
     * @param stack The item to add.
     */
    public void offer(ItemStack stack) {
        var item = stack.getItem();
        var remaining = stack.getCount();

        var depth = 0;
        ItemStack workingStack = null;
        while (remaining > 0 && depth++ < 333) {
            if (workingStack == null) {
                workingStack = this.backing.stream()
                    .filter(s -> s.getItem() == item)
                    .filter(s -> s.getCount() < item.getMaxCount())
                    .findFirst()
                    .orElse(null);
                if (workingStack == null) {
                    break;
                }
            }

            // Calculate how many items we can take from 'remaining'
            // without putting the 'workingStack' over the 'getMaxCount'.
            var toTake = Math.min(remaining, item.getMaxCount() - workingStack.getCount());
            workingStack.setCount(workingStack.getCount() + toTake);
            remaining -= toTake;
        }

        while (remaining > 0) {
            var newStack = stack.copy();
            newStack.setCount(Math.min(remaining, item.getMaxCount()));
            this.backing.add(newStack);
            remaining -= newStack.getCount();
        }
    }

    /**
     * Removes a specific quantity of an item from the storage.
     *
     * @param item The item to remove.
     * @param count The quantity to remove.
     * @return Whether the operation was successful.
     */
    public boolean take(Item item, int count) {
        var counter = 0;

        var stacks = this.backing.stream()
            .filter(stack -> stack.getItem() == item)
            .toList();
        if (stacks.isEmpty()) return false;
        if (this.count(item) < count) return false;

        var index = 0;
        while (counter < count) {
            if (index >= stacks.size()) return false;

            var stack = stacks.get(index++);
            var stackCount = stack.getCount();

            if (stackCount > count - counter) {
                stack.setCount(stackCount - count + counter);
                counter = count;
            } else {
                counter += stackCount;
                stack.setCount(0);
                this.backing.remove(stack);
            }
        }

        return true;
    }

    /**
     * @return A JSON & Base64 serialized version of this item storage.
     */
    public List<String> serialize() {
        var items = new ArrayList<String>();

        for (var stack : this.backing) {
            var json = ItemStack.CODEC.encode(stack,
                JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
            var encodeResult = json.result();

            if (encodeResult.isEmpty()) {
                continue;
            }

            var base64 = EncodingUtils.base64Encode(
                encodeResult.get().toString());
            items.add(base64);
        }

        return Collections.unmodifiableList(items);
    }

    /**
     * @param serialized The Base64 & JSON-encoded item storage.
     */
    public void deserialize(List<String> serialized) {
        for (var item : serialized) {
            var json = JsonParser.parseString(
                EncodingUtils.strBase64Decode(item));
            var result = ItemStack.CODEC.decode(JsonOps.INSTANCE, json);
            var data = result.result();

            if (data.isEmpty()) {
                continue;
            }

            this.backing.add(data.get().getFirst());
        }
    }
}
