package moe.seikimo.mwhrd.utils;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.PlayerModel;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.Optional;
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
     * Checks if the player is in the specified world.
     *
     * @param world The world to check.
     * @param player The player to check.
     * @return Whether the player is in the world.
     */
    static boolean inWorld(RuntimeWorldHandle world, PlayerEntity player) {
        return Players.inWorld(world.getRegistryKey(), player);
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

    /**
     * Fetches the data model for the given player.
     *
     * @param player The player to fetch the model for.
     * @return The player's data model.
     */
    static PlayerModel getModel(ServerPlayerEntity player) {
        if (!(player instanceof IDBObject<?> dbObject)) {
            throw new RuntimeException("Player is not instance of IDBObject.");
        }

        if (!(dbObject.mwhrd$getData() instanceof PlayerModel playerModel)) {
            throw new RuntimeException("Player data is not instance of PlayerModel.");
        }

        return playerModel;
    }

    /**
     * Repairs the player's gear.
     * This is a recursive function.
     *
     * @param player The player to repair the gear for.
     * @param amount The amount of experience to repair the gear with.
     * @return The amount of experience left after repairing the gear.
     */
    static int repairPlayerGears(ServerPlayerEntity player, int amount) {
        var optional = EnchantmentHelper.chooseEquipmentWith(EnchantmentEffectComponentTypes.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (optional.isPresent()) {
            int experience;

            var itemStack = optional.get().stack();

            int newDamage = EnchantmentHelper.getRepairWithExperience(player.getServerWorld(), itemStack, amount);
            int damage = Math.min(newDamage, itemStack.getDamage());

            itemStack.setDamage(itemStack.getDamage() - damage);
            if (damage > 0 && (experience = amount - damage * amount / newDamage) > 0) {
                return Players.repairPlayerGears(player, experience);
            }

            return 0;
        }

        return amount;
    }

    /**
     * Adds experience to the player.
     * This will recursively repair the player's gear first.
     *
     * @param player The player to add experience to.
     * @param experience The amount of experience to add.
     */
    static void addExperience(ServerPlayerEntity player, int experience) {
        var remaining = Players.repairPlayerGears(player, experience);
        if (remaining > 0) {
            player.addExperience(remaining);
        }
    }
}
