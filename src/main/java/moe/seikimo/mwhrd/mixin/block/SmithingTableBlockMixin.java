package moe.seikimo.mwhrd.mixin.block;

import moe.seikimo.mwhrd.gui.block.SmithingCraftingGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmithingTableBlock.class)
public abstract class SmithingTableBlockMixin extends CraftingTableBlock {
    public SmithingTableBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player instanceof ServerPlayerEntity serverPlayer && player.isSneaking()) {
            SmithingCraftingGui.open(state, pos, serverPlayer);
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            player.incrementStat(Stats.INTERACT_WITH_SMITHING_TABLE);
        }

        return ActionResult.SUCCESS;
    }
}
