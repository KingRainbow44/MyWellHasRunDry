package moe.seikimo.mwhrd.game;

import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Random;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Hardcore {
    /**
     * Registers listeners for hardcore mode.
     */
    static void initialize() {
        PlayerBlockBreakEvents.AFTER.register(Hardcore::onBreak);
    }

    /**
     * Invoked when a block is broken by a player.
     */
    static void onBreak(
        World world, PlayerEntity player,
        BlockPos pos, BlockState state, BlockEntity blockEntity
    ) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        var playerModel = Players.getModel(serverPlayer);
        if (!playerModel.isSurvivedHardcore()) return;

        // Check if the player should receive experience.
        if (Random.success(0.5d)) return;

        // Add experience to the player's inventory.
        Players.addExperience(serverPlayer, 4);
    }
}
