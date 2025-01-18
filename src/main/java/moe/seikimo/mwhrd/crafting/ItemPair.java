package moe.seikimo.mwhrd.crafting;

import net.minecraft.item.Item;

/**
 * Represents an item and its quantity.
 *
 * @param item The item.
 * @param quantity The quantity of the item.
 */
public record ItemPair(Item item, int quantity) {
    /**
     * Default constructor for a single item.
     *
     * @param item The item.
     */
    public ItemPair(Item item) {
        this(item, 1);
    }
}
