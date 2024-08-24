package moe.seikimo.mwhrd.mixin.event;

import moe.seikimo.mwhrd.events.BlockBreakEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "afterBreak", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V"
    ), cancellable = true)
    public void afterBreak(
        World world, PlayerEntity player,
        BlockPos pos, BlockState state, BlockEntity blockEntity,
        ItemStack tool, CallbackInfo ci
    ) {
        var result = BlockBreakEvent.EVENT.invoker().afterBreak(
            world, player, pos, state, blockEntity, tool
        );

        if (!result.isAccepted()) {
            ci.cancel(); // We do not drop any stacks.
        }
    }
}
