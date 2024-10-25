package moe.seikimo.mwhrd.custom;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import moe.seikimo.mwhrd.custom.items.SoulOfLight;
import moe.seikimo.mwhrd.custom.items.TheAtlas;
import moe.seikimo.mwhrd.custom.items.armor.EnlightenedDiamondArmor;
import moe.seikimo.mwhrd.custom.items.tools.sword.EnlightenedDiamondSword;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface CustomItems {
    Item THE_ATLAS = Items.register(Identifier.of("mwhrd", "the_atlas"), new TheAtlas());
    Item SOUL_OF_LIGHT = Items.register(Identifier.of("mwhrd", "soul_of_light"), new SoulOfLight());

    Item ENLIGHTENED_DIAMOND_HELMET = Items.register(Identifier.of("mwhrd", "enlightened_diamond_helmet"), new EnlightenedDiamondArmor((ArmorItem) Items.LEATHER_HELMET));
    Item ENLIGHTENED_DIAMOND_CHESTPLATE = Items.register(Identifier.of("mwhrd", "enlightened_diamond_chestplate"), new EnlightenedDiamondArmor((ArmorItem) Items.LEATHER_CHESTPLATE));
    Item ENLIGHTENED_DIAMOND_LEGGINGS = Items.register(Identifier.of("mwhrd", "enlightened_diamond_leggings"), new EnlightenedDiamondArmor((ArmorItem) Items.LEATHER_LEGGINGS));
    Item ENLIGHTENED_DIAMOND_BOOTS = Items.register(Identifier.of("mwhrd", "enlightened_diamond_boots"), new EnlightenedDiamondArmor((ArmorItem) Items.LEATHER_BOOTS));
    Set<Item> ENLIGHTENED_DIAMOND_ARMOR = Set.of(ENLIGHTENED_DIAMOND_HELMET, ENLIGHTENED_DIAMOND_CHESTPLATE, ENLIGHTENED_DIAMOND_LEGGINGS, ENLIGHTENED_DIAMOND_BOOTS);

    Item ENLIGHTENED_DIAMOND_SWORD = Items.register(Identifier.of("mwhrd", "enlightened_diamond_sword"), new EnlightenedDiamondSword());

    Item BLOSSOM_SAPLING = Items.register(Identifier.of("mwhrd", "blossom_sapling"), new PolymerBlockItem(CustomBlocks.BLOSSOM_SAPLING, new Item.Settings(), Items.CHERRY_SAPLING));

    ItemGroup ITEM_GROUP = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemGroup.mwhrd"))
        .icon(Items.END_PORTAL_FRAME::getDefaultStack)
        .entries((context, entries) -> {
            entries.add(THE_ATLAS);
            entries.add(SOUL_OF_LIGHT);
            ENLIGHTENED_DIAMOND_ARMOR.forEach(entries::add);
            entries.add(ENLIGHTENED_DIAMOND_SWORD);
            entries.add(BLOSSOM_SAPLING);
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
