package moe.seikimo.mwhrd.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moe.seikimo.mwhrd.utils.CustomItems;
import moe.seikimo.mwhrd.gui.AdvancedBeaconGui;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

@Mixin(BeaconBlock.class)
public abstract class BeaconBlockMixin extends BlockWithEntity {
    public BeaconBlockMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "onUse", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/player/PlayerEntity;openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;"
    ))
    public OptionalInt openScreen(
        PlayerEntity instance,
        NamedScreenHandlerFactory factory,
        @Local BeaconBlockEntity beaconBlock
    ) {
        var advancedBeacon = (IAdvancedBeacon) beaconBlock;
        if (!advancedBeacon.mwhrd$isAdvanced() ||
            !(instance instanceof ServerPlayerEntity serverPlayer)) {
            return instance.openHandledScreen(beaconBlock);
        }

        AdvancedBeaconGui.open(advancedBeacon, serverPlayer);
        return OptionalInt.empty();
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof IAdvancedBeacon beacon) {
            var item = CustomItems.ADVANCED_BEACON.copy();
            item.set(DataComponentTypes.CUSTOM_DATA, beacon.mwhrd$serializeComponent());

            var itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);

            // Disable beacon effects on players.
            beacon.mwhrd$destroy();
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return Collections.emptyList();
    }
}
