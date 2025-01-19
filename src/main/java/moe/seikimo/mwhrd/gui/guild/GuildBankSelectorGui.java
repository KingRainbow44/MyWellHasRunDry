package moe.seikimo.mwhrd.gui.guild;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.experimental.ExtensionMethod;
import moe.seikimo.mwhrd.game.guilds.GuildInstance;
import moe.seikimo.mwhrd.game.guilds.GuildPermission;
import moe.seikimo.mwhrd.utils.GUI;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@ExtensionMethod(Players.class)
public final class GuildBankSelectorGui extends SimpleGui {
    private static final int WITHDRAW_INDEX = 2;
    private static final int VIEW_INDEX = 4;
    private static final int INSERT_INDEX = 6;

    /**
     * Opens the GUI for the player.
     *
     * @param guild The guild to open the GUI for.
     * @param player The player instance.
     */
    public static void open(GuildInstance guild, ServerPlayerEntity player) {
        // Check if the guild is the appropriate level.
        if (guild.getLevel() < 15) {
            player.sendMessage(Text.translatable("text.mwhrd.guild.bank.level")
                .formatted(Formatting.RED));
            return;
        }

        // Check if the player has permission.
        if (!guild.hasPermission(player, GuildPermission.MEMBER)) {
            player.sendMessage(Text.translatable("text.mwhrd.guild.bank.permission")
                .formatted(Formatting.RED));
            return;
        }

        var gui = new GuildBankSelectorGui(guild, player);
        gui.open();
    }

    private final GuildInstance guild;

    /**
     * Constructs a new instance of the guild bank/storage GUi.
     *
     * @param guild The guild to open the GUI for.
     * @param player The player to open the GUI for.
     */
    public GuildBankSelectorGui(GuildInstance guild, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X1, player, false);

        this.guild = guild;

        this.setTitle(Text.literal("Guild Bank - " + guild.getName()));

        // Draw the GUI contents.
        GUI.drawBorderFull(this);
        this.drawButtons();
    }

    /**
     * Draws the buttons for the GUI.
     */
    private void drawButtons() {
        this.setSlot(
            WITHDRAW_INDEX,
            new GuiElementBuilder(Items.DROPPER)
                .setName(Text.literal("Withdraw Stacks")
                    .formatted(Formatting.GREEN))
                .setLore(List.of(
                    Text.literal("Withdraws stacks from the guild bank which are ")
                        .formatted(Formatting.GRAY),
                    Text.literal("similar to the items in your inventory.")
                        .formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("Click to withdraw stacks!")
                        .formatted(Formatting.YELLOW)
                ))
                .setCallback(this::withdrawStacks)
        );

        this.setSlot(
            VIEW_INDEX,
            new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("View Guild Bank")
                    .formatted(Formatting.GREEN))
                .setLore(List.of(
                    Text.literal("View the contents of the guild bank.")
                        .formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("Click to view the guild bank!")
                        .formatted(Formatting.YELLOW)
                ))
                .setCallback(() -> GuildBankItemGui.open(this.guild, this.getPlayer()))
        );

        this.setSlot(
            INSERT_INDEX,
            new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Insert Stacks")
                    .formatted(Formatting.GREEN))
                .setLore(List.of(
                    Text.literal("Inserts items from your inventory into")
                        .formatted(Formatting.GRAY),
                    Text.literal("matching stacks in the guild bank.")
                        .formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("Click to insert stacks!")
                        .formatted(Formatting.YELLOW)
                ))
                .setCallback(this::insertStacks)
        );
    }

    /**
     * Inserts stacks into the guild bank.
     */
    private void insertStacks() {
        var inventory = this.getPlayer().getInventory();
        var storage = this.guild.getBank();

        // Get all items that are in the storage.
        var storageTypes = storage.uniqueItems();

        // Iterate through the player's inventory.
        // If a stack's type matches, insert it into the storage.
        for (var i = 9; i < 36; i++) {
            var item = inventory.getStack(i);
            if (item.isEmpty()) {
                continue;
            }

            var type = item.getItem();
            if (storageTypes.contains(type)) {
                // Insert the stack into the storage.
                storage.insert(item);
                // Clear the player's stack.
                item.setCount(0);
            }
        }

        this.getPlayer().sendMessage(Text.translatable("text.mwhrd.guild.bank.insert")
            .formatted(Formatting.GREEN));
    }

    /**
     * Withdraws stacks from the guild bank.
     */
    private void withdrawStacks() {
        var inventory = this.getPlayer().getInventory();
        var storage = this.guild.getBank();

        // Using the types in the inventory, match them to those of the storage.
        for (var i = 9; i < 36; i++) {
            var stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            // Check if the stack is less than the max stack size.
            var item = stack.getItem();
            if (stack.getCount() >= item.getMaxCount()) {
                continue;
            }

            // Otherwise, we should try to withdraw a stack from the storage.
            var needed = item.getMaxCount() - stack.getCount();
            try {
                storage.remove(item, needed);
                stack.setCount(item.getMaxCount());
            } catch (IllegalArgumentException ignored) {
                // When this occurs, we don't have enough of the stack.
            }
        }

        this.getPlayer().sendMessage(Text.translatable("text.mwhrd.guild.bank.withdraw")
            .formatted(Formatting.GREEN));
    }
}
