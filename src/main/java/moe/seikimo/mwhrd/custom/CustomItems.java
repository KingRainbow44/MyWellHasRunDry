package moe.seikimo.mwhrd.custom;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import moe.seikimo.mwhrd.custom.items.SoulOfLight;
import moe.seikimo.mwhrd.custom.items.TheAtlas;
import moe.seikimo.mwhrd.custom.items.armor.EnlightenedDiamondBoots;
import moe.seikimo.mwhrd.custom.items.armor.EnlightenedDiamondChestplate;
import moe.seikimo.mwhrd.custom.items.armor.EnlightenedDiamondHelmet;
import moe.seikimo.mwhrd.custom.items.armor.EnlightenedDiamondLeggings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface CustomItems {
    Item THE_ATLAS = Items.register(Identifier.of("mwhrd", "the_atlas"), new TheAtlas());
    Item SOUL_OF_LIGHT = Items.register(Identifier.of("mwhrd", "soul_of_light"), new SoulOfLight());

    Item ENLIGHTENED_DIAMOND_HELMET = Items.register(Identifier.of("mwhrd", "enlightened_diamond_helmet"), new EnlightenedDiamondHelmet());
    Item ENLIGHTENED_DIAMOND_CHESTPLATE = Items.register(Identifier.of("mwhrd", "enlightened_diamond_chestplate"), new EnlightenedDiamondChestplate());
    Item ENLIGHTENED_DIAMOND_LEGGINGS = Items.register(Identifier.of("mwhrd", "enlightened_diamond_leggings"), new EnlightenedDiamondLeggings());
    Item ENLIGHTENED_DIAMOND_BOOTS = Items.register(Identifier.of("mwhrd", "enlightened_diamond_boots"), new EnlightenedDiamondBoots());

    ItemGroup ITEM_GROUP = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemGroup.mwhrd"))
        .icon(Items.END_PORTAL_FRAME::getDefaultStack)
        .entries((context, entries) -> {
            entries.add(THE_ATLAS);
            entries.add(SOUL_OF_LIGHT);

            entries.add(ENLIGHTENED_DIAMOND_HELMET);
            entries.add(ENLIGHTENED_DIAMOND_CHESTPLATE);
            entries.add(ENLIGHTENED_DIAMOND_LEGGINGS);
            entries.add(ENLIGHTENED_DIAMOND_BOOTS);
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
