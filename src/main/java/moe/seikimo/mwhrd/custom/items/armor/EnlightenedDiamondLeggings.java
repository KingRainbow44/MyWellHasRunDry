package moe.seikimo.mwhrd.custom.items.armor;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class EnlightenedDiamondLeggings extends ArmorItem implements PolymerItem {
    public EnlightenedDiamondLeggings() {
        super(
            ArmorMaterials.DIAMOND,
            Type.LEGGINGS,
            new Settings()
                .maxCount(1)
                .maxDamage(100)
        );
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.LEATHER_LEGGINGS;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return 0x9bb3e8;
    }
}
