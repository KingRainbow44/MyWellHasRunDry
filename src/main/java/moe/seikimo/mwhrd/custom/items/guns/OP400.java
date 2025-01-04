package moe.seikimo.mwhrd.custom.items.guns;

import moe.seikimo.mwhrd.custom.CustomComponents;
import moe.seikimo.mwhrd.custom.components.GunComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public final class OP400 extends BaseGun {
    public OP400(Settings settings) {
        super(
            settings
                .rarity(Rarity.EPIC),
            new GunComponent(31, 1, 500, 30, 75),
            Items.SPYGLASS
        );
    }

    @Override
    public float getDamage(ItemStack stack, int distance) {
        return 31;
    }

    @Override
    public SoundEvent getFireSound() {
        return SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE;
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.END_ROD;
    }

    @Override
    public double getImpactRange() {
        return 0.2d;
    }

    @Override
    public float getCritMultiplier() {
        return 8f;
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
        user.setGlowing(false);
        return super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        user.setGlowing(false);
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (
            user instanceof ServerPlayerEntity player &&
                world instanceof ServerWorld serverWorld &&
                (
                    remainingUseTicks <= 1 ||
                        (player.isSneaking() && remainingUseTicks < 42)
                )
        ) {
            this.shoot(serverWorld, player, stack);
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return user instanceof PlayerEntity player && player.isSneaking() ? 8 : 50;
    }
}
