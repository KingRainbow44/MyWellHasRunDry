package moe.seikimo.mwhrd.game.guilds;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PostLoad;
import dev.morphia.annotations.PrePersist;
import lombok.Data;
import moe.seikimo.data.DatabaseObject;
import moe.seikimo.general.JObject;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.BasicPlayerInfo;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.utils.Maps;
import moe.seikimo.mwhrd.utils.items.DynamicItemStorage;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

@Data
@Entity("guilds")
public final class GuildInstance implements DatabaseObject<GuildInstance> {
    private static final SimpleCommandExceptionType ALREADY_IN_GUILD = new SimpleCommandExceptionType(Text.translatable("commands.guild.join.already_in_guild"));

    private static final Map<Formatting, String> DEFAULT_NAMES = Maps.guildNames()
        .put(Formatting.RED, "Red Team")
        .put(Formatting.GOLD, "Gold Team")
        .put(Formatting.YELLOW, "Yellow Team")
        .put(Formatting.GREEN, "Green Team")
        .put(Formatting.AQUA, "Aqua Team")
        .put(Formatting.BLUE, "Blue Team")
        .put(Formatting.LIGHT_PURPLE, "Light Purple Team")
        .put(Formatting.WHITE, "White Team")
        .build();

    private static final Map<Formatting, BossBar.Color> COLOR_MAP = Maps.bossBarColors()
        .put(Formatting.RED, BossBar.Color.RED)
        .put(Formatting.GOLD, BossBar.Color.YELLOW)
        .put(Formatting.YELLOW, BossBar.Color.YELLOW)
        .put(Formatting.GREEN, BossBar.Color.GREEN)
        .put(Formatting.AQUA, BossBar.Color.BLUE)
        .put(Formatting.BLUE, BossBar.Color.BLUE)
        .put(Formatting.LIGHT_PURPLE, BossBar.Color.PURPLE)
        .put(Formatting.WHITE, BossBar.Color.WHITE)
        .build();

    /**
     * Calculates the experience needed for the next level.
     *
     * @param level The current level.
     * @return The experience needed for the next level.
     */
    public static int experienceNeeded(int level) {
        return (int) (100 * Math.pow(level, 2));
    }

    /**
     * Calculates the experience required to level up.
     *
     * @param level The current level.
     * @return The experience required to level up.
     */
    public static int experienceRemaining(int level) {
        return experienceNeeded(level) - experienceNeeded(level - 1);
    }

    /**
     * Calculates the level from the experience.
     *
     * @param experience The experience to calculate.
     * @return The level.
     */
    public static int calculateLevel(long experience) {
        var level = 0;
        while (experience >= experienceNeeded(level + 1)) {
            level++;
        }

        return level;
    }

    /**
     * This is a color code which identifies the guild.
     */
    @Id private Integer guildId;

    /** The name of the guild. */
    private String name;

    /** The string-serialized UUID of the owner. */
    private BasicPlayerInfo owner;

    /** The members of the guild. */
    private List<BasicPlayerInfo> members = new ArrayList<>();
    private Map<String, GuildPermission> permissions = new HashMap<>();

    /** The guild's level. */
    private int level = 0;
    private long experience = 0;

    /** Bank item storage. */
    private DynamicItemStorage bank = new DynamicItemStorage(6, 8);
    private Map<Integer, String> pageNames = new HashMap<>();

    @ApiStatus.Internal
    private Map<Integer, String> bankIcons = new HashMap<>();

    private transient Formatting color;
    private transient Map<Integer, Item> pageIcons = new HashMap<>();

    private transient ServerBossBar experienceBar;

    @VisibleForTesting
    @ApiStatus.Internal
    public GuildInstance() {
        this(Formatting.WHITE);

        // For Morphia.
    }

    /**
     * Constructor for a new guild instance.
     *
     * @param color The color of the guild.
     */
    public GuildInstance(Formatting color) {
        this.guildId = color.getColorIndex();
        this.color = color;
        this.name = DEFAULT_NAMES.getOrDefault(color, "Guild");

        // Create a new experience bar.
        this.experienceBar = new ServerBossBar(
            Text.empty(),
            COLOR_MAP.get(this.color),
            BossBar.Style.NOTCHED_10
        );

        this.updateExperienceBar();
    }

    @PostLoad
    public void afterLoad() {
        this.color = Formatting.byColorIndex(this.guildId);

        // Load all bank icons.
        for (var entry : this.bankIcons.entrySet()) {
            var identifier = Identifier.of(entry.getValue());
            var item = Registries.ITEM.get(identifier);
            this.pageIcons.put(entry.getKey(), item);
        }

        // Set experience bar color.
        this.experienceBar.setColor(COLOR_MAP.get(this.color));

        this.updateExperienceBar();
    }

