package moe.seikimo.mwhrd.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static moe.seikimo.mwhrd.utils.GUI.BORDER;

/**
 * TODO: Implement additional UI for additional actions.
 * These include: withdraw everything, sort by item, etc.
 */
public final class BeaconStorageGui extends SimpleGui {
    private static final int INFO = 31;

    /**
     * Opens the GUI for the player.
     *
     * @param player The player instance.
     */
    public static void open(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        new BeaconStorageGui(beacon, player).open();
    }

    private IAdvancedBeacon beacon;

    private final GuiElement CLEAR_STORAGE =
        new GuiElementBuilder(Items.BARRIER)
            .setName(Text.literal("Empty Storage")
                .formatted(Formatting.RED))
            .addLoreLine(Text.literal("This will delete every item in the storage!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.BOLD, Formatting.DARK_RED))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Shift + Right-click to clear the storage!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.YELLOW))
            .setCallback(clickType -> {
                if (clickType != ClickType.MOUSE_RIGHT_SHIFT) return;

                this.beacon.mwhrd$getStorage().clear();
                this.getPlayer().sendMessage(Text.literal("Storage cleared!")
                    .formatted(Formatting.GREEN));

                this.drawPages();
                this.drawItems();

                this.beacon.mwhrd$save();
            })
            .build();

    private int currentPage = 0;

    public BeaconStorageGui(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.beacon = beacon;

        this.setTitle(Text.literal("Beacon Storage"));

        this.drawPages();
        this.drawItems();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        if (type != ClickType.MOUSE_LEFT_SHIFT) return true;
        // Check if the index is within the storage.
        if (index <= 53) return true;

        // Add the item to the storage.
        var stack = this.getPlayer()
            .currentScreenHandler
            .getSlot(index)
            .getStack();
        if (stack == null || stack.isEmpty()) return true;

        var item = stack.getItem();
        if (!(item instanceof BlockItem) || !item.canBeNested()) {
            this.getPlayer().sendMessage(Text.literal("Only blocks can be stored!")
                .formatted(Formatting.RED));
            return false;
        }

        var storage = this.beacon.mwhrd$getStorage();
        storage.offer(stack.copy());

        this.getPlayer().sendMessage(Text.literal("Deposited ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("%sx ".formatted(stack.getCount()))
                .formatted(Formatting.YELLOW))
            .append(stack.toHoverableText()
                .copy().formatted(Formatting.YELLOW))
            .append(Text.literal(" into the storage.")
                .formatted(Formatting.GREEN)));
        stack.setCount(0);

        this.drawPages();
        this.drawItems();

        this.beacon.mwhrd$save();

        return false;
    }

    private void drawPages() {
        if (this.currentPage <= 0) {
            this.setSlot(0, this.countPages() != 0 ?
                CLEAR_STORAGE : BORDER);
        } else {
            this.setSlot(0, this.pager(false));
        }

        // Calculate the page starting index.
        var startingPage = (int) Math.floor(this.currentPage / 7f) * 7;

        // Draws the page buttons across 1-7.
        for (var i = 1; i < 8; i++) {
            var pageIndex = startingPage + i;
            var goTo = pageIndex - 1;
            this.setSlot(i, this.pager(goTo));
        }

        if (this.currentPage + 1 >= this.countPages()) {
            this.setSlot(8, this.countPages() != 0 ?
                CLEAR_STORAGE : BORDER);
        } else {
            this.setSlot(8, this.pager(true));
        }
    }

    private void drawItems() {
        var storage = this.beacon.mwhrd$getStorage();

        // We have space from 9-53 to draw item stacks.
        var start = this.currentPage * 45;
        for (var i = 0; i < 45; i++) {
            var index = start + i;
            if (index >= storage.size()) {
                this.setSlot(i + 9, ItemStack.EMPTY);
                continue;
            }

            var stack = storage.get(index);
            if (stack == null) {
                this.setSlot(i + 9, ItemStack.EMPTY);
                continue;
            }

            this.setSlot(i + 9, new GuiElementBuilder(stack)
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Click to withdraw this item!")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.YELLOW))
                .hideDefaultTooltip()
                .setCallback(() -> {
                    var toGive = storage.remove(index);

                    this.getPlayer().sendMessage(Text.literal("Withdrew ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal("%sx ".formatted(toGive.getCount()))
                            .formatted(Formatting.YELLOW))
                        .append(toGive.toHoverableText()
                            .copy().formatted(Formatting.YELLOW))
                        .append(Text.literal(" from the storage.")
                            .formatted(Formatting.GREEN)));
                    this.getPlayer().getInventory().offerOrDrop(toGive);

                    this.drawPages();
                    this.drawItems();

                    this.beacon.mwhrd$save();
                }));
        }

        if (storage.size() == 0) {
            this.setSlot(INFO, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("No items stored!")
                    .formatted(Formatting.RED))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Add items by shift-clicking them into the storage!")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Alternatively, destroy blocks with upgrades.")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.GRAY)));
        }
    }

    private int countPages() {
        return (int) Math.ceil(this.beacon.mwhrd$getStorage().size() / 45f);
    }

    /**
     * Creates a page navigation button.
     *
     * @param isNext Whether the button is for the next page.
     * @return The button builder.
     */
    private GuiElement pager(boolean isNext) {
        if ((!isNext && this.currentPage <= 0) ||
            (isNext && this.currentPage + 1 >= this.countPages())) {
            return BORDER;
        }

        var nextPage = !isNext || this.currentPage + 1 < this.countPages();
        return new GuiElementBuilder(nextPage ?
            Items.ARROW : Items.GRAY_STAINED_GLASS_PANE)
            .setName(Text.literal("%s Page".formatted(isNext ? "Next" : "Previous"))
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("To page %s".formatted(
                isNext ? this.currentPage + 2 : this.currentPage))
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.GRAY))
            .setCallback(() ->{
                if (nextPage) {
                    this.currentPage += isNext ? 1 : -1;
                    this.drawPages();
                    this.drawItems();
                }
            })
            .build();
    }

    /**
     * Creates a page navigation button.
     *
     * @param index The page index.
     * @return The button builder.
     */
    private GuiElement pager(int index) {
        if (index + 1 > this.countPages()) {
            return BORDER;
        }

        return new GuiElementBuilder(index == this.currentPage ?
            Items.LIME_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE)
            .setName(Text.literal("Page %s".formatted(index + 1))
                .formatted(this.currentPage == index ? Formatting.GREEN : Formatting.GRAY))
            .setCallback(() -> {
                this.currentPage = index;
                this.drawPages();
                this.drawItems();
            })
            .build();
    }
}
