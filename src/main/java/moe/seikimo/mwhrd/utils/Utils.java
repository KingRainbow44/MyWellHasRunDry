package moe.seikimo.mwhrd.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

    TreeMap<Integer, String> ROMAN = new TreeMap<>() {{
        put(1000, "M");
        put(900, "CM");
        put(500, "D");
        put(400, "CD");
        put(100, "C");
        put(90, "XC");
        put(50, "L");
        put(40, "XL");
        put(10, "X");
        put(9, "IX");
        put(5, "V");
        put(4, "IV");
        put(1, "I");
    }};

    /**
     * Converts a number to a roman numeral.
     *
     * @param number The number to convert.
     * @return The roman numeral representation of the number.
     */
    static String toRoman(int number) {
        var l = ROMAN.floorKey(number);
        if (number == l) {
            return ROMAN.get(number);
        }

        return ROMAN.get(l) + toRoman(number - l);
    }
}
