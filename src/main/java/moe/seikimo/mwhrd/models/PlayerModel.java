package moe.seikimo.mwhrd.models;

import com.google.gson.JsonObject;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PostLoad;
import dev.morphia.annotations.PrePersist;
import lombok.Data;
import moe.seikimo.data.DatabaseObject;
import moe.seikimo.general.JObject;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.ItemStorage;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity("players")
public final class PlayerModel implements DatabaseObject<PlayerModel> {
    @Id private String playerUuid;

    @ApiStatus.Internal
    private List<String> lootItems = new ArrayList<>();

    private boolean survivedHardcore = false;
    private boolean hardcore = false;
    private long hardcoreUntil = -1;

    private boolean banned = false;
    private long bannedUntil = -1;

    private transient ServerPlayerEntity handle;
    private transient ItemStorage loot = new ItemStorage();

    @VisibleForTesting
    @ApiStatus.Internal
    public PlayerModel() {
        // For Morphia.
    }

    @PrePersist
    public void beforeSave() {
        this.lootItems.clear();
        this.lootItems.addAll(this.loot.serialize());
    }

    @PostLoad
    public void afterLoad() {
        this.loot.deserialize(this.lootItems);
    }

    /**
     * Sets the player's handle.
     *
     * @param handle The player's handle.
     */
    public void setHandle(ServerPlayerEntity handle) {
        this.handle = handle;

        if (this.isBanned() &&
            System.currentTimeMillis() > this.bannedUntil) {
            this.unbanPlayer();
            handle.sendMessage(Text.literal("You are now unbanned!")
                .formatted(Formatting.GREEN));
        }

        var maxHealth = handle.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth == null) throw new IllegalStateException("Max health attribute is null.");

        if (this.isHardcore() &&
            System.currentTimeMillis() > this.hardcoreUntil) {
            this.unsetHardcore(true);
            maxHealth.setBaseValue(20);
        } else if (this.isHardcore()) {
            maxHealth.setBaseValue(40);
        }

        var interactionManager = handle.interactionManager;
        if (this.isBanned()) {
            interactionManager.changeGameMode(GameMode.SPECTATOR);
            this.reset();
        } else if (interactionManager.getGameMode() != GameMode.CREATIVE) {
            interactionManager.changeGameMode(GameMode.SURVIVAL);
        }
    }

    /// <editor-fold desc="Ban System">

    /**
     * Bans a player until the specified duration.
     *
     * @param duration The duration to ban the player for.
     */
    public void banPlayer(Duration duration) {
        this.banned = true;
        this.bannedUntil = System.currentTimeMillis() + duration.toMillis();

        if (this.handle != null) {
            this.handle.interactionManager.changeGameMode(GameMode.SPECTATOR);
            this.reset();
        }

        this.save();
    }

    /**
     * Unbans a player.
     */
    public void unbanPlayer() {
        this.banned = false;
        this.bannedUntil = -1;

        this.save();

        if (this.handle != null) {
            new Thread(() -> {
                try {
                    Thread.sleep((long) 1e3);
                } catch (InterruptedException ignored) { }

                var world = this.handle.getServerWorld();
                var pos = world.getSpawnPos();
                this.handle.teleport(world, pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);

                this.handle.interactionManager.changeGameMode(GameMode.SURVIVAL);
            }).start();
        }
    }

    /**
     * Helper method for resetting a player's inventory.
     */
    private void reset() {
        this.handle.getInventory().clear();
        this.handle.getEnderChestInventory().clear();

        this.handle.setSpawnPoint(
            World.OVERWORLD,
            MyWellHasRunDry.getDefaultSpawn(),
            0.0F, false, false
        );
    }

    /// </editor-fold>

    /// <editor-fold desc="Hardcore System">

    /**
     * Sets the player as hardcore.
     *
     * @param hardcoreUntil The duration to set the player as hardcore for.
     */
    public void setHardcore(Duration hardcoreUntil) {
        this.hardcore = true;
        this.hardcoreUntil = System.currentTimeMillis() + hardcoreUntil.toMillis();

        try {
            this.save();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (this.handle != null) {
            // Broadcast to the server.
            MyWellHasRunDry.getServer().getPlayerManager().broadcast(
                this.handle.getName().copy()
                    .formatted(Formatting.RED)
                    .append(Text.literal(" has enabled ")
                        .formatted(Formatting.RED))
                    .append(Text.literal("hardcore mode")
                        .formatted(Formatting.BOLD, Formatting.DARK_RED))
                    .append(Text.literal(".")
                        .formatted(Formatting.RED)),
                false
            );

            var maxHealth = this.handle.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (maxHealth == null) {
                throw new IllegalStateException("Max health attribute is null.");
            }

            maxHealth.setBaseValue(40);
        }
    }

    /**
     * Unsets the player as hardcore.
     */
    public void unsetHardcore(boolean survived) {
        this.hardcore = false;
        this.survivedHardcore = survived;
        this.hardcoreUntil = -1;

        this.save();

        if (this.handle != null) {
            if (survived) {
                this.handle.sendMessage(Text.literal("You survived hardcore mode!")
                    .formatted(Formatting.GREEN));
            }

            var maxHealth = this.handle.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (maxHealth == null) throw new IllegalStateException("Max health attribute is null.");

            maxHealth.setBaseValue(20);
        }
    }

    /// </editor-fold>

    @Override
    public JsonObject explain() {
        return JObject.c()
            .add("uuid", this.getPlayerUuid())
            .add("isBanned", this.isBanned())
            .add("bannedUntil", this.getBannedUntil())
            .gson();
    }
}
