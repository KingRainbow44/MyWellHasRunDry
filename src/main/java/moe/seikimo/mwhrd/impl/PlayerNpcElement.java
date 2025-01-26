package moe.seikimo.mwhrd.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.PlayerList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class PlayerNpcElement extends GenericEntityElement {
    private final GameProfile profile;

    /**
     * Creates a player NPC with the default username 'NPC'.
     */
    public PlayerNpcElement() {
        this("NPC");
    }

    /**
     * Creates a player NPC with the given username.
     *
     * @param username The name of the player.
     */
    public PlayerNpcElement(String username) {
        this.profile = new GameProfile(this.getUuid(), username);
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        packetConsumer.accept(this.createListPacket());
        super.startWatching(player, packetConsumer);
    }

    /**
     * Alerts all viewers to re-track the NPC.
     */
    public void refresh() {
        var holder = this.getHolder();
        if (holder == null) {
            return;
        }

        holder.getWatchingPlayers().forEach(networkHandler ->
            networkHandler.sendPacket(this.createListPacket()));
    }

    /**
     * Sets the skin property of the NPC.
     *
     * @param existing The existing game profile to copy the skin from.
     */
    public void setSkin(GameProfile existing) {
        var collection = existing.getProperties().get("textures");
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("The existing profile does not have a skin");
        }

        var textures = collection.iterator().next();
        this.profile.getProperties().put("textures", textures);

        this.refresh();
    }

    /**
     * Sets the skin property of the NPC.
     *
     * @param username The username of the player to copy the skin from.
     */
    public void setSkin(String username) {
        var cache = MyWellHasRunDry.getServer().getUserCache();
        Objects.requireNonNull(cache, "User cache is not available");

        try {
            var request = cache.findByNameAsync(username);
            var profile = request.get();

            if (profile.isEmpty()) {
                throw new IllegalArgumentException("Invalid username");
            }

            // Set the skin of the NPC.
            this.setSkin(profile.get());
        } catch (InterruptedException | ExecutionException ignored) {
            throw new RuntimeException("Failed to retrieve the player profile");
        }
    }

    /**
     * Sets the skin property of the NPC.
     *
     * @param texture The texture of the skin.
     * @param signature The signature of the skin.
     */
    public void setSkin(String texture, String signature) {
        var property = new Property("textures", texture, signature);
        this.profile.getProperties().put("textures", property);

        this.refresh();
    }

    /**
     * Creates the player list packet for the NPC.
     * <p>
     * NOTE: This is required in order for the client to render the NPC.
     * "Server attempted to add player prior to sending player info (Player id [this.getUuid()])"
     *
     * @return The player list packet.
     */
    private PlayerListS2CPacket createListPacket() {
        var entry = new PlayerListS2CPacket.Entry(
            this.getUuid(), this.profile,
            false, 0, GameMode.SURVIVAL, null, false, -1, null
        );

        return PlayerList.create(PlayerList.NEW_ACTIONS, List.of(entry));
    }
}
