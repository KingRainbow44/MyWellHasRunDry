package moe.seikimo.mwhrd.datagen;

import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.custom.CustomTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public final class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(
        FabricDataOutput output,
        CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture
    ) {
        super(output, completableFuture, null);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
            .add(CustomItems.ENLIGHTENED_DIAMOND_HELMET, CustomItems.ENLIGHTENED_DIAMOND_CHESTPLATE,
                CustomItems.ENLIGHTENED_DIAMOND_LEGGINGS, CustomItems.ENLIGHTENED_DIAMOND_BOOTS);

        this.getOrCreateTagBuilder(CustomTags.ENLIGHTENED_ARMOR)
            .add(CustomItems.ENLIGHTENED_DIAMOND_ARMOR.toArray(new Item[0]));
    }
}
