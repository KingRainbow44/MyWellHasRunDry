package moe.seikimo.mwhrd.utils;

import lombok.SneakyThrows;
import moe.seikimo.general.EncodingUtils;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    /**
     * Converts a Vec3i to a BlockPos.
     *
     * @param pos The Vec3i to convert.
     * @return The converted BlockPos.
     */
    static BlockPos blockPos(Vec3i pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Converts a Vec3i to a BlockPos.
     *
     * @param pos The Vec3i to convert.
     * @return The converted BlockPos.
     */
    static BlockPos blockPos(Vec3d pos) {
        return new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
    }

    /**
     * Returns a random element from an array.
     *
     * @param array The array to pick a random element from.
     * @return A random element from the array.
     */
    static <T> T random(T[] array) {
        return array[(int) (Math.random() * array.length)];
    }

    /**
     * Base64-encodes a Minecraft tag.
     *
     * @param tag The tag to encode.
     * @return The base64-encoded tag.
     */
    @SneakyThrows
    static String base64Encode(NbtElement tag) {
        var output = new ByteArrayOutputStream();
        var stream = new DataOutputStream(output);
        NbtIo.write(tag, stream);

        var bytes = output.toByteArray();
        return EncodingUtils.base64Encode(bytes);
    }

    /**
     * Base64-decodes a Minecraft tag.
     *
     * @param base64 The base64-encoded tag.
     * @return The decoded tag.
     */
    @SneakyThrows
    static NbtElement base64Decode(String base64) {
        var bytes = EncodingUtils.base64Decode(base64);
        var input = new ByteArrayInputStream(bytes);
        var stream = new DataInputStream(input);
        return NbtIo.readCompound(stream);
    }

    /**
     * Looks up an enchantment by its registry key.
     *
     * @param key The registry key of the enchantment.
     * @return The registry entry of the enchantment.
     */
    static RegistryEntry<Enchantment> lookup(RegistryKey<Enchantment> key) {
        return MyWellHasRunDry
            .getEnchantmentRegistry()
            .entryOf(key);
    }

    /**
     * Enchants the given stack with the given enchantment at the given level.
     *
     * @param stack The stack to enchant.
     * @param enchantment The enchantment to apply.
     * @param level The level of the enchantment.
     */
    static void enchant(ItemStack stack, RegistryKey<Enchantment> enchantment, int level) {
        var key = MyWellHasRunDry.getEnchantmentRegistry().entryOf(enchantment);
        stack.addEnchantment(key, level);
    }

    /**
     * Checks if the given entity is in the given world.
     *
     * @param entity The entity to check.
     * @param registryKey The registry key of the world.
     * @return Whether the entity is in the world.
     */
    static boolean inWorld(LivingEntity entity, RegistryKey<World> registryKey) {
        return entity.getWorld().getRegistryKey().equals(registryKey);
    }

    /**
     * Compares the given worlds.
     *
     * @param source The source world.
     * @param destination The destination world.
     * @return Whether the worlds are the same.
     */
    static boolean compare(ServerWorld source, ServerWorld destination) {
        return source.getRegistryKey().equals(destination.getRegistryKey());
    }

    /**
     * Compares the given world to the given registry key.
     *
     * @param source The source world.
     * @param destination The destination world.
     * @return Whether the worlds are the same.
     */
    static boolean compare(ServerWorld source, RegistryKey<World> destination) {
        return source.getRegistryKey().equals(destination);
    }

    /**
     * Compares the given damage source to the given damage type.
     *
     * @param source The source of the damage.
     * @param type The type of the damage.
     * @return Whether the source matches the type.
     */
    static boolean compare(DamageSource source, RegistryKey<DamageType> type) {
        var sourceType = source.getTypeRegistryEntry().getKey();
        return sourceType
            .map(k -> k.equals(type))
            .orElse(false);
    }
}
