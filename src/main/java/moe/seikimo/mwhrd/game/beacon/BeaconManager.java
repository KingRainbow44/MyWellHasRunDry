package moe.seikimo.mwhrd.game.beacon;

import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import lombok.Getter;
import moe.seikimo.mwhrd.custom.entities.AdvancedBeaconBlockEntity;
import moe.seikimo.mwhrd.game.beacon.powers.TeleportationPower;
import moe.seikimo.mwhrd.gui.beacon.BeaconTeleportGui;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class BeaconManager {
    public static long FUEL_TIME = 20L * 60L * 60L; // 20 ticks * 60 seconds * 60 minutes

    @Getter private static final Map<BlockPos, AdvancedBeaconBlockEntity> allBeacons = new HashMap<>();
    @Getter private static final Map<BlockPos, BeaconEntry> tpBeacons = new HashMap<>();

    /**
     * Opens the beacon menu for the player.
     *
     * @param player The player.
     */
    public static void openBeaconMenu(ServerPlayerEntity player) {
        // Check if the player is standing on a beacon.
        var world = player.getEntityWorld();
        var pos = player.getBlockPos();
        var blockEntity = world.getBlockEntity(pos.down());

        if (!(blockEntity instanceof AdvancedBeaconBlockEntity beacon)) return;

        var tpPower = beacon.getPowers().get(BeaconEffect.EYE_OF_TELEPORTATION);
        if (!(tpPower instanceof TeleportationPower power)) return;
        if (power.isForceDisabled()) return;

        var upgrades = beacon.getEffectList();
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
        return blockEntity instanceof AdvancedBeaconBlockEntity;
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
    public static void register(AdvancedBeaconBlockEntity entity, BeaconEntry entry) {
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
        var target = entry.teleportTo().toCenterPos();
        player.teleport(serverWorld,
            target.getX(), target.getY(), target.getZ(),
            Collections.emptySet(),
            player.getYaw(), player.getPitch(), true);

        // Remove the experience level.
        player.addExperienceLevels(-5);

        player.sendMessage(Text.literal("Teleported to ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(entry.name())
                .formatted(Formatting.YELLOW))
            .append(Text.literal("!")
                .formatted(Formatting.GREEN)));
    }
}
