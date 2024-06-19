package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.interfaces.IItemStackReference;
import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.enums.VaultState;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VaultBlockEntity.Server.class)
public abstract class VaultBlockEntityMixin {
    @Inject(method = "tryUnlock", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/entity/VaultBlockEntity$Server;unlock(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/vault/VaultConfig;Lnet/minecraft/block/vault/VaultServerData;Lnet/minecraft/block/vault/VaultSharedData;Ljava/util/List;)V"
    ), cancellable = true)
    private static void tryUnlock(
        ServerWorld world, BlockPos pos, BlockState state,
        VaultConfig config, VaultServerData serverData, VaultSharedData sharedData,
        PlayerEntity player, ItemStack stack, CallbackInfo ci
    ) {
        // Generate the player's loot.
        var list = VaultBlockEntity.Server.generateLoot(world, config, pos, player);
        if (list.isEmpty()) {
            return;
        }

        // Call our special method and cancel the original unlock.
        VaultBlockEntityMixin.unlock(world, state, pos, config, serverData, sharedData, list, player);

        // Mark the vault as rewarded.
        serverData.markPlayerAsRewarded(player);
        sharedData.updateConnectedPlayers(world, pos, serverData, config, config.deactivationRange());

        ci.cancel();
    }

    /**
     * Special unlock function which has a reference to the player.
     */
    @Unique
    private static void unlock(
        ServerWorld world, BlockState state, BlockPos pos,
        VaultConfig config, VaultServerData serverData,
        VaultSharedData sharedData, List<ItemStack> itemsToEject,
        PlayerEntity player
    ) {
        for (var item : itemsToEject) {
            // Set the item stack reference to the player.
            var stackRef = (IItemStackReference) (Object) item;
            if (stackRef != null) {
                stackRef.mwhrd$setReference(player);
            }
        }

        serverData.setItemsToEject(itemsToEject);
        sharedData.setDisplayItem(serverData.getItemToDisplay());
        serverData.setStateUpdatingResumeTime(world.getTime() + 14L);
        VaultBlockEntity.Server.changeVaultState(world, pos, state, (BlockState)state.with(VaultBlock.VAULT_STATE, VaultState.UNLOCKING), config, sharedData);
    }
}
