package moe.seikimo.mwhrd.beacon.powers;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import lombok.Setter;
import moe.seikimo.mwhrd.beacon.*;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Setter
@Getter
public final class TeleportationPower extends BeaconPower {
    private String name = "My Cool Base!"; // This is the name of the beacon on the GUI.
    private BlockPos target = BlockPos.ORIGIN.toImmutable(); // This is where players will teleport to.

    private boolean forceDisabled = false;
    private boolean enabled = false;

    public TeleportationPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    public void init(IAdvancedBeacon beacon, World world) {
        super.init(beacon, world);

        this.update(world);
    }

    @Override
    public void fuelTick(int fuel) {
        if (this.minimumFuel().compare(BeaconFuel.getFuel(fuel))) {
            this.setForceDisabled(false);
            return;
        }

        this.setForceDisabled(true);
        this.setEnabled(false);
        this.update(this.world);
    }

    @Override
    public BeaconFuel minimumFuel() {
        return BeaconFuel.MEDIUM;
    }

    @Override
    public void add(World world) {
        this.setTarget(this.blockPos.up().mutableCopy());
        this.update(world);
    }

    @Override
    public void read(World world, NbtCompound tag) {
        this.name = tag.getString("name");
        this.target = BlockPos.fromLong(tag.getLong("target")).mutableCopy();
        this.enabled = tag.getBoolean("enabled");

        if (world != null) {
            this.update(world);
        }
    }

    @Override
    public void write(World world, NbtCompound tag) {
        tag.putString("name", this.name);
        tag.putLong("target", this.target.asLong());
        tag.putBoolean("enabled", this.enabled);
    }

    @Override
    public SimpleGui getGui(World world, PlayerEntity player) {
        if (this.isForceDisabled()) {
            player.sendMessage(Text.literal("This beacon does not have enough fuel!")
                .formatted(Formatting.RED));
            return null;
        }

        this.update(world);

        return new Interface(this, world, (ServerPlayerEntity) player);
    }

    /**
     * This registers the beacon with the server.
     */
    private void update(World world) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        if (this.handle instanceof BeaconBlockEntity beacon) {
            var blockPos = beacon.getPos();

            var target = this.getTarget();
            if (!(target instanceof BlockPos.Mutable) ||
                target.getSquaredDistance(blockPos) > 15) {
                this.setTarget(blockPos.up().mutableCopy());
            }

            // Force-load the chunk.
            var chunkX = blockPos.getX() >> 4;
            var chunkZ = blockPos.getZ() >> 4;
            if (serverWorld.isChunkLoaded(chunkX, chunkZ) &&
                !this.isForceDisabled()) {
                serverWorld.setChunkForced(chunkX, chunkZ, this.isEnabled());
            }

            // Register the beacon.
            if (!this.isEnabled()) {
                BeaconManager.getTpBeacons().remove(blockPos);
            } else if (!this.isForceDisabled()) {
                BeaconManager.register(beacon, new BeaconEntry(
                    this.getName(), world, this.getTarget()
                ));
            }
        }
    }

    static final class Interface extends SimpleGui {
        private static final GuiElement BORDER =
            new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.empty())
                .hideTooltip()
                .build();

        private static final int TOGGLE = 13;
        private static final int NAME = 11;
        private static final int SET_POS = 15;

        private final TeleportationPower self;
        private final World world;

        public Interface(TeleportationPower self, World world, ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X3, player, false);

            this.self = self;
            this.world = world;

            this.setTitle(Text.literal("Eye of Teleportation Settings"));
            this.drawBorders();
            this.drawButtons();
        }

        /**
         * Draws the borders of the user interface.
         */
        private void drawBorders() {
            for (var i = 0; i < this.getSize(); i++) {
                this.setSlot(i, BORDER);
            }
        }

        /**
         * Draws the buttons of the user interface.
         */
        private void drawButtons() {
            this.setSlot(TOGGLE, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Text.literal("Toggle Teleportation")
                    .formatted(this.self.isEnabled() ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Click to %s teleportation."
                    .formatted(this.self.isEnabled() ? "disable" : "enable"))
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.YELLOW))
                .setCallback(() -> {
                    this.self.setEnabled(!this.self.isEnabled());
                    this.self.update(this.world);

                    this.self.handle.mwhrd$save();

                    this.getPlayer().sendMessage(Text.literal("Teleportation %s!"
                        .formatted(this.self.isEnabled() ? "enabled" : "disabled"))
                        .formatted(Formatting.GREEN));

                    this.drawButtons();
                }));

            this.setSlot(NAME, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Text.literal("Change Location Name")
                    .formatted(Formatting.GREEN))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Current Name")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(" " + self.getName())
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.BLUE))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Click to change the waypoint name!")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.YELLOW))
                .setCallback(() -> {
                    // Prompt the player to enter a new name.
                    new SignGui(player) {
                        {
                            this.setLine(0, Text.empty());
                            this.setLine(1, Text.literal("^^^^^^^^^^^^^^^"));
                            this.setLine(2, Text.literal("Enter a New"));
                            this.setLine(3, Text.literal("Waypoint Name"));
                        }

                        @Override
                        public void onClose() {
                            var input = this.getLine(0).getString();
                            if (input.length() < 2) {
                                this.getPlayer().sendMessage(Text.literal("Waypoint name must be at least 2 characters!")
                                    .formatted(Formatting.RED));
                                return;
                            }

                            self.setName(input);
                            self.update(world);

                            self.handle.mwhrd$save();

                            this.getPlayer().sendMessage(Text.literal("Waypoint name changed to: %s"
                                    .formatted(self.getName()))
                                .formatted(Formatting.GREEN));
                        }
                    }.open();
                }));

            var target = this.self.getTarget();
            this.setSlot(SET_POS, new GuiElementBuilder(Items.RED_BED)
                .setName(Text.literal("Set Teleport Position")
                    .formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("This will reference where you are standing!")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Current Position")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(" (%s, %s, %s)"
                        .formatted(target.getX(), target.getY(), target.getZ()))
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.BLUE))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Click to update the position!")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.YELLOW))
                .setCallback(() -> {
                    this.self.setTarget(this.player.getBlockPos().mutableCopy());
                    this.self.update(this.world);

                    this.self.handle.mwhrd$save();

                    this.getPlayer().sendMessage(Text.literal("Teleportation position set!")
                        .formatted(Formatting.GREEN));

                    this.drawButtons();
                }));
        }
    }
}
