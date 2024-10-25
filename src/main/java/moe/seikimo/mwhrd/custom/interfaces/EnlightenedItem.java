package moe.seikimo.mwhrd.custom.interfaces;

import moe.seikimo.mwhrd.interfaces.nbt.IItemNbtWrapper;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public interface EnlightenedItem {
    String UPGRADE_TIER = "upgrade_tier";

    /**
     * Generates a recipe for the item.
     *
     * @param item The item.
     * @param exporter The recipe exporter.
     */
    static void recipeFor(Item item, RecipeExporter exporter) {
        SmithingTransformRecipeJsonBuilder.create(
                Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.ofItems(item),
                Ingredient.ofItems(Items.NETHERITE_INGOT),
                RecipeCategory.COMBAT, item
            )
            .criterion("has_netherite_ingot", RecipeProvider.conditionsFromItem(Items.NETHERITE_INGOT))
            .offerTo(exporter, RecipeProvider.getItemPath(item) + "_smithing");
    }

    /**
     * @return The item identifier.
     */
    Identifier getIdentifier();

    /**
     * Applies upgrades to the item.
     *
     * @param stack The item stack.
     * @param tier The upgrade tier.
     * @param attributeId The identifier to use for the attribute.
     */
    void applyUpgrades(ItemStack stack, int tier, Identifier attributeId);

    /**
     * Upgrades the item.
     *
     * @param stack The item stack to upgrade.
     * @param player The player.
     */
    default void upgrade(ItemStack stack, PlayerEntity player) {
        // Get the current component for upgrade tier.
        var nbt = ((IItemNbtWrapper) (Object) stack).mwhrd$asNbt();

        var upgradeTier = nbt.contains(UPGRADE_TIER) ? nbt.getInt(UPGRADE_TIER) : 0;
        // Check if the item is already at max tier.
        if (upgradeTier >= 10) {
            player.sendMessage(Text.translatable("text.mwhrd.max_tier")
                .formatted(Formatting.RED));

            // Return the upgrade items.
            player.getInventory().offerOrDrop(new ItemStack(Items.NETHERITE_INGOT));
            player.getInventory().offerOrDrop(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));

            return;
        }

        // Replace the component with the new upgrade tier.
        nbt.set(UPGRADE_TIER, ++upgradeTier);

        // Apply stack upgrades.
        var modifier = Identifier.of("mwhrd", this.getIdentifier().getPath() + "_modifier");
        this.applyUpgrades(stack, upgradeTier, modifier);

        stack.set(DataComponentTypes.DAMAGE, 0);
        stack.set(DataComponentTypes.MAX_DAMAGE, 100 * upgradeTier);
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
            Text.literal("Tier %s".formatted(Utils.toRoman(upgradeTier)))
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY)
        )));

        player.sendMessage(Text.translatable("text.mwhrd.tier_upgrade",
                stack.toHoverableText().copy().formatted(Formatting.YELLOW),
                Text.literal(Utils.toRoman(upgradeTier)))
            .formatted(Formatting.GREEN));
    }
}
