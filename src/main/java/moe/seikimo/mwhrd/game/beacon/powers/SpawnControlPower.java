package moe.seikimo.mwhrd.game.beacon.powers;

import moe.seikimo.mwhrd.game.beacon.BeaconEffect;
import moe.seikimo.mwhrd.game.beacon.ToggleablePower;
import net.minecraft.util.math.BlockPos;

public final class SpawnControlPower extends ToggleablePower {
    public SpawnControlPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    protected BeaconEffect getEffect() {
        return BeaconEffect.DISABLE_SPAWNS;
    }
}
