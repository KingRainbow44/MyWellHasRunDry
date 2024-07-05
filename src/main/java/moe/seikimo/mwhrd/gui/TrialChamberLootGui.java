package moe.seikimo.mwhrd.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.GUI;
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

import static moe.seikimo.mwhrd.utils.GUI.BORDER;

public final class TrialChamberLootGui extends SimpleGui {
    private static final Set<Item> BLACKLIST = Set.of(
        Items.OMINOUS_TRIAL_KEY, Items.TRIAL_KEY
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

        GUI.drawBorders(this);
        this.drawButtons(player);
        this.drawPlayerLoot(player);
        this.setTitle(Text.literal("Trial Chamber Loot"));
    }

    /**
     * Draws the buttons for the GUI.
     *
     * @param player The player to draw the buttons for.
     */
    private void drawButtons(ServerPlayerEntity player) {
        var storage = MyWellHasRunDry.getLoot(player);
        var loot = storage.getBacking();

        // Count player keys.
        var badKeys = (int) storage.count(Items.OMINOUS_TRIAL_KEY);
        var normalKeys = (int) storage.count(Items.TRIAL_KEY);

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
                var key = loot.take(type, 1);
                if (!key) return;

                // Add the key to the player's inventory.
                player.getInventory().offerOrDrop(type.getDefaultStack());

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

        GUI.drawBorderedList(this, loot.getBacking(), item -> {
            // Check if the item is blacklisted.
            if (BLACKLIST.contains(item.getItem())) {
                return null;
            }

            return new GuiElementBuilder(item)
                .addLoreLine(Text.empty())
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
                .build();
        });
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
