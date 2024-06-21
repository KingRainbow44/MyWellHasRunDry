package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

@Mixin(TrialSpawnerData.class)
public abstract class TrialSpawnerDataMixin {
    @Shadow @Final public Set<UUID> players;

    @Inject(method = "updatePlayers", at = @At("TAIL"))
    public void updatePlayers(
        ServerWorld world,
        BlockPos pos,
        TrialSpawnerLogic logic,
        CallbackInfo ci
    ) {
        for (var playerUuid : this.players) {
            var player = world.getPlayerByUuid(playerUuid);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                var condPlayer = (IPlayerConditions) serverPlayer;
                condPlayer.mwhrd$setOminous(logic.isOminous());
            }
        }
    }
}
