package moe.seikimo.mwhrd.custom.blocks;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import moe.seikimo.mwhrd.custom.CustomStructures;
import moe.seikimo.mwhrd.utils.Worlds;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class BlossomSaplingBlock extends SaplingBlock implements PolymerBlock {
    public BlossomSaplingBlock() {
        super(null, AbstractBlock.Settings.create());
    }

    @Override
    public void generate(ServerWorld world, BlockPos pos, BlockState state, Random random) {
        Worlds.paste(world, pos, CustomStructures.BLOSSOM_TREE, new BlockPos(-18, -1, -10));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.CHERRY_SAPLING.getDefaultState()
            .with(SaplingBlock.STAGE, state.get(SaplingBlock.STAGE));
    }
}
