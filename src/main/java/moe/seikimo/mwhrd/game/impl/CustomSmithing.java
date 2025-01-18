package moe.seikimo.mwhrd.game.impl;

import moe.seikimo.mwhrd.crafting.CustomSmithingRecipe;
import moe.seikimo.mwhrd.crafting.ItemPair;
import moe.seikimo.mwhrd.custom.CustomItems;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Contains all custom smithing recipes.
 */
public interface CustomSmithing {
    CustomSmithingRecipe TURF_BLASTER = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.IRON_INGOT, 3),
            new ItemPair(Items.REDSTONE, 1),
            new ItemPair(Items.POLISHED_BLACKSTONE_BUTTON, 1),
            new ItemPair(Items.BLAZE_POWDER, 1),
            new ItemPair(Items.ANCIENT_DEBRIS, 1),
            new ItemPair(Items.GOLDEN_HORSE_ARMOR)
        ),
        player -> CustomItems.TURF_GUN.getDefaultStack(),
        List.of(
            Text.literal("For getting your feet wet.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe PROTOTYPE_LAUNCHER = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.IRON_INGOT, 2),
            new ItemPair(Items.IRON_BLOCK, 1),
            new ItemPair(Items.REPEATER, 1),
            new ItemPair(Items.BLAZE_ROD, 2),
            new ItemPair(Items.TRIPWIRE_HOOK, 1),
            new ItemPair(Items.NETHERITE_INGOT, 1),
            new ItemPair(Items.IRON_HORSE_ARMOR)
        ),
        player -> CustomItems.PROTOTYPE_LAUNCHER.getDefaultStack(),
        List.of(
            Text.literal("Trusty and reliable. Always works.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe FRENZY = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.IRON_INGOT, 3),
            new ItemPair(Items.DIAMOND, 3),
            new ItemPair(Items.REDSTONE_TORCH, 1),
            new ItemPair(Items.BLAZE_POWDER, 4),
            new ItemPair(Items.LEVER, 1),
            new ItemPair(Items.DIAMOND_HORSE_ARMOR)
        ),
        player -> CustomItems.FRENZY.getDefaultStack(),
        List.of(
            Text.literal("Really good if you can't aim.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe OPERATOR = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.WHITE_STAINED_GLASS_PANE, 1),
            new ItemPair(Items.LANTERN, 1),
            new ItemPair(Items.SPYGLASS),
            new ItemPair(Items.NETHERITE_INGOT, 3),
            new ItemPair(Items.BLAZE_ROD, 8),
            new ItemPair(Items.IRON_BLOCK, 1),
            new ItemPair(Items.COMPARATOR, 2),
            new ItemPair(Items.REDSTONE_TORCH, 1),
            new ItemPair(Items.NETHERITE_SCRAP, 1)
        ),
        player -> CustomItems.OP_400.getDefaultStack(),
        List.of(
            Text.literal("Assassination plot anyone?")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe THE_MINECART = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.NETHER_STAR, 1),
            new ItemPair(Items.REDSTONE_BLOCK, 16),
            new ItemPair(Items.TRIDENT),
            new ItemPair(Items.TNT, 32),
            new ItemPair(Items.BREEZE_ROD, 128),
            new ItemPair(Items.NETHERITE_INGOT, 2)
        ),
        player -> CustomItems.MINECART_BLASTER.getDefaultStack(),
        List.of(
            Text.literal("Probably the best way to")
                .formatted(Formatting.GRAY),
            Text.literal("mine for netherite.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe TEST_GUN = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.NETHER_STAR, 4),
            new ItemPair(CustomItems.FRENZY),
            new ItemPair(CustomItems.PROTOTYPE_LAUNCHER),
            new ItemPair(CustomItems.TURF_GUN),
            new ItemPair(CustomItems.MINECART_BLASTER),
            new ItemPair(CustomItems.OP_400),
            new ItemPair(Items.PIGLIN_HEAD, 1)
        ),
        player -> CustomItems.TEST_GUN.getDefaultStack(),
        List.of(
            Text.literal("The developer's gun.")
                .formatted(Formatting.GRAY),
            Text.literal("Extremely hard to obtain.")
                .formatted(Formatting.GRAY),
            Text.literal("Really powerful.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe LIGHT_CRYSTAL = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.GUNPOWDER, 2),
            new ItemPair(Items.IRON_NUGGET, 1)
        ),
        player -> CustomItems.LIGHT_CRYSTAL.getDefaultStack(),
        List.of(
            Text.literal("Used with mini blasters as ammo.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe MEDIUM_CRYSTAL = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.GUNPOWDER, 6),
            new ItemPair(Items.IRON_INGOT, 4)
        ),
        player -> CustomItems.MEDIUM_CRYSTAL.getDefaultStack(),
        List.of(
            Text.literal("Used with heavy blasters as ammo.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe HEAVY_CRYSTAL = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.GUNPOWDER, 16),
            new ItemPair(Items.IRON_INGOT, 2)
        ),
        player -> CustomItems.HEAVY_CRYSTAL.getDefaultStack(),
        List.of(
            Text.literal("Used with the 'OPERATOR 400' as ammo.")
                .formatted(Formatting.GRAY)
        )
    );

    CustomSmithingRecipe EXPLOSIVE_CRYSTAL = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.GUNPOWDER, 16),
            new ItemPair(Items.END_CRYSTAL, 1),
            new ItemPair(Items.FIREWORK_STAR, 1)
        ),
        player -> CustomItems.EXPLOSIVE_CRYSTAL.getDefaultStack(),
        List.of(
            Text.literal("Used with 'The Minecart' as ammo.")
                .formatted(Formatting.GRAY)
        )
    );

    List<CustomSmithingRecipe> ALL = List.of(
        TURF_BLASTER,
        PROTOTYPE_LAUNCHER,
        FRENZY,
        OPERATOR,
        THE_MINECART,
        LIGHT_CRYSTAL,
        MEDIUM_CRYSTAL,
        HEAVY_CRYSTAL,
        EXPLOSIVE_CRYSTAL,
        TEST_GUN
    );
}
