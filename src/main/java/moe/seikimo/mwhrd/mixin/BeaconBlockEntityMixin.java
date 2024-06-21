package moe.seikimo.mwhrd.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moe.seikimo.mwhrd.Utils;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin implements IAdvancedBeacon {
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
            applySpecialEffects(world, pos, beaconLevel);
        } else {
            BeaconBlockEntity.applyPlayerEffects(world, pos, beaconLevel, primaryEffect, secondaryEffect);
        }
    }

    @Unique
    private static void applySpecialEffects(World world, BlockPos pos, int beaconLevel) {
        for (var player : Utils.getNearbyPlayers(world, pos, 50.0)) {
            if (player instanceof ServerPlayerEntity) {
                player.sendMessage(Text.literal("You are now under the effects of the beacon!"), true);
            }
        }
    }

    @Unique private boolean advanced = false;

    @Inject(method = "writeNbt", at = @At("RETURN"))
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.putBoolean("advanced", this.mwhrd$isAdvanced());
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        this.mwhrd$setAdvanced(nbt.getBoolean("advanced"));
    }

    @Override
    public void mwhrd$setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    @Override
    public boolean mwhrd$isAdvanced() {
        return this.advanced;
    }
}
