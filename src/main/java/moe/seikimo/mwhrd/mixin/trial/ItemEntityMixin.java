package moe.seikimo.mwhrd.mixin.trial;

import moe.seikimo.mwhrd.utils.TrialChamberLoot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getStack();

    @Inject(method = "onPlayerCollision", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"
    ), cancellable = true)
    public void checkIfKey(PlayerEntity player, CallbackInfo ci) {
        var itemStack = this.getStack();
        var item = itemStack.getItem();
        var count = itemStack.getCount();

        if (item != Items.TRIAL_KEY) return;

        // Add the key to the player's loot.
        TrialChamberLoot.addLoot(player.getUuid(), itemStack);

        // Run default behavior.
        var thisEntity = (ItemEntity) (Object) this;
        player.sendPickup(thisEntity, count);
        player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), count);
        player.triggerItemPickedUpByEntityCriteria(thisEntity);

        thisEntity.discard();

        ci.cancel();
    }
}
