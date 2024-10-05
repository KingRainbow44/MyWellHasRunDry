package moe.seikimo.mwhrd.datagen;

import moe.seikimo.mwhrd.custom.CustomItems;
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
        for (var item : CustomItems.ENLIGHTENED_DIAMOND_ARMOR) {
            SmithingTransformRecipeJsonBuilder.create(
                Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.ofItems(item),
                Ingredient.ofItems(Items.NETHERITE_INGOT),
                RecipeCategory.COMBAT, item
            )
                .criterion("has_netherite_ingot", RecipeProvider.conditionsFromItem(Items.NETHERITE_INGOT))
                .offerTo(exporter, RecipeProvider.getItemPath(item) + "_smithing");
        }
    }
}
