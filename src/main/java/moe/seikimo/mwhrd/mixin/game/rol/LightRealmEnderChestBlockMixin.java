package moe.seikimo.mwhrd.mixin.game.rol;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(EnderChestBlock.class)
public abstract class LightRealmEnderChestBlockMixin extends AbstractChestBlock<EnderChestBlockEntity> {
    protected LightRealmEnderChestBlockMixin(
        Settings settings,
        Supplier<BlockEntityType<? extends EnderChestBlockEntity>> entityTypeRetriever
    ) {
        super(settings, entityTypeRetriever);
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        // Prevent the use of the ender chest in the realm of light.
        if (Utils.compare(world, MyWellHasRunDry.getRealmOfLight())) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
