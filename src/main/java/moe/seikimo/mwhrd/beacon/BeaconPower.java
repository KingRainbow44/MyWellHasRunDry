package moe.seikimo.mwhrd.beacon;

import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.RequiredArgsConstructor;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@RequiredArgsConstructor
public abstract class BeaconPower {
    protected final BlockPos blockPos;
    protected IAdvancedBeacon handle;
    protected World world;

    /**
     * Called when the beacon receives its world.
     *
     * @param beacon The beacon to initialize.
     * @param world The world the beacon is in.
     */
    public void init(IAdvancedBeacon beacon, World world) {
        this.handle = beacon;
        this.world = world;
    }

    /**
     * This is invoked when the beacon is ticked.
     */
    public void fuelTick(int fuel) {}

    /**
     * Invoked when the power is added for the first time.
     */
    public void add(World world) {}

    /**
     * This is invoked when a player removes the beacon power.
     */
    public void delete() {}

    /**
     * This is invoked when a player is affected by the beacon.
     * This can be called by the beacon's tick method.
     *
     * @param world The world the player is in.
     * @param level The beacon's level.
     * @param player The player to apply the beacon power to.
     */
    public void apply(World world, int level, PlayerEntity player) {}

    /**
     * This is invoked when a player is no longer affected by the beacon.
     * This can be called when the player disconnects, when the beacon is removed, or when the beacon's tick method is called.
     *
     * @param world The world the player is in.
     * @param player The player to remove the beacon power from.
     */
    public void remove(World world, PlayerEntity player) {}

    /**
     * This is invoked when the beacon needs to save its data.
     *
     * @param world The world the beacon is in.
     * @param tag The tag to write the data to.
     */
    public void write(World world, NbtCompound tag) {}

    /**
     * This is invoked after the beacon has been loaded from the world.
     *
     * @param world The world the beacon is in.
     * @param tag The tag to read the data from.
     */
    public void read(World world, NbtCompound tag) {}

    /**
     * @return This should return a GUI for the beacon power.
     */
    public SimpleGui getGui(World world, PlayerEntity player) {
        return null;
    }

    /**
     * This is a helper method to show the GUI for the beacon power.
     *
     * @param world The world the beacon is in.
     * @param player The player to show the GUI to.
     */
    public final void showGui(World world, PlayerEntity player) {
        var gui = this.getGui(world, player);
        if (gui != null) {
            gui.open();
        }
    }

    public interface Initializer {
        BeaconPower create(BlockPos blockPos);
    }

    /**
     * This is an empty beacon power which does nothing.
     */
    public static final class Empty extends BeaconPower {
        public Empty(BlockPos blockPos) {
            super(blockPos);
        }
    }
}
