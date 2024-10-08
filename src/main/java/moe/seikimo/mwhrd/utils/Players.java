package moe.seikimo.mwhrd.utils;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.TeleportTarget;

import java.util.function.Consumer;

public interface Players {
    /**
     * Runs a consumer for each player.
     *
     * @param consumer The consumer to run.
     */
    static void all(Consumer<ServerPlayerEntity> consumer) {
        MyWellHasRunDry.getServer().getPlayerManager().getPlayerList().forEach(consumer);
    }

    /**
     * Broadcasts a message to all players.
     *
     * @param message The message to broadcast.
     * @param overlay Whether to overlay the message.
     */
    static void broadcast(Text message, boolean overlay) {
        Players.all(player -> player.sendMessage(message, overlay));
    }

    /**
     * Sends a list of messages to a player.
     *
     * @param player The player to send the messages to.
     * @param messages The messages to send.
     */
    static void bulkSend(PlayerEntity player, Text... messages) {
        for (var message : messages) {
            player.sendMessage(message, false);
        }
    }

    /**
     * Teleports the player to their spawnpoint, or the world spawnpoint if the player has no spawnpoint.
     *
     * @param player The player to teleport.
     */
    static void respawn(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        var target = serverPlayer.getRespawnTarget(false, TeleportTarget.NO_OP);
        player.teleportTo(target);
    }
}
