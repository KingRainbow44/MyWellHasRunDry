package moe.seikimo.mwhrd.custom.items.guns;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGun extends SimplePolymerItem {
    /**
     * Gets all entities within a certain range.
     *
     * @param world The world to search in.
     * @param except The entity to exclude.
     * @param source The source of the search.
     * @param range The range to search in.
     * @return A list of entities within the range.
     */
    protected static List<LivingEntity> otherEntities(
        ServerWorld world, LivingEntity except,
        Vec3d source, double range
    ) {
        var entities = new ArrayList<LivingEntity>();
        for (var entity : world.getOtherEntities(
            except,
            Box.of(source, range, range, range),
            entity -> entity instanceof LivingEntity
        )) {
            entities.add((LivingEntity) entity);
        }

        return entities;
    }

    /**
     * Checks if a point is contained in a box.
     *
     * @param check The box to check.
     * @param point The point to check.
     * @param expand The amount to expand the point by.
     * @return Whether the point is contained in the box.
     */
    protected static boolean containedIn(Box check, Vec3d point, float expand) {
        var min = new Vec3d(
            point.getX() - expand,
            point.getY() - expand,
            point.getZ() - expand
        );

        var max = new Vec3d(
            point.getX() + expand,
            point.getY() + expand,
            point.getZ() + expand
        );

        return check.contains(min) || check.contains(max);
    }

    public BaseGun(
        Settings settings, Item clientItem
    ) {
        super(
            settings
                .maxCount(1),
            clientItem
        );
    }

    /**
     * @return The range of the beam (in blocks).
     */
    public abstract int getRange();

    /**
     * Computes the damage at a certain distance.
     *
     * @param distance The distance to compute the damage at.
     * @return The damage at the given distance.
     */
    public abstract float getDamage(int distance);

    /**
     * @return The particle to spawn at each step.
     */
    public SimpleParticleType getParticle() {
        return ParticleTypes.ELECTRIC_SPARK;
    }

    /**
     * @return The amount of particles to spawn.
     */
    public int getParticleCount() {
        return 1;
    }

    /**
     * @return The impact range of the bullet.
     */
    private double getImpactRange() {
        return 0.4d;
    }

    /**
     * Invoked when a bullet reaches a block.
     *
     * @param iteration The iteration of the bullet. (usually blocks)
     * @param shooter The entity that shot the bullet.
     * @param position The bullet's position.
     * @param world The world the bullet is in.
     * @return Whether the bullet should stop.
     */
    public boolean bulletImpact(int iteration, LivingEntity shooter, Vec3d position, ServerWorld world) {
        // Create the damage source.
        var source = world.getDamageSources()
            .magic();

        // Check for entities within the impact range.
        for (var entity : BaseGun.otherEntities(world, shooter, position, this.getImpactRange())) {
            var damage = this.getDamage(iteration);

            // Check if the collision was within the head range of the entity.
            var headBox = Box.of(entity.getEyePos(), 0.75f, 0.75f, 0.75f);
            var bulletBox = Box.of(position, 0.1f, 0.1f, 0.1f);

            if (headBox.intersects(bulletBox)) {
                damage *= 2;
            }

            if (shooter instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("eye pos is %s".formatted(entity.getEyePos())));
                player.sendMessage(Text.literal("bullet pos is %s".formatted(position)));
                player.sendMessage(Text.literal("overlap? " + headBox.contains(position)));
                player.sendMessage(Text.literal("you dealt %f damage".formatted(damage)));
            }

            entity.damage(world, source, damage);
            return true;
        }

        // If the bullet is on a wall, stop it.
        var state = world.getBlockState(Utils.blockPos(position));
        if (!state.isTransparent()) {
            return true;
        }

        return false;
    }

    /**
     * Draws particles in a line from the player's face.
     *
     * @param player The player to draw the particles for.
     * @param world The world to draw the particles in.
     */
    public void bulletLoop(ServerPlayerEntity player, ServerWorld world) {
        var position = player.getEyePos();
        var offset = player.getRotationVector()
            .multiply(0.5f);

        // Add space between the player's face and the beam.
        position = position.add(offset);

        for (var i = 0; i < this.getRange(); i++) {
            // Offset the position.
            position = position.add(offset);

            // Spawn a particle at the position.
            world.spawnParticles(
                this.getParticle(),
                position.getX(), position.getY(), position.getZ(),
                this.getParticleCount(), 0, 0, 0, 0
            );

            // Check if the bullet should stop.
            if (this.bulletImpact(i, player, position, world)) {
                break;
            }
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;
        if (!(user instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

        this.bulletLoop(serverPlayer, serverWorld);

        return ActionResult.SUCCESS;
    }
}
