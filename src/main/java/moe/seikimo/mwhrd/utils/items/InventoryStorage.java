package moe.seikimo.mwhrd.utils.items;

import dev.morphia.annotations.Embedded;
import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;

/**
 * An inventory storage for a player.
 * <p>
 * Stores all items.
 */
@Data
@Embedded
public final class InventoryStorage {
    private MappedItemStorage
        inventory = new MappedItemStorage(),
        enderChest = new MappedItemStorage();

    /**
     * Clears the backing storages.
     */
    public void clear() {
        this.inventory.clear();
        this.enderChest.clear();
    }

    /**
     * Serializes the player's entire inventory.
     *
     * @param player The player to serialize.
     */
    public void input(PlayerEntity player) {
        this.inventory.fromInventory(player.getInventory());
        this.enderChest.fromInventory(player.getEnderChestInventory());
    }

    /**
     * Deserializes the player's entire inventory.
     *
     * @param player The player to deserialize.
     */
    public void output(PlayerEntity player) {
        this.inventory.writeToInventory(player.getInventory());
        this.enderChest.writeToInventory(player.getEnderChestInventory());
    }
}
