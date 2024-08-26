package moe.seikimo.mwhrd.game.lightrealm;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.utils.MobGear;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * In the generator, nodes create a spiral around (0, -50, 0).
 * At each node, a structure block should be placed.
 */
@Slf4j
public final class LightTowerGenerator {
    private static final Vec3i START_POS = new Vec3i(0, -50, 0);
    private static final String[] STRUCTURES = {
        "island_1", "island_2", "island_3", "island_4"
    };

    private final boolean genOnly;
    private final ServerWorld world;

    @Getter(onMethod_ = @ApiStatus.Internal)
    private final List<Vec3i> nodes = new ArrayList<>();

    /**
     * Internal public constructor used for testing.
     */
    @ApiStatus.Internal
    public LightTowerGenerator() {
        this(null, true);
    }

    /**
     * Public constructor used in generating the light tower.
     */
    public LightTowerGenerator(ServerWorld world) {
        this(world, false);
    }

    private LightTowerGenerator(ServerWorld world, boolean genOnly) {
        this.world = world;
        this.genOnly = genOnly;
    }

    /**
     * Places the islands around the tower.
     */
    public void placeIslands() {
        if (this.genOnly) {
            throw new UnsupportedOperationException("Cannot place islands in test mode.");
        }
        if (this.world == null) {
            throw new IllegalStateException("Cannot place islands without a world.");
        }
        if (this.nodes.isEmpty()) {
            throw new IllegalStateException("Cannot place islands without nodes.");
        }

        // Place the islands around the tower.
        var totalNodes = this.nodes.size();
        for (var i = 0; i < totalNodes; i++) try {
            var node = this.nodes.get(i);
            var position = Utils.blockPos(node);

            // Pick a random structure.
            var isFinal = i == totalNodes - 1;
            var structure = isFinal ? "boss_island" : Utils.random(STRUCTURES);

            // Set the block state to a structure block.
            var blockState = Blocks.STRUCTURE_BLOCK.getDefaultState();
            this.world.setBlockState(position, blockState);

            // Get the structure block entity.
            var structureBlock = this.world
                .getBlockEntity(position, BlockEntityType.STRUCTURE_BLOCK)
                .orElseThrow(() -> new IllegalArgumentException("Unable to place structure block at " + position));

            structureBlock.setMode(StructureBlockMode.LOAD);
            structureBlock.setOffset(new BlockPos(-7, -9, -7));
            structureBlock.setTemplateName(Identifier.of("mwhrd", structure));
            structureBlock.loadAndPlaceStructure(this.world);

            // Delete the structure block.
            this.world.setBlockState(position, Blocks.AIR.getDefaultState());

            // Spawn a mob on the islands.
            var mob = new ZombieEntity(EntityType.ZOMBIE, this.world);
            mob.setPersistent();
            mob.setPosition(Vec3d.of(node));
            MobGear.applyArmor(mob);
            this.world.spawnEntity(mob);
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to place island at node {}", i, ex);
        }
    }

    /**
     * Populates the nodes list with the positions of the nodes.
     *
     * @param height The tower's height. Must be a multiple of 6.
     */
    public void generate(int height) {
        if (height % 6 != 0) {
            // This is to always ensure our tower spiral ends in the center.
            throw new IllegalArgumentException("Height must be a multiple of 6.");
        }

        // Simple algorithm to generate a spiral around the start position.
        for (var i = 0; i < height; i++) {
            var y = START_POS.getY() + (i * 16);
            var x = switch (i % 6) { case 1 -> 10; case 3 -> -10; default -> 0; };
            var z = switch (i % 6) { case 2 -> 10; case 4 -> -10; default -> 0; };

            nodes.add(new Vec3i(x, y, z));
        }
    }
}
