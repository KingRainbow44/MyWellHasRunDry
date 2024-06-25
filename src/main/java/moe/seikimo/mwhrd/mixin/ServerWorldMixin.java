package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.beacon.BeaconLevel;
import moe.seikimo.mwhrd.beacon.BeaconManager;
import moe.seikimo.mwhrd.beacon.powers.SpawnControlPower;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true)
    public void spawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof HostileEntity)) return;

        var entityPos = entity.getBlockPos();
        BeaconManager.getAllBeacons()
            .values().stream()
            .filter(beacon -> {
                var power = beacon.mwhrd$getPower(SpawnControlPower.class);
                return power != null && power.isEnabled();
            })
            .filter(beacon -> {
                var handle = (BeaconBlockEntity) beacon;
                var pos = handle.getPos();
                var level = BeaconLevel.of(handle.level);
                return level != null && entityPos.isWithinDistance(pos, level.getRange());
            })
            .findAny()
            .ifPresent(beacon -> cir.setReturnValue(false));
    }
}
