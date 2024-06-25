package moe.seikimo.mwhrd.beacon.powers;

import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.beacon.ToggleablePower;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class UnbreakableToolsPower extends ToggleablePower {
    public UnbreakableToolsPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    protected BeaconEffect getEffect() {
        return BeaconEffect.UNBREAKING_TOOLS;
    }

    @Override
    public void apply(World world, int level, PlayerEntity player) {
        if (player instanceof IPlayerConditions condPlayer) {
            condPlayer.mwhrd$setUnbreakable(this.enabled);
        }
    }

    @Override
    public void remove(World world, PlayerEntity player) {
        if (player instanceof IPlayerConditions condPlayer) {
            condPlayer.mwhrd$setUnbreakable(false);
        }
    }
}
