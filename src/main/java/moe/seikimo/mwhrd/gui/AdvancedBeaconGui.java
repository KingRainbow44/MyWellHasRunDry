package moe.seikimo.mwhrd.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.utils.CustomItems;
import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.beacon.BeaconFuel;
import moe.seikimo.mwhrd.beacon.BeaconLevel;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Math;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static moe.seikimo.mwhrd.utils.GUI.BORDER;

public final class AdvancedBeaconGui extends SimpleGui {
    private static final int DESTROY = 4;
    private static final int ICON = 22;
    private static final int STORAGE = 49;

    private static final int EFFECTS = 29;
    private static final int SETTINGS = 33;

    private static final int[] UPGRADES = {30, 31, 32};

    private static final int[] LEVELS = {36, 27, 18, 9};
    private static final int[] FUEL = {17, 26, 35, 44};

    /**
     * Opens the GUI for the player.
     *
     * @param player The player instance.
     */
    public static void open(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        var gui = new AdvancedBeaconGui(beacon, player);
        gui.open();
    }

    private final GuiElement EMPTY =
        new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE)
            .setName(Text.literal("Empty Upgrade")
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Drop an upgrade here to apply it!")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.AQUA))
            .setCallback(this::beaconInput)
            .build();

    private final BeaconBlockEntity vanillaBeacon;
    private final IAdvancedBeacon beacon;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     *                              will be treated as slots of this gui
     */
    public AdvancedBeaconGui(IAdvancedBeacon beacon, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.beacon = beacon;
        this.vanillaBeacon = (BeaconBlockEntity) beacon;

        this.setTitle(Text.literal("Advanced Beacon Menu"));

        // Draw the GUI contents.
        this.drawBorders();
        this.drawLevel();
        this.drawFuel();
        this.drawButtons();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        if (type != ClickType.MOUSE_LEFT_SHIFT ||
            action != SlotActionType.QUICK_MOVE) return true;

        // Get the clicked slot.
        var stack = this.player
            .currentScreenHandler
            .getSlot(index)
            .getStack();
        if (stack == null || stack.isEmpty()) return false;

        // Check if the stack is blaze powder.
        if (stack.getItem() == Items.BLAZE_POWDER) {
            this.inputFuel(stack);
            return false;
        }

        return true;
    }

    /**
     * Draws a gray glass-pane border around the GUI.
     */
    private void drawBorders() {
        // This draws the top & bottom border.
        for (var i = 0; i < 9; i++) {
            this.setSlot(i, BORDER);
            this.setSlot(i + 45, BORDER);
        }
    }

    /**
     * Draws the beacon's level display.
     */
    private void drawLevel() {
        for (var level : BeaconLevel.values()) {
            if (level == BeaconLevel.TIER_0) continue;

            var index = LEVELS[level.getLevel() - 1];
            var element = getLevelElement(this.vanillaBeacon.level, level);

            this.setSlot(index, element);
        }
    }

    /**
     * Draws the beacon's fuel display.
     */
    private void drawFuel() {
        var beaconLevel = BeaconLevel.valueOf("TIER_" + this.vanillaBeacon.level);
        var fuelLevel = BeaconFuel.getFuel(this.beacon.mwhrd$getFuel());

        var fuelDuration = (int) Math.ceil((float)
            this.beacon.mwhrd$getFuel() /
            beaconLevel.getFuelCost());

        var fuelItem = new GuiElementBuilder(switch (fuelLevel) {
            case UNSTABLE -> Items.RED_STAINED_GLASS_PANE;
            case LOW -> Items.ORANGE_STAINED_GLASS_PANE;
            case MEDIUM -> Items.YELLOW_STAINED_GLASS_PANE;
            case HIGH -> Items.LIME_STAINED_GLASS_PANE;
        })
            .setName(Text.literal("Fuel Status: " + fuelLevel.getName())
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(fuelLevel.getColor()));

        if (this.beacon.mwhrd$getFuel() < BeaconFuel.HIGH.getHighBound()) {
            fuelItem.addLoreLine(
                Text.literal("Use Blaze Powder to refuel the beacon!")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .formatted(Formatting.AQUA)
            );
        }

        List.of(
            Text.empty(),
            Text.literal("Fuel Remaining")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY),
            Text.literal(" " + this.beacon.mwhrd$getFuel() + " fuel")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.BLUE),
            Text.literal("Hours Remaining")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY),
            Text.literal(" " + fuelDuration + " hour(s)")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(fuelLevel.getColor())
        ).forEach(fuelItem::addLoreLine);

        for (var slot : FUEL) {
            this.setSlot(slot, fuelItem);
        }
    }

    /**
     * Draws the buttons for the GUI.
     */
    private void drawButtons() {
        this.setSlot(ICON, new GuiElementBuilder(Items.BEACON)
            .setName(Text.literal("Advanced Beacon")
                .formatted(Formatting.YELLOW))
            .addLoreLine(Text.literal("Drop an upgrade/item here to apply/use it!")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY))
            .setCallback(this::beaconInput));

        this.setSlot(DESTROY, new GuiElementBuilder(Items.BARRIER)
            .setName(Text.literal("Destroy Beacon")
                .formatted(Formatting.RED))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Click here to destroy the beacon!")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.AQUA))
            .addLoreLine(Text.literal("This drops the block as an item.")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY))
            .setCallback(this::removeBeacon));

        var upgrades = this.beacon.mwhrd$getEffectList().stream()
            .filter(BeaconEffect::isDraw)
            .toList();
        for (var i = 0; i < 3; i++) {
            var index = UPGRADES[i];
            if (i >= upgrades.size()) {
                this.setSlot(index, this.EMPTY);
            } else {
                var upgrade = upgrades.get(i);
                this.setSlot(index, new GuiElementBuilder(upgrade.getDisplayItem())
                    .setName(Text.literal(upgrade.getDisplayName())
                        .formatted(Formatting.YELLOW))
                    .addLoreLine(Text.empty())
                    .addLoreLine(Text.literal("Left-click to view the upgrade's settings.")
                        .setStyle(Style.EMPTY.withItalic(false))
                        .formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Right-click to remove the upgrade!")
                        .setStyle(Style.EMPTY.withItalic(false))
                        .formatted(Formatting.RED))
                    .setCallback((_i, type, action) -> {
                        if (type == ClickType.MOUSE_RIGHT) {
                            var power = this.beacon
                                .mwhrd$getEffectMap()
                                .remove(upgrade);
                            if (power != null) {
                                power.delete();
                                this.beacon.mwhrd$save();
                            }
                        } else {
                            var power = this.beacon.mwhrd$getEffectMap().get(upgrade);
                            if (power != null) {
                                power.showGui(this.vanillaBeacon.getWorld(), this.getPlayer());
                            }
                        }

                        this.drawButtons();
                    }));
            }
        }

        this.setSlot(STORAGE, new GuiElementBuilder(Items.ENDER_CHEST)
            .setName(Text.literal("Beacon Storage")
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("A magical storage with infinite space!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.literal("Contains all building blocks for world manipulation.")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Click to access it!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.YELLOW))
            .setCallback(() -> BeaconStorageGui.open(this.beacon, this.getPlayer())));

        this.setSlot(EFFECTS, new GuiElementBuilder(Items.POTION)
            .setComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(
                Optional.empty(), Optional.of(0xFFA148), Collections.emptyList()))
            .hideDefaultTooltip()
            .setName(Text.literal("Potion Effects")
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("View and manage the beacon's potion effects.")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Click to change settings!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.YELLOW))
            .setCallback(() -> BeaconEffectsGui.open(this.beacon, this.getPlayer())));

        this.setSlot(SETTINGS, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Text.literal("Beacon Settings")
                .formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("Change the beacon's settings.")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Click to change settings!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.YELLOW))
            .setCallback(() -> BeaconSettingsGui.open(this.beacon, this.getPlayer())));
    }

    /// <editor-fold desc="Button Callbacks">

    /**
     * Takes an item in to upgrade the beacon.
     */
    private void beaconInput(int index, ClickType type, SlotActionType action) {
        if (action != SlotActionType.PICKUP) return;

        var cursorItem = this.player.currentScreenHandler.getCursorStack();
        if (cursorItem.isEmpty()) return;

        // Check if the item is fuel.
        if (cursorItem.getItem() == Items.BLAZE_POWDER) {
            this.inputFuel(cursorItem);
            return;
        }

        // Check if the item is an upgrade.
        var data = cursorItem.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null || !data.contains("beacon_upgrade")) return;

        var upgradeId = data.copyNbt().getString("beacon_upgrade");
        var upgrade = BeaconEffect.getById(upgradeId);
        if (upgrade == null) {
            this.player.sendMessage(Text.literal("Invalid upgrade!"), true);
            return;
        }

        // Check if the upgrade is already applied.
        var upgrades = this.beacon.mwhrd$getEffectList().stream()
            .filter(BeaconEffect::isDraw)
            .toList();
        if (upgrades.contains(upgrade)) {
            this.player.sendMessage(Text.literal("Upgrade already applied!"));
            return;
        }

        // Check if the upgrades are all full.
        if (upgrades.size() >= 3) {
            this.player.sendMessage(Text.literal("All upgrade slots are full!"));
            return;
        }

        // Check if the beacon is at the required level.
        if (this.vanillaBeacon.level < upgrade.getMinLevel().ordinal()) {
            this.player.sendMessage(Text.literal("Beacon is not at the required level!"));
            return;
        }

        // Add the upgrade to the beacon.
        this.beacon.mwhrd$addEffect(upgrade);
        this.beacon.mwhrd$save();

        // Decrement the stack.
        cursorItem.decrement(1);

        this.drawButtons();
    }

    /**
     * Helper method to update the fuel of the beacon.
     */
    private void inputFuel(ItemStack fuel) {
        if (fuel.getItem() != Items.BLAZE_POWDER) return;

        var fuelLevel = this.beacon.mwhrd$getFuel();
        var maxFuel = BeaconFuel.HIGH.getHighBound();

        if (fuelLevel >= maxFuel) {
            this.getPlayer().sendMessage(Text.literal("Beacon is already full on fuel!")
                .formatted(Formatting.RED));
            return;
        }

        var added = 0;
        while (fuel.getCount() > 0) {
            // Decrement the fuel stack.
            fuel.decrement(1);

            // Add the fuel to the beacon.
            added += 2;
            this.beacon.mwhrd$setFuel(Math.min(maxFuel,
                fuelLevel = fuelLevel + 2));

            if (fuelLevel >= maxFuel) {
                break;
            }
        }

        this.beacon.mwhrd$save();
        this.drawFuel();

        this.getPlayer().sendMessage(Text.literal("Added %s fuel to the beacon!"
            .formatted(added))
            .formatted(Formatting.GREEN));
    }

    /**
     * Removes the beacon from the world.
     */
    private void removeBeacon() {
        // Check if the block exists.
        var world = this.vanillaBeacon.getWorld();
        if (world == null) {
            this.close();
            return;
        }

        var block = world.getBlockEntity(this.vanillaBeacon.getPos());
        if (!(block instanceof IAdvancedBeacon)) {
            this.close();
            return;
        }

        // Write the beacon's data to the item.
        var item = CustomItems.ADVANCED_BEACON.copy();
        item.set(DataComponentTypes.CUSTOM_DATA,
            this.beacon.mwhrd$serializeComponent());

        // Drop the item and remove the block.
        var pos = this.vanillaBeacon.getPos();
        var itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);

        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        this.beacon.mwhrd$destroy();

        // Close the menu.
        this.close();
    }

    /// </editor-fold>

    /**
     * Creates the level element depending on the beacon's level.
     */
    private static GuiElement getLevelElement(
        int beaconLevel, BeaconLevel tier
    ) {
        var isTier = tier.getLevel() <= beaconLevel;

        var builder = new GuiElementBuilder(tier.getItem())
            .setName(Text.literal("Tier " + tier.getLevel())
                .formatted(isTier ? Formatting.GREEN : Formatting.RED))
            .addLoreLine(Text.empty())
            .addLoreLine(Text.literal("Beacon Range")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.literal(" " + tier.getRange() + " blocks")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.BLUE))
            .addLoreLine(Text.literal("Fuel Consumption")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY))
            .addLoreLine(Text.literal(" " + tier.getFuelCost() + " fuel/hr")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.BLUE));

        if (tier.getLevel() == beaconLevel) {
            builder
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("This is your current tier!")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .formatted(Formatting.GREEN));
        } else if (!isTier) {
            builder
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Locked!")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .formatted(Formatting.RED));
        }

        return builder.build();
    }
}
