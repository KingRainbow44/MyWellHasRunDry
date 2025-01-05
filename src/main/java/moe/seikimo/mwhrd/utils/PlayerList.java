package moe.seikimo.mwhrd.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Use <a href="https://mineskin.org">MineSkin</a> to get textures and signatures.
 */
public interface PlayerList {
    /**
     * All actions required to add a new player.
     */
    EnumSet<Action> NEW_ACTIONS = EnumSet.of(
        Action.ADD_PLAYER,
        Action.INITIALIZE_CHAT,
        Action.UPDATE_GAME_MODE,
        Action.UPDATE_LISTED,
        Action.UPDATE_LATENCY,
        Action.UPDATE_DISPLAY_NAME,
        Action.UPDATE_HAT,
        Action.UPDATE_LIST_ORDER
    );

    /**
     * Sourced from <a href="https://minecraft-heads.com/custom-heads/decoration/1121-dark-gray-head">Minecraft Heads</a>.
     */
    String DARK_GRAY_HEAD = "ewogICJ0aW1lc3RhbXAiIDogMTY0NDcwNTExNjQ2OCwKICAicHJvZmlsZUlkIiA6ICJmZDQ3Y2I4YjgzNjQ0YmY3YWIyYmUxODZkYjI1ZmMwZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDEyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ZmOWJiOWU1NjEyNWM4MjI3Yjk0YmJkYTlmNmUwZjg2MjkzMWMyMjkyNTViYThmMTIwNWQxM2M0NGMxYmI1NjEiCiAgICB9CiAgfQp9";
    String DARK_GRAY_SIGN = "D24yzbg+aBETxe5e+acQR8xJwBkhf8+CdkNYi1ufu3NgXk6YK67dIij8o3QtMx/y3rR6xupRq7bKHUGGgkw+joCC/mtG6yDdLbD32s//VAhA+VVDbIQq/CJrJ8oYarerElTjOF08zxQCw8n97cfI10gkoZvdTDouRfTfQYIIo6vvG9kTGyAJv7mIriTvxE/nwP3m6WlwRmtKWOqDhiMRNoWwo9btCp5JTZR9HVFaZdsNQvh6gUmjBqHoKtr/xWOVveEhQ5mc8WZh0dAiiC3Astfr0VIx7HW1+xNu+Z7xvRMgbZ+SbKuRwotW2KHCN+BDymTbiQ3GBljjXDjwFao0sBHQ24DjafWQcuEEWNsDnhDHtmG3tKdvGQbZ1bYhh97EjRYKXG+eZKMrFGG4jr9oCg0JD3JMBc88Z0mJWyKzPF9B+klFocmrFBF/UgkQnzkNShfkpC6RjUfCymrnAFAoV6XBcznbKQzyKKAMeNE3LPFZ3iS2Tygbrqo2Sjmq9zGpjva04RxWHJ1oeKzROQkge0z96AOO7ChTFTXqnNnAjdkfW2TjK7pSIwS0vMGsUgm1C/amzMpZdJuI0FXFEzz1jhFi5cdwHXSQY1gVpa4VTLNQvu1xgcnbOVJaV0Ty+AebI2s6CLt6OcpI3QKY+KPlITuwj5HydMiQvfYldhiHPjc=";

    /**
     * Creates a fake player entry for the tab list.
     * <p />
     * By specifying a blank head texture and signature, the player will use the head of the UUID given.
     *
     * @param name The name of the player.
     * @param displayName The display name of the player.
     * @param order The order of the player in the list.
     * @param headTexture The texture of the player's head.
     * @param headSignature The signature of the player's head.
     * @return The fake player entry.
     */
    static PlayerListS2CPacket.Entry fakePlayer(
        String name, @Nullable Text displayName, int order,
        @Nullable String headTexture, @Nullable String headSignature
    ) {
        // Generate a UUID based on the index.
        var uuid = UUID.randomUUID();
        if (order != 0) {
            uuid = Utils.uuidFromIndex(order);
        }

        var gameProfile = new GameProfile(uuid, name);

        // If a head texture was specified, add it.
        if (headTexture != null) {
            var property = new Property("textures", headTexture, headSignature);
            gameProfile.getProperties().put("textures", property);
        }

        return new PlayerListS2CPacket.Entry(
            uuid, gameProfile, true, 0,
            GameMode.SURVIVAL, displayName,
            false, order, null
        );
    }

    /**
     * Creates a real player entry for the tab list.
     *
     * @param player The player to create an entry for.
     * @param displayName The display name of the player.
     * @param order The order of the player in the list.
     * @return The real player entry.
     */
    static PlayerListS2CPacket.Entry realPlayer(
        ServerPlayerEntity player,
        @Nullable Text displayName,
        int order
    ) {
        if (displayName == null) {
            displayName = player.getPlayerListName();
        }

        return new PlayerListS2CPacket.Entry(
            player.getUuid(), player.getGameProfile(), true,
            player.networkHandler.getLatency(),
            player.interactionManager.getGameMode(),
            displayName,
            player.isPartVisible(PlayerModelPart.HAT),
            order,
            Nullables.map(player.getSession(), PublicPlayerSession::toSerialized)
        );
    }

    /**
     * Creates a player list packet with raw entries.
     *
     * @param actions The actions to perform.
     * @param entries The entries to include.
     * @return The player list packet.
     */
    @SneakyThrows
    static PlayerListS2CPacket create(EnumSet<Action> actions, List<PlayerListS2CPacket.Entry> entries) {
        var packet = new PlayerListS2CPacket(actions, List.of());
        packet.entries = entries;

        return packet;
    }

    /**
     * Removes all players from the server list.
     *
     * @param networkHandler The network handler to remove players from.
     */
    static void removeAllPlayers(ServerPlayNetworkHandler networkHandler) {
        // Get the UUIDs of the fake players.
        var uuids = new ArrayList<UUID>();
        for (var i = 0; i < 80; i++) {
            uuids.add(Utils.uuidFromIndex(i));
        }

        // Get all UUIDs of online players.
        var playerUuids = MyWellHasRunDry.getServer()
            .getPlayerManager()
            .getPlayerList()
            .stream()
            .map(ServerPlayerEntity::getUuid)
            .toList();
        uuids.addAll(playerUuids);

        // Remove all players from the list.
        var packet = new PlayerRemoveS2CPacket(uuids);
        networkHandler.sendPacket(packet);
    }
}
