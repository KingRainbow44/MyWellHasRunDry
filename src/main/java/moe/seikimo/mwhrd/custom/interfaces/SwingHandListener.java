package moe.seikimo.mwhrd.custom.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * If the item implements {@link SwingHandListener}, then {@link Item#use(World, PlayerEntity, Hand)} will get invoked when player swings their hand.
 */
public interface SwingHandListener {
    /**
     * Invoked when the player swings their hand.
     * By default, this will call {@link Item#use(World, PlayerEntity, Hand)}.
     *
     * @param stack The item stack the player is holding.
     * @param world The world.
     * @param player The player.
     * @param hand The hand that was swung.
     */
    default void onSwingHand(ItemStack stack, World world, PlayerEntity player, Hand hand) {
        if (this instanceof Item item) {
            item.use(world, player, hand);
        }
    }
}