    @PrePersist
    public void beforeSave() {
        // Serialize all bank icons.
        this.bankIcons.clear();
        for (var entry : this.pageIcons.entrySet()) {
            var item = entry.getValue();
            var identifier = Registries.ITEM.getId(item);
            this.bankIcons.put(entry.getKey(), identifier.toString());
        }
    }

    /**
     * @return The display title used in the player list.
     */
    public Text getDisplayName() {
        return Text.literal(this.name)
            .formatted(Formatting.BOLD, this.color);
    }

    /**
     * @return The text component for the boss bar display.
     */
    private Text getBarName() {
        var displayName = Text.literal(this.name)
            .formatted(this.color);

        return displayName
            .append(Text.literal(" Lv")
                .formatted(Formatting.DARK_AQUA))
            .append(this.getBarLevel());
    }

    /**
     * Formats the level and experience for the boss bar.
     *
     * @return The formatted text.
     */
    private Text getBarLevel() {
        var current = this.experience - experienceNeeded(this.level);
        var total = experienceRemaining(this.level + 1);

        return Text.literal(this.level + " ")
            .formatted(Formatting.DARK_AQUA)
            .append(Text.literal("(" + current + " / " + total + ")")
                .formatted(Formatting.AQUA));
    }

    /**
     * Retrieves the members of the guild.
     * This list is sorted by those who are online first.
     *
     * @return The members of the guild.
     */
    public List<BasicPlayerInfo> getMembers() {
        var members = new ArrayList<>(this.members);
        members.sort((a, b) -> {
            var aOnline = a.isOnline();
            var bOnline = b.isOnline();

            if (aOnline && !bOnline) {
                return -1;
            } else if (!aOnline && bOnline) {
                return 1;
            }

            return 0;
        });

        return members;
    }

    /**
     * @param player The player to check.
     * @return If the player is the owner of the guild.
     */
    public boolean isOwner(PlayerEntity player) {
        var owner = this.getOwner();
        if (owner == null) {
            return false;
        }

        return owner.uuid().equals(player.getUuidAsString());
    }

    /**
     * @param player The player to check.
     * @return If the player is the owner of the guild.
     */
    public boolean isOwner(BasicPlayerInfo player) {
        var owner = this.getOwner();
        if (owner == null) {
            return false;
        }

        return owner.uuid().equals(player.uuid());
    }

    /**
     * Checks if a player is a member of the guild.
     *
     * @param player The player to check.
     * @return If the player is a member of the guild.
     */
    public boolean isMember(PlayerEntity player) {
        return this.members.stream()
            .anyMatch(info -> info.uuid().equals(player.getUuidAsString()));
    }

    /**
     * Checks if a player has a specific permission.
     *
     * @param player The player to check.
     * @param permission The permission to check.
     * @return If the player has the permission.
     */
    public boolean hasPermission(PlayerEntity player, GuildPermission permission) {
        return this.permissions
            .getOrDefault(player.getUuidAsString(), GuildPermission.RECRUIT)
            .hasPermission(permission);
    }

    /**
     * Checks if a player has a specific permission.
     *
     * @param player The player to check.
     * @param permission The permission to check.
     * @return If the player has the permission.
     */
    public boolean hasPermission(BasicPlayerInfo player, GuildPermission permission) {
        return this.permissions
            .getOrDefault(player.uuid(), GuildPermission.RECRUIT)
            .hasPermission(permission);
    }

    /**
     * Retrieves the permission of a player.
     *
     * @param player The player to check.
     * @return The permission of the player.
     */
    public GuildPermission getPermission(PlayerEntity player) {
        return this.permissions.getOrDefault(player.getUuidAsString(), GuildPermission.RECRUIT);
    }

    /**
     * Retrieves the permission of a player.
     *
     * @param player The player to check.
     * @return The permission of the player.
     */
    public GuildPermission getPermission(BasicPlayerInfo player) {
        return this.permissions.getOrDefault(player.uuid(), GuildPermission.RECRUIT);
    }

    public void setPermission(BasicPlayerInfo player, GuildPermission permission) {
        var oldPermission = this.permissions.put(player.uuid(), permission);
        var demoted = oldPermission != null && oldPermission.ordinal() > permission.ordinal();

        this.broadcast(Text.translatable(
            "text.mwhrd.guild.permission.set",
            player.username(),
            demoted ? "demoted" : "promoted",
            permission.toString()
        ).formatted(Formatting.AQUA));
    }

    /**
     * Sets the permission of a player.
     *
     * @param player The player to set the permission for.
     * @param permission The permission to set.
     */
    public void setPermission(PlayerEntity player, GuildPermission permission) {
        this.setPermission(BasicPlayerInfo.from(player), permission);
    }

