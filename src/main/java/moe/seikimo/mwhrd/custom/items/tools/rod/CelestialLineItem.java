package moe.seikimo.mwhrd.custom.items.tools.rod;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.custom.entities.CelestialFishingBobberEntity;
import moe.seikimo.mwhrd.interfaces.player.IDeepPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public final class CelestialLineItem extends SimplePolymerItem {
    public CelestialLineItem(Settings settings) {
        super(
            settings
                .rarity(Rarity.UNCOMMON)
                .maxDamage(128)
                .enchantable(1)
                .fireproof(),
            Items.FISHING_ROD
        );
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        var itemStack = user.getStackInHand(hand);

        var deepPlayer = (IDeepPlayer) user;
        var fishHook = deepPlayer.mwhrd$getFishHook();
        if (fishHook != null) {
            fishHook.use(itemStack);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));

            // Spawn the fishing bobber.
            if (world instanceof ServerWorld serverWorld) {
                ProjectileEntity.spawn(
                    new CelestialFishingBobberEntity(user, world),
                    serverWorld, itemStack
                );
            }

            user.incrementStat(Stats.USED.getOrCreateStat(this));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return super.use(world, user, hand);
    }
}
