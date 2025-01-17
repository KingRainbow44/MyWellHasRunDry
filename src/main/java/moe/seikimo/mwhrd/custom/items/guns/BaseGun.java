package moe.seikimo.mwhrd.custom.items.guns;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomComponents;
import moe.seikimo.mwhrd.custom.CustomDamageSources;
import moe.seikimo.mwhrd.custom.components.GunComponent;
import moe.seikimo.mwhrd.custom.components.ReloadComponent;
import moe.seikimo.mwhrd.custom.interfaces.SwingHandListener;
import moe.seikimo.mwhrd.interfaces.player.IGunWielder;
import moe.seikimo.mwhrd.utils.Attributes;
import moe.seikimo.mwhrd.utils.GUI;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGun
    extends SimplePolymerItem
    implements SwingHandListener {
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

    public BaseGun(
        Settings settings, GunComponent gunData, Item clientItem
    ) {
        super(
            settings
                .maxDamage(100)
                .component(
                    DataComponentTypes.ATTRIBUTE_MODIFIERS,
                    new AttributeModifiersComponent(
                        List.of(
                            new AttributeModifiersComponent.Entry(
                                EntityAttributes.ATTACK_SPEED,
                                Attributes.add(Identifier.of(MyWellHasRunDry.MOD_ID, "gun_atk_spd"), 34f),
                                AttributeModifierSlot.MAINHAND
                            )
                        ),
                        false
                    )
                )
                .component(
                    CustomComponents.GUN,
                    gunData
                ),
            clientItem
        );
    }

    /**
     * @return The range of the beam (in blocks).
     */
    public int getRange(ItemStack stack) {
        var gunData = stack.get(CustomComponents.GUN);
        if (gunData == null) return 0;

        return gunData.range();
    }

    /**
     * Computes the damage at a certain distance.
     *
     * @param entity The entity to compute the damage for.
     * @param distance The distance to compute the damage at.
     * @return The damage at the given distance.
     */
    public float getDamage(LivingEntity entity, ItemStack stack, int distance) {
        var gunData = stack.get(CustomComponents.GUN);
        if (gunData == null) return 0;

        return gunData.baseDamage() - distance / 2.5f;
    }

    public boolean isRapidFire() {
        return false;
    }

    /**
     * @return The sound that gets played when a bullet is fired.
     */
    public SoundEvent getFireSound() {
        return SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;
    }

    /**
     * NOTE: This value more controls the 'speed' rather than the 'pitch' of a noise.
     *
     * @return The pitch of the fire sound.
     */
    public float getFirePitch() {
        return 2f;
    }

    /**
     * @return The particle to spawn at each step.
     */
    public SimpleParticleType getParticle() {
        return ParticleTypes.ELECTRIC_SPARK;
    }

    /**
     * @return The item used for ammo.
     */
    public Item getMagazineItem() {
        return Items.GHAST_TEAR;
    }

    /**
     * @return The amount of ammo per item.
     */
    public int getMagazineCount() {
        return 1;
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
    public double getImpactRange() {
        return 0.4d;
    }

    /**
     * @return The amount to multiply the damage by when a critical hit is scored.
     */
    public float getCritMultiplier() {
        return 2f;
    }

    /**
     * Invoked when a bullet reaches a block.
     *
     * @param stack The item stack of the bullet.
     * @param iteration The iteration of the bullet. (usually blocks)
     * @param shooter The entity that shot the bullet.
     * @param position The bullet's position.
     * @param world The world the bullet is in.
     * @return Whether the bullet should stop.
     */
    public boolean bulletImpact(
        ItemStack stack,
        int iteration,
        PlayerEntity shooter,
        Vec3d position,
        ServerWorld world
    ) {
        var blockPos = Utils.blockPos(position);

        // Check for entities within the impact range.
        for (var entity : BaseGun.otherEntities(world, shooter, position, this.getImpactRange())) {
            var damage = this.getDamage(entity, stack, iteration);
            var damageSource = CustomDamageSources.railgunExplosion(world, shooter);

            // Check if the collision was within the head range of the entity.
            var headBox = Box.of(entity.getEyePos(), 0.6f, 0.6f, 0.6f);
            var bulletBox = Box.of(position, 0.1f, 0.1f, 0.1f);

            if (headBox.intersects(bulletBox)) {
                damage *= this.getCritMultiplier();
            }

            entity.damage(world, damageSource, damage);
            entity.timeUntilRegen = 2;
            return true;
        }

        // If the bullet is on a wall, stop it.
        var state = world.getBlockState(blockPos);
        if (state.isTransparent()) {
            world.breakBlock(blockPos, true);
        }

        if (state.getHardness(world, blockPos) < 5 &&
            Utils.random(0, 5) == 0) {
            world.breakBlock(blockPos, false);
        }

        return !state.isTransparent();
    }

    /**
     * Draws particles in a line from the player's face.
     *
     * @param stack The item stack to draw the particles for.
     * @param player The player to draw the particles for.
     * @param world The world to draw the particles in.
     */
    public void bulletLoop(ItemStack stack, ServerPlayerEntity player, ServerWorld world) {
        var offset = player.getRotationVector().multiply(0.1f);
        var position = player.getEyePos().add(offset);

        // Play the shoot sound effect.
        world.playSound(
            null,
            player.getBlockPos(),
            this.getFireSound(),
            SoundCategory.PLAYERS,
            1.0f, this.getFirePitch()
        );

        var stopPosition = player.getBlockPos();
        for (var i = 0; i < this.getRange(stack) * 10; i++) {
            // Offset the position.
            position = position.add(offset);

            // Spawn a particle at the position.
            world.spawnParticles(
                this.getParticle(),
                position.getX(), position.getY(), position.getZ(),
                this.getParticleCount(), 0, 0, 0, 0
            );

            // Check if the bullet should stop.
            if (this.bulletImpact(stack, i / 10, player, position, world)) {
                break;
            }
        }

        // Play the impact sound.
        world.playSound(
            null,
            stopPosition,
            SoundEvents.BLOCK_STONE_BREAK,
            SoundCategory.BLOCKS,
            1.0f, 1.0f
        );
    }

    /**
     * Method to shoot a bullet from the gun.
     *
     * @param world The world to shoot the bullet in.
     * @param player The player that is shooting the bullet.
     * @param stack The item stack of the gun.
     */
    public ActionResult shoot(ServerWorld world, ServerPlayerEntity player, ItemStack stack) {
        if (!(player instanceof IGunWielder gunWielder)) return ActionResult.PASS;

        // The player must not be in an existing cooldown.
        if (!gunWielder.mwhrd$canFire()) return ActionResult.PASS;

        // Get item stack data.
        var gunData = stack.getOrDefault(CustomComponents.GUN, GunComponent.EMPTY);
        var reloadData = stack.getOrDefault(CustomComponents.GUN_RELOAD, ReloadComponent.EMPTY);
        var bullets = stack.getOrDefault(CustomComponents.GUN_BULLETS, 0);

        // Check if the gun is reloading, or if the gun is empty.
        if (reloadData.reloading() || bullets <= 0) {
            return ActionResult.PASS;
        }

        // Subtract a bullet.
        stack.set(CustomComponents.GUN_BULLETS, bullets - 1);

        var progress = 1 - (float) (bullets - 1) / gunData.maxAmmo();
        var displayedDamage = Math.round(Math.clamp(progress * 100, 0, 100));
        stack.set(DataComponentTypes.DAMAGE, displayedDamage);

        // Run the bullet loop.
        this.bulletLoop(stack, player, world);
        // Set the cooldown.
        gunWielder.mwhrd$setCooldown(gunData.fireRate());

        return ActionResult.SUCCESS;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
        // Modify lore component.
        var lore = new ArrayList<Text>();

        var component = out.get(DataComponentTypes.LORE);
        if (component != null) {
            lore.addAll(component.lines());
        }

        // Read the components.
        var gunData = stack.get(CustomComponents.GUN);
        if (gunData == null) return;

        // Append lines relating to the gun statistics.
        lore.addAll(List.of(
            Text.empty(),
            Text.translatable("item.modifiers.mainhand")
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.GRAY),
            Text.translatable("text.mwhrd.gun.base_damage", gunData.baseDamage())
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.DARK_GREEN),
            Text.translatable("text.mwhrd.gun.fire_rate", gunData.fireRate() * 0.05f)
                .setStyle(GUI.CLEAR)
                .formatted(Formatting.DARK_GREEN)
        ));

        // Set the lore lines.
        out.set(
            DataComponentTypes.LORE,
            new LoreComponent(lore)
        );
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (this.isRapidFire()) {
            return ItemUsage.consumeHeldItem(world, user, hand);
        } else {
            if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;
            if (!(user instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            // Get the item stack being used.
            var stack = user.getStackInHand(hand);

            return this.shoot(serverWorld, serverPlayer, stack);
        }
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ActionResult.FAIL;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.FAIL;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (this.isRapidFire() &&
            user instanceof ServerPlayerEntity player &&
            world instanceof ServerWorld serverWorld) {
            this.shoot(serverWorld, player, stack);
        }
    }

    @Override
    public void onSwingHand(ItemStack stack, World world, PlayerEntity player, Hand hand) {
        // Get the backing data behind the gun.
        var gunData = stack.getOrDefault(CustomComponents.GUN, GunComponent.EMPTY);
        var bullets = stack.getOrDefault(CustomComponents.GUN_BULLETS, 0);

        // Check if the gun is already reloading.
        var reloadData = stack.getOrDefault(
            CustomComponents.GUN_RELOAD, ReloadComponent.EMPTY);
        if (reloadData.reloading() || gunData.maxAmmo() == bullets) {
            return;
        }

        // Check if ammo is available to reload the gun.
        var ammoItem = this.getMagazineItem();
        var perAmmo = this.getMagazineCount();

        var itemCount = player.getInventory().count(ammoItem);
        if (itemCount == 0) {
            return;
        }

        var ammoInInv = perAmmo * itemCount;
        var newAmmo = Math.min(gunData.maxAmmo(), bullets + ammoInInv);
        var itemsNeeded = Math.ceilDiv(gunData.maxAmmo() - bullets, perAmmo);

        // Subtract the ammo from the player's inventory.
        player.getInventory().remove(
            s -> s.getItem() == ammoItem,
            itemsNeeded,
            player.playerScreenHandler.getCraftingInput()
        );

        // Reload the gun.
        stack.set(CustomComponents.GUN_RELOAD, ReloadComponent.of(gunData.reloadTime(), newAmmo));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        // Check if the gun is reloading.
        var gunData = stack.getOrDefault(
            CustomComponents.GUN, GunComponent.EMPTY);
        var reloadData = stack.getOrDefault(
            CustomComponents.GUN_RELOAD, ReloadComponent.EMPTY);

        if (!reloadData.reloading()) {
            return;
        }

        // Continue reloading the gun.
        reloadData = reloadData.minus();
        stack.set(CustomComponents.GUN_RELOAD, reloadData);

        // Update the durability progress.
        var remainingTicks = reloadData.remainingTicks();
        var maxTicks = gunData.reloadTime();

        var progress = 1 - (float) remainingTicks / maxTicks;
        var displayedDamage = Math.round(Math.clamp(progress * 100, 0, 100));
        stack.set(DataComponentTypes.DAMAGE, 100 - displayedDamage);

        // If the gun is fully reloaded, set the bullets.
        if (!reloadData.reloading()) {
            var newAmmo = gunData.maxAmmo();
            if (reloadData.newCount() != -1) {
                newAmmo = reloadData.newCount();
            }

            stack.set(CustomComponents.GUN_BULLETS, newAmmo);
        }
    }
}
