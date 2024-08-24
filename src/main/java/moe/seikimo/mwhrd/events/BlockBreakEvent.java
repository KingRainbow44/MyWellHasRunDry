package moe.seikimo.mwhrd.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockBreakEvent {
    /**
     * Event that is called after a block is broken.
     */
    Event<AfterBreak> EVENT = EventFactory.createArrayBacked(
        AfterBreak.class,
        (listeners) -> (
            world, player, pos, state, blockEntity, tool
        ) -> {
            var result = ActionResult.SUCCESS;
            for (AfterBreak listener : listeners) {
                switch (listener.afterBreak(world, player, pos, state, blockEntity, tool)) {
                    case FAIL -> {
                        // We stop all listeners if one fails.
                        return ActionResult.FAIL;
                    }
                    case CONSUME, CONSUME_PARTIAL ->
                        // We only return consume if all listeners return consume.
                        result = ActionResult.CONSUME;
                }
            }

            return result;
        }
    );

    @FunctionalInterface
    interface AfterBreak {
        /**
         * Invoked after a block is broken.
         *
         * @param world The world the block was broken in.
         * @param player The player who broke the block.
         * @param pos The block's position.
         * @param state The block's previous state.
         * @param blockEntity The block's entity if it had one.
         * @param tool The tool used to break the block.
         * @return The result of the event.
         */
        ActionResult afterBreak(
            World world, PlayerEntity player,
            BlockPos pos, BlockState state, BlockEntity blockEntity,
            ItemStack tool
        );
    }
}
