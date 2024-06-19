package moe.seikimo.mwhrd.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class TrialChamberLootGui extends SimpleGui {
    private static final Set<Item> BLACKLIST = Set.of(
        Items.OMINOUS_TRIAL_KEY, Items.TRIAL_KEY
    );

    private static final GuiElement BORDER =
        new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
            .setName(Text.empty())
            .build();

    private static final List<Text> DETAILS = List.of(
        Text.literal("Trial Chamber Loot Details:")
            .setStyle(Style.EMPTY.withItalic(false).withUnderline(true))
            .withColor(Color.GRAY.getRGB()),
        Text.empty(),
        Text.literal("  - You can withdraw items by clicking on them.")
            .setStyle(Style.EMPTY.withItalic(false))
            .withColor(Color.GRAY.getRGB()),
        Text.literal("  - Items are *DELETED* when the server restarts.")
            .setStyle(Style.EMPTY.withItalic(false))
            .withColor(Color.GRAY.getRGB())
    );

    private static final int BAD_KEY_INDEX = 36;
    private static final int OUT_ALL_INDEX = 45;
    private static final int NORMAL_KEY_INDEX = 46;

    /**
     * Opens the GUI for the player.
     *
     * @param player The player instance.
     */
    public static void open(ServerPlayerEntity player) {
        var gui = new TrialChamberLootGui(player);
        gui.open();
    }

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     */
    public TrialChamberLootGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.drawBorders();
        this.drawButtons(player);
        this.drawPlayerLoot(player);
        this.setTitle(Text.literal("Trial Chamber Loot"));
    }

    /**
     * Draws a light-gray glass pane border around the GUI.
     */
    private void drawBorders() {
        // This draws the top & bottom border.
        for (var i = 0; i < 9; i++) {
            this.setSlot(i, BORDER);
            this.setSlot(i + 45, BORDER);
        }

        // This draws the left & right border.
        for (var i = 1; i < 6; i++) {
            this.setSlot(i * 9, BORDER);
            this.setSlot(i * 9 + 8, BORDER);
        }
    }

    /**
     * Draws the buttons for the GUI.
     *
     * @param player The player to draw the buttons for.
     */
    private void drawButtons(ServerPlayerEntity player) {
        var loot = MyWellHasRunDry.getLoot(player);

        // Draw the details.
        this.setSlot(8, new GuiElementBuilder(Items.OAK_SIGN)
            .setName(Text.literal("Details"))
            .setLore(DETAILS)
            .build());

        // Count player keys.
        var badKeys = TrialChamberLootGui.countItems(loot, Items.OMINOUS_TRIAL_KEY);
        var normalKeys = TrialChamberLootGui.countItems(loot, Items.TRIAL_KEY);

        // Display buttons.
        if (badKeys > 0) {
            this.drawKeyButton(BAD_KEY_INDEX, badKeys, "Ominous Trial Keys", Items.OMINOUS_TRIAL_KEY);
        } else {
            this.setSlot(BAD_KEY_INDEX, BORDER);
        }
        if (normalKeys > 0) {
            this.drawKeyButton(NORMAL_KEY_INDEX, normalKeys, "Trial Keys", Items.TRIAL_KEY);
        } else {
            this.setSlot(NORMAL_KEY_INDEX, BORDER);
        }

        // Check if the player's inventory isn't full.
        var inventory = player.getInventory();
        if (!loot.isEmpty()) {
            this.setSlot(OUT_ALL_INDEX, new GuiElementBuilder(Items.MACE)
                .setName(Text.literal("Withdraw All"))
                .addLoreLine(Text.literal("Click to withdraw all loot!")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .withColor(Color.GREEN.getRGB()))
                .addLoreLine(Text.literal("If you're inventory is full, this will drop items.")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .withColor(Color.RED.getRGB()))
                .hideDefaultTooltip()
                .setCallback(() -> {
                    // Add all items to the player's inventory.
                    loot.forEach(inventory::offerOrDrop);
                    // Clear the player's loot.
                    loot.clear();

                    this.drawButtons(player);
                    this.drawPlayerLoot(player);
                })
                .build());
        } else {
            this.setSlot(OUT_ALL_INDEX, BORDER);
        }
    }

    /**
     * Helper method for drawing a key button.
     */
    private void drawKeyButton(int slot, int count, String title, Item type) {
        var loot = MyWellHasRunDry.getLoot(player);
        this.setSlot(slot, new GuiElementBuilder(type)
            .setName(Text.literal(title))
            .addLoreLine(Text.literal("Click to withdraw 1 key!")
                .setStyle(Style.EMPTY.withItalic(false))
                .withColor(Color.GREEN.getRGB()))
            .setCount(count)
            .setCallback(() -> {
                // Remove one key from the player's loot.
                var key = loot.stream()
                    .filter(item -> item.getItem() == type)
                    .findFirst()
                    .orElse(null);
                if (key == null) return;

                // Remove the key from the player's loot.
                loot.remove(key);
                // Add the key to the player's inventory.
                player.getInventory().offerOrDrop(key);

                this.drawButtons(player);
            })
            .build());
    }

    /**
     * Fetches the player's loot and draws it in the GUI.
     *
     * @param player The player to draw the loot for.
     */
    private void drawPlayerLoot(ServerPlayerEntity player) {
        var loot = MyWellHasRunDry.getLoot(player);

        var slotIndex = 0;
        for (var i = 0; i < 28; i++) {
            var slot = TrialChamberLootGui.indexToSlot(slotIndex++);
            if (i >= loot.size()) {
                this.setSlot(slot, ItemStack.EMPTY);
            } else {
                var item = loot.get(i);
                // Check if the item is blacklisted.
                if (BLACKLIST.contains(item.getItem())) {
                    slotIndex--;
                    continue;
                }

                this.setSlot(slot, new GuiElementBuilder(item)
                    .addLoreLine(Text.literal("Click to add to your inventory!")
                        .setStyle(Style.EMPTY.withItalic(false))
                        .withColor(Color.GREEN.getRGB()))
                    .setCallback(() -> {
                        // Add the item to the player's inventory.
                        player.getInventory().offerOrDrop(item);
                        // Remove the item from the player's loot.
                        loot.remove(item);

                        // Re-draw the player's loot.
                        this.drawButtons(player);
                        this.drawPlayerLoot(player);
                    })
                    .build());
            }
        }
    }

    /**
     * Converts an index to an inventory slot.
     * This avoids the border slots.
     *
     * @param index The index to convert.
     * @return The slot in the inventory.
     */
    public static int indexToSlot(int index) {
        // Each row has 7 available slots.
        // There are a total of 4 available rows.
        // The first usable slot is at index 10.
        // The last usable slot is at index 43.

        var rows = index / 7;
        var columns = index % 7;

        return 10 + rows * 9 + columns;
    }

    /**
     * Counts how many items of the specified type are in the list.
     *
     * @param items The list of items to count.
     * @param type The type of item to count.
     * @return The number of items of the specified type in the list.
     */
    public static int countItems(List<ItemStack> items, Item type) {
        var count = new AtomicInteger();
        items.stream()
            .filter(item -> item.getItem() == type)
            .forEach(item -> count.getAndIncrement());
        return count.get();
    }
}
