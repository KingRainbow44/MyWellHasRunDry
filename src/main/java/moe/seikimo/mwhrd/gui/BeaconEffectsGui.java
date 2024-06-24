package moe.seikimo.mwhrd.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.beacon.powers.EffectsPower;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class BeaconEffectsGui extends SimpleGui {
    private static final Set<RegistryEntry<StatusEffect>> BLACKLISTED = Set.of(
        StatusEffects.INSTANT_DAMAGE, StatusEffects.INSTANT_HEALTH
    );

    private static final int LEARN = 4;

    /**
     * Opens the beacon effects GUI.
     *
     * @param beacon The beacon to open the GUI for.
     * @param player The player to open the GUI for.
     */
    public static void open(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        new BeaconEffectsGui(beacon, player).open();
    }

    /**
     * Creates a translatable text from an effect ID.
     *
     * @param id The effect ID.
     * @return The translatable text.
     */
    private static Text translate(String id) {
        var parts = id.split(":");
        if (parts.length != 2) return Text.empty();
        return Text.translatableWithFallback(
            "effect." + parts[0] + "." + parts[1],
            "No Effect");
    }

    private final IAdvancedBeacon beacon;
    private final EffectsPower effects;

    public BeaconEffectsGui(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.beacon = beacon;
        this.effects = beacon.mwhrd$getPower(EffectsPower.class);

        this.setTitle(Text.literal("Beacon Effects"));

        GUI.drawBorders(this);
        this.drawEffects();
        this.drawButtons();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        if (index != LEARN || type != ClickType.MOUSE_LEFT) return true;

        var stack = this.getPlayer()
            .currentScreenHandler
            .getCursorStack();
        if (stack == null || stack.isEmpty()) return false;

        var potion = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potion == null) {
            this.getPlayer().sendMessage(Text.literal("This is not a potion!")
                .formatted(Formatting.RED));
            return false;
        }

        var known = this.effects.getKnown();
        potion.forEachEffect(instance -> {
            var effect = instance.getEffectType();
            if (BLACKLISTED.contains(effect)) return;

            if (!known.contains(effect)) {
                known.add(effect);
                this.getPlayer().sendMessage(Text.literal("Learned effect: ")
                    .formatted(Formatting.GREEN)
                    .append(translate(effect.getIdAsString()).copyContentOnly()
                        .formatted(Formatting.YELLOW))
                    .append(Text.literal("!")
                        .formatted(Formatting.GREEN)));
            }
        });

        this.drawEffects();

        this.beacon.mwhrd$save();

        return false;
    }

    private void drawEffects() {
        var effects = this.effects.getKnown();
        var selected = this.effects.getEffects();

        GUI.drawBorderedList(this, effects, item -> {
            var currentPotency = this.effects.getPotency(item);
            var number = switch (currentPotency) {
                default -> "";
                case 1 -> " I";
                case 2 -> " II";
                case 3 -> " III";
            };

            var builder = new GuiElementBuilder(Items.LINGERING_POTION)
                .hideDefaultTooltip()
                .setComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(
                    Optional.empty(), Optional.of(item.value().getColor()), Collections.emptyList()
                ))
                .setName(translate(item.getIdAsString()).copyContentOnly()
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(number)
                        .formatted(Formatting.GREEN)));

            if (selected.contains(item)) {
                builder
                    .addLoreLine(GUI.lore("Currently selected!", Formatting.GRAY))
                    .addLoreLine(Text.empty());

                if (this.effects.used()) {
                    builder
                        .addLoreLine(GUI.lore("You cannot add more effects!", Formatting.RED))
                        .addLoreLine(GUI.lore("Right-click to decrease potency!", Formatting.AQUA));
                } else {
                    builder
                        .addLoreLine(GUI.lore("Click to increase potency!", Formatting.YELLOW));
                    if (currentPotency > 1) {
                        builder.addLoreLine(GUI.lore("Right-click to decrease potency!", Formatting.AQUA));
                    } else {
                        builder.addLoreLine(GUI.lore("Right-click to remove effect!", Formatting.AQUA));
                    }
                }

                builder.setCallback(clickType -> {
                    if (clickType == ClickType.MOUSE_RIGHT) {
                        this.effects.decreasePotency(item);
                    } else try {
                        this.effects.addEffect(item);
                    } catch (Exception exception) {
                        this.getPlayer().sendMessage(Text.literal(exception.getMessage())
                            .formatted(Formatting.RED));
                    }

                    this.drawEffects();

                    this.beacon.mwhrd$save();
                });
            } else {
                builder.addLoreLine(Text.empty());
                if (this.effects.used()) {
                    builder
                        .addLoreLine(GUI.lore("You cannot add more effects!", Formatting.RED))
                        .addLoreLine(GUI.lore("Remove effects to add more!", Formatting.RED));
                } else {
                    builder
                        .addLoreLine(GUI.lore("Click to add this effect!", Formatting.YELLOW))
                        .setCallback(() -> {
                            try {
                                this.effects.addEffect(item);
                            } catch (Exception exception) {
                                this.getPlayer().sendMessage(Text.literal(exception.getMessage())
                                    .formatted(Formatting.RED));
                            }

                            this.drawEffects();

                            this.beacon.mwhrd$save();
                        });
                }
            }

            return builder.build();
        });
    }

    private void drawButtons() {
        this.setSlot(LEARN, new GuiElementBuilder(Items.BREWING_STAND)
            .setName(Text.literal("Learn Effects")
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.empty())
            .addLoreLine(GUI.lore("Drop a potion here to learn it!", Formatting.YELLOW)));
    }
}
