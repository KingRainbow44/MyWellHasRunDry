package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.interfaces.IItemStackReference;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import moe.seikimo.mwhrd.interfaces.nbt.IItemNbtWrapper;
import moe.seikimo.mwhrd.utils.items.ItemNbt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements
    IItemStackReference,
    IItemNbtWrapper
{
    @Shadow
    public abstract int getDamage();

    @Unique private boolean unbreakable = false;

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
        if (this.unbreakable && this.getDamage() < damage) {
            ci.cancel();
        }
    }

    /// <editor-fold desc="IItemStackReference">

    @Unique private PlayerEntity reference;

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

    /// </editor-fold>

    /// <editor-fold desc="IItemNbtWrapper">

    @Override
    public ItemNbt mwhrd$asNbt() {
        return new ItemNbt((ItemStack) (Object) this);
    }

    /// </editor-fold>
}
