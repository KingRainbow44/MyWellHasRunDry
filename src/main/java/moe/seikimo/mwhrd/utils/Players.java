package moe.seikimo.mwhrd.utils;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.game.guilds.GuildInstance;
import moe.seikimo.mwhrd.game.quest.PlayerQuestManager;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.interfaces.player.IPlayer;
import moe.seikimo.mwhrd.interfaces.player.IStoryPlayer;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.models.PlayerQuestData;
import moe.seikimo.mwhrd.utils.items.ItemBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.UUID;
import java.util.function.Consumer;

public final class Players {
    private Players() {
        // Utility class, no instantiation allowed.
    }

    /**
     * Runs a consumer for each player.
     *
     * @param consumer The consumer to run.
     */
    public static void all(Consumer<ServerPlayerEntity> consumer) {
        MyWellHasRunDry.getServer().getPlayerManager().getPlayerList().forEach(consumer);
    }

    /**
     * Broadcasts a message to all players.
     *
     * @param message The message to broadcast.
     * @param overlay Whether to overlay the message.
     */
    public static void broadcast(Text message, boolean overlay) {
        Players.all(player -> player.sendMessage(message, overlay));
    }

    /**
     * Sends a list of messages to a player.
     *
     * @param player The player to send the messages to.
     * @param messages The messages to send.
     */
    public static void bulkSend(PlayerEntity player, Text... messages) {
        for (var message : messages) {
            player.sendMessage(message, false);
        }
    }

    /**
     * Resets a player to their default attributes.
     *
     * @param player The player to reset.
     */
    public static void reset(PlayerEntity player) {
        // Reset the player's health.
        player.setHealth(player.getMaxHealth());

        // Reset the player's hunger.
        var hungerManager = player.getHungerManager();
        hungerManager.setFoodLevel(20);
        hungerManager.setSaturationLevel(5f);

        // Clear the player's effects.
        player.clearStatusEffects();
    }

    /**
     * Teleports the player to their spawnpoint, or the world spawnpoint if the player has no spawnpoint.
     *
     * @param player The player to teleport.
     */
    public static void respawn(PlayerEntity player) {
        Players.reset(player);

        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        // Teleport the player to their spawnpoint.
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
    public static boolean inWorld(RegistryKey<World> world, PlayerEntity player) {
        return world.equals(player.getEntityWorld().getRegistryKey());
    }

    /**
     * Checks if the player is in the specified world.
     *
     * @param world The world to check.
     * @param player The player to check.
     * @return Whether the player is in the world.
     */
    public static boolean inWorld(RuntimeWorldHandle world, PlayerEntity player) {
        return Players.inWorld(world.getRegistryKey(), player);
    }

    /**
     * Helper method to kick a player from the server.
     *
     * @param player The player to kick.
     * @param text The reason for the kick.
     */
    public static void kickPlayer(ServerPlayerEntity player, Text text) {
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
    public static PlayerModel getModel(ServerPlayerEntity player) {
        if (!(player instanceof IDBObject<?> dbObject)) {
            throw new RuntimeException("Player is not instance of IDBObject.");
        }

        if (!(dbObject.mwhrd$getData() instanceof PlayerModel playerModel)) {
            throw new RuntimeException("Player data is not instance of PlayerModel.");
        }

        return playerModel;
    }

    /**
     * Fetches the quest manager for the given player.
     *
     * @param player The player to fetch the quest manager for.
     * @return The player's quest manager.
     */
    public static PlayerQuestManager getQuestManager(ServerPlayerEntity player) {
        if (!(player instanceof IStoryPlayer storyPlayer)) {
            throw new RuntimeException("Player is not a story player");
        }

        return storyPlayer.mwhrd$getQuestManager();
    }

    /**
     * Fetches the quest data for the given player.
     *
     * @param player The player to fetch the quest data for.
     * @return The player's quest data.
     */
    public static PlayerQuestData getQuestData(ServerPlayerEntity player) {
        return Players.getModel(player).getQuestData();
    }

    /**
     * Casts the player to the IPlayer interface.
     *
     * @param player The player to cast.
     * @return The player as an IPlayer.
     */
    public static IPlayer extend(ServerPlayerEntity player) {
        if (!(player instanceof IPlayer customPlayer)) {
            throw new RuntimeException("Player does not have MWHRD mixins.");
        }

        return customPlayer;
    }

    /**
     * Gets the guild instance for the player.
     *
     * @param player The player to get the guild for.
     * @return The guild instance for the player.
     */
    @Nullable
    public static GuildInstance getGuild(ServerPlayerEntity player) {
        var model = Players.getModel(player);
        return model.getGuild();
    }

    /**
     * Gets the guild instance for the player, or throws an exception if the player is not in a guild.
     *
     * @param player The player to get the guild for.
     * @return The guild instance for the player.
     * @throws NullPointerException if the player is not in a guild.
     */
    public static GuildInstance getGuildNotNull(ServerPlayerEntity player) throws NullPointerException {
        var guild = Players.getGuild(player);
        if (guild == null) {
            throw new NullPointerException("Player is not in a guild.");
        }
        return guild;
    }

    /**
     * Repairs the player's gear.
     * This is a recursive function.
     *
     * @param player The player to repair the gear for.
     * @param amount The amount of experience to repair the gear with.
     * @return The amount of experience left after repairing the gear.
     */
    public static int repairPlayerGears(ServerPlayerEntity player, int amount) {
        var optional = EnchantmentHelper.chooseEquipmentWith(EnchantmentEffectComponentTypes.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (optional.isPresent()) {
            int experience;

            var itemStack = optional.get().stack();

            int newDamage = EnchantmentHelper.getRepairWithExperience(player.getEntityWorld(), itemStack, amount);
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
    public static void addExperience(ServerPlayerEntity player, int experience) {
        // Try getting the player's guild.
        var model = Players.getModel(player);
        var guild = model.getGuild();

        // If the player is in a guild
        // ,and they have experience split enabled...
        if (guild != null && model.getGuildSplit() > 0) {
            /// ...add experience to the guild.
            var guildExperience = (int) Math.max(1, Math.floor(experience * (model.getGuildSplit() / 100f)));
            guild.addExperience(guildExperience);

            experience -= guildExperience;
        }

        var remaining = Players.repairPlayerGears(player, experience);
        if (remaining > 0) {
            player.addExperience(remaining);
        }
    }

    /**
     * Checks if the player is in an allowed world.
     *
     * @param player The player to check.
     * @return Whether the player is in an allowed world.
     */
    public static boolean inAllowedWorld(ServerPlayerEntity player) {
        var world = player.getEntityWorld().getRegistryKey();
        return Utils.ALLOWED_WORLDS.contains(world);
    }

    /**
     * Creates a head item stack for the player with the specified UUID.
     *
     * @param uuid The UUID of the player to create the head for.
     * @return The head item stack.
     */
    public static ItemStack headOf(UUID uuid) {
        return ItemBuilder.of(Items.PLAYER_HEAD)
            .component(
                DataComponentTypes.PROFILE,
                ProfileComponent.ofDynamic(uuid)
            )
            .build();
    }
}
