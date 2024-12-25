package moe.seikimo.mwhrd.custom;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.custom.items.SoulOfLight;
import moe.seikimo.mwhrd.custom.items.TheAtlas;
import moe.seikimo.mwhrd.custom.items.armor.EnlightenedDiamondArmor;
import moe.seikimo.mwhrd.custom.items.tools.sword.EnlightenedDiamondSword;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

import static moe.seikimo.mwhrd.utils.Utils.itemKey;

public interface CustomItems {
    Item THE_ATLAS = Items.register(itemKey("the_atlas"), TheAtlas::new);
    Item ATLAS_SHARD = Items.register(itemKey("atlas_shard"), settings -> new SimplePolymerItem(settings, Items.DISC_FRAGMENT_5));
    Item SOUL_OF_LIGHT = Items.register(itemKey("soul_of_light"), SoulOfLight::new);
    Item ENLIGHTENED_DIAMOND_HELMET = Items.register(itemKey("enlightened_diamond_helmet"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_HELMET, EquipmentType.HELMET, settings));
    Item ENLIGHTENED_DIAMOND_CHESTPLATE = Items.register(itemKey("enlightened_diamond_chestplate"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_CHESTPLATE, EquipmentType.CHESTPLATE, settings));
    Item ENLIGHTENED_DIAMOND_LEGGINGS = Items.register(itemKey("enlightened_diamond_leggings"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_LEGGINGS, EquipmentType.LEGGINGS, settings));
    Item ENLIGHTENED_DIAMOND_BOOTS = Items.register(itemKey("enlightened_diamond_boots"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_BOOTS, EquipmentType.BOOTS, settings));
    Set<Item> ENLIGHTENED_DIAMOND_ARMOR = Set.of(ENLIGHTENED_DIAMOND_HELMET, ENLIGHTENED_DIAMOND_CHESTPLATE, ENLIGHTENED_DIAMOND_LEGGINGS, ENLIGHTENED_DIAMOND_BOOTS);
    Item ENLIGHTENED_DIAMOND_SWORD = Items.register(itemKey("enlightened_diamond_sword"), EnlightenedDiamondSword::new);

    Item BLOSSOM_SAPLING = Items.register(itemKey("blossom_sapling"), settings -> new PolymerBlockItem(CustomBlocks.BLOSSOM_SAPLING, settings, Items.CHERRY_SAPLING));

    ItemGroup THE_REALM_OF_LIGHT = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemGroup.mwhrd.the_realm_of_light"))
        .icon(Items.END_PORTAL_FRAME::getDefaultStack)
        .entries((context, entries) -> {
            entries.add(THE_ATLAS);
            entries.add(ATLAS_SHARD);
            entries.add(SOUL_OF_LIGHT);
            entries.add(ENLIGHTENED_DIAMOND_SWORD);
            ENLIGHTENED_DIAMOND_ARMOR.forEach(entries::add);
        })
        .build();

    /**
     * Registers all custom content.
     */
    static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(
            Identifier.of("mwhrd", "item_group"),
            CustomItems.THE_REALM_OF_LIGHT
        );
    }
}
