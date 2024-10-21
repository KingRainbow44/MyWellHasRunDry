package moe.seikimo.mwhrd.utils;

import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;

public interface BorderHelper {
    /**
     * Sets the world border for a player.
     *
     * @param player The player to set the world border for.
     * @param center The center of the world border.
     * @param radius The radius of the world border.
     */
    static void setWorldBorder(ServerPlayerEntity player, BlockPos center, int radius) {
        var border = new WorldBorder();
        border.setSize(radius);
        border.setMaxRadius(radius);
        border.setCenter(center.getX(), center.getZ());

        player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(border));
    }
}
