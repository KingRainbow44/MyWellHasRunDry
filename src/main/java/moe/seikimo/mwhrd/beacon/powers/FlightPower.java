package moe.seikimo.mwhrd.beacon.powers;

import moe.seikimo.mwhrd.beacon.BeaconPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public final class FlightPower extends BeaconPower {
    @Override
    public void apply(World world, int level, PlayerEntity player) {
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
