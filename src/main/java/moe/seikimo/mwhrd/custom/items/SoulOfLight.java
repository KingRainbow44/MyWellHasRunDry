package moe.seikimo.mwhrd.custom.items;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;

public final class SoulOfLight extends SimplePolymerItem {
    public SoulOfLight(Settings settings) {
        super(
            settings
                .maxCount(64)
                .rarity(Rarity.EPIC),
            Items.LIGHT_GRAY_DYE
        );
    }
}
