package moe.seikimo.mwhrd.custom;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.custom.items.SoulOfLight;
import moe.seikimo.mwhrd.custom.items.TheAtlas;
import moe.seikimo.mwhrd.custom.items.armor.EnlightenedDiamondArmor;
import moe.seikimo.mwhrd.custom.items.tools.axe.EnlightenedDiamondAxe;
import moe.seikimo.mwhrd.custom.items.tools.hoe.EnlightenedDiamondHoe;
import moe.seikimo.mwhrd.custom.items.tools.pickaxe.EnlightenedDiamondPickaxe;
import moe.seikimo.mwhrd.custom.items.tools.shovel.EnlightenedDiamondShovel;
import moe.seikimo.mwhrd.custom.items.tools.sword.EnlightenedDiamondSword;
import moe.seikimo.mwhrd.utils.items.LoreBuilder;
import moe.seikimo.mwhrd.utils.items.NbtBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.HashSet;
import java.util.Set;

import static eu.pb4.polymer.core.api.item.PolymerItemGroupUtils.registerPolymerItemGroup;
import static moe.seikimo.mwhrd.utils.Utils.itemKey;

public interface CustomItems {
    Item BEACON_BASE = Items.register(
        itemKey("beacon_base"),
        settings -> new SimplePolymerItem(settings
            .component(DataComponentTypes.LORE, LoreBuilder.of(false)
                .literal("Legend has it this beacon can emit special radiation.", Formatting.AQUA)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.EPIC),
            Items.SPAWNER
        )
    );
    Item ADVANCED_BEACON = Items.register(
        itemKey("advanced_beacon"),
        settings -> new SimplePolymerItem(settings
            .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                .set("adv_beacon", 1)
                .build())
            .component(DataComponentTypes.LORE, LoreBuilder.of(false)
                .literal("This beacon emits special radiation!", Formatting.AQUA)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.EPIC),
            Items.BEACON
        )
    );
    Item PLOT_PURGER_UPGRADE = Items.register(
        itemKey("plot_purger_upgrade"),
        settings -> new SimplePolymerItem(settings
            .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                .set("beacon_upgrade", "plot_purger")
                .build())
            .component(DataComponentTypes.LORE, LoreBuilder.of()
                .add(Text.translatable("text.mwhrd.beacon.item_applied_to")
                    .formatted(Formatting.GRAY))
                .literal(" Unlocks Plot Purging", Formatting.BLUE)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.RARE),
            Items.WITHER_SPAWN_EGG
        )
    );
    Item PIXEL_PRINTER_UPGRADE = Items.register(
        itemKey("pixel_printer_upgrade"),
        settings -> new SimplePolymerItem(settings
            .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                .set("beacon_upgrade", "pixel_printer")
                .build())
            .component(DataComponentTypes.LORE, LoreBuilder.of()
                .add(Text.translatable("text.mwhrd.beacon.item_applied_to")
                    .formatted(Formatting.GRAY))
                .literal(" Unlocks Item Duplication", Formatting.BLUE)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.RARE),
            Items.BAT_SPAWN_EGG
        )
    );
    Item FLIGHT_CRYSTAL_UPGRADE = Items.register(
        itemKey("flight_crystal_upgrade"),
        settings -> new SimplePolymerItem(settings
            .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                .set("beacon_upgrade", "flight_crystal")
                .build())
            .component(DataComponentTypes.LORE, LoreBuilder.of()
                .add(Text.translatable("text.mwhrd.beacon.item_applied_to")
                    .formatted(Formatting.GRAY))
                .literal(" Grants Flight", Formatting.BLUE)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.UNCOMMON),
            Items.ALLAY_SPAWN_EGG
        )
    );
    Item TELEPORT_EYE_UPGRADE = Items.register(
        itemKey("teleport_eye_upgrade"),
        settings -> new SimplePolymerItem(settings
            .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                .set("beacon_upgrade", "eye_of_teleportation")
                .build())
            .component(DataComponentTypes.LORE, LoreBuilder.of()
                .add(Text.translatable("text.mwhrd.beacon.item_applied_to")
                    .formatted(Formatting.GRAY))
                .literal(" Enables Teleportation", Formatting.BLUE)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.UNCOMMON),
            Items.ENDERMAN_SPAWN_EGG
        )
    );
    Item WORLDEDIT_UPGRADE = Items.register(
        itemKey("worldedit_upgrade"),
        settings -> new SimplePolymerItem(settings
            .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                .set("beacon_upgrade", "worldedit")
                .build())
            .component(DataComponentTypes.LORE, LoreBuilder.of()
                .add(Text.translatable("text.mwhrd.beacon.item_applied_to")
                    .formatted(Formatting.GRAY))
                .literal(" Enables WorldEdit", Formatting.BLUE)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.EPIC),
            Items.ENDER_DRAGON_SPAWN_EGG
        )
    );

    Item THE_ATLAS = Items.register(itemKey("the_atlas"), TheAtlas::new);
    Item ATLAS_SHARD = Items.register(itemKey("atlas_shard"), settings -> new SimplePolymerItem(settings, Items.DISC_FRAGMENT_5));
    Item SOUL_OF_LIGHT = Items.register(itemKey("soul_of_light"), SoulOfLight::new);
    Item ENLIGHTENED_DIAMOND_HELMET = Items.register(itemKey("enlightened_diamond_helmet"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_HELMET, EquipmentType.HELMET, settings));
    Item ENLIGHTENED_DIAMOND_CHESTPLATE = Items.register(itemKey("enlightened_diamond_chestplate"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_CHESTPLATE, EquipmentType.CHESTPLATE, settings));
    Item ENLIGHTENED_DIAMOND_LEGGINGS = Items.register(itemKey("enlightened_diamond_leggings"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_LEGGINGS, EquipmentType.LEGGINGS, settings));
    Item ENLIGHTENED_DIAMOND_BOOTS = Items.register(itemKey("enlightened_diamond_boots"), settings -> new EnlightenedDiamondArmor(Items.LEATHER_BOOTS, EquipmentType.BOOTS, settings));
    Item ENLIGHTENED_DIAMOND_SWORD = Items.register(itemKey("enlightened_diamond_sword"), EnlightenedDiamondSword::new);
    Item ENLIGHTENED_DIAMOND_AXE = Items.register(itemKey("enlightened_diamond_axe"), EnlightenedDiamondAxe::new);
    Item ENLIGHTENED_DIAMOND_HOE = Items.register(itemKey("enlightened_diamond_hoe"), EnlightenedDiamondHoe::new);
    Item ENLIGHTENED_DIAMOND_PICKAXE = Items.register(itemKey("enlightened_diamond_pickaxe"), EnlightenedDiamondPickaxe::new);
    Item ENLIGHTENED_DIAMOND_SHOVEL = Items.register(itemKey("enlightened_diamond_shovel"), EnlightenedDiamondShovel::new);
    Set<Item> ENLIGHTENED_DIAMOND_ARMOR = Set.of(ENLIGHTENED_DIAMOND_HELMET, ENLIGHTENED_DIAMOND_CHESTPLATE, ENLIGHTENED_DIAMOND_LEGGINGS, ENLIGHTENED_DIAMOND_BOOTS);
    Set<Item> ENLIGHTENED_DIAMOND_TOOLS = Set.of(ENLIGHTENED_DIAMOND_SWORD, ENLIGHTENED_DIAMOND_AXE, ENLIGHTENED_DIAMOND_PICKAXE, ENLIGHTENED_DIAMOND_HOE, ENLIGHTENED_DIAMOND_SHOVEL);
    Set<Item> ENLIGHTENED_DIAMOND_ITEMS = new HashSet<>() {{
        this.addAll(ENLIGHTENED_DIAMOND_ARMOR);
        this.addAll(ENLIGHTENED_DIAMOND_TOOLS);
    }};

    Item BLOSSOM_SAPLING = Items.register(itemKey("blossom_sapling"), settings -> new PolymerBlockItem(CustomBlocks.BLOSSOM_SAPLING, settings, Items.CHERRY_SAPLING));

    ItemGroup MY_WELL_HAS_RUN_DRY = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemGroup.mwhrd"))
        .icon(Items.BUCKET::getDefaultStack)
        .entries((context, entries) -> {
            entries.add(BLOSSOM_SAPLING);
        })
        .build();

    ItemGroup LUCK_AND_LUXURY = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemGroup.mwhrd.luck_and_luxury"))
        .icon(Items.BEACON::getDefaultStack)
        .entries((context, entries) -> {
            entries.add(BEACON_BASE);
            entries.add(ADVANCED_BEACON);
            entries.add(PLOT_PURGER_UPGRADE);
            entries.add(PIXEL_PRINTER_UPGRADE);
            entries.add(FLIGHT_CRYSTAL_UPGRADE);
            entries.add(TELEPORT_EYE_UPGRADE);
            entries.add(WORLDEDIT_UPGRADE);
        })
        .build();

    ItemGroup THE_REALM_OF_LIGHT = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemGroup.mwhrd.the_realm_of_light"))
        .icon(Items.END_PORTAL_FRAME::getDefaultStack)
        .entries((context, entries) -> {
            entries.add(THE_ATLAS);
            entries.add(ATLAS_SHARD);
            entries.add(SOUL_OF_LIGHT);
            entries.add(ENLIGHTENED_DIAMOND_SWORD);
            entries.add(ENLIGHTENED_DIAMOND_AXE);
            entries.add(ENLIGHTENED_DIAMOND_HOE);
            entries.add(ENLIGHTENED_DIAMOND_PICKAXE);
            entries.add(ENLIGHTENED_DIAMOND_SHOVEL);
            ENLIGHTENED_DIAMOND_ARMOR.forEach(entries::add);
        })
        .build();

    /**
     * Registers all custom content.
     */
    static void register() {
        registerPolymerItemGroup(Identifier.of("mwhrd", "my_well_has_run_dry"), CustomItems.MY_WELL_HAS_RUN_DRY);
        registerPolymerItemGroup(Identifier.of("mwhrd", "luck_and_luxury"), CustomItems.LUCK_AND_LUXURY);
        registerPolymerItemGroup(Identifier.of("mwhrd", "the_realm_of_light"), CustomItems.THE_REALM_OF_LIGHT);
    }
}
