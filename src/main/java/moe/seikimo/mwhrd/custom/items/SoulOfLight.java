package moe.seikimo.mwhrd.custom.items;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.List;

public final class SoulOfLight extends SimplePolymerItem {
    public SoulOfLight(Settings settings) {
        super(
            settings
                .maxCount(64)
                .rarity(Rarity.EPIC)
                .component(
                    DataComponentTypes.LORE,
                    new LoreComponent(List.of(
                        Text.empty(),
                        Text.translatable("item.mwhrd.soul_of_light.warning.1")
                            .formatted(Formatting.RED),
                        Text.translatable("item.mwhrd.soul_of_light.warning.2")
                            .formatted(Formatting.RED),
                        Text.translatable("item.mwhrd.soul_of_light.warning.3")
                            .formatted(Formatting.RED)
                    ))
                ),
            Items.LIGHT_GRAY_DYE
        );
    }
}
