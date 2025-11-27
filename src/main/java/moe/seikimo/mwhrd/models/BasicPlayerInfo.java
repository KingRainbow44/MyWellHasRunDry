package moe.seikimo.mwhrd.models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

@Entity
public record BasicPlayerInfo(
    @Id String uuid,
    String username
) {
    /**
     * Gets the UUID of the player.
     *
     * @return The UUID of the player.
     */
    public UUID getUUID() {
        return UUID.fromString(this.uuid);
    }

    /**
     * Checks if the player is online.
     *
     * @return {@code true} if the player is online, {@code false} otherwise.
     */
    public boolean isOnline() {
        return this.toOnline() != null;
    }

    /**
     * Converts the player to an online player.
     *
     * @return The online player.
     */
    public ServerPlayerEntity toOnline() {
        return MyWellHasRunDry.getServer()
            .getPlayerManager()
            .getPlayer(UUID.fromString(this.uuid));
    }

    /**
     * Creates a new instance of {@link BasicPlayerInfo} from a {@link PlayerEntity}.
     *
     * @param player The player to create the instance from.
     * @return The new instance.
     */
    public static BasicPlayerInfo from(PlayerEntity player) {
        return new BasicPlayerInfo(
            player.getUuidAsString(),
            player.getGameProfile().name()
        );
    }

    /**
     * Checks if a player is contained in a list of {@link BasicPlayerInfo}.
     *
     * @param list The list to check.
     * @param player The player to check for.
     * @return {@code true} if the player is contained in the list, {@code false} otherwise.
     */
    public static boolean contains(
        List<BasicPlayerInfo> list, PlayerEntity player
    ) {
        return list.stream().anyMatch(info -> info.uuid().equals(player.getUuidAsString()));
    }
}
