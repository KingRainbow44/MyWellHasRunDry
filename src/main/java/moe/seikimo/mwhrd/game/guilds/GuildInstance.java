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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
        while (experience >= experienceNeeded(level)) {
            experience -= experienceNeeded(level);
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

    /** The guild's level. */
    private int level = 0;
    private long experience = 0;

    /** Bank item storage. */
    private DynamicItemStorage bank = new DynamicItemStorage(6, 8);
    private Map<Integer, String> pageNames = new HashMap<>();

    @ApiStatus.Internal
    private Map<Integer, Integer> bankIcons = new HashMap<>();

    private transient Formatting color;
    private transient Map<Integer, Item> pageIcons = new HashMap<>();

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
    }

    @PostLoad
    public void afterLoad() {
        this.color = Formatting.byColorIndex(this.guildId);

        // Load all bank icons.
        for (var entry : this.bankIcons.entrySet()) {
            var item = Item.byRawId(entry.getValue());
            if (item != null) {
                this.pageIcons.put(entry.getKey(), item);
            }
        }
    }

    @PrePersist
    public void beforeSave() {
        // Serialize all bank icons.
        this.bankIcons.clear();
        for (var entry : this.pageIcons.entrySet()) {
            this.bankIcons.put(entry.getKey(), Item.getRawId(entry.getValue()));
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

        // Set the guild for the player.
        if (player instanceof IDBObject<?> dbObject) {
            var model = dbObject.mwhrd$getData();
            if (model instanceof PlayerModel playerModel) {
                playerModel.setGuild(this);
            }
        }

        // If the guild has no owner, assign an owner.
        if (this.owner == null) {
            this.owner = info;
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
        this.members.removeIf(info -> info.uuid()
            .equals(player.getUuidAsString()));

        // Unset the player's guild in the database.
        if (player instanceof IDBObject<?> dbObject) {
            var model = dbObject.mwhrd$getData();
            if (model instanceof PlayerModel playerModel) {
                playerModel.setGuild(null);
            }
        }

        // Check if the guild is vacant.
        if (this.members.isEmpty()) {
            // Reset the guild.
            this.disband();
            return;
        } else if (this.isOwner(player)) {
            // Transfer ownership to the next member.
            this.owner = this.members.getFirst();
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
        this.owner = null;
        this.name = DEFAULT_NAMES.getOrDefault(this.color, "Guild");
        this.bank.clear();
        this.level = 0;
        this.experience = 0;
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
        var newLevel = GuildInstance.calculateLevel(experience);

        // If the level has changed, broadcast the message.
        if (newLevel > this.level) {
            this.level = newLevel;

            this.broadcast(Text.translatable("text.mwhrd.guild.level.next", this.getDisplayName(), newLevel)
                .formatted(Formatting.AQUA));
        }
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
