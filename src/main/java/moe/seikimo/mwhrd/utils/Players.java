package moe.seikimo.mwhrd.utils;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

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

    /**
     * Checks if the player is in the specified world.
     *
     * @param world The world to check.
     * @param player The player to check.
     * @return Whether the player is in the world.
     */
    static boolean inWorld(RegistryKey<World> world, PlayerEntity player) {
        return world.equals(player.getWorld().getRegistryKey());
    }

    /**
     * Helper method to kick a player from the server.
     *
     * @param player The player to kick.
     * @param text The reason for the kick.
     */
    static void kickPlayer(ServerPlayerEntity player, Text text) {
        var connection = player.networkHandler.connection;
        connection.send(new DisconnectS2CPacket(text), PacketCallbacks.always(() -> connection.disconnect(text)));
        connection.tryDisableAutoRead();
    }
}
