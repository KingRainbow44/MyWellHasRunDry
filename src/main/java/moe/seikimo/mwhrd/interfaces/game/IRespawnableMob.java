package moe.seikimo.mwhrd.interfaces.game;

import net.minecraft.util.math.BlockPos;

public interface IRespawnableMob {
    void mwhrd$setSpawnPoint(BlockPos pos);

    BlockPos mwhrd$getSpawnPoint();
}
