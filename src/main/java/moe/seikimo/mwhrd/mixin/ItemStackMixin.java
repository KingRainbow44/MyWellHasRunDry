package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.interfaces.IItemStackReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStackReference {
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
}
