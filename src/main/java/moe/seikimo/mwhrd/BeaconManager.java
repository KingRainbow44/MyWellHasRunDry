package moe.seikimo.mwhrd;

import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

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

        // Update the block from the item's state.
        var nbt = customData.copyNbt();
        var effects = Arrays.stream(nbt.getIntArray("effects"))
            .mapToObj(i -> BeaconEffect.values()[i])
            .toList();
        var fuel = nbt.getInt("fuel");

        var beacon = (IAdvancedBeacon) blockEntity;
        beacon.mwhrd$setEffects(new ArrayList<>(effects));
        beacon.mwhrd$setFuel(fuel);
        beacon.mwhrd$setAdvanced(true);
        stack.decrement(1);

        return ActionResult.SUCCESS;
    }
}
