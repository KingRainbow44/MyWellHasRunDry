package moe.seikimo.mwhrd.mixin.game.deep;

import moe.seikimo.mwhrd.custom.entities.CelestialFishingBobberEntity;
import moe.seikimo.mwhrd.interfaces.player.IDeepPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class DeepPlayerMixin extends LivingEntity implements IDeepPlayer {
    @Unique
    @Nullable
    public CelestialFishingBobberEntity fishHook;

    public DeepPlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public CelestialFishingBobberEntity mwhrd$getFishHook() {
        return this.fishHook;
    }

    @Override
    public void mwhrd$setFishHook(@Nullable CelestialFishingBobberEntity fishHook) {
        this.fishHook = fishHook;
    }
}
