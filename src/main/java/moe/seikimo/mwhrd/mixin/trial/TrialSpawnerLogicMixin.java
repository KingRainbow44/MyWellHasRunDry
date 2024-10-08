package moe.seikimo.mwhrd.mixin.trial;

import com.llamalad7.mixinextras.sugar.Local;
import moe.seikimo.mwhrd.interfaces.IEntityConditions;
import moe.seikimo.mwhrd.utils.MobGear;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.TrialChamberLoot;
import moe.seikimo.mwhrd.interfaces.ITrialSpawnerUtils;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(TrialSpawnerLogic.class)
public abstract class TrialSpawnerLogicMixin implements ITrialSpawnerUtils {
    @Unique private static final Set<Class<? extends MobEntity>> BLACKLIST = Set.of(
        CaveSpiderEntity.class,
        SpiderEntity.class,
        BreezeEntity.class,
        SilverfishEntity.class
    );

    @Final @Shadow private TrialSpawnerData data;

    @Shadow
    public abstract boolean isOminous();

    @Override
    public Set<UUID> mwhrd$getSpawnerPlayers() {
        return this.data.players;
    }

    @Inject(method = "ejectLootTable", at = @At("HEAD"), cancellable = true)
    public void addToPlayerLoot(ServerWorld world, BlockPos pos, RegistryKey<LootTable> lootTable, CallbackInfo ci) {
        // Get the player to give the loot to.
        var players = this.mwhrd$getSpawnerPlayers();
        var player = players.iterator().next();

        // Determine the loot.
        var table = world.getServer().getReloadableRegistries().getLootTable(lootTable);
        var loot = table.generateLoot(new LootContextParameterSet.Builder(world)
            .build(LootContextTypes.EMPTY));
        if (loot.isEmpty()) return;

        for (var itemStack : loot) {
            // Add the item to the player's loot.
            TrialChamberLoot.addLoot(player, itemStack);

            // We need to create a fake item to show what the player received.
            var itemEntity = TrialChamberLoot.spawnItem(
                world, itemStack, 2, Direction.UP,
                Vec3d.ofBottomCenter(pos).offset(Direction.UP, 1.2));
            itemEntity.setPickupDelayInfinite();

            // Delete the entity after 8s seconds.
            itemEntity.itemAge = 5840;
        }
        world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_EJECTS_ITEM, pos, 0);

        ci.cancel();
    }

    @Inject(method = "trySpawnMob", at = @At("TAIL"))
    public void onMobSpawned(
        ServerWorld world, BlockPos pos, CallbackInfoReturnable<Optional<UUID>> cir,
        @Local(ordinal = 0) Entity spawnedEntity
    ) {
        if (!this.isOminous()) return;
        if (!(spawnedEntity instanceof MobEntity mob)) return;
        if (spawnedEntity instanceof IEntityConditions condMob) {
            condMob.mwhrd$setTrial(true);
        }

        // Apply full armor to the mob.
        if (!BLACKLIST.contains(mob.getClass())) {
            MobGear.applyArmor(mob);
        }

        // Apply mob effects.
        mob.addStatusEffect(new StatusEffectInstance(
            StatusEffects.STRENGTH, Integer.MAX_VALUE
        ));
        mob.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED, Integer.MAX_VALUE, 1
        ));

        if (mob instanceof AbstractSkeletonEntity) {
            mob.equipStack(EquipmentSlot.MAINHAND, MobGear.BOW.copy());
            mob.equipStack(EquipmentSlot.OFFHAND, MobGear.ARROWS.copy());
        } else {
            mob.equipStack(EquipmentSlot.MAINHAND, MobGear.SWORD.copy());
        }

        if (MyWellHasRunDry.getRandom().nextInt(0, 5) == 0) {
            mob.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }
    }
}
