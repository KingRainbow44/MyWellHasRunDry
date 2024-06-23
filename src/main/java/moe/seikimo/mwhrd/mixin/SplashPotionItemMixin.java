package moe.seikimo.mwhrd.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.ThrowablePotionItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SplashPotionItem.class)
public abstract class SplashPotionItemMixin extends ThrowablePotionItem {
    public SplashPotionItemMixin(Item.Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxCount() {
        return 64;
    }
}
