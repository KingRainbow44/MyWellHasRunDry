package moe.seikimo.mwhrd.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * An event that is triggered when the script cache is purged.
 */
public interface ScriptCachePurgeEvent {
    Event<Purge> EVENT = EventFactory.createArrayBacked(
        Purge.class,
        (listeners) -> () -> {
            for (var listener : listeners) {
                listener.onPurge();
            }
        }
    );

    @FunctionalInterface
    interface Purge {
        /**
         * Invoked when the script cache is purged.
         */
        void onPurge();
    }
}
