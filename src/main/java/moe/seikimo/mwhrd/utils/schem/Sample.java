package moe.seikimo.mwhrd.utils.schem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A sample is used to represent the blocks and entities in a schematic.
 * Using it, we can place the schematic and read all of its required blocks.
 */
public final class Sample implements BlockView, ModifiableWorld {
    private final Schematic schematic;

    private final int[][][] blockData;
    private final BiMap<BlockState, Integer> palette;

    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private final Map<BlockPos, NbtCompound> blockEntities = new HashMap<>();
    private final BiMap<NbtCompound, Vec3d> entities = HashBiMap.create();

    /**
     * Create a new block sample from a schematic.
     *
     * @param schematic The Sponge schematic to create the sample from.
     */
    public Sample(Schematic schematic) {
        this.schematic = schematic;

        this.blockData = schematic.readBlockData();
        this.palette = ImmutableBiMap.copyOf(schematic.palette());

        // Copy the data into the view.
        this.initialize();
    }

    /**
     * Reads the data from the schematic and populates the block data.
     */
    private void initialize() {
        var lookup = this.palette.inverse();

        // Copy the blocks into the view.
        for (var x = 0; x < this.schematic.width(); x++) {
            for (var y = 0; y < this.schematic.height(); y++) {
                for (var z = 0; z < this.schematic.length(); z++) {
                    var position = new BlockPos(x, y, z);
                    var value = lookup.get(this.blockData[x][y][z]);

                    this.setBlockState(position, value, Block.NOTIFY_LISTENERS);
                    this.blocks.put(position, value);
                }
            }
        }

        // Copy the block entities into the view.
        for (var blockEntityTag : this.schematic.blockEntities()) {
            // Re-serialize position if needed.
            if (
                blockEntityTag.contains("Pos") && !(
                    blockEntityTag.contains("x") ||
                    blockEntityTag.contains("y") ||
                    blockEntityTag.contains("z")
                )
            ) {
                var pos = blockEntityTag.getIntArray("Pos")
                    .orElse(new int[]{0, 0, 0});

                blockEntityTag.putInt("x", pos[0]);
                blockEntityTag.putInt("y", pos[1]);
                blockEntityTag.putInt("z", pos[2]);
            } else if (
                !blockEntityTag.contains("Pos") && (
                    blockEntityTag.contains("x") &&
                    blockEntityTag.contains("y") &&
                    blockEntityTag.contains("z")
                )
            ) {
                blockEntityTag.putIntArray("Pos", new int[]{
                    blockEntityTag.getInt("x", 0),
                    blockEntityTag.getInt("y", 0),
                    blockEntityTag.getInt("z", 0)
                });
            }

            var array = blockEntityTag.getIntArray("Pos")
                .orElse(new int[]{0, 0, 0});
            var position = new BlockPos(array[0], array[1], array[2]);

            this.blockEntities.put(position, blockEntityTag);
        }

        // Copy the entities into the view.
        for (var entityTag : this.schematic.entities()) {
            var array = entityTag.getList("Pos");
            if (array.isEmpty()) {
                continue;
            }

            var pos = array.get();
            var position = new Vec3d(
                pos.getDouble(0, 0d),
                pos.getDouble(1, 0d),
                pos.getDouble(2, 0d)
            );

            this.entities.put(entityTag, position);
        }
    }

    /**
     * Places the sample at the given position.
     *
     * @param origin The position to place the sample at.
     */
    public void place(World world, BlockPos origin) {
        var registry = MyWellHasRunDry.getRegistry();

        // Place all blocks in the world.
        this.blocks.forEach((pos, state) ->
            world.setBlockState(origin.add(pos), state, Block.NOTIFY_ALL));

        // Add all block entities.
        for (var entry : this.blockEntities.entrySet()) {
            var relPos = entry.getKey();
            var absPos = origin.add(relPos);

            var tag = entry.getValue();
            tag.putInt("x", absPos.getX());
            tag.putInt("y", absPos.getY());
            tag.putInt("z", absPos.getZ());

            var state = world.getBlockState(absPos);
            var blockEntity = BlockEntity.createFromNbt(absPos, state, tag, registry);
            if (blockEntity != null) {
                world.addBlockEntity(blockEntity);
            }
        }

        // Add all entities.
        for (var entry : this.entities.entrySet()) {
            var tag = entry.getKey();

            var optionPos = tag.getList("Pos");
            if (optionPos.isEmpty()) {
                continue;
            }
            var pos = optionPos.get();

            var absPos = entry.getValue().add(
                origin.getX(), origin.getY(), origin.getZ());
            pos.set(0, NbtDouble.of(absPos.getX()));
            pos.set(1, NbtDouble.of(absPos.getY()));
            pos.set(2, NbtDouble.of(absPos.getZ()));

            tag.put("Pos", pos);

            var entity = EntityType.getEntityFromNbt(tag, world, SpawnReason.LOAD);
            entity.ifPresent(world::spawnEntity);
        }
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        var state = this.getBlockState(pos);
        var block = state.getBlock();
        if (!(block instanceof BlockWithEntity withEntity)) {
            return null;
        }

        return withEntity.createBlockEntity(pos, state);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.blocks.get(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return this.schematic.height();
    }

    @Override
    public int getBottomY() {
        return 0;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        this.blocks.put(pos, state);
        return true;
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean move) {
        return this.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
        return this.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
    }
}
