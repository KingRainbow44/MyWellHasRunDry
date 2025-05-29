package moe.seikimo.mwhrd.mixin.beacon;

import moe.seikimo.mwhrd.custom.CustomEntities;
import moe.seikimo.mwhrd.custom.entities.AdvancedBeaconBlockEntity;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeaconBlock.class)
public abstract class BeaconBlockMixin extends BlockWithEntity implements Stainable {
    public BeaconBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedBeaconBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BeaconBlock.validateTicker(type, CustomEntities.ADVANCED_BEACON, AdvancedBeaconBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof AdvancedBeaconBlockEntity blockEntity) {
            player.openHandledScreen(blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_BEACON);
        }

        return ActionResult.SUCCESS;
    }
}
