package moe.seikimo.mwhrd.custom.items.armor;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class EnlightenedDiamondBoots extends ArmorItem implements PolymerItem {
    public EnlightenedDiamondBoots() {
        super(
            ArmorMaterials.DIAMOND,
            Type.BOOTS,
            new Settings()
                .maxCount(1)
                .maxDamage(100)
        );
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.LEATHER_BOOTS;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return 0x9bb3e8;
    }
}
