package moe.seikimo.mwhrd.beacon;

import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import lombok.Getter;
import moe.seikimo.mwhrd.beacon.powers.TeleportationPower;
import moe.seikimo.mwhrd.gui.BeaconTeleportGui;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public final class BeaconManager {
    @Getter private static final Map<BlockPos, IAdvancedBeacon> allBeacons = new HashMap<>();
    @Getter private static final Map<BlockPos, BeaconEntry> tpBeacons = new HashMap<>();

    /**
     * Opens the beacon menu for the player.
     *
     * @param player The player.
     */
    public static void openBeaconMenu(ServerPlayerEntity player) {
        // Check if the player is standing on a beacon.
        var world = player.getWorld();
        var pos = player.getBlockPos();
        var blockEntity = world.getBlockEntity(pos.down());

        if (!(blockEntity instanceof IAdvancedBeacon beacon)) return;
        if (!beacon.mwhrd$isAdvanced()) return;

        var tpPower = beacon.mwhrd$getEffectMap().get(BeaconEffect.EYE_OF_TELEPORTATION);
        if (!(tpPower instanceof TeleportationPower power)) return;
        if (power.isForceDisabled()) return;

        var upgrades = beacon.mwhrd$getEffectList();
        if (!upgrades.contains(BeaconEffect.EYE_OF_TELEPORTATION)) return;

        // Open the beacon menu.
        var screenHandler = player.currentScreenHandler;
        if (screenHandler instanceof VirtualScreenHandler) return;

        var condPlayer = (IPlayerConditions) player;
        if (System.currentTimeMillis() < condPlayer.mwhrd$getClosedCooldown()) return;

        new BeaconTeleportGui(player).open();
    }

    /**
     * Checks if a beacon exists at the given position.
     *
     * @param world The world.
     * @param pos The position.
     * @return Whether the beacon exists.
     */
    public static boolean beaconExists(World world, BlockPos pos) {
        // Check if the position is loaded.
        var chunkX = pos.getX() >> 4;
        var chunkZ = pos.getZ() >> 4;
        if (!world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
            return false;
        }

        var blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof IAdvancedBeacon beacon &&
            beacon.mwhrd$isAdvanced();
    }

    /**
     * Removes all beacons which are invalid.
     */
    public static void purge() {
        new HashMap<>(BeaconManager.tpBeacons).forEach((pos, entry) -> {
            if (!BeaconManager.beaconExists(entry.world(), pos)) {
                BeaconManager.tpBeacons.remove(pos);
            }
        });
    }

    /**
     * Registers the beacon with the server.
     *
     * @param entity The beacon block entity.
     * @param entry The beacon entry.
     */
    public static void register(BeaconBlockEntity entity, BeaconEntry entry) {
        BeaconManager.purge(); // Remove all invalid beacons.

        // Add the beacon to the map.
        BeaconManager.tpBeacons.put(entity.getPos(), entry);
    }

    /**
     * Teleports the player to the beacon.
     *
     * @param key The beacon key.
     * @param player The player.
     */
    public static void teleport(BlockPos key, ServerPlayerEntity player) {
        // Check if a player has an experience level.
        var levels = player.experienceLevel;
        if (levels <= 5) {
            player.sendMessage(Text.literal("You at least 5 experience levels to teleport!")
                .formatted(Formatting.RED));
            return;
        }

        // Check if the beacon exists.
        BeaconManager.purge();

        var entry = BeaconManager.tpBeacons.get(key);
        if (entry == null) {
            player.sendMessage(Text.literal("The beacon no longer exists!")
                .formatted(Formatting.RED));
            return;
        }

        if (!(entry.world() instanceof ServerWorld serverWorld)) {
            player.sendMessage(Text.literal("The beacon is in an invalid world!")
                .formatted(Formatting.RED));
            return;
        }

        // Teleport the player.
        var target = entry.teleportTo();
        player.teleport(serverWorld,
            target.getX() + .5, target.getY() + .5, target.getZ() + .5,
            player.getYaw(), player.getPitch());

        // Remove the experience level.
        player.addExperienceLevels(-5);

        player.sendMessage(Text.literal("Teleported to ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(entry.name())
                .formatted(Formatting.YELLOW))
            .append(Text.literal("!")
                .formatted(Formatting.GREEN)));
    }

    /**
     * Handles the placing of beacons.
     *
     * @param stack The item stack.
     * @param world The world.
     * @param result The block hit result.
     * @return The action result.
     */
    public static ActionResult handleBeacon(
        ItemStack stack, World world, BlockHitResult result
    ) {
        // Check if the stack contains the custom beacon data.
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return ActionResult.PASS;
        }
        if (!customData.contains("advanced_beacon")) {
            return ActionResult.PASS;
        }

        // Set the block.
        var newBlock = result.getBlockPos().add(result.getSide().getVector());
        world.setBlockState(newBlock, Blocks.BEACON.getDefaultState());
        var blockEntity = (BeaconBlockEntity) world.getBlockEntity(newBlock);
        if (blockEntity == null) {
            world.setBlockState(newBlock, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }

        // Update the beacon block from the item.
        var beacon = (IAdvancedBeacon) blockEntity;
        beacon.mwhrd$deserialize(customData.copyNbt());

        stack.decrement(1);

        return ActionResult.SUCCESS;
    }
}
