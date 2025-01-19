package moe.seikimo.mwhrd.game;

import moe.seikimo.mwhrd.interfaces.game.IHardcorePlayer;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Random;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.time.Duration;

public interface Hardcore {
    /** 1 Minecraft Day x 100 Days */
    long HARDCORE_DURATION = 24_000 * 100;

    /**
     * Registers listeners for hardcore mode.
     */
    static void initialize() {
        PlayerBlockBreakEvents.AFTER.register(Hardcore::onBreak);
        ServerPlayConnectionEvents.DISCONNECT.register(Hardcore::onDisconnect);
    }

    /**
     * Invoked when {@link PlayerModel#setHandle(ServerPlayerEntity)} is called.
     *
     * @param player The player.
     * @param model The player's model.
     */
    static void onLogin(ServerPlayerEntity player, PlayerModel model) {
        if (!model.isHardcoreV2()) return;

        // Check if the player has completed hardcore mode.
        if (model.getAliveTicks() >= HARDCORE_DURATION) {
            model.finishHardcore(true);
        } else {
            player.sendMessage(Text.translatable("text.mwhrd.survived", model.getAliveTicks() / 24_000)
                .formatted(Formatting.GREEN));
        }
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

        // Determine which hardcore experience model to use.
        var hardcore = playerModel.isSurvivedHardcore();
        var hardcoreV2 = playerModel.isSurvivedHardcoreV2();

        if (!hardcore && !hardcoreV2) return;

        var chance = hardcoreV2 ? 0.5d : 0.1d;
        var experience = hardcoreV2 ? 4 : 1;

        // Check if the player should receive experience.
        if (Random.success(chance)) return;

        // Add experience to the player's inventory.
        Players.addExperience(serverPlayer, experience);
    }

    /**
     * Invoked when a player disconnects from the server.
     */
    static void onDisconnect(ServerPlayNetworkHandler networkHandler, MinecraftServer server) {
        var player = networkHandler.getPlayer();
        var model = Players.getModel(player);

        // Check if the player is in combat & hardcore.
        if (!model.isHardcoreV2()) return;

        if (!(player instanceof IHardcorePlayer hardcorePlayer)) return;
        if (!hardcorePlayer.mwhrd$hasTargets()) return;

        // Mark the player as dead.
        model.banPlayer(Duration.ofHours(24));
        model.finishHardcore(false);
    }
}
