package moe.seikimo.mwhrd.game.beacon.powers;

import moe.seikimo.mwhrd.game.beacon.BeaconEffect;
import moe.seikimo.mwhrd.game.beacon.BeaconFuel;
import moe.seikimo.mwhrd.game.beacon.ToggleablePower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FlightPower extends ToggleablePower {
    public FlightPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    protected BeaconEffect getEffect() {
        return BeaconEffect.FLIGHT_CRYSTAL;
    }

    @Override
    public void fuelTick(int fuel) {
        if (!this.minimumFuel().compare(BeaconFuel.getFuel(fuel))) {
            this.handle.getLastOnlinePlayers().forEach(player ->
                this.remove(this.world, player));
        }
    }

    @Override
    protected void toggle(boolean newState) {
        if (!newState) {
            this.handle.getLastOnlinePlayers().forEach(player ->
                this.remove(this.world, player));
        }
    }

    @Override
    public BeaconFuel minimumFuel() {
        return BeaconFuel.LOW;
    }

    @Override
    public void apply(World world, int level, PlayerEntity player) {
        if (player == null || player.isCreative() || player.isSpectator()) return;
        if (this.handle == null) return;

        var fuel = this.handle.getFuel();
        if (!this.minimumFuel().compare(BeaconFuel.getFuel(fuel))) return;

        player.getAbilities().allowFlying = this.enabled;
        player.sendAbilitiesUpdate();
    }

    @Override
    public void remove(World world, PlayerEntity player) {
        if (player == null || player.isCreative() || player.isSpectator()) return;

        var abilities = player.getAbilities();
        abilities.allowFlying = false;
        abilities.flying = false;

        player.sendAbilitiesUpdate();
    }
}