    /**
     * Adds a new member to the guild.
     *
     * @param player The player to add.
     */
    public void addMember(ServerPlayerEntity player) throws CommandSyntaxException {
        // Check if the player is already in a guild.
        if (GuildManager.inGuild(player)) {
            throw ALREADY_IN_GUILD.create();
        }

        // Add the member.
        var info = BasicPlayerInfo.from(player);
        this.members.add(info);
        this.permissions.put(info.uuid(), GuildPermission.RECRUIT);

        // Set the guild for the player.
        if (player instanceof IDBObject<?> dbObject) {
            var model = dbObject.mwhrd$getData();
            if (model instanceof PlayerModel playerModel) {
                playerModel.setGuild(this);
            }
        }

        // Add the player to the experience bar.
        this.experienceBar.addPlayer(player);

        // If the guild has no owner, assign an owner.
        if (this.owner == null) {
            this.owner = info;
            this.permissions.put(info.uuid(), GuildPermission.OWNER);
        }

        this.save();

        // Update the player list.
        GuildManager.doPlayerListUpdate();
    }

    /**
     * Removes a member from the guild.
     *
     * @param player The player to remove.
     */
    public void removeMember(ServerPlayerEntity player) {
        var uuid = player.getUuidAsString();

        this.members.removeIf(info -> info.uuid().equals(uuid));
        this.permissions.remove(uuid);

        // Unset the player's guild in the database.
        if (player instanceof IDBObject<?> dbObject) {
            var model = dbObject.mwhrd$getData();
            if (model instanceof PlayerModel playerModel) {
                playerModel.setGuild(null);
            }
        }

        // Remove the player from the experience bar.
        this.experienceBar.removePlayer(player);

        // Check if the guild is vacant.
        if (this.members.isEmpty()) {
            // Reset the guild.
            this.disband();
            return;
        } else if (this.isOwner(player)) {
            // Transfer ownership to the next member.
            this.owner = this.members.getFirst();
            this.permissions.put(this.owner.uuid(), GuildPermission.OWNER);
        }

        this.save();

        // Update the player list.
        GuildManager.doPlayerListUpdate();
    }

    /**
     * Renames the guild.
     *
     * @param newName The new name of the guild.
     */
    public void rename(String newName) {
        this.name = newName;
        this.save();

        this.updateExperienceBar();

        // Update the player list.
        GuildManager.doPlayerListUpdate();
    }

    /**
     * Disbands the guild.
     */
    public void disband() {
        // Broadcast the message to all online members.
        this.broadcast(Text.translatable("text.mwhrd.guild.disbanded")
            .formatted(Formatting.RED));

        // Reset the guild.
        this.reset();
        this.save();

        // Update the player list.
        GuildManager.doPlayerListUpdate();
    }

    /**
     * Resets the guild.
     */
    public void reset() {
        this.members.clear();
        this.permissions.clear();
        this.owner = null;
        this.name = DEFAULT_NAMES.getOrDefault(this.color, "Guild");
        this.bank.clear();
        this.level = 0;
        this.experience = 0;
        this.experienceBar.clearPlayers();
        this.updateExperienceBar();
    }

    /**
     * Broadcasts a message to all online members.
     *
     * @param message The message to broadcast.
     */
    public void broadcast(Text message) {
        this.members.stream()
            .map(BasicPlayerInfo::toOnline)
            .filter(Objects::nonNull)
            .forEach(player -> player.sendMessage(message));
    }

    /**
     * Adds experience to the guild.
     *
     * @param experience The experience to add.
     */
    public void addExperience(int experience) {
        this.experience += experience;
        var newLevel = GuildInstance.calculateLevel(this.experience);

        // If the level has changed, broadcast the message.
        if (newLevel > this.level) {
            this.level = newLevel;

            this.broadcast(Text.translatable("text.mwhrd.guild.level.next", this.getDisplayName(), newLevel)
                .formatted(Formatting.AQUA));
        }

        this.updateExperienceBar();
    }

    /**
     * Updates the boss bar.
     */
    public void updateExperienceBar() {
        // Update the experience bar name.
        this.experienceBar.setName(this.getBarName());

        // Set the experience bar progress.
        var current = this.experience - experienceNeeded(this.level);
        var total = experienceRemaining(this.level + 1);
        this.experienceBar.setPercent((float) current / total);
    }

    @Override
    public JsonObject explain() {
        return JObject.c()
            .add("color", this.guildId)
            .add("name", this.name)
            .add("level", this.level)
            .add("experience", this.experience)
            .gson();
    }
}
