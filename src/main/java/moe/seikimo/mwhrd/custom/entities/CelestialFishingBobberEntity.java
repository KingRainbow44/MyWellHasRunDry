package moe.seikimo.mwhrd.custom.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import moe.seikimo.mwhrd.custom.CustomEntities;
import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.custom.components.RodComponent;
import moe.seikimo.mwhrd.interfaces.player.IDeepPlayer;
import moe.seikimo.mwhrd.utils.Maths;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public final class CelestialFishingBobberEntity extends ProjectileEntity implements PolymerEntity {
    private static final TrackedData<Boolean> CAUGHT_FISH = DataTracker.registerData(CelestialFishingBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> HOOK_ENTITY_ID = DataTracker.registerData(CelestialFishingBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /**
     * This is the default value defined by Minecraft for the maximum distance a fishing bobber can be cast.
     */
    private static final double MAX_DISTANCE = 1024d;

    /**
     * The time in ticks after which the fishing bobber will be discarded if it is not reeled in.
     * This is set to 1200 ticks, which is equivalent to 60 seconds (1 minute).
     */
    private static final long DISCARD_TIME = 1200L;

    /** Random number generator used for velocity ONLY. */
    private final Random velocityRandom = Random.create();

    private final float durability, strength, quantity;

    /** Fishing bobber position state. */
    private State state = State.FLYING;
    /** The entity the bobber is hooked to. */
    private Entity hookedTo = null;

    /** The entity will be discarded after this time. */
    private long discardTimer = 0;
    /** The amount of ticks that the bobber has been out of open water. */
    private int waterTicks = 0;
    /** The amount of ticks before the 'caught' fish expires. */
    private int hookCountdown = 0;
    /** The amount of ticks before the fish is 'caught'. */
    private int travelCountdown = 0;
    /** The amount of ticks before a fish is placed into the ocean. */
    private int waitCountdown = 0;

    /** The fish angle is used for displaying particles in the world. */
    private float fishAngle = 0f;

    /** Whether the bobber is in open water or not. */
    private boolean inOpenWater = true;
    /** Whether the bobber has a fish or not. */
    private boolean caughtFish = false;

    public CelestialFishingBobberEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);

        // Set default component values.
        // These are unused in this constructor.
        this.durability = 0;
        this.strength = 0;
        this.quantity = 1;
    }

    public CelestialFishingBobberEntity(PlayerEntity caster, RodComponent component, World world) {
        super(CustomEntities.CELESTIAL_FISHING_BOBBER, world);

        // Set the component values.
        this.durability = component.durability();
        this.strength = component.strength();
        this.quantity = component.quantity();

        this.setOwner(caster);
        this.initPosVelocity(caster);
    }

    /**
     * Sets the initial position of the bobber.
     *
     * @param caster The player who cast the fishing bobber.
     */
    private void initPosVelocity(PlayerEntity caster) {
        var pitch = caster.getPitch();
        var yaw = caster.getYaw();
        var h = MathHelper.cos(-yaw * ((float)Math.PI / 180) - (float)Math.PI);
        var i = MathHelper.sin(-yaw * ((float)Math.PI / 180) - (float)Math.PI);

        // Set the position.
        this.refreshPositionAndAngles(
            caster.getX() - (i * 0.3d),
            caster.getEyeY(),
            caster.getZ() - (h * 0.3d),
            caster.getYaw(),
            caster.getPitch()
        );

        var j = -MathHelper.cos(-pitch * ((float)Math.PI / 180));
        var k = MathHelper.sin(-pitch * ((float)Math.PI / 180));

        var velocity = new Vec3d(-i, MathHelper.clamp(-(k / j), -5f, 5f), -h);
        // "this random constant is taken from fishing rods..."
        var a = this.random.nextTriangular(0.5, 0.0103365);
        var b = this.random.nextTriangular(0.5, 0.0103365);
        var c = this.random.nextTriangular(0.5, 0.0103365);
        // "end random constant"
        velocity = velocity.multiply(
            0.6 / velocity.length() + a,
            0.6 / velocity.length() + b,
            0.6 / velocity.length() + c
        );

        // Set the velocity.
        this.setVelocity(velocity);

        // Update yaw and pitch.
        this.setYaw((float) (MathHelper.atan2(velocity.x, velocity.z) * Maths.RAD_TO_DEG));
        this.setPitch((float) (MathHelper.atan2(velocity.y, velocity.horizontalLength()) * Maths.RAD_TO_DEG));
        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();
    }

    /**
     * @return The player who owns this fishing bobber, or null if it is not owned by a player.
     */
    public PlayerEntity getPlayerOwner() {
        return this.getOwner() instanceof PlayerEntity playerEntity ? playerEntity : null;
    }

    /**
     * Invoked when the bobber is reeled in.
     *
     * @param itemStack The item stack used to reel in the fishing bobber, typically a fishing rod.
     * @return The durability used on the item.
     */
    public int use(ItemStack itemStack) {
        var durability = 0;
        var player = this.getPlayerOwner();

        // Check if the call is invalid.
        if (player == null || this.removeIfInvalid(player)) {
            return durability; // Invalid call, no durability used.
        }

        // Check if the bobber caught anything.
        // TODO: Check catch.

        // If the bobber hit the ground, reduce durability.
        if (this.isOnGround()) {
            durability = 2;
        }

        // Discard the bobber and return.
        this.discard();
        return durability;
    }

    /**
     * Removes the bobber entity if its invalid.
     *
     * @param player The player who owns the fishing bobber.
     * @return True if the bobber was removed, false otherwise.
     */
    private boolean removeIfInvalid(PlayerEntity player) {
        var mainStack = player.getMainHandStack();
        var offStack = player.getOffHandStack();

        // Validate the state of the bobber entity.
        var playerExists = !player.isRemoved() && player.isAlive();
        var holdingRod = mainStack.isOf(CustomItems.CELESTIAL_LINE) || offStack.isOf(CustomItems.CELESTIAL_LINE);
        var closeEnough = this.squaredDistanceTo(player) <= MAX_DISTANCE;
        if (!playerExists || !holdingRod || !closeEnough) {
            this.discard();
            return true;
        }

        return false;
    }

    /**
     * Checks if the bobber has collided with any entities.
     */
    private void checkForCollisions() {
        this.hitOrDeflect(ProjectileUtil.getCollision(this, this::canHit));
    }

    /**
     * Sets the hooked entity for the fishing bobber.
     *
     * @param entity The entity to hook the fishing bobber to, or null to unhook.
     */
    private void setHookedEntity(@Nullable Entity entity) {
        this.hookedTo = entity;
        this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
    }

    /**
     * Checks between the start and end positions and
     * determines the position type of the bobber.
     *
     * @param start The start block.
     * @param end The end block.
     * @return The position type of the bobber.
     */
    private PositionType getPositionType(BlockPos start, BlockPos end) {
        return BlockPos.stream(start, end)
            .map(this::getPositionType)
            .reduce((t1, t2) -> t1 == t2 ? t1 : PositionType.INVALID)
            .orElse(PositionType.INVALID);
    }

    /**
     * Determines the position type from a single block.
     *
     * @param target The target block position to check.
     * @return The position type of the target block.
     */
    private PositionType getPositionType(BlockPos target) {
        // Check if the block is air or a lily pad.
        var state = this.getWorld().getBlockState(target);
        if (state.isAir() || state.isOf(Blocks.LILY_PAD)) {
            return PositionType.ABOVE_WATER;
        }

        // Check if the block is still water.
        var fluid = state.getFluidState();
        if (fluid.isIn(FluidTags.WATER) && fluid.isStill() &&
            state.getCollisionShape(this.getWorld(), target).isEmpty()) {
            return PositionType.INSIDE_WATER;
        }

        return PositionType.INVALID;
    }

    /**
     * Checks if the block has nearby open water.
     *
     * @param blockPos The position of the block to check.
     * @return True if there is open water nearby, false otherwise.
     */
    private boolean hasOpenWater(BlockPos blockPos) {
        var type = PositionType.INVALID;

        // Resolve the position type.
        for (var i = -1; i < 3; i++) {
            var target = this.getPositionType(
                blockPos.add(-2, i, -2),
                blockPos.add(2, i, 2)
            );

            switch (target) {
                case ABOVE_WATER -> {
                    if (type != PositionType.INVALID) break;
                    return false;
                }
                case INSIDE_WATER -> {
                    if (type != PositionType.ABOVE_WATER) break;
                    return false;
                }
                case INVALID -> {
                    return false;
                }
            }
            type = target;
        }

        return true;
    }

    /**
     * Contains all the logic required for fishing.
     */
    private void fishingTick(BlockPos blockPos) {
        var world = (ServerWorld) this.getWorld();
        var nextBlock = blockPos.up();

        // Determine the speed of the bobber.
        // This is determined by a few things:
        // 1. If the bobber is exposed to rain.
        // 2. If the bobber is exposed to skylight.
        // 3. If the bobber has attributes.
        var speed = 1;
        if (this.random.nextFloat() < 0.25f && world.hasRain(nextBlock)) {
            speed++; // Increase speed if there is rain at the block.
        }
        if (this.random.nextFloat() < 0.5f && !world.isSkyVisible(nextBlock)) {
            speed--; // Decrease speed if there is no visible skylight at the block.
        }
        if (this.random.nextFloat() < 0.75f && this.strength > 0) {
            speed += (int) Math.floor(Math.min(2, this.strength)); // Increase speed based on the strength of the fishing rod.
        }

        // Check if the catch is going to expire.
        if (this.hookCountdown > 0) {
            this.hookCountdown--;

            // If the timer has expired...
            if (this.hookCountdown <= 0) {
                // ...remove the fish from the bobber.
                this.waitCountdown = 0;
                this.travelCountdown = 0;

                this.getDataTracker().set(CAUGHT_FISH, false);
            }
        }
        // else, if the bobber is traveling to the bobber...
        else if (this.travelCountdown > 0) {
            this.travelCountdown -= speed;

            // If the timer is above 0, show the travel path.
            if (this.travelCountdown > 0) {
                this.fishAngle += this.random.nextTriangular(0f, 9.188f);

                var angle = (float) (this.fishAngle * (Math.PI / 180f));
                var sin = MathHelper.sin(angle);
                var cos = MathHelper.cos(angle);

                // Calculate the position.
                var x = this.getX() + (double) (sin * this.travelCountdown * .1f);
                var y = MathHelper.floor(this.getY()) + 1f;
                var z = this.getZ() + (double) (cos * this.travelCountdown * .1f);

                // Spawn the particles.
                var state = world.getBlockState(BlockPos.ofFloored(x, y - 1, z));
                if (!state.isOf(Blocks.WATER)) return;

                world.spawnParticles(
                    ParticleTypes.FISHING,
                    x, y, z, 0,
                    cos * 0.04f, 0.01, -sin * 0.04f,
                    1.0f
                );
                world.spawnParticles(
                    ParticleTypes.FISHING,
                    x, y, z, 0,
                    -cos * 0.04f, 0.01, sin * 0.04f,
                    1.0f
                );
            }
            // Otherwise, mark the bobber with a fish.
            else {
                // Play the catch sound.
                this.playSound(
                    SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
                    0.25f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f
                );

                // Spawn the particles to indicate the fish catch.
                var y = this.getY() + 0.5d;
                world.spawnParticles(
                    ParticleTypes.BUBBLE,
                    this.getX(), y, this.getZ(),
                    (int) (1.0f + this.getWidth() * 20.0f),
                    this.getWidth(), 0.0, this.getWidth(),
                    0.2f
                );
                world.spawnParticles(
                    ParticleTypes.FISHING,
                    this.getX(), y, this.getZ(),
                    (int) (1.0f + this.getWidth() * 20.0f),
                    this.getWidth(), 0.0, this.getWidth(),
                    0.2f
                );

                // This gives the player between 1-2s to reel in the fish.
                this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);

                this.getDataTracker().set(CAUGHT_FISH, true);
            }
        }
        // else, if we are waiting to catch a fish...
        else if (this.waitCountdown > 0) {
            this.waitCountdown -= speed;

            // Check if we should spawn particles.
            // It depends on the wait countdown.
            var chance = 0.15f;
            if (this.waitCountdown < 20) {
                chance += (float) (20 - this.waitCountdown) * 0.05f;
            } else if (this.waitCountdown < 40) {
                chance += (float) (40 - this.waitCountdown) * 0.02f;
            } else if (this.waitCountdown < 60) {
                chance += (float) (60 - this.waitCountdown) * 0.01f;
            }

            // Try spawning the particles.
            if (this.random.nextFloat() < chance) {
                var rad = MathHelper.nextFloat(this.random, 0f, 360f) * ((float) Math.PI / 180);
                var deg = MathHelper.nextFloat(this.random, 25f, 60f);

                var x = this.getX() + (double) (MathHelper.sin(rad) * deg) * 0.1;
                var y = MathHelper.floor(this.getY()) + 1f;
                var z = this.getZ() + (double) (MathHelper.cos(rad) * deg) * 0.1;

                // Spawn the particles.
                var state = world.getBlockState(BlockPos.ofFloored(x, y - 1, z));
                if (state.isOf(Blocks.WATER)) {
                    world.spawnParticles(
                        ParticleTypes.FISHING,
                        x, y, z,
                        2 + this.random.nextInt(2),
                        0.1f, 0.0, 0.1f, 0
                    );
                }
            }

            // If the wait countdown has expired, we can start the travel countdown.
            if (this.waitCountdown <= 0) {
                // Update the angle.
                this.fishAngle = MathHelper.nextFloat(this.random, 0f, 360f);
                // Set the travel countdown to a random value between 20 and 40 ticks.
                this.travelCountdown = MathHelper.nextInt(this.random, 20, 80);
            }
        }
        // Otherwise, decrease the wait timer.
        else {
            this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
            // TODO: Do we need reduction countdowns?
        }
    }

    @Override
    public void tick() {
        // Use the velocity of the client to ensure no de-sync.
        var seed = this.getUuid().getLeastSignificantBits() ^ this.getWorld().getTime();
        this.velocityRandom.setSeed(seed);

        super.tick();

        // Check for valid owner.
        var owner = this.getPlayerOwner();
        if (owner == null) {
            this.discard(); // No owner? No fishing bobber.
            return;
        }

        // Try removing the entity.
        if (this.removeIfInvalid(owner)) {
            return;
        }

        // Run discard routine.
        if (this.isOnGround()) {
            if (++this.discardTimer >= DISCARD_TIME) {
                this.discard();
                return;
            }
        } else {
            // Reset the discard timer.
            this.discardTimer = 0;
        }

        var blockPos = this.getBlockPos();
        var fluid = this.getWorld().getFluidState(blockPos);

        // Check if the bobber is in water.
        var waterHeight = 0f;
        if (fluid.isIn(FluidTags.WATER)) {
            waterHeight = fluid.getHeight(this.getWorld(), blockPos);
        }
        var inWater = waterHeight > 0f;

        // Update the state/position of the bobber.
        switch (this.state) {
            case FLYING -> {
                // If we are hooked to an entity, do nothing.
                // (we only reset the velocity)
                if (this.hookedTo != null) {
                    this.setVelocity(Vec3d.ZERO);
                    this.state = State.HOOKED_IN_ENTITY;
                    return;
                }

                // If we are in water, we should bob up.
                if (inWater) {
                    this.setVelocity(this.getVelocity().multiply(0.3d, 0.2d, 0.3d));
                    this.state = State.BOBBING;
                    return;
                }

                // Otherwise, try colliding.
                this.checkForCollisions();
            }
            case HOOKED_IN_ENTITY -> {
                var entity = this.hookedTo;

                // Ignore if we aren't hooked to an entity in this state.
                if (entity == null) {
                    return;
                }

                // Validate the hooked entity.
                // We check if they are valid & if they are in the same world as the bobber.
                if (entity.isRemoved() || !Utils.compare(this.getWorld(), entity.getWorld())) {
                    this.state = State.FLYING;
                    this.setHookedEntity(null);
                } else {
                    // Update the position to the entity's position.
                    this.setPosition(
                        entity.getX(),
                        entity.getBodyY(0.8),
                        entity.getZ()
                    );
                }

                // We skip any logic either way.
                return;
            }
            case BOBBING -> {
                var velocity = this.getVelocity();

                // Calculate the new Y velocity.
                var yModifier = this.getY() + velocity.y - blockPos.getY() - waterHeight;
                if (Math.abs(yModifier) < 0.01d) {
                    yModifier += Math.signum(yModifier) * 0.1;
                }

                // Set the new velocity.
                this.setVelocity(
                    velocity.x * 0.9,
                    velocity.y - yModifier * this.random.nextFloat() * 0.2,
                    velocity.z * 0.9
                );

                // Update open water status.
                this.inOpenWater = this.hookCountdown <= 0 && this.travelCountdown <= 0 ||
                    this.inOpenWater && this.waterTicks < 10 && this.hasOpenWater(blockPos);

                if (inWater) {
                    // Decrease the needs in water.
                    this.waterTicks = Math.max(0, this.waterTicks - 1);

                    // Check if the bobber has caught something.
                    if (this.caughtFish) {
                        // Show the pull 'animation'.
                        this.setVelocity(this.getVelocity().add(
                            0.0,
                            -0.1d * this.velocityRandom.nextFloat() * this.velocityRandom.nextFloat(),
                            0.0
                        ));
                    }

                    // Run the fishing logic.
                    this.fishingTick(blockPos);
                } else {
                    this.waterTicks = Math.min(10, this.waterTicks + 1);
                }
            }
        }

        // If the bobber has left water, reduce the velocity.
        // Either case, this also ensures it falls at a linear rate.
        if (!fluid.isIn(FluidTags.WATER)) {
            this.setVelocity(this.getVelocity().add(0, -0.03d, 0));
        }
        this.move(MovementType.SELF, this.getVelocity());

        // Update the entity.
        this.tickBlockCollision();
        this.updateRotation();

        if (this.state == State.FLYING && (this.isOnGround() || this.horizontalCollision)) {
            this.setVelocity(Vec3d.ZERO);
        }

        this.setVelocity(this.getVelocity().multiply(0.92d));
        this.refreshPosition();
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);

        if (entity instanceof IDeepPlayer player) {
            player.mwhrd$setFishHook(this);
        }
    }

    @Override
    public void onRemoved() {
        if (this.getPlayerOwner() instanceof IDeepPlayer player) {
            player.mwhrd$setFishHook(null); // Clear the fishing bobber from the player.
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.getPlayerOwner() instanceof IDeepPlayer player) {
            player.mwhrd$setFishHook(null); // Clear the fishing bobber from the player.
        }

        super.remove(reason);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(HOOK_ENTITY_ID, 0);
        builder.add(CAUGHT_FISH, false);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (HOOK_ENTITY_ID.equals(data)) {
            var entityId = this.getDataTracker().get(HOOK_ENTITY_ID);
            this.setHookedEntity(entityId > 0 ?
                this.getWorld().getEntityById(entityId - 1) :
                null);
        }

        if (CAUGHT_FISH.equals(data)) {
            this.caughtFish = this.getDataTracker().get(CAUGHT_FISH);
            if (this.caughtFish) {
                this.setVelocity(
                    this.getVelocity().x,
                    -0.4f * MathHelper.nextFloat(this.velocityRandom, 0.6f, 1.0f),
                    this.getVelocity().z
                );
            }
        }

        super.onTrackedDataSet(data);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);

        if (this.getPlayerOwner() == null) {
            this.discard(); // No player? No fishing bobber.
        }
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.FISHING_BOBBER;
    }

    enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING
    }

    enum PositionType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }
}
