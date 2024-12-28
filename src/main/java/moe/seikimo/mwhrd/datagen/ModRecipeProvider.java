package moe.seikimo.mwhrd.datagen;

import moe.seikimo.mwhrd.custom.CustomItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends FabricRecipeProvider {
    /**
     * Generates a recipe for the item.
     *
     * @param generator The recipe generator.
     * @param item The item.
     * @param exporter The recipe exporter.
     */
    private static void enlightenedSmithing(RecipeGenerator generator, Item item, RecipeExporter exporter) {
        SmithingTransformRecipeJsonBuilder.create(
                Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.ofItems(item),
                Ingredient.ofItems(Items.NETHERITE_INGOT),
                RecipeCategory.COMBAT, item
            )
            .criterion("has_netherite_ingot", generator.conditionsFromItem(Items.NETHERITE_INGOT))
            .offerTo(exporter, RecipeGenerator.getItemPath(item) + "_smithing");
    }

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup lookup, RecipeExporter exporter) {
        var generator = new EnlightenedRecipeGenerator(lookup, exporter);

        /// <editor-fold desc="Enlightened Items">
        for (var item : CustomItems.ENLIGHTENED_DIAMOND_ITEMS) {
            ModRecipeProvider.enlightenedSmithing(generator, item, exporter);
        }
        /// </editor-fold>

        return generator;
    }

    @Override
    public String getName() {
        return "";
    }
}
