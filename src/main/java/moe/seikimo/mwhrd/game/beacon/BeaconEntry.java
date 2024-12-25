package moe.seikimo.mwhrd.game.beacon;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record BeaconEntry(
    String name, World world, BlockPos teleportTo
) {
}
