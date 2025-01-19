package moe.seikimo.mwhrd.mixin.game.hardcore;

import moe.seikimo.mwhrd.interfaces.game.IHardcorePlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class HardcoreEntityMixin extends LivingEntity {
    protected HardcoreEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "setTarget", at = @At("HEAD"))
    public void mwhrd$addTarget(LivingEntity target, CallbackInfo ci) {
       if (target == null) {
           return;
       }

        // Check if the target is a player.
        if (target instanceof IHardcorePlayer hardcorePlayer) {
            hardcorePlayer.mwhrd$addTarget((MobEntity) (Object) this);
        }
    }
}
