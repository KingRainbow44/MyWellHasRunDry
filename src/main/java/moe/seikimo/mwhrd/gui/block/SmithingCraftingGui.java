package moe.seikimo.mwhrd.gui.block;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.crafting.CustomSmithingRecipe;
import moe.seikimo.mwhrd.game.impl.CustomSmithing;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

public final class SmithingCraftingGui extends SimpleGui {
    private static final int UPGRADE = 45;
    private static final int SMITHING = 53;

    private static final int INFO = 39;
    private static final int CRAFT = 40;
    private static final int NAVIGATE = 41;

    private static final int[] ITEM_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    };

    /**
     * Open the custom smithing crafting GUI.
     *
     * @param state The block state of the smithing table.
     * @param position The position of the block.
     * @param player The player to open the GUI for.
     */
    public static void open(BlockState state, BlockPos position, ServerPlayerEntity player) {
        new SmithingCraftingGui(state, position, player).open();
    }

    private final BlockState state;
    private final BlockPos position;

    private int currentPage = 0;
    private Pair<Integer, CustomSmithingRecipe> selected = null;

    public SmithingCraftingGui(BlockState state, BlockPos position, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.state = state;
        this.position = position;

        this.setTitle(Text.literal("Smithing Table - Crafting"));

        // Draw the GUI contents.
        GUI.drawBorderFull(this);
        this.drawButtons();
        this.drawOptions();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        this.getPlayer().sendMessage(Text.literal("clicked " + index));
        return super.onAnyClick(index, type, action);
    }

    /**
     * Draws the buttons for the GUI.
     */
    private void drawButtons() {
        var player = this.player;

        this.setSlot(SMITHING, new GuiElementBuilder(Items.SMITHING_TABLE)
            .setName(Text.literal("Open Upgrade Table")
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("This will open the normal smithing table menu.")
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Click to open!")
                .formatted(Formatting.YELLOW))
            .setCallback(() -> {
                var world = player.getWorld();

                player.openHandledScreen(this.state.createScreenHandlerFactory(world, this.position));
                player.incrementStat(Stats.INTERACT_WITH_SMITHING_TABLE);
            }));

        this.setSlot(UPGRADE, new GuiElementBuilder(Items.ANVIL)
            .setName(Text.literal("Open Upgrade Menu")
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("This will open the custom upgrade menu.")
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Click to open!")
                .formatted(Formatting.YELLOW))
            .setCallback(() -> {
                var world = player.getWorld();

                player.openHandledScreen(this.state.createScreenHandlerFactory(world, this.position));
                player.incrementStat(Stats.INTERACT_WITH_SMITHING_TABLE);
            }));

        this.setSlot(INFO, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
            .setName(Text.literal("Information")
                .formatted(Formatting.GREEN)));

        this.setSlot(NAVIGATE, new GuiElementBuilder(Items.ARROW)
            .setName(Text.literal("Navigate Pages")
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Click to jump to the next page.")
                .formatted(Formatting.YELLOW))
            .addLoreLine(Text.literal("Right-Click to go back to the previous page.")
                .formatted(Formatting.AQUA))
            .setCallback((clickType) -> {
                this.selected = null;

                if (clickType == ClickType.MOUSE_RIGHT) {
                    this.currentPage = Math.max(0, this.currentPage - 1);
                } else {
                    this.currentPage = Math.min(CustomSmithing.ALL.size() / ITEM_SLOTS.length, this.currentPage + 1);
                }

                this.drawButtons();
                this.drawOptions();
            }));

        var craft = new GuiElementBuilder(Items.STONECUTTER)
            .setName(Text.literal("Craft Item")
                .formatted(Formatting.GREEN));

        if (this.selected != null) {
            craft
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Click to craft!")
                    .formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Right-Click to craft x10!")
                    .formatted(Formatting.AQUA))
                .setCallback((clickType) -> {
                    var recipe = this.selected.getRight();
                    var times = clickType == ClickType.MOUSE_RIGHT ? 10 : 1;

                    for (var i = 0; i < times; i++) {
                        var result = recipe.craft(player);
                        if (result) {
                            continue;
                        }

                        if (i == 0) {
                            player.sendMessage(Text.translatable("text.mwhrd.gui.craft.no_ingredients")
                                .formatted(Formatting.RED));
                        }
                        break;
                    }
                });
        } else {
            craft
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Select a recipe to craft.")
                    .formatted(Formatting.GRAY));
        }

        this.setSlot(CRAFT, craft);
    }

    /**
     * Draws item crafting options.
     */
    private void drawOptions() {
        var player = this.getPlayer();

        for (var i = 0; i < ITEM_SLOTS.length; i++) {
            var slotIndex = ITEM_SLOTS[i];

            // Determine the recipe.
            var recipeIndex = i + (this.currentPage * ITEM_SLOTS.length);
            if (recipeIndex >= CustomSmithing.ALL.size()) {
                this.setSlot(slotIndex, ItemStack.EMPTY);
                continue;
            }

            var recipe = CustomSmithing.ALL.get(recipeIndex);

            // Draw the recipe.
            var selected = this.selected != null && this.selected.getLeft() == recipeIndex;
            var element = recipe.toGuiElement(player, selected)
                .setCallback(() -> {
                    this.selected = new Pair<>(recipeIndex, recipe);

                    this.drawOptions();
                    this.drawButtons();
                })
                .build();

            this.setSlot(ITEM_SLOTS[i], element);
        }
    }
}
