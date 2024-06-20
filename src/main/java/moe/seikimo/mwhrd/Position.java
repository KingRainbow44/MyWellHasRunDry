package moe.seikimo.mwhrd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Getter
@RequiredArgsConstructor
public final class Position {
    private final ServerWorld world;
    private final BlockPos pos;
}
