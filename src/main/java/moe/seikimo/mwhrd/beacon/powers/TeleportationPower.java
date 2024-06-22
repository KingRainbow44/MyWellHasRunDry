package moe.seikimo.mwhrd.beacon.powers;

import moe.seikimo.mwhrd.beacon.BeaconPower;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class TeleportationPower extends BeaconPower {
    private String name; // This is the name of the beacon on the GUI.
    private BlockPos target; // This is where players will teleport to.

    @Override
    public void read(World world, NbtCompound tag) {
        this.name = tag.getString("name");
        this.target = BlockPos.fromLong(tag.getLong("target"));
    }

    @Override
    public void write(World world, NbtCompound tag) {
        tag.putString("name", this.name);
        tag.putLong("target", this.target.asLong());
    }
}
