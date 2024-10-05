package moe.seikimo.mwhrd.custom;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import moe.seikimo.mwhrd.custom.items.TheAtlas;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface CustomItems {
    Item THE_ATLAS = Items.register(Identifier.of("mwhrd", "the_atlas"), new TheAtlas());

    ItemGroup ITEM_GROUP = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemGroup.mwhrd"))
        .icon(Items.BEDROCK::getDefaultStack)
        .entries((context, entries) -> {
            entries.add(CustomItems.THE_ATLAS);
        })
        .build();

    /**
     * Registers all custom content.
     */
    static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(
            Identifier.of("mwhrd", "item_group"),
            CustomItems.ITEM_GROUP
        );
    }
}
