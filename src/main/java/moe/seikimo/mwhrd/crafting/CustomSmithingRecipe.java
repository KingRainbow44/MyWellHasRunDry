package moe.seikimo.mwhrd.crafting;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a custom smithing recipe.
 *
 * @param ingredients The ingredients required to craft the item.
 * @param onCraft This function will return the item stack to give the player.
 */
public record CustomSmithingRecipe(
    List<ItemPair> ingredients,
    Function<ServerPlayerEntity, ItemStack> onCraft
) {
    /**
     * @param player The player viewing the element.
     * @param selected Whether the element is selected.
     * @return A GUI element to select this recipe.
     */
    public GuiElementBuilder toGuiElement(ServerPlayerEntity player, boolean selected) {
        var dummy = this.onCraft.apply(player);

        var builder = new GuiElementBuilder(dummy.getItem())
            .addLoreLine(Text.empty())
            .addLoreLine(Text.translatable("text.mwhrd.gui.craft.ingredients")
                .formatted(Formatting.YELLOW));

        // Add ingredients.
        for (var ingredient : this.ingredients) {
            var item = ingredient.item();
            var name = item.getName();

            // Check if the item has a rarity.
            if (item.getComponents().get(DataComponentTypes.RARITY) instanceof Rarity rarity) {
                name = name.copy().formatted(rarity.getFormatting());
            }

            builder.addLoreLine(name.copy()
                .append(Text.literal(" x" + ingredient.quantity())
                    .formatted(Formatting.GRAY)));
        }

        // Set glowing if selected.
        if (selected) {
            builder.glow();
        }

        return builder
            .addLoreLine(Text.empty())
            .addLoreLine(
                (selected ?
                    Text.translatable("text.mwhrd.gui.selected") :
                    Text.translatable("text.mwhrd.gui.select"))
                    .formatted(Formatting.YELLOW)
            );
    }

    /**
     * Attempts to craft the item using materials in the player's inventory.
     *
     * @param player The player to craft the item for.
     * @return Whether the item was successfully crafted.
     */
    public boolean craft(ServerPlayerEntity player) {
        var inventory = player.getInventory();

        // Check if the player has the required materials.
        for (var ingredient : this.ingredients) {
            var item = ingredient.item();
            var quantity = ingredient.quantity();

            if (inventory.count(item) < quantity) {
                return false;
            }
        }

        // Remove the materials from the player's inventory.
        for (var ingredient : this.ingredients) {
            var item = ingredient.item();
            var quantity = ingredient.quantity();

            inventory.remove(
                s -> s.getItem() == item,
                quantity,
                player.playerScreenHandler.getCraftingInput()
            );
        }

        // Give the player the crafted item.
        var crafted = this.onCraft.apply(player);
        inventory.offerOrDrop(crafted);

        return true;
    }
}
