package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.game.beacon.BeaconLevel;
import moe.seikimo.mwhrd.game.beacon.BeaconManager;
import moe.seikimo.mwhrd.game.beacon.powers.SpawnControlPower;
import moe.seikimo.mwhrd.interfaces.IEntityConditions;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    private static final Set<EntityType<?>> BLACKLIST = Set.of(
        EntityType.WITHER,
        EntityType.ENDER_DRAGON
    );

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true)
    public void spawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof HostileEntity) &&
            entity.getType() != EntityType.SLIME) return;

        // Check if the entity is a trial entity.
        if (entity instanceof IEntityConditions conditions) {
            // We won't handle mob spawning if its from a trial spawner.
            if (conditions.mwhrd$isTrial()) return;
        }

        // Check if the entity is in the blacklist.
        if (BLACKLIST.contains(entity.getType())) {
            return;
        }

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
