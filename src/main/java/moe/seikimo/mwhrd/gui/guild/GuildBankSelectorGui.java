package moe.seikimo.mwhrd.gui.guild;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.experimental.ExtensionMethod;
import moe.seikimo.mwhrd.game.guilds.GuildInstance;
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
                .setCallback(() -> {
                    this.getPlayer().sendMessage(Text.literal("you should probably withdraw"));
                })
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
                .setCallback(() -> {
                    this.getPlayer().sendMessage(Text.literal("you should probably insert"));
                })
        );
    }
}
