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
import moe.seikimo.mwhrd.utils.items.ItemStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * This is a color code which identifies the guild.
     */
    @Id private Integer guildId;

    /** The name of the guild. */
    private String name;

    /** The string-serialized UUID of the owner. */
    private BasicPlayerInfo owner;

    /** The members of the guild. */
    private List<BasicPlayerInfo> members = new ArrayList<>();

    private DynamicItemStorage bank = new DynamicItemStorage(6, 8);

    private transient Formatting color;

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
        this.members.stream()
            .map(BasicPlayerInfo::toOnline)
            .filter(Objects::nonNull)
            .forEach(player -> player.sendMessage(Text.translatable("text.mwhrd.guild.disbanded")
                .formatted(Formatting.RED)));

        // Reset the guild.
        this.members.clear();
        this.owner = null;
        this.name = DEFAULT_NAMES.getOrDefault(this.color, "Guild");
        this.bank.clear();

        this.save();

        // Update the player list.
        GuildManager.doPlayerListUpdate();
    }

    @Override
    public JsonObject explain() {
        return JObject.c()
            .add("color", this.guildId)
            .add("name", this.name)
            .gson();
    }
}
