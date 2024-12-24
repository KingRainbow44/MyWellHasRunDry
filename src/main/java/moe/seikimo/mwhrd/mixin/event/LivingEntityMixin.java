package moe.seikimo.mwhrd.mixin.event;

import moe.seikimo.mwhrd.events.EntityPreDeathEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
        method = "damage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"
        ),
        cancellable = true
    )
    public void onPreDeath(
        ServerWorld world, DamageSource source, float amount,
        CallbackInfoReturnable<Boolean> cir
    ) {
        // The method is cancelled with 'false' if the entity did not take damage.
        var result = EntityPreDeathEvent.EVENT
            .invoker()
            .onPreDeath((LivingEntity) (Object) this, source);
        if (!result) {
            cir.setReturnValue(false);
        }
    }
}
