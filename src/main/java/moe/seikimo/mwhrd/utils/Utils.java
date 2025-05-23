package moe.seikimo.mwhrd.utils;

import lombok.SneakyThrows;
import moe.seikimo.general.EncodingUtils;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public interface Utils {
    Set<RegistryKey<World>> ALLOWED_WORLDS = Set.of(
        World.OVERWORLD,
        World.NETHER,
        World.END
    );

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
        return new BlockPos(
            (int) Math.round(pos.getX()),
            (int) Math.round(pos.getY()),
            (int) Math.round(pos.getZ())
        );
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
     * Returns a random integer between min and max.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A random integer between min and max.
     */
    static int random(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
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
        var instance = MyWellHasRunDry.getEnchantmentRegistry().get(key);
        return MyWellHasRunDry.getEnchantmentRegistry().getEntry(instance);
    }

    /**
     * Enchants the given stack with the given enchantment at the given level.
     *
     * @param stack The stack to enchant.
     * @param enchantment The enchantment to apply.
     * @param level The level of the enchantment.
     */
    static void enchant(ItemStack stack, RegistryKey<Enchantment> enchantment, int level) {
        var key = Utils.lookup(enchantment);
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
     * Checks if the given entity is in the given world.
     *
     * @param entity The entity to check.
     * @param world The world to check.
     * @return Whether the entity is in the world.
     */
    static boolean inWorld(LivingEntity entity, RuntimeWorldHandle world) {
        return entity.getWorld().getRegistryKey().equals(world.getRegistryKey());
    }

    /**
     * Compares the given worlds.
     *
     * @param source The source world.
     * @param destination The destination world.
     * @return Whether the worlds are the same.
     */
    static boolean compare(World source, World destination) {
        return source.getRegistryKey().equals(destination.getRegistryKey());
    }

    /**
     * Compares the given worlds.
     *
     * @param source The source world.
     * @param destination The destination world.
     * @return Whether the worlds are the same.
     */
    static boolean compare(World source, RuntimeWorldHandle destination) {
        return source.getRegistryKey().equals(destination.getRegistryKey());
    }

    /**
     * Compares the given world to the given registry key.
     *
     * @param source The source world.
     * @param destination The destination world.
     * @return Whether the worlds are the same.
     */
    static boolean compare(World source, RegistryKey<World> destination) {
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

    /**
     * Fetches the equippable slot of the given item.
     *
     * @param item The item to fetch the slot of.
     * @return The item's equippable slot.
     */
    static EquipmentSlot getSlot(Item item) {
        // Get the item's equippable component.
        var component = item
            .getComponents()
            .get(DataComponentTypes.EQUIPPABLE);

        // Check if the component is null.
        if (component == null) {
            return EquipmentSlot.BODY;
        }

        return component.slot();
    }

    /**
     * Creates a registry key for the given ID.
     *
     * @param id The ID of the registry key.
     * @return The registry key.
     */
    static RegistryKey<Item> itemKey(String id) {
        return RegistryKey.of(
            RegistryKeys.ITEM,
            Identifier.of("mwhrd", id)
        );
    }

    /**
     * Creates a registry key for the given ID.
     *
     * @param id The ID of the registry key.
     * @return The registry key.
     */
    static RegistryKey<EntityType<?>> entityKey(String id) {
        return RegistryKey.of(
            RegistryKeys.ENTITY_TYPE,
            Identifier.of("mwhrd", id)
        );
    }

    /**
     * Creates a registry key for the given ID.
     *
     * @param id The ID of the registry key.
     * @return The registry key.
     */
    static RegistryKey<Block> blockKey(String id) {
        return RegistryKey.of(
            RegistryKeys.BLOCK,
            Identifier.of("mwhrd", id)
        );
    }

    /**
     * Creates a new-line separated single-line text.
     *
     * @param texts The texts to concatenate.
     * @return The concatenated text.
     */
    static Text list(Text... texts) {
        var combined = Text.empty();

        for (var i = 0; i < texts.length; i++) {
            var text = texts[i];
            combined
                .append(text)
                .append(i < texts.length - 1 ?
                    Text.literal("\n") :
                    Text.empty());
        }

        return combined;
    }

    /**
     * Converts a number into a v4 UUID.
     *
     * @param index The index to convert.
     * @return The UUID.
     */
    static UUID uuidFromIndex(int index) {
        var buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(index);
        return UUID.nameUUIDFromBytes(buffer.array());
    }

    /**
     * Creates a new list with the given size.
     *
     * @param size The size of the list.
     * @return The new list.
     * @param <T> The type of the list.
     */
    static <T> List<T> newList(int size) {
        var list = new ArrayList<T>(size);

        // Fill the list with nulls.
        for (var i = 0; i < size; i++) {
            list.add(null);
        }

        return list;
    }

    /**
     * Removes the italics formatting from a text.
     *
     * @param source The source text.
     * @return The text without italics formatting.
     */
    static Text clearFormatting(Text source) {
        return source
            .copy()
            .setStyle(
                source.getStyle()
                    .withItalic(false)
            );
    }

    /**
     * Returns a rarity-adjusted name of the item.
     *
     * @param item The item to get the name of.
     * @return The rarity-adjusted name of the item.
     */
    static Text nameOf(Item item) {
        var name = item.getName();

        // Check if the item has a rarity.
        if (item.getComponents().get(DataComponentTypes.RARITY) instanceof Rarity rarity) {
            name = name.copy().formatted(rarity.getFormatting());
        }

        return name;
    }


    /**
     * Returns a number in the format:
     * x,xxx,xxx (as needed)
     *
     * @param number The number to format.
     * @return The formatted number.
     */
    static String pretty(long number) {
        return String.format("%,d", number);
    }

    List<EquipmentSlot> ARMOR = List.of(
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    );

    /**
     * Provides a list of armor item stacks.
     *
     * @param equipment The entity's equipment holder.
     * @return The list of item stacks.
     */
    static List<ItemStack> iterate(EntityEquipment equipment) {
        var stacks = new ArrayList<ItemStack>();

        for (var slot : ARMOR) {
            var stack = equipment.get(slot);
            stacks.add(stack);
        }

        return stacks;
    }

    /**
     * Checks if an item stack is a pickaxe.
     *
     * @param stack The item stack to check.
     * @return Whether the item stack is a pickaxe.
     */
    static boolean isWeapon(ItemStack stack) {
        var item = stack.getItem();
        var key = Registries.ITEM.getEntry(item);
        return key.isIn(ItemTags.SWORDS) || key.isIn(ItemTags.AXES);
    }

    /**
     * Checks if an item stack is a mining tool.
     *
     * @param stack The item stack to check.
     * @return Whether the item stack is a mining tool.
     */
    static boolean isMiningTool(ItemStack stack) {
        var item = stack.getItem();
        var key = Registries.ITEM.getEntry(item);
        return key.isIn(ItemTags.PICKAXES) ||
            key.isIn(ItemTags.AXES) ||
            key.isIn(ItemTags.SHOVELS) ||
            key.isIn(ItemTags.HOES);
    }

    /**
     * Checks if an item stack is armor.
     *
     * @param stack The item stack to check.
     * @return Whether the item stack is armor.
     */
    static boolean isArmor(ItemStack stack) {
        var item = stack.getItem();
        var key = Registries.ITEM.getEntry(item);
        return key.isIn(ItemTags.HEAD_ARMOR) ||
            key.isIn(ItemTags.CHEST_ARMOR) ||
            key.isIn(ItemTags.LEG_ARMOR) ||
            key.isIn(ItemTags.FOOT_ARMOR);
    }
}
