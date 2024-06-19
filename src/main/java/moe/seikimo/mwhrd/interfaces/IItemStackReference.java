package moe.seikimo.mwhrd.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface IItemStackReference {
    /**
     * Sets the item stack's player reference.
     */
    void mwhrd$setReference(PlayerEntity player);

    /**
     * @return The item stack's player reference.
     */
    PlayerEntity mwhrd$getReference();
}
