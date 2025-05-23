package moe.seikimo.mwhrd.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerRemoveEvent {
    Event<Remove> EVENT = EventFactory.createArrayBacked(
        Remove.class,
        (listeners) -> (manager, player) -> {
            for (var listener : listeners) {
                listener.onPlayerRemove(manager, player);
            }
        }
    );

    @FunctionalInterface
    interface Remove {
        /**
         * Invoked when a player is removed from the game.
         *
         * @param manager The manager that handles the player.
         * @param player The player that was removed.
         */
        void onPlayerRemove(PlayerManager manager, ServerPlayerEntity player);
    }
}
