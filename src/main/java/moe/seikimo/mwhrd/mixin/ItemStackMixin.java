package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.interfaces.IItemStackReference;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStackReference {
    @Unique private PlayerEntity reference;

    @Unique private boolean unbreakable = false;

    @Unique
    @Override
    public void mwhrd$setReference(PlayerEntity player) {
        this.reference = player;
    }

    @Unique
    @Override
    public PlayerEntity mwhrd$getReference() {
        return this.reference;
    }

    @Inject(method = "inventoryTick", at = @At("RETURN"))
    public void inventoryTick(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (selected && entity instanceof IPlayerConditions condPlayer) {
            this.unbreakable = condPlayer.mwhrd$isUnbreakable();
        } else {
            this.unbreakable = false;
        }
    }

    @Inject(method = "setDamage", at = @At("HEAD"), cancellable = true)
    public void setDamage(int damage, CallbackInfo ci) {
        if (this.unbreakable) {
            ci.cancel();
        }
    }
}
