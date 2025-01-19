package moe.seikimo.mwhrd.gui.guild;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.experimental.ExtensionMethod;
import moe.seikimo.mwhrd.game.guilds.GuildInstance;
import moe.seikimo.mwhrd.impl.ArrayBackedInventory;
import moe.seikimo.mwhrd.utils.GUI;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.utils.items.DynamicItemStorage;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

@ExtensionMethod(Players.class)
public final class GuildBankItemGui extends SimpleGui {
    /**
     * Opens the GUI for the player.
     *
     * @param guild The guild to open the GUI for.
     * @param player The player instance.
     */
    public static void open(GuildInstance guild, ServerPlayerEntity player) {
        var gui = new GuildBankItemGui(guild, player);
        gui.open();
    }

    private final GuildInstance guild;
    private final DynamicItemStorage storage;

    private final Map<Integer, Item> icons;
    private final Map<Integer, String> names;

    /** This is the INDEX of the page. */
    private int currentPage = 0;

    /**
     * Constructs a new instance of the guild bank/storage GUi.
     *
     * @param guild The guild to open the GUI for.
     * @param player The player to open the GUI for.
     */
    public GuildBankItemGui(GuildInstance guild, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.guild = guild;
        this.storage = guild.getBank();
        this.icons = guild.getPageIcons();
        this.names = guild.getPageNames();

        this.setAutoUpdate(false);
        this.setTitle(Text.literal("Guild Bank - " + guild.getName()));

        // Draw the GUI contents.
        this.drawSidebar();
        this.drawItems();
    }

    @Override
    public void onClose() {
        this.guild.save();
    }

    /**
     * Draws the sidebar for selecting the page.
     */
    private void drawSidebar() {
        // Calculate the starting page index.
        var startingPage = (int) Math.floor(this.currentPage / 6f) * 6;

        GUI.drawVerticalLine(this, 8, Utils.<Void>newList(6), (element, i) -> {
            var index = i + startingPage;
            var selected = this.currentPage % 6 == i;

            // Resolve the slot item.
            var customItem = false;
            var slotItem = selected ?
                Items.LIME_STAINED_GLASS_PANE :
                Items.GRAY_STAINED_GLASS_PANE;

            if (this.icons.containsKey(index)) {
                customItem = true;
                slotItem = this.icons.get(index);
            }

            // Resolve the name.
            var name = "Page " + (index + 1);
            if (this.names.containsKey(index)) {
                name = this.names.get(index);
            }

            // Initialize the element builder.
            var builder = new GuiElementBuilder(slotItem)
                .setName(Text.literal(name)
                    .formatted(Formatting.GREEN))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Shift + Right-click to set page name!")
                    .formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Middle-click to set page icon!")
                    .formatted(Formatting.YELLOW))
                .setCallback(clickType -> {
                    switch (clickType) {
                        // Try to change which set of 6 we are viewing.
                        case MOUSE_RIGHT -> {
                            // If this is the top button, go back to the previous set of 6.
                            if (i == 0) {
                                this.currentPage = Math.max(0, this.currentPage - 6);
                            } else if (i == 5) {
                                // If this is the bottom button, go to the next set of 6.
                                this.currentPage = this.currentPage + 6;
                            } else {
                                // Otherwise, go to the selected page.
                                this.currentPage = index;
                            }
                        }
                        // Navigate to the selected page.
                        case MOUSE_LEFT -> this.currentPage = index;
                        case MOUSE_MIDDLE -> {
                            // Get the item type in the cursor.
                            var cursor = this.player.currentScreenHandler.getCursorStack();
                            var newIcon = cursor.getItem();

                            // If the item is not air, set the icon.
                            if (newIcon != Items.AIR) {
                                this.icons.put(index, newIcon);

                                this.player.sendMessage(
                                    Text.translatable("text.mwhrd.guild.bank.update_icon",
                                        index + 1, newIcon.getName().getString()
                                    ).formatted(Formatting.GREEN)
                                );
                            } else {
                                this.icons.remove(index);

                                this.player.sendMessage(
                                    Text.translatable("text.mwhrd.guild.bank.update_icon.clear", index + 1)
                                        .formatted(Formatting.GREEN)
                                );
                            }
                        }
                        case MOUSE_RIGHT_SHIFT -> {
                            // Open a sign GUI to set the name.
                            var gui = new GuildBankNameGui(this.getPlayer(), newName -> {
                                newName = newName.trim();

                                if (newName.isEmpty()) {
                                    this.names.remove(index);
                                    this.player.sendMessage(
                                        Text.translatable("text.mwhrd.guild.bank.update_name.clear", index + 1)
                                            .formatted(Formatting.GREEN)
                                    );
                                } else {
                                    this.names.put(index, newName);
                                    this.player.sendMessage(
                                        Text.translatable("text.mwhrd.guild.bank.update_name",
                                            index + 1, newName
                                        ).formatted(Formatting.GREEN)
                                    );
                                }

                                this.open();

                                this.drawSidebar();
                                this.drawItems();
                            });

                            gui.open();
                            return;
                        }
                    }

                    // Update the GUI.
                    this.drawSidebar();
                    this.drawItems();
                });

            // If the item is custom & selected, add a lore line.
            if (selected && customItem) {
                builder.glow();
            }

            // Add additional lore if needed.
            switch (i) {
                case 0 -> {
                    if (startingPage > 0) {
                        builder
                            .addLoreLine(Text.empty())
                            .addLoreLine(Text.literal("Right-click to go back 6 pages.")
                                .formatted(Formatting.GRAY));
                    }
                }
                case 5 -> builder
                    .addLoreLine(Text.empty())
                    .addLoreLine(Text.literal("Right-click to go forward 6 pages.")
                        .formatted(Formatting.GRAY));
            }

            return builder.build();
        });
    }

    /**
     * Draws the items in the bank to the GUI.
     */
    private void drawItems() {
        var page = this.storage.getOrAllocate(this.currentPage);
        var inventory = new ArrayBackedInventory(page);

        for (var y = 0; y < 6; y++) {
            for (var x = 0; x < 8; x++) {
                var pageIndex = y * 8 + x;
                var slotIndex = y * 9 + x;

                var slot = new Slot(
                    inventory, pageIndex,
                    8 + x * 18, 18 + y * 18);

                this.setSlotRedirect(slotIndex, slot);
            }
        }

        this.sendGui();
    }
}
