package moe.seikimo.mwhrd.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moe.seikimo.mwhrd.Utils;
import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.beacon.BeaconLevel;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity implements IAdvancedBeacon {
    @Shadow
    protected abstract void addComponents(ComponentMap.Builder componentMapBuilder);

    @Redirect(method = "tick", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/entity/BeaconBlockEntity;applyPlayerEffects(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/registry/entry/RegistryEntry;)V"
    ))
    private static void tick(
        World world, BlockPos pos, int beaconLevel,
        @Nullable RegistryEntry<StatusEffect> primaryEffect,
        @Nullable RegistryEntry<StatusEffect> secondaryEffect,
        @Local(argsOnly = true) BeaconBlockEntity self
    ) {
        if (self instanceof IAdvancedBeacon beacon && beacon.mwhrd$isAdvanced()) {
            applySpecialEffects(world, pos, beaconLevel, beacon);
        } else {
            BeaconBlockEntity.applyPlayerEffects(world, pos, beaconLevel, primaryEffect, secondaryEffect);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private static void tick(
        World world, BlockPos pos, BlockState state,
        BeaconBlockEntity blockEntity, CallbackInfo ci,
        @Local(ordinal = 0) int level
    ) {
        if (level == 0) {
            // Remove
        }
    }

    @Unique
    private static void applySpecialEffects(
        World world, BlockPos pos, int beaconLevel,
        IAdvancedBeacon self
    ) {
        var level = BeaconLevel.valueOf("TIER_" + beaconLevel);
        var players = Utils.getNearbyPlayers(world, pos, level.getRange());
        var lastPlayers = self.mwhrd$getLastPlayers();

        // For any players in last that aren't in players, remove the effect.
        var effects = self.mwhrd$getEffects();
        for (var playerUuid : new ArrayList<>(lastPlayers)) {
            var player = world.getPlayerByUuid(playerUuid);
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                continue;
            }

            if (!players.contains(player)) {
                for (var effect : effects) {
                    effect.getRemoveCallback().removeEffects(world, serverPlayer);
                }
                lastPlayers.remove(playerUuid);
            }
        }

        // Apply the effects to the players.
        for (var player : players) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                for (var effect : effects) {
                    effect.getApplyCallback().applyEffects(world, beaconLevel, serverPlayer);
                }

                if (!lastPlayers.contains(player.getUuid())) {
                    lastPlayers.add(player.getUuid());
                }
            }
        }
    }

    @Unique private int fuel = 0;
    @Unique private boolean advanced = false;
    @Unique private List<UUID> lastPlayers = new ArrayList<>();
    @Unique private List<BeaconEffect> effects = new ArrayList<>();

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.putBoolean("advanced", this.mwhrd$isAdvanced());
        nbt.putIntArray("effects", this.effects.stream()
            .map(BeaconEffect::ordinal)
            .toList());
        nbt.putInt("fuel", this.fuel);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        this.mwhrd$setAdvanced(nbt.getBoolean("advanced"));
        this.effects = new ArrayList<>(Arrays.stream(nbt.getIntArray("effects"))
            .mapToObj(ord -> BeaconEffect.values()[ord])
            .toList());
        this.fuel = nbt.getInt("fuel");
    }

    @Override
    public NbtComponent mwhrd$serialize() {
        var serialized = new NbtCompound();
        serialized.putInt("advanced_beacon", 1);
        serialized.putIntArray("effects", this.effects.stream()
            .map(BeaconEffect::ordinal)
            .toList());
        serialized.putInt("fuel", this.fuel);

        return NbtComponent.of(serialized);
    }

    @Override
    public void mwhrd$destroy() {
        var world = this.getWorld();
        if (world == null) return;

        // Disable beacon effects on players.
        this.mwhrd$getLastPlayers().stream()
            .map(world::getPlayerByUuid)
            .map(ServerPlayerEntity.class::cast)
            .forEach(beaconPlayer -> {
                for (var effect : this.mwhrd$getEffects()) {
                    effect.getRemoveCallback().removeEffects(world, beaconPlayer);
                }
            });
    }

    @Override
    public List<BeaconEffect> mwhrd$getEffects() {
        return this.effects;
    }

    @Override
    public void mwhrd$setEffects(List<BeaconEffect> effects) {
        this.effects = effects;
    }

    @Override
    public List<UUID> mwhrd$getLastPlayers() {
        return this.lastPlayers;
    }

    @Override
    public void mwhrd$setLastPlayers(List<UUID> lastPlayers) {
        this.lastPlayers = lastPlayers;
    }

    @Override
    public void mwhrd$setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    @Override
    public boolean mwhrd$isAdvanced() {
        return this.advanced;
    }

    @Override
    public int mwhrd$getFuel() {
        return this.fuel;
    }

    @Override
    public void mwhrd$setFuel(int fuel) {
        this.fuel = fuel;
    }
}
