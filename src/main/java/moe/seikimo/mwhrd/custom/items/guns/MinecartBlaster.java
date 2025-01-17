package moe.seikimo.mwhrd.custom.items.guns;

import moe.seikimo.mwhrd.custom.CustomComponents;
import moe.seikimo.mwhrd.custom.CustomDamageSources;
import moe.seikimo.mwhrd.custom.components.GunComponent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public final class MinecartBlaster extends BaseGun {
    public MinecartBlaster(Settings settings) {
        super(
            settings
                .rarity(Rarity.RARE),
            new GunComponent(30, 1, 500, 0, 60),
            Items.TRIDENT
        );
    }

    @Override
    public SoundEvent getFireSound() {
        return SoundEvents.BLOCK_ANVIL_LAND;
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.WAX_ON;
    }

    @Override
    public Item getMagazineItem() {
        return Items.END_CRYSTAL;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 200;
    }

    @Override
    public boolean bulletImpact(ItemStack stack, int iteration, PlayerEntity shooter, Vec3d position, ServerWorld world) {
        var collision = super.bulletImpact(stack, iteration, shooter, position, world);
        if (!collision) return false;

        // Create an explosion at the impact location.
        world.createExplosion(
            shooter,
            CustomDamageSources.gunshot(world, shooter),
            null,
            position.getX(),
            position.getY(),
            position.getZ(),
            8f,
            false,
            World.ExplosionSourceType.MOB
        );

        return true;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        var ammo = itemStack.getOrDefault(CustomComponents.GUN_BULLETS, 0);

        // If the gun is not loaded, display a Ghast Tear.
        return ammo == 0 ? Items.GHAST_TEAR : super.getPolymerItem(itemStack, context);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var ammo = stack.getOrDefault(CustomComponents.GUN_BULLETS, 0);

        if (ammo <= 0) {
            return ActionResult.PASS;
        }

        user.setGlowing(true);
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        var remaining = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (remaining < 10) {
            return false;
        }

        if (
            user instanceof ServerPlayerEntity player &&
                world instanceof ServerWorld serverWorld
        ) {
            this.shoot(serverWorld, player, stack);
        }

        return true;
    }
}
