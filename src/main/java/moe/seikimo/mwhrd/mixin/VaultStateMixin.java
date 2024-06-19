package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.TrialChamberLoot;
import moe.seikimo.mwhrd.interfaces.IItemStackReference;
import net.minecraft.block.enums.VaultState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VaultState.class)
public abstract class VaultStateMixin {
    @Redirect(method = "ejectItem", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/dispenser/ItemDispenserBehavior;spawnItem(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/Position;)V"
    ))
    public void ejectItem(
        World world, ItemStack stack,
        int speed, Direction side, Position pos
    ) {
        // Add the item to the player's loot.
        var stackRef = (IItemStackReference) (Object) stack;
        if (stackRef != null) {
            var player = stackRef.mwhrd$getReference();
            if (player != null) {
                TrialChamberLoot.addLoot(player.getUuid(), stack);
            }
        }

        // We need to create a fake item to show what the player received.
        var itemEntity = TrialChamberLoot.spawnItem(
            world, stack, 2, Direction.UP, pos);
        itemEntity.setPickupDelayInfinite();

        // Delete the entity after 8s seconds.
        TrialChamberLoot.scheduleForDespawn(itemEntity, 8e3);
    }
}
