package moe.seikimo.mwhrd.mixin.trial;

import moe.seikimo.mwhrd.interfaces.IEntityConditions;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import moe.seikimo.mwhrd.utils.MobGear;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin
    extends LivingEntity
    implements IEntityConditions {

    @Shadow
    private @Nullable LivingEntity target;

    @Shadow
    public abstract void equipStack(EquipmentSlot slot, ItemStack stack);

    @Unique private boolean stickTarget = false;
    @Unique private boolean trial = false;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    public void setTarget(LivingEntity target, CallbackInfo ci) {
        if (this.stickTarget) {
            if (this.target == null || this.target.isDead()) {
                this.stickTarget = false;
                return;
            }

            ci.cancel();
            return;
        }

        if (this.mwhrd$isTrial() && !(target instanceof PlayerEntity)) {
            ci.cancel();
            return;
        }

        if (target instanceof IPlayerConditions condPlayer) {
            if (condPlayer.mwhrd$isHardcore()) {
                this.stickTarget = true;

                // Apply overpowered gear.
                MobGear.applyArmor((MobEntity) (Object) this);
                this.equipStack(EquipmentSlot.OFFHAND, Items.TOTEM_OF_UNDYING.getDefaultStack());

                // Apply strong weapon.
                if ((Object) this instanceof AbstractSkeletonEntity) {
                    this.equipStack(EquipmentSlot.MAINHAND, MobGear.BOW.copy());
                } else {
                    this.equipStack(EquipmentSlot.MAINHAND, MobGear.SWORD.copy());
                }

                // Apply status effects.
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 2));

                // Increase tracking distance.
                var distance = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
                if (distance != null) {
                    distance.setBaseValue(2048f);
                }
            }
        }
    }

    @Override
    public void mwhrd$setTrial(boolean trial) {
        this.trial = trial;
    }

    @Override
    public boolean mwhrd$isTrial() {
        return this.trial;
    }
}
