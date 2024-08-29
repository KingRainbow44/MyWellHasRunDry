package moe.seikimo.mwhrd.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface PlayerCraftEvent {
    /**
     * Event that is called when a player crafts an item.
     */
    Event<Craft> EVENT = EventFactory.createArrayBacked(
        Craft.class,
        (listeners) -> (
            player, item
        ) -> {
            for (var listener : listeners) {
                listener.onCraft(player, item);
            }
        }
    );

    @FunctionalInterface
    interface Craft {
        /**
         * Invoked when a player crafts an item.
         *
         * @param player The player who crafted the item.
         * @param item The item that was crafted.
         */
        void onCraft(PlayerEntity player, ItemStack item);
    }
}
