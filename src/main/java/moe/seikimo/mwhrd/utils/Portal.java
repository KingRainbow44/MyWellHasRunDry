package moe.seikimo.mwhrd.utils;

import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.ContextPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A portal utility class to allow for custom portals.
 */
public final class Portal {
    private final WorldView world;
    private final Direction.Axis axis;
    private final Direction negativeDir;
    private int foundPortalBlocks;
    @Nullable
    private BlockPos lowerCorner;
    private int height;
    private final int width;

    private ContextPredicate frameValidator = (state, world, pos) -> state.isOf(Blocks.OBSIDIAN);

    public static Optional<Portal> getNewPortal(WorldAccess world, BlockPos pos, Direction.Axis axis) {
        return Portal.getOrEmpty(world, pos, areaHelper -> areaHelper.isValid() && areaHelper.foundPortalBlocks == 0, axis, null);
    }

    public static Optional<Portal> getNewPortal(WorldAccess world, BlockPos pos, Direction.Axis axis, ContextPredicate frameValidator) {
        return Portal.getOrEmpty(world, pos, areaHelper -> areaHelper.isValid() && areaHelper.foundPortalBlocks == 0, axis, frameValidator);
    }

    public static Optional<Portal> getOrEmpty(WorldAccess world, BlockPos pos, Predicate<Portal> validator, Direction.Axis axis, ContextPredicate frameValidator) {
        var optional = Optional.of(new Portal(world, pos, axis, frameValidator)).filter(validator);
        if (optional.isPresent()) {
            return optional;
        }
        var axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(new Portal(world, pos, axis2, frameValidator)).filter(validator);
    }

    public Portal(WorldView world, BlockPos pos, Direction.Axis axis, ContextPredicate frameValidator) {
        if (frameValidator != null) {
            this.frameValidator = frameValidator;
        }

        this.world = world;
        this.axis = axis;
        this.negativeDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.lowerCorner = this.getLowerCorner(pos);

        if (this.lowerCorner == null) {
            this.lowerCorner = pos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.getWidth();
            if (this.width > 0) {
                this.height = this.getHeight();
            }
        }
    }

    @Nullable
    private BlockPos getLowerCorner(BlockPos pos) {
        int i = Math.max(this.world.getBottomY(), pos.getY() - 21);
        while (pos.getY() > i && Portal.validStateInsidePortal(this.world.getBlockState(pos.down()))) {
            pos = pos.down();
        }
        Direction direction = this.negativeDir.getOpposite();
        int j = this.getWidth(pos, direction) - 1;
        if (j < 0) {
            return null;
        }
        return pos.offset(direction, j);
    }

    private int getWidth() {
        int i = this.getWidth(this.lowerCorner, this.negativeDir);
        if (i < 2 || i > 21) {
            return 0;
        }
        return i;
    }

    private int getWidth(BlockPos pos, Direction direction) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int i = 0; i <= 21; ++i) {
            mutable.set(pos).move(direction, i);
            BlockState blockState = this.world.getBlockState(mutable);
            if (!Portal.validStateInsidePortal(blockState)) {
                if (!this.frameValidator.test(blockState, this.world, mutable)) break;
                return i;
            }
            BlockState blockState2 = this.world.getBlockState(mutable.move(Direction.DOWN));
            if (!this.frameValidator.test(blockState2, this.world, mutable)) break;
        }
        return 0;
    }

    private int getHeight() {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int i = this.getPotentialHeight(mutable);
        if (i < 3 || i > 21 || !this.isHorizontalFrameValid(mutable, i)) {
            return 0;
        }
        return i;
    }

    private boolean isHorizontalFrameValid(BlockPos.Mutable pos, int height) {
        for (int i = 0; i < this.width; ++i) {
            BlockPos.Mutable mutable = pos.set(this.lowerCorner).move(Direction.UP, height).move(this.negativeDir, i);
            if (this.frameValidator.test(this.world.getBlockState(mutable), this.world, mutable)) continue;
            return false;
        }
        return true;
    }

    private int getPotentialHeight(BlockPos.Mutable pos) {
        for (int i = 0; i < 21; ++i) {
            pos.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, -1);
            if (!this.frameValidator.test(this.world.getBlockState(pos), this.world, pos)) {
                return i;
            }
            pos.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, this.width);
            if (!this.frameValidator.test(this.world.getBlockState(pos), this.world, pos)) {
                return i;
            }
            for (int j = 0; j < this.width; ++j) {
                pos.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, j);
                BlockState blockState = this.world.getBlockState(pos);
                if (!Portal.validStateInsidePortal(blockState)) {
                    return i;
                }
                if (!blockState.isOf(Blocks.NETHER_PORTAL)) continue;
                ++this.foundPortalBlocks;
            }
        }
        return 21;
    }

    private static boolean validStateInsidePortal(BlockState state) {
        return state.isAir() || state.isOf(Blocks.NETHER_PORTAL);
    }

    public boolean isValid() {
        return this.lowerCorner != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortal(WorldAccess world) {
        this.createPortal(world, Blocks.NETHER_PORTAL);
    }

    public void createPortal(WorldAccess world, Block block) {
        if (!this.isValid()) return;

        var blockState = block.getDefaultState().with(NetherPortalBlock.AXIS, this.axis);
        BlockPos.iterate(this.lowerCorner, this.lowerCorner.offset(Direction.UP, this.height - 1).offset(this.negativeDir, this.width - 1))
            .forEach(pos -> world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS | Block.FORCE_STATE));
    }

    public boolean wasAlreadyValid() {
        return this.isValid() && this.foundPortalBlocks == this.width * this.height;
    }

    public static Vec3d entityPosInPortal(BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d entityPos, EntityDimensions entityDimensions) {
        Direction.Axis axis;
        double g;
        double f;
        double d = (double)portalRect.width - (double)entityDimensions.width();
        double e = (double)portalRect.height - (double)entityDimensions.height();
        BlockPos blockPos = portalRect.lowerLeft;
        if (d > 0.0) {
            f = (double)blockPos.getComponentAlongAxis(portalAxis) + (double)entityDimensions.width() / 2.0;
            g = MathHelper.clamp(MathHelper.getLerpProgress(entityPos.getComponentAlongAxis(portalAxis) - f, 0.0, d), 0.0, 1.0);
        } else {
            g = 0.5;
        }
        if (e > 0.0) {
            axis = Direction.Axis.Y;
            f = MathHelper.clamp(MathHelper.getLerpProgress(entityPos.getComponentAlongAxis(axis) - (double)blockPos.getComponentAlongAxis(axis), 0.0, e), 0.0, 1.0);
        } else {
            f = 0.0;
        }
        axis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double h = entityPos.getComponentAlongAxis(axis) - ((double)blockPos.getComponentAlongAxis(axis) + 0.5);
        return new Vec3d(g, f, h);
    }

    public static Vec3d findOpenPosition(Vec3d fallback, ServerWorld world, Entity entity, EntityDimensions dimensions) {
        if (dimensions.width() > 4.0f || dimensions.height() > 4.0f) {
            return fallback;
        }
        var d = (double)dimensions.height() / 2.0;
        var vec3d = fallback.add(0.0, d, 0.0);
        var voxelShape = VoxelShapes.cuboid(Box.of(vec3d, dimensions.width(), 0.0, dimensions.width()).stretch(0.0, 1.0, 0.0).expand(1.0E-6));
        var optional = world.findClosestCollision(entity, voxelShape, vec3d, dimensions.width(), dimensions.height(), dimensions.width());
        var optional2 = optional.map(pos -> pos.subtract(0.0, d, 0.0));
        return optional2.orElse(fallback);
    }
}
