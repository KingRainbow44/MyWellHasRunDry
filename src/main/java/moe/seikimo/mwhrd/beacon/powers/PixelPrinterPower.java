package moe.seikimo.mwhrd.beacon.powers;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.beacon.BeaconFuel;
import moe.seikimo.mwhrd.beacon.BeaconPower;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public final class PixelPrinterPower extends BeaconPower {
    private static final Set<Item> BLACKLISTED = Set.of(
        Items.BEACON, Items.SPAWNER, Items.NETHERITE_BLOCK,
        Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK, Items.IRON_BLOCK,
        Items.GOLD_BLOCK, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK,
        Items.ANCIENT_DEBRIS, Items.DRAGON_EGG, Items.BEDROCK,
        Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE, Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE,
        Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE,
        Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE, Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE,
        Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE, Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE,
        Items.NETHER_GOLD_ORE, Items.NETHER_QUARTZ_ORE, Items.HEAVY_CORE,
        Items.RAW_COPPER_BLOCK, Items.RAW_IRON_BLOCK, Items.RAW_GOLD_BLOCK, Items.TURTLE_EGG, Items.RESPAWN_ANCHOR,
        Items.TNT, Items.BARRIER, Items.REINFORCED_DEEPSLATE, Items.WITHER_SKELETON_SKULL
    );

    private int itemFuel = 0; // Maxes out at 640. 1 fuel per item printed.

    public PixelPrinterPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    public BeaconFuel minimumFuel() {
        return BeaconFuel.HIGH;
    }

    @Override
    public void fuelTick(int fuel) {
        if (this.itemFuel == 640) return;

        var expected = (int) Math.floor(fuel * .1);
        this.itemFuel = Math.min(640,
            this.itemFuel + expected);
    }

    @Override
    public void read(World world, NbtCompound tag) {
        this.itemFuel = tag.getInt("item_fuel");
    }

    @Override
    public void write(World world, NbtCompound tag) {
        tag.putInt("item_fuel", this.itemFuel);
    }

    @Override
    public SimpleGui getGui(World world, PlayerEntity player) {
        if (!this.minimumFuel().compare(this.handle.mwhrd$fuel())) {
            player.sendMessage(Text.literal("Not enough beacon fuel!")
                .formatted(Formatting.RED));
            return null;
        }

        return new Interface(this, (ServerPlayerEntity) player);
    }

    static final class Interface extends SimpleGui {
        private static final int PRINTER = 13;

        private final PixelPrinterPower self;

        public Interface(PixelPrinterPower self, ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X3, player, false);

            this.self = self;

            this.setTitle(Text.literal("Pixel Printer"));

            GUI.drawBorderFull(this);
            this.drawFuel();
            this.drawButtons();
        }

        private void drawFuel() {
            var fuelCount = this.self.itemFuel;
            var fuel = new GuiElementBuilder(fuelCount == 0 ?
                Items.RED_STAINED_GLASS_PANE : Items.LIME_STAINED_GLASS_PANE)
                .hideTooltip();

            for (var i = 10; i <= 16; i++) {
                this.setSlot(i, fuel);
            }
        }

        private void drawButtons() {
            this.setSlot(PRINTER, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Text.literal("Pixel Printer")
                    .formatted(Formatting.GREEN))
                .addLoreLine(GUI.lore("Fuel is consumed when printing items.", Formatting.GRAY))
                .addLoreLine(GUI.lore("Every hour, the printer copies 10% of the beacon's fuel.", Formatting.GRAY))
                .addLoreLine(GUI.lore("The printer can hold up to 640 fuel.", Formatting.GRAY))
                .addLoreLine(Text.empty())
                .addLoreLine(GUI.lore("Item Fuel", Formatting.GRAY))
                .addLoreLine(GUI.lore(" %s/640 fuel".formatted(this.self.itemFuel), Formatting.BLUE))
                .addLoreLine(Text.empty())
                .addLoreLine(GUI.lore("Only blocks can be copied!", Formatting.GOLD))
                .addLoreLine(Text.empty())
                .addLoreLine(GUI.lore("Add an item here to print copies of it!", Formatting.YELLOW))
                .addLoreLine(GUI.lore("Right-click to convert 10% of *ALL* beacon fuel into item fuel!", Formatting.AQUA))
                .setCallback(this::printItems));
        }

        private void printItems(int index, ClickType type, SlotActionType action) {
            var handle = this.self.handle;

            if (type == ClickType.MOUSE_RIGHT) {
                // Burn all the beacon's fuel.
                var beaconFuel = handle.mwhrd$getFuel();
                var itemFuel = (int) Math.floor(beaconFuel * .1);
                this.self.itemFuel += Math.min(640, itemFuel);
                handle.mwhrd$setFuel(0);

                this.getPlayer().sendMessage(Text.literal("Converted beacon fuel into %s item fuel!"
                    .formatted(itemFuel))
                    .formatted(Formatting.GREEN));
            } else {
                var stack = this.getPlayer()
                    .currentScreenHandler
                    .getCursorStack();
                if (stack == null || stack.isEmpty()) return;

                // Validate the stack for duplication.
                var item = stack.getItem();
                if (!(item instanceof BlockItem blockItem)) {
                    this.getPlayer().sendMessage(Text.literal("Only blocks can be copied!")
                        .formatted(Formatting.RED));
                    return;
                }
                if (blockItem.getBlock() instanceof BlockWithEntity) {
                    this.getPlayer().sendMessage(Text.literal("This block cannot be copied!")
                        .formatted(Formatting.RED));
                    return;
                }

                if (!item.canBeNested() || item.getMaxCount() != 64 || BLACKLISTED.contains(item)) {
                    this.getPlayer().sendMessage(Text.literal("This item cannot be copied!")
                        .formatted(Formatting.RED));
                    return;
                }

                var itemFuel = this.self.itemFuel;
                if (itemFuel == 0) return;

                // Deduct the fuel.
                var toConsume = Math.min(640, stack.getCount());
                if (itemFuel < toConsume) {
                    this.getPlayer().sendMessage(Text.literal("Not enough item fuel!")
                        .formatted(Formatting.RED));
                    return;
                }
                this.self.itemFuel = Math.max(0, itemFuel - toConsume);

                // Copy the item.
                var copy = stack.copy();
                this.getPlayer().sendMessage(Text.literal("Printed a copy of ")
                    .formatted(Formatting.GREEN)
                    .append(copy.toHoverableText().copy()
                        .formatted(Formatting.YELLOW))
                    .append(Text.literal("!")
                        .formatted(Formatting.GREEN)));

                // Add to the player's inventory.
                this.getPlayer().getInventory().offerOrDrop(copy);
            }

            this.drawFuel();
            this.drawButtons();
        }
    }
}
