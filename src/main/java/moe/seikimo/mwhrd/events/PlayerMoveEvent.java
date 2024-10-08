package moe.seikimo.mwhrd.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PlayerMoveEvent {
    /**
     * Event that is called when a player moves.
     */
    Event<Move> EVENT = EventFactory.createArrayBacked(
        Move.class,
        (listeners) -> (
            world, newPosition, player
        ) -> {
            for (Move listener : listeners) {
                listener.onMove(world, newPosition, player);
            }
        }
    );

    @FunctionalInterface
    interface Move {
        /**
         * Invoked when a player moves.
         *
         * @param world The world the player is in.
         * @param newPosition The player's new position.
         * @param player The player who moved.
         */
        void onMove(World world, BlockPos newPosition, PlayerEntity player);
    }
}
