package moe.seikimo.mwhrd.game.beacon.powers;

import moe.seikimo.mwhrd.game.beacon.BeaconFuel;
import moe.seikimo.mwhrd.game.beacon.BeaconPower;
import net.minecraft.util.math.BlockPos;

public final class WorldEditPower extends BeaconPower {
    public WorldEditPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    public BeaconFuel minimumFuel() {
        return BeaconFuel.MEDIUM;
    }
}
