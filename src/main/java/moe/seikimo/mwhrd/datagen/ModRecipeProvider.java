package moe.seikimo.mwhrd.datagen;

import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.custom.interfaces.EnlightenedItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        /// <editor-fold desc="Enlightened Items">
        for (var item : CustomItems.ENLIGHTENED_DIAMOND_ARMOR) {
            EnlightenedItem.recipeFor(item, exporter);
        }
        EnlightenedItem.recipeFor(CustomItems.ENLIGHTENED_DIAMOND_SWORD, exporter);
        /// </editor-fold>
    }
}
