package moe.seikimo.mwhrd.managers;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acts as an asynchronous scheduling manager for the main thread.
 */
public final class TickManager {
    private static final List<Pair<TickEvent<?>, ?>> events = new ArrayList<>();
    private static final Map<TickEvent<?>, Integer> ticks = new HashMap<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(TickManager::onTick);
    }

    /**
     * Schedules an event to run every tick.
     *
     * @param event The event to run.
     * @param data The data to pass to the event.
     * @param <T> The type of data to pass to the event.
     */
    public static <T> void schedule(TickEvent<T> event, T data) {
        events.add(new Pair<>(event, data));
    }

    /**
     * Schedules an event to run every tick.
     *
     * @param event The event to run.
     */
    public static void schedule(TickEvent<Void> event) {
        TickManager.schedule(event, null);
    }

    /**
     * Executes all scheduled events.
     *
     * @param server The server to execute the events in.
     */
    private static void onTick(MinecraftServer server) {
        var copy = new ArrayList<>(events);
        for (var event : copy) {
            var data = event.getRight();
            var consumer = (TickEvent<?>) event.getLeft();

            var ticks = TickManager.ticks.getOrDefault(consumer, 0);

            if (!consumer.invoke(server, ticks, data)) {
                events.remove(event);
            }

            TickManager.ticks.put(consumer, ticks + 1);
        }
    }

    /**
     * A functional consumer for handling tick events.
     */
    public interface TickEvent<T> {
        /**
         * Invoker for the tick event.
         *
         * @param server The server which the consumer is operating in.
         * @param ticks The ticks that have elapsed.
         * @param object The data passed to the consumer.
         * @return True if the event should continue executing, false if it should be cancelled.
         */
        @SuppressWarnings("unchecked")
        default boolean invoke(MinecraftServer server, int ticks, Object object) {
            return this.tick(server, ticks, (T) object);
        }

        /**
         * Invoked at the start of every server tick.
         *
         * @param server The server which the consumer is operating in.
         * @param ticks The ticks that have elapsed.
         * @param object The data passed to the consumer.
         * @return True if the event should continue executing, false if it should be cancelled.
         */
        boolean tick(MinecraftServer server, int ticks, T object);
    }
}
