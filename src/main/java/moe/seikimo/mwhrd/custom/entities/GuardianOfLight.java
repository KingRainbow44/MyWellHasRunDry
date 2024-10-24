package moe.seikimo.mwhrd.custom.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import moe.seikimo.mwhrd.custom.entities.goals.GuardianAttackGoal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public final class GuardianOfLight extends HostileEntity implements PolymerEntity {
    private long ticksAlive = 0L;
    private boolean toweringUp = false;

    public GuardianOfLight(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @return An attribute container for the entity's default attributes.
     */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 150d)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.7d)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14d)
            .add(EntityAttributes.GENERIC_ARMOR, 10d)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35d);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new GuardianAttackGoal(this));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 16f));
        this.goalSelector.add(8, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.WITHER_SKELETON;
    }

    @Override
    protected Box getAttackBox() {
        Box box3;
        var entity = this.getVehicle();
        if (entity != null) {
            Box box = entity.getBoundingBox();
            Box box2 = this.getBoundingBox();
            box3 = new Box(Math.min(box2.minX, box.minX), box2.minY, Math.min(box2.minZ, box.minZ), Math.max(box2.maxX, box.maxX), box2.maxY, Math.max(box2.maxZ, box.maxZ));
        } else {
            box3 = this.getBoundingBox();
        }
        return box3.expand(2, 1, 2);
    }

    @Override
    public void tick() {
        this.ticksAlive++;
        this.tryBuild();
        super.tick();
    }

    @Override
    public void baseTick() {
        super.baseTick();

        this.setAir(this.getMaxAir());
    }

    /**
     * Attempts to break the block at the given position.
     * @param block The position of the block to break.
     */
    private void tryToBreak(BlockPos block) {
        var blockState = this.getWorld().getBlockState(block);
        if (blockState.getBlock().getHardness() <= -1f) return;

        this.getWorld().breakBlock(block, false, this); // Break the block.
    }

    /**
     * Attempts to place a block at the target position.
     * @param target The position to place the block at.
     * @param block The block to place.
     */
    private void tryToPlace(BlockPos target, BlockState block) {
        var blockState = this.getWorld().getBlockState(target);
        if (blockState.getBlock().getHardness() <= -1f) return;

        this.getWorld().setBlockState(target, block); // Place the block.
    }

    /**
     * Validates the mob's position to build up.
     * @return True if the mob is in a valid position to build up.
     */
    private boolean isNearby() {
        // Check if the mob is within 10 blocks of the target horizontally.
        var target = this.getTarget();
        if (target == null) return false;

        var targetPos = target.getBlockPos().withY(0);
        var mobPos = this.getBlockPos().withY(0);
        return mobPos.getSquaredDistance(targetPos) <= 10;
    }

    /**
     * Applies velocity to mimic jumping.
     */
    private void velocityJump() {
        var currentVelocity = this.getVelocity();
        this.setVelocity(
            currentVelocity.x,
            this.getJumpVelocity() + 0.05f,
            currentVelocity.z);
    }

    /**
     * Returns the mob's target block.
     * @return A block coordinate.
     */
    private BlockPos getTargetBlock() {
        var direction = this.getHorizontalFacing();
        var x = (int) this.getX() + direction.getOffsetX();
        var y = (int) this.getY() + direction.getOffsetY();
        var z = (int) this.getZ() + direction.getOffsetZ();
        return new BlockPos(x, y, z);
    }

    /**
     * Attempts to build towards the target.
     */
    private void tryBuild() {
        var target = this.getTarget();
        if (target == null) return;

        var world = this.getWorld();

        // Check if the mob should tower up.
        if (this.toweringUp && this.ticksAlive % 10 == 0) {
            var upBlock1 = world.getBlockState(this.getBlockPos().up()).getBlock(); // Get the block above the mob.
            var upBlock2 = world.getBlockState(this.getBlockPos().up(1)).getBlock(); // Get the block above the mob.
            if (upBlock1 == Blocks.AIR && upBlock2 == Blocks.AIR) {
                this.velocityJump(); // Make the mob jump.
                this.tryToPlace(this.getBlockPos().down(), // Place a block below the mob.
                    Blocks.DIRT.getDefaultState());

                // Check if the mob is within distance.
                if (world.getClosestPlayer(this, 6) == null) return;
                // Check if the mob should explode.
                if (this.random.nextInt(50) == 0 && toweringUp) {
                    // Create a non-breaking explosion.
                    world.createExplosion(this, this.getX(), this.getY(), this.getZ(),
                        2f, World.ExplosionSourceType.MOB);
                }
            } else {
                this.tryToBreak(this.getBlockPos().up(1));
                this.tryToBreak(this.getBlockPos().up(2));
            }
        }

        // Determine if the mob should attack the target.
        if (this.random.nextInt(20) != 0 ||
            this.ticksAlive % 20 == 0) return;

        // Check if the mob is near the target.
        if (this.isInAttackRange(target)) return;

        // Determine the direction the mob should attack.
        if (target.getY() == this.getY()) { // Mob should attack horizontally.
            if (this.toweringUp) this.toweringUp = false;

            // Determine if the mob should break blocks in front of it.
            var position = this.getTargetBlock();

            // Check if the targeted block is air.
            var block = world.getBlockState(position);
            if (!block.isAir()) this.tryToBreak(position); // If the targeted block is not air, break it.
            // Check if the block below the target is air.
            var belowBlock = world.getBlockState(position.up());
            if (!belowBlock.isAir()) this.tryToBreak(position.up()); // If the block below the target is not air, break it.

            // Get the direction the mob should bridge towards.
            var targetBlock = this.getTargetBlock().down();

            // Check if the targeted block is air.
            var targetBlockState = world.getBlockState(position);
            if (targetBlockState.isAir()) {
                // If the targeted block is air, place a block.
                this.tryToPlace(targetBlock, world.getBlockState(this.getBlockPos().down(1)));
            }
        } else if (target.getY() > this.getY()) { // Mob should attempt to elevate.
            // Determine if the mob is surrounded by blocks.
            var upBlock = world.getBlockState(this.getBlockPos().up());
            var downBlock = world.getBlockState(this.getBlockPos().down());
            if (!upBlock.isAir() && !downBlock.isAir()) { // Surrounded by blocks.
                // Get the current block.
                var currentBlock = this.getBlockPos();
                // Mob should attempt to pillar up.
                this.tryToBreak(this.getBlockPos().up(1)); // Attempt to break the block above the mob.
                this.velocityJump(); // Jump.
                this.tryToPlace(currentBlock, world.getBlockState(currentBlock.down())); // Set the current block to the block below.
            } else if (this.isNearby()) { // Not surrounded by blocks.
                this.toweringUp = true; // Set the mob to tower up.
            }
        } else { // Mob should attempt to descend.
            if (this.toweringUp) this.toweringUp = false;

            // Check if there is ground below the block the mob is standing on.
            var belowBlock = world.getBlockState(this.getBlockPos().down());
            if (!belowBlock.isAir()) {
                // If there is no ground below the block the mob is standing on, break the block below the mob.
                this.tryToBreak(this.getBlockPos().down());
            }
        }
    }
}
