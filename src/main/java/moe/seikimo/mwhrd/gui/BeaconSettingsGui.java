package moe.seikimo.mwhrd.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class BeaconSettingsGui extends SimpleGui {
    /**
     * Opens the Beacon Settings GUI for the given player.
     *
     * @param beacon The beacon to open the settings for
     * @param player The player to open the GUI for
     */
    public static void open(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        new BeaconSettingsGui(beacon, player).open();
    }

    private final IAdvancedBeacon beacon;

    public BeaconSettingsGui(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.beacon = beacon;

        this.setTitle(Text.literal("Beacon Settings"));

        GUI.drawBorders(this);
        this.drawButtons();
    }

    private void drawButtons() {
        var map = this.beacon.mwhrd$getEffectMap();
        var upgrades = this.beacon.mwhrd$getEffectList().stream()
            .filter(e -> !e.isDraw())
            .toList();

        GUI.drawBorderedList(this, upgrades, item -> {
            var power = map.get(item);

            return new GuiElementBuilder(item.getDisplayItem())
                .setName(Text.literal(item.getDisplayName())
                    .formatted(Formatting.GREEN))
                .hideDefaultTooltip()
                .addLoreLine(Text.empty())
                .addLoreLine(GUI.lore("Click to open the settings!", Formatting.YELLOW))
                .setCallback(() -> power.showGui(this.getPlayer().getWorld(), this.getPlayer()))
                .build();
        });
    }
}
