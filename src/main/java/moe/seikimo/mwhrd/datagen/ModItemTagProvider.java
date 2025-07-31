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
        var enlightenedArmor = CustomItems.ENLIGHTENED_DIAMOND_ARMOR.toArray(new Item[0]);
        this.valueLookupBuilder(CustomTags.ENLIGHTENED_ARMOR).add(enlightenedArmor);

        this.valueLookupBuilder(ItemTags.HEAD_ARMOR)
            .add(CustomItems.ENLIGHTENED_DIAMOND_HELMET);
        this.valueLookupBuilder(ItemTags.CHEST_ARMOR)
            .add(CustomItems.ENLIGHTENED_DIAMOND_CHESTPLATE);
        this.valueLookupBuilder(ItemTags.LEG_ARMOR)
            .add(CustomItems.ENLIGHTENED_DIAMOND_LEGGINGS);
        this.valueLookupBuilder(ItemTags.FOOT_ARMOR)
            .add(CustomItems.ENLIGHTENED_DIAMOND_BOOTS);

        this.valueLookupBuilder(ItemTags.SWORDS)
            .add(CustomItems.ENLIGHTENED_DIAMOND_SWORD);
        this.valueLookupBuilder(ItemTags.AXES)
            .add(CustomItems.ENLIGHTENED_DIAMOND_AXE);
        this.valueLookupBuilder(ItemTags.PICKAXES)
            .add(CustomItems.ENLIGHTENED_DIAMOND_PICKAXE);
        this.valueLookupBuilder(ItemTags.SHOVELS)
            .add(CustomItems.ENLIGHTENED_DIAMOND_SHOVEL);
        this.valueLookupBuilder(ItemTags.HOES)
            .add(CustomItems.ENLIGHTENED_DIAMOND_HOE);
    }
}
