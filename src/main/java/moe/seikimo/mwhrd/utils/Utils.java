package moe.seikimo.mwhrd.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
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

    /**
     * Serializes a block position to a string.
     *
     * @param position The block position to serialize.
     * @return The serialized block position.
     */
    static String serialize(BlockPos position) {
        if (position == null) {
            return "No position set";
        }
        return "(%s, %s, %s)".formatted(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Creates a rectangle of block positions.
     *
     * @param corner1 The first corner of the rectangle.
     * @param corner2 The second corner of the rectangle.
     * @return A list of block positions in the rectangle.
     */
    static List<BlockPos> rectangle(BlockPos corner1, BlockPos corner2) {
        var positions = new ArrayList<BlockPos>();

        var minX = Math.min(corner1.getX(), corner2.getX());
        var minY = Math.min(corner1.getY(), corner2.getY());
        var minZ = Math.min(corner1.getZ(), corner2.getZ());

        var maxX = Math.max(corner1.getX(), corner2.getX());
        var maxY = Math.max(corner1.getY(), corner2.getY());
        var maxZ = Math.max(corner1.getZ(), corner2.getZ());

        for (var x = minX; x <= maxX; x++) {
            for (var y = minY; y <= maxY; y++) {
                for (var z = minZ; z <= maxZ; z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }

        return positions;
    }
}
