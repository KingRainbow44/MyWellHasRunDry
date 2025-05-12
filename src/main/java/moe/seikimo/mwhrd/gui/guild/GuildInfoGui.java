package moe.seikimo.mwhrd.gui.guild;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.game.guilds.GuildInstance;
import moe.seikimo.mwhrd.game.guilds.GuildPermission;
import moe.seikimo.mwhrd.utils.GUI;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public final class GuildInfoGui extends SimpleGui {
    private static final int INFO = 22;

    /**
     * Opens the guild info GUI for the specified player.
     *
     * @param instance The guild instance to display.
     * @param player The player to open the GUI for.
     */
    public static void open(GuildInstance instance, ServerPlayerEntity player) {
        new GuildInfoGui(instance, player).open();
    }

    private final GuildInstance guild;

    public GuildInfoGui(GuildInstance instance, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.guild = instance;

        this.setTitle(Text.literal("Guild Info"));

        // Draw the GUI contents.
        GUI.drawHorizontalLine(this, 18, Utils.<Void>newList(9), (element, i) -> GUI.BORDER);
        this.drawPlayers();
        this.drawButtons();
    }

    /**
     * Draws the heads of all players in the guild.
     */
    private void drawPlayers() {
        var members = new ArrayList<>(this.guild.getMembers());

        // Sort by rank.
        members.sort((p1, p2) -> {
            var rank1 = this.guild.getPermission(p1);
            var rank2 = this.guild.getPermission(p2);

            return rank2.compareTo(rank1);
        });

        var max = Math.min(members.size(), 18);

        for (var i = 0; i < max; i++) {
            var player = members.get(i);
            var head = Players.headOf(player.getUUID());

            var targetRank = this.guild.getPermission(player);

            var builder = new GuiElementBuilder(head)
                .setName(Text.literal(player.username())
                    .formatted(this.guild.getColor()))
                .addLoreLine(Text.literal("Rank: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(targetRank.toString())
                        .formatted(Formatting.DARK_AQUA)));

            if (
                !player.getUUID().equals(this.player.getUuid()) &&
                    targetRank.ordinal() >= GuildPermission.OFFICER.ordinal()
            ) {
                builder
                    .addLoreLine(Text.empty())
                    .addLoreLine(Text.literal("Left-click to promote")
                        .formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Right-click to demote")
                        .formatted(Formatting.AQUA))
                    .setCallback(event -> {
                        // Get the target rank.
                        var executorRank = this.guild.getPermission(this.player);
                        var newTargetRank = switch (event) {
                            case MOUSE_LEFT -> targetRank.getNext();
                            case MOUSE_RIGHT -> targetRank.getPrevious();
                            default -> targetRank;
                        };

                        // If the rank is the same, do nothing.
                        if (newTargetRank == null || targetRank == newTargetRank) {
                            return;
                        }

                        // Check if the player has permission to change the rank.
                        if (newTargetRank.ordinal() >= executorRank.ordinal()) {
                            return;
                        }

                        // Update the permission.
                        this.guild.setPermission(player, newTargetRank);

                        // Re-draw players.
                        this.drawPlayers();
                    });
            }

            this.setSlot(i, builder);
        }
    }

    /**
     * Draws the buttons for the GUI.
     */
    private void drawButtons() {
        this.setSlot(INFO, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
            .setName(this.guild.getDisplayName())
            .addLoreLine(
                Text.literal("Guild Level: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal("Lv" + this.guild.getLevel())
                        .formatted(Formatting.DARK_AQUA))
            )
            .addLoreLine(
                Text.literal("Total Experience: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(Utils.pretty(this.guild.getExperience()) + " XP")
                        .formatted(Formatting.DARK_AQUA))
            )
        );
    }
}
