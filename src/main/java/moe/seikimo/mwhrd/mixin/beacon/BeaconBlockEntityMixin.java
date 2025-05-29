package moe.seikimo.mwhrd.mixin.beacon;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomBlocks;
import moe.seikimo.mwhrd.custom.entities.AdvancedBeaconBlockEntity;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.BeaconModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin
    extends BlockEntity
    implements IAdvancedBeacon, IDBObject<BeaconModel> {

    @Unique
    private static final Set<String> KEEP_KEYS = Set.of(
        "adv_beacon", "fuel", "powers"
    );

    @Shadow
    protected abstract void addComponents(ComponentMap.Builder componentMapBuilder);

    @Inject(method = "tick", at = @At("HEAD"))
    private static void tick(
        World world, BlockPos pos, BlockState state,
        BeaconBlockEntity blockEntity, CallbackInfo ci
    ) {
        // Ignore if the block entity is not an instance of IAdvancedBeacon.
        if (!(blockEntity instanceof IAdvancedBeacon advBeacon)) {
            return;
        }

        // If this beacon is advanced, replace it with an AdvancedBeaconBlockEntity.
        if (advBeacon.mwhrd$isAdvanced()) {
            // Get the existing data of the beacon.
            var registry = MyWellHasRunDry.getRegistry();
            var existing = blockEntity.createComponentlessNbt(registry);

            // Replace the block state.
            var newState = CustomBlocks.ADVANCED_BEACON.getDefaultState();
            if (world.setBlockState(pos, newState, Block.NOTIFY_ALL)) {
                world.removeBlockEntity(pos);

                // Set the new block entity.
                var newBlockEntity = new AdvancedBeaconBlockEntity(pos, state);
                newBlockEntity.setCachedState(newState);
                newBlockEntity.readComponentlessNbt(existing, registry);
                world.addBlockEntity(newBlockEntity);
            }
        }
    }

    @Unique
    private boolean advanced = false;

    @Unique
    private NbtCompound toMigrate = new NbtCompound();

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        this.advanced = nbt.getBoolean("adv_beacon", false);

        nbt.forEach((key, value) -> {
            if (KEEP_KEYS.contains(key)) {
                this.toMigrate.put(key, value);
            }
        });
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("adv_beacon", this.advanced);
        this.toMigrate.forEach(nbt::put);
    }

    @Override
    public boolean mwhrd$isAdvanced() {
        return this.advanced;
    }
}
