package moe.seikimo.mwhrd.gui.mca;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.game.guilds.GuildInstance;
import moe.seikimo.mwhrd.game.mca.GuildProgress;
import moe.seikimo.mwhrd.utils.GUI;
import moe.seikimo.mwhrd.utils.items.ItemBuilder;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class McaProgressGui extends SimpleGui {
    private static final int START_INDEX = 10;
    private static final int END_INDEX = 16;

    private static final int BAR_START_INDEX = 11;

    /**
     * Opens the guild's progress GUI for the specified player.
     *
     * @param guild The guild instance to display.
     * @param player The player to open the GUI for.
     */
    public static void open(GuildInstance guild, ServerPlayerEntity player) {
        var gui = new McaProgressGui(guild, player);
        gui.open();
    }

    private final GuildInstance guild;

    /**
     * Constructs a new instance of the guild's progress towards aliveness!
     *
     * @param guild The guild to open the GUI for.
     * @param player The player to open the GUI for.
     */
    public McaProgressGui(GuildInstance guild, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.guild = guild;

        this.setTitle(Text.translatable("text.mwhrd.gui.mca.title"));

        // Draw the GUI contents.
        GUI.drawBorderFull(this);
        this.drawEndpoints();
        this.drawProgress();
    }

    /**
     * The endpoints represent the start and end of the progress bar.
     */
    private void drawEndpoints() {
        this.setSlot(
            START_INDEX,
            new GuiElementBuilder(
                ItemBuilder.of(Items.RED_CONCRETE_POWDER)
                    .build()
            )
                .hideDefaultTooltip()
                .setName(Text.translatable("text.mwhrd.gui.mca.progress.start")
                    .formatted(Formatting.RED))
                .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress.start.1")
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress.start.2")
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress.start.3")
                    .formatted(Formatting.GRAY))
        );

        this.setSlot(
            END_INDEX,
            new GuiElementBuilder(
                ItemBuilder.of(Items.GREEN_CONCRETE)
                    .build()
            )
                .hideDefaultTooltip()
                .setName(Text.translatable("text.mwhrd.gui.mca.progress.end")
                    .formatted(Formatting.GREEN))
                .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress.end.1")
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress.end.2")
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress.end.3")
                    .formatted(Formatting.GRAY))
        );
    }

    /**
     * Draws the progress bar.
     */
    private void drawProgress() {
        var currentProgress = this.guild.getProgress();

        var offset = 0;
        for (var progress : GuildProgress.values()) {
            if (progress == GuildProgress.COMPLETED) {
                // We do not include the 'COMPLETED' attribute in the progress bar.
                continue;
            }

            var active = false;
            var current = false;

            // Get the item for the current progress.
            var item = currentProgress.getIcon();
            if (currentProgress != progress) {
                if (currentProgress.ordinal() >= progress.ordinal()) {
                    item = Items.LIME_STAINED_GLASS_PANE;
                    active = true;
                } else {
                    item = Items.RED_STAINED_GLASS_PANE;
                }
            } else {
                current = true;
            }

            // Set the slot in the GUI.
            this.setSlot(
                BAR_START_INDEX + offset++,
                new GuiElementBuilder(item)
                    .setName(Text.translatable("text.mwhrd.gui.mca.progress." + offset)
                        .formatted(active ? Formatting.GREEN : Formatting.RED))
                    .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress." + offset + ".desc.1")
                        .formatted(Formatting.GRAY))
                    .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress." + offset + ".desc.2")
                        .formatted(Formatting.GRAY))
                    .addLoreLine(Text.translatable("text.mwhrd.gui.mca.progress." + offset + ".desc.3")
                        .formatted(Formatting.GRAY))
                    .glow(current)
            );
        }
    }
}
