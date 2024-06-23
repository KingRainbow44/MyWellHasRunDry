package moe.seikimo.mwhrd.utils;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import moe.seikimo.general.EncodingUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Serializable, infinite item storage.
 */
public final class ItemStorage {
    private final List<ItemStack> backing
        = Collections.synchronizedList(new ArrayList<>());

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
        this.backing.add(stack);
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
