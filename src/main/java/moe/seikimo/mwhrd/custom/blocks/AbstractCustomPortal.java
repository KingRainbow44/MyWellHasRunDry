package moe.seikimo.mwhrd.custom.blocks;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import moe.seikimo.mwhrd.utils.Portal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.tick.ScheduledTickView;
import xyz.nucleoid.packettweaker.PacketContext;

public abstract class AbstractCustomPortal extends SimplePolymerBlock implements net.minecraft.block.Portal {
    public static final EnumProperty<Axis> AXIS = Properties.HORIZONTAL_AXIS;

    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public AbstractCustomPortal(Settings settings) {
        super(
            settings
                .noCollision()
                .ticksRandomly()
                .strength(-1f)
                .dropsNothing()
                .sounds(BlockSoundGroup.GLASS)
                .luminance(state -> 11)
                .pistonBehavior(PistonBehavior.BLOCK),
            Blocks.NETHER_PORTAL
        );

        this.setDefaultState(this.getStateManager()
            .getDefaultState()
            .with(AXIS, Axis.X));
    }

    /**
     * @return The frame validator for this portal.
     */
    protected abstract ContextPredicate getFrameValidator();

    /**
     * Invoked when an entity collides with the portal.
     *
     * @param entity The entity that collided with the portal.
     * @param pos The position of the portal.
     */
    protected void transportEntity(Entity entity, BlockPos pos) {
        if (entity.canUsePortals(false)) {
            entity.tryUsePortal(this, pos);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(AXIS) == Axis.Z ? Z_SHAPE : X_SHAPE;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighborPos,
        BlockState neighborState,
        Random random
    ) {
        Axis axis = direction.getAxis(), currentAxis = state.get(AXIS);
        var valid = currentAxis != axis && axis.isHorizontal();

        if (
            !valid && !neighborState.isOf(this) &&
                !new Portal(world, pos, currentAxis, this.getFrameValidator()).wasAlreadyValid()
        ) {
                return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }

        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean moving) {
        this.transportEntity(entity, pos);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.get(AXIS)) {
                case X -> state.with(AXIS, Axis.Z);
                case Z -> state.with(AXIS, Axis.X);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return super.getPolymerBlockState(state, context)
            .with(AXIS, state.get(AXIS));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}
