package moe.seikimo.mwhrd.beacon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public abstract class BeaconPower {
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

    public interface Initializer {
        BeaconPower create();
    }

    /**
     * This is an empty beacon power which does nothing.
     */
    public static final class Empty extends BeaconPower { }
}
