package moe.seikimo.mwhrd.game.impl;

import moe.seikimo.mwhrd.crafting.CustomSmithingRecipe;
import moe.seikimo.mwhrd.crafting.ItemPair;
import net.minecraft.item.Items;

import java.util.List;

/**
 * Contains all custom smithing recipes.
 */
public interface CustomSmithing {
    CustomSmithingRecipe MINECART_CRYSTAL = new CustomSmithingRecipe(
        List.of(
            new ItemPair(Items.GUNPOWDER, 8),
            new ItemPair(Items.END_CRYSTAL, 1)
        ),
        player -> Items.END_CRYSTAL.getDefaultStack()
    );

    List<CustomSmithingRecipe> ALL = List.of(
        MINECART_CRYSTAL
    );
}
