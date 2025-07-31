package moe.seikimo.mwhrd.utils.items;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.PostLoad;
import dev.morphia.annotations.PrePersist;
import dev.morphia.annotations.Transient;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.NBT;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Serializable mapped item storage.
 */
@Embedded
public final class MappedItemStorage implements Iterable<ItemStack> {
    private final transient Int2ObjectMap<ItemStack> backing = new Int2ObjectOpenHashMap<>();

    /** This is the map to be serialized by Morphia. */
    private Map<Integer, String> backing$1 = new HashMap<>();

    /**
     * This method is invoked before being serialized by Morphia.
     */
    @PrePersist
    private void beforeSave() {
        var registry = MyWellHasRunDry
            .getServer()
            .getRegistryManager();

        // Clear the backing map.
        this.backing$1.clear();

        // Serialize each entry.
        for (var entry : this.backing.int2ObjectEntrySet()) {
            var index = entry.getIntKey();
            var stack = entry.getValue();

            // If the stack is empty, skip it.
            if (stack.isEmpty()) {
                continue;
            }

            // Serialize the stack.
            var serialized = NBT.writeItemStack(stack);
            // Write the stack.
            this.backing$1.put(index, Utils.base64Encode(serialized));
        }
    }

    /**
     * This method is invoked after being deserialized by Morphia.
     */
    @PostLoad
    private void onLoad() {
        var registry = MyWellHasRunDry
            .getServer()
            .getRegistryManager();

        // Clear the backing map.
        this.backing.clear();

        // Deserialize each entry.
        for (var entry : this.backing$1.entrySet()) {
            var index = entry.getKey();
            var stack = entry.getValue();

            // Deserialize the stack.
            var deserialized = Utils.base64Decode(stack);
            var decoded = NBT.readItemStack(deserialized);

            // Write the stack.
            decoded.ifPresent(itemStack -> this.backing.put((int) index, itemStack));
        }
    }

    /**
     * Clears the storage.
     */
    public void clear() {
        this.backing.clear();
        this.backing$1.clear();
    }

    /**
     * Serializes an entity equipment into this storage.
     *
     * @param equipment The equipment to serialize.
     */
    public void fromEquipment(EntityEquipment equipment) {
        this.backing.clear();

        // Serialize the equipment.
        for (var slot : EquipmentSlot.values()) {
            var entry = equipment.get(slot);
            if (entry == null) {
                continue;
            }

            this.backing.put(slot.getIndex(), entry);
        }
    }

    /**
     * Serializes an inventory into this storage.
     *
     * @param inventory The inventory to serialize.
     */
    public void fromInventory(Inventory inventory) {
        this.backing.clear();

        // Serialize the inventory.
        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            this.backing.put(i, stack);
        }
    }

    /**
     * Deserializes this storage into entity equipment.
     *
     * @param equipment The equipment to deserialize into.
     */
    public void writeToEquipment(EntityEquipment equipment) {
        equipment.clear();

        for (var slot : EquipmentSlot.values()) {
            var entry = this.backing.get(slot.getIndex());
            if (entry == null) {
                continue;
            }

            equipment.put(slot, entry);
        }
    }

    /**
     * Deserializes this storage into an inventory.
     *
     * @param inventory The inventory to deserialize into.
     */
    public void writeToInventory(Inventory inventory) {
        // Clear the inventory.
        inventory.clear();

        // Write the inventory.
        for (var entry : this.backing.int2ObjectEntrySet()) {
            var index = entry.getIntKey();
            var stack = entry.getValue();

            // If the stack is empty, skip it.
            if (stack.isEmpty()) {
                continue;
            }

            // Write the stack.
            inventory.setStack(index, stack);
        }
    }

    @NotNull
    @Override
    @Deprecated
    public Iterator<ItemStack> iterator() {
        return this.backing.values().iterator();
    }
}
