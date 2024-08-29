package moe.seikimo.mwhrd.mixin.event;

import moe.seikimo.mwhrd.events.PlayerCraftEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "onCraftByPlayer", at = @At("HEAD"))
    public void onCraftByPlayer(World world, PlayerEntity player, int amount, CallbackInfo ci) {
        PlayerCraftEvent.EVENT
            .invoker()
            .onCraft(player, (ItemStack) (Object) this);
    }
}
