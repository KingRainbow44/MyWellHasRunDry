package moe.seikimo.mwhrd.crafting;

import net.minecraft.item.Item;

/**
 * Represents an item and its quantity.
 *
 * @param item The item.
 * @param quantity The quantity of the item.
 */
public record ItemPair(Item item, int quantity) {
}
