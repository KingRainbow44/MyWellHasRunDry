package moe.seikimo.mwhrd.custom.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import moe.seikimo.mwhrd.custom.CustomEntities;
import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.interfaces.player.IDeepPlayer;
import moe.seikimo.mwhrd.utils.Maths;
import moe.seikimo.mwhrd.utils.Utils;
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
import net.minecraft.registry.tag.FluidTags;
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

    /** Fishing bobber position state. */
    private State state = State.FLYING;
    /** The entity the bobber is hooked to. */
    private Entity hookedTo = null;
    /** The entity will be discarded after this time. */
    private long discardTimer = 0;

    public CelestialFishingBobberEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public CelestialFishingBobberEntity(PlayerEntity caster, World world) {
        this(CustomEntities.CELESTIAL_FISHING_BOBBER, world);

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
                // TODO: Check if in open water.

                if (inWater) {
                    // TODO: Update water ticks.
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
