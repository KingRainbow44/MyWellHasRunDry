package moe.seikimo.mwhrd.utils;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public interface GUI {
    Style CLEAR = Style.EMPTY.withItalic(false);
    GuiElement BORDER = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
        .setName(Text.empty()).hideTooltip().build();

    /**
     * Converts an index to an inventory slot.
     * This avoids the border slots.
     *
     * @param index The index to convert.
     * @return The slot in the inventory.
     */
    static int indexToSlot(int index) {
        // Each row has 7 available slots.
        // There are a total of 4 available rows.
        // The first usable slot is at index 10.
        // The last usable slot is at index 43.

        var rows = index / 7;
        var columns = index % 7;

        return 10 + rows * 9 + columns;
    }
}
