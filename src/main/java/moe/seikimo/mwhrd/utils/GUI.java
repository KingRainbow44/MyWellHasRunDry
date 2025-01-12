package moe.seikimo.mwhrd.utils;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    /**
     * Draws a border across the entire UI.
     *
     * @param gui The GUI to draw the border on.
     */
    static void drawBorderFull(SimpleGui gui) {
        for (var i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, BORDER);
        }
    }

    /**
     * Draws the default border for the GUI.
     */
    static void drawBorders(SimpleGui gui) {
        // This draws the top & bottom border.
        for (var i = 0; i < 9; i++) {
            gui.setSlot(i, BORDER);
            gui.setSlot(i + 45, BORDER);
        }

        // This draws the left & right border.
        for (var i = 1; i < 6; i++) {
            gui.setSlot(i * 9, BORDER);
            gui.setSlot(i * 9 + 8, BORDER);
        }
    }

    /**
     * Draws a list using the default border.
     *
     * @param gui The GUI to draw the list on.
     * @param backing The backing list to draw.
     * @param mapper The function to map the list elements to GUI elements.
     */
    static <T> void drawBorderedList(
        SimpleGui gui, List<T> backing, Function<T, GuiElement> mapper
    ) {
        var slotIndex = 0;
        for (var i = 0; i < 28; i++) {
            var slot = GUI.indexToSlot(slotIndex++);
            if (i >= backing.size()) {
                gui.setSlot(slot, ItemStack.EMPTY);
            } else {
                var element = mapper.apply(backing.get(i));
                if (element == null) {
                    slotIndex--;
                    continue;
                }
                gui.setSlot(slot, element);
            }
        }
    }

    /**
     * Draws a line using the mapper.
     *
     * @param gui The GUI to draw the line on.
     * @param start The starting index of the line.
     * @param backing The backing list to draw.
     * @param mapper The function to map the list elements to GUI elements.
     * @param <T> The type of the backing list.
     */
    static <T> void drawLine(
        SimpleGui gui, int start, List<T> backing, BiFunction<T, Integer, GuiElement> mapper
    ) {
        // Determine the max index to draw.
        // Indexes start from '0', so we subtract one.
        var maxIndex = gui.getWidth() * gui.getHeight() - 1;
        if (maxIndex < 0) {
            throw new IllegalArgumentException("GUI has an invalid size");
        }

        var currentIndex = 0;
        var nextSlot = start;

        // Continue drawing until we are out of bounds.
        while (nextSlot <= maxIndex) {
            // Otherwise, we should draw the element.
            var index = currentIndex++;
            var data = backing.get(index);
            gui.setSlot(nextSlot, mapper.apply(data, index));

            nextSlot += 9;
        }
    }

    /**
     * Creates a lore line.
     *
     * @param text The text to create the lore line from.
     * @param formatting The formatting to apply to the text.
     * @return The lore line.
     */
    static MutableText lore(String text, Formatting... formatting) {
        return Text.literal(text)
            .setStyle(CLEAR)
            .formatted(formatting);
    }
}
