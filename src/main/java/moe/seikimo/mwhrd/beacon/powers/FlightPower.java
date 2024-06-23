package moe.seikimo.mwhrd.beacon.powers;

import moe.seikimo.mwhrd.beacon.BeaconFuel;
import moe.seikimo.mwhrd.beacon.BeaconPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FlightPower extends BeaconPower {
    public FlightPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    public void fuelTick(int fuel) {
        if (!this.minimumFuel().compare(BeaconFuel.getFuel(fuel))) {
            this.handle.mwhrd$getPlayers().forEach(player ->
                this.remove(this.world, player));
        }
    }

    @Override
    public BeaconFuel minimumFuel() {
        return BeaconFuel.LOW;
    }

    @Override
    public void apply(World world, int level, PlayerEntity player) {
        if (this.handle == null) return;

        var fuel = this.handle.mwhrd$getFuel();
        if (!this.minimumFuel().compare(BeaconFuel.getFuel(fuel))) return;

        player.getAbilities().allowFlying = true;
        player.sendAbilitiesUpdate();
    }

    @Override
    public void remove(World world, PlayerEntity player) {
        var abilities = player.getAbilities();
        abilities.allowFlying = false;
        abilities.flying = false;

        player.sendAbilitiesUpdate();
    }
}
