package moe.seikimo.mwhrd.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public interface PlayerSwingHandEvent {
    Event<Swing> EVENT = EventFactory.createArrayBacked(
        Swing.class,
        (listeners) -> (player, hand) -> {
            for (var listener : listeners) {
                listener.onSwing(player, hand);
            }
        }
    );

    @FunctionalInterface
    interface Swing {
        /**
         * Invoked when the player swings their hand.
         *
         * @param player The player who swung their hand.
         * @param hand The hand that was swung.
         */
        void onSwing(ServerPlayerEntity player, Hand hand);
    }
}
