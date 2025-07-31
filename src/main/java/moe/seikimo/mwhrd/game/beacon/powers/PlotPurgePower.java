package moe.seikimo.mwhrd.game.beacon.powers;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.game.beacon.BeaconFuel;
import moe.seikimo.mwhrd.game.beacon.BeaconLevel;
import moe.seikimo.mwhrd.game.beacon.BeaconPower;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.interfaces.ISelectionPlayer;
import moe.seikimo.mwhrd.models.BeaconModel;
import moe.seikimo.mwhrd.utils.GUI;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.game.worldedit.AsyncPool;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

import static moe.seikimo.mwhrd.utils.GUI.BORDER;

@Slf4j
public final class PlotPurgePower extends BeaconPower {
    private static final int FUEL_COST = 4; // The cost in fuel per hour.
    private static final Set<Item> BLACKLISTED = Set.of();

    public PlotPurgePower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    public BeaconFuel minimumFuel() {
        return BeaconFuel.HIGH;
    }

    @Override
    public SimpleGui getGui(World world, PlayerEntity player) {
        return new Interface(this, (ServerPlayerEntity) player);
    }

    /**
     * Removes all blocks in the selected area.
     *
     * @param player The player invoking the power.
     * @param pos1 The first position of the selection.
     * @param pos2 The second position of the selection.
     */
    private void removeBlocks(ServerPlayerEntity player, BlockPos pos1, BlockPos pos2) {
        var future = AsyncPool.getInstance().clear(
            player, player.getWorld(), pos1, pos2);
        player.sendMessage(Text.literal("Operation is underway...")
            .formatted(Formatting.LIGHT_PURPLE));

        future.whenComplete((changed, exception) -> {
            if (exception != null) {
                PlotPurgePower.log.warn("Failed to remove blocks.", exception);
                player.sendMessage(Text.literal("Failed to remove blocks.")
                    .formatted(Formatting.RED));
            } else {
                // Get the beacon's data.
                var holder = (IDBObject<?>) this.handle;
                if (holder.mwhrd$getData() instanceof BeaconModel data) {
                    // Add all blocks to the beacon's storage.
                    changed.stream()
                        .filter(state -> !BLACKLISTED.contains(state.getBlock().asItem()))
                        .filter(state -> state.getBlock().getLootTableKey().isPresent())
                        .map(state -> state.getBlock().asItem().getDefaultStack())
                        .forEach(item -> data.getItemStorage().offer(item));
                } else {
                    player.sendMessage(Text.literal("Failed to add blocks to the storage.")
                        .formatted(Formatting.RED));
                }

                player.sendMessage(Text.literal("Changed %s blocks."
                    .formatted(changed.size()))
                    .formatted(Formatting.LIGHT_PURPLE));
            }
        });
    }

    static final class Interface extends SimpleGui {
        private static final int INFO = 1;
        private static final int NUKE = 5;

        private static final List<Text> INFO_TEXT = List.of(
            Text.literal("This is a requirement for WorldEdit to function!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.DARK_GRAY),
            Text.empty(),
            Text.literal("Usage:")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.AQUA),
            Text.literal("1. Select two points with //pos1 and //pos2")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.YELLOW),
            Text.literal("2. Click the nuke button in the menu!")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.YELLOW),
            Text.empty(),
            Text.literal("All blocks in the selected area will be removed.")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.RED),
            Text.literal("Removed blocks will end up in the beacon's storage.")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.AQUA)
        );

        private final PlotPurgePower handle;
        private final ISelectionPlayer select;

        public Interface(PlotPurgePower handle, ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X1, player, false);

            this.handle = handle;
            this.select = (ISelectionPlayer) player;

            this.setTitle(Text.literal("Plot Purger"));

            this.drawBorder();
            this.drawButtons();
        }

        private void drawBorder() {
            for (var i = 0; i < this.getSize(); i++) {
                this.setSlot(i, BORDER);
            }
        }

        private void drawButtons() {
            this.setSlot(INFO, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Text.literal("Information")
                    .formatted(Formatting.GREEN))
                .setLore(INFO_TEXT));

            var builder = new GuiElementBuilder(Items.TNT)
                .setName(Text.literal("Remove Blocks!")
                    .formatted(Formatting.LIGHT_PURPLE));
            if (this.select.hasSelection()) {
                builder
                    .addLoreLine(Text.literal("This will remove all selected blocks!")
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.RED))
                    .addLoreLine(Text.empty())
                    .addLoreLine(Text.literal("Estimated Blocks")
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(" " + this.select.selectionSize())
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.BLUE))
                    .addLoreLine(Text.empty())
                    .addLoreLine(Text.literal("Position 1")
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(" " + Utils.serialize(this.select.mwhrd$getPos1()))
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.BLUE))
                    .addLoreLine(Text.literal("Position 2")
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(" " + Utils.serialize(this.select.mwhrd$getPos2()))
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.BLUE))
                    .addLoreLine(Text.empty())
                    .addLoreLine(Text.literal("Click to remove blocks!")
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.YELLOW))
                    .setCallback(this::removeBlocks);
            } else {
                builder
                    .addLoreLine(Text.literal("You must have a selection to use this!")
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.RED))
                    .addLoreLine(Text.empty())
                    .addLoreLine(Text.literal("Select two points with //pos1 and //pos2.")
                        .setStyle(GUI.CLEAR)
                        .formatted(Formatting.YELLOW));
            }

            this.setSlot(NUKE, builder.build());
        }

        /**
         * Invokes the power to remove blocks.
         */
        private void removeBlocks() {
            var pos1 = this.select.mwhrd$getPos1();
            var pos2 = this.select.mwhrd$getPos2();

            // Check if the rectangle falls out of the beacon's range.
            var blockEntity = this.handle.handle;

            var level = BeaconLevel.of(blockEntity.getLevel());
            if (level == null) return;
            var range = level.getRange();

            var pos = blockEntity.getPos();
            if (!pos.isWithinDistance(pos1.toCenterPos(), range) ||
                !pos.isWithinDistance(pos2.toCenterPos(), range)) {
                this.player.sendMessage(Text.literal("The selection is out of range!")
                    .formatted(Formatting.RED));
                return;
            }

            // Check if the beacon has enough fuel.
            var fuelCost = level.getFuelCost() * FUEL_COST;

            // Adjust the fuel cost for the selection size.
            var selectionSize = this.select.selectionSize();
            fuelCost *= 1 + (selectionSize / 1000);

            // Round the fuel cost to the nearest integer.
            fuelCost = Math.min(fuelCost, 640);

            if (blockEntity.getFuel() < fuelCost) {
                this.player.sendMessage(Text.literal("The beacon does not have enough fuel!")
                    .formatted(Formatting.RED));
                return;
            }
            blockEntity.setFuel(blockEntity.getFuel() - fuelCost);

            this.handle.removeBlocks(this.player, pos1, pos2);
        }
    }
}
