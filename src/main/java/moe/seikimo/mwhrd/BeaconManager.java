package moe.seikimo.mwhrd;

import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public final class BeaconManager {
    public static ActionResult handleBeacon(
        ItemStack stack, World world, BlockHitResult result
    ) {
        // Check if the stack contains the custom beacon data.
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return ActionResult.PASS;
        }
        if (!customData.contains("advanced_beacon")) {
            return ActionResult.PASS;
        }

        // Set the block.
        var newBlock = result.getBlockPos().add(result.getSide().getVector());
        world.setBlockState(newBlock, Blocks.BEACON.getDefaultState());
        var blockEntity = (BeaconBlockEntity) world.getBlockEntity(newBlock);
        if (blockEntity == null) {
            world.setBlockState(newBlock, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }

        ((IAdvancedBeacon) blockEntity).mwhrd$setAdvanced(true);

        return ActionResult.SUCCESS;
    }
}
