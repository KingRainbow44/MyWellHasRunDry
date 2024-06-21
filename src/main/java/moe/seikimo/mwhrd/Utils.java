package moe.seikimo.mwhrd;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public interface Utils {
    /**
     * Fetches all nearby players.
     *
     * @param player The player to fetch nearby players from.
     * @param radius The radius to search for players.
     * @return A list of nearby players.
     */
    static List<PlayerEntity> getNearbyPlayers(
        PlayerEntity player, double radius
    ) {
        return getNearbyPlayers(player.getWorld(), player.getBlockPos(), radius);
    }

    /**
     * Fetches all nearby players.
     *
     * @param world The world to fetch players from.
     * @param base The base position to search from.
     * @param radius The radius to search for players.
     * @return A list of nearby players.
     */
    static List<PlayerEntity> getNearbyPlayers(
        World world, BlockPos base, double radius
    ) {
        var box = new Box(base).expand(radius).stretch(0.0, world.getHeight(), 0.0);
        return world.getNonSpectatingEntities(PlayerEntity.class, box);
    }
}
