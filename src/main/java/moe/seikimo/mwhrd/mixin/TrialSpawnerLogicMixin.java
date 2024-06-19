package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.TrialChamberLoot;
import moe.seikimo.mwhrd.interfaces.ITrialSpawnerUtils;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.block.spawner.TrialSpawnerLogic;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

@Mixin(TrialSpawnerLogic.class)
public abstract class TrialSpawnerLogicMixin implements ITrialSpawnerUtils {
    @Final @Shadow private TrialSpawnerData data;

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
            TrialChamberLoot.scheduleForDespawn(itemEntity, 8e3);
        }
        world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_EJECTS_ITEM, pos, 0);

        ci.cancel();
    }
}
