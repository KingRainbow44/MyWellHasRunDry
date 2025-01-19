package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import moe.seikimo.mwhrd.game.guilds.GuildManager;
import moe.seikimo.mwhrd.gui.guild.GuildBankSelectorGui;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GuildCommand {
    /**
     * Registers the guild command.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var guild = dispatcher.register(literal("guild")
            .requires(ServerCommandSource::isExecutedByPlayer)
            .then(literal("join")
                .then(argument("guild", greedyString())
                    .executes(GuildCommand::joinGuild)))
            .then(literal("leave")
                .executes(GuildCommand::leaveGuild))
            .then(literal("rename")
                .then(argument("name", greedyString())
                    .executes(GuildCommand::renameGuild)))
            .then(literal("disband")
                .executes(GuildCommand::disbandGuild))
            .then(literal("bank")
                .executes(GuildCommand::openGuildBank))
            .then(literal("split")
                .then(argument("amount", integer())
                    .executes(GuildCommand::experienceSplit)))
        );

        // Register '/g' alias.
        dispatcher.register(literal("g").redirect(guild));
    }

    /**
     * Adds a player to the specified guild.
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code guild} The name of the guild's color.</li>
     * </ul>
     */
    private static int joinGuild(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = Objects.requireNonNull(source.getPlayer());

        // Join the guild using the guild manager.
        var guildName = context.getArgument("guild", String.class);

        // Parse the guild name.
        var guildColor = Formatting.byName(guildName);
        if (guildColor == null) {
            guildColor = GuildManager.getGuildByName(guildName);
        }
        if (guildColor == null) {
            source.sendMessage(Text.translatable("commands.guild.join.invalid_guild"));
            return 0;
        }

        var guild = GuildManager.getGuild(guildColor);

        guild.addMember(player);

        source.sendMessage(Text.translatable("commands.guild.join.success", guild.getDisplayName()));

        return 1;
    }

    /**
     * Removes a player from their guild.
     */
    private static int leaveGuild(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var player = Objects.requireNonNull(source.getPlayer());

        // Leave the guild using the guild manager.
        var guild = GuildManager.getGuild(player);
        if (guild == null) {
            source.sendMessage(Text.translatable("commands.guild.not_in_guild"));
            return 0;
        }

        guild.removeMember(player);

        source.sendMessage(Text.translatable("commands.guild.leave.success", guild.getDisplayName()));

        return 1;
    }

    /**
     * Renames the guild the player is in.
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code name} The new name of the guild.</li>
     * </ul>
     */
    private static int renameGuild(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var player = Objects.requireNonNull(source.getPlayer());

        // Rename the guild using the guild manager.
        var guild = GuildManager.getGuild(player);
        if (guild == null) {
            source.sendMessage(Text.translatable("commands.guild.not_in_guild"));
            return 0;
        }

        // Check if the player is the guild owner.
        if (!guild.isOwner(player)) {
            source.sendError(Text.translatable("commands.guild.no_permission"));
            return 0;
        }

        var name = context.getArgument("name", String.class);
        guild.rename(name);

        source.sendMessage(Text.translatable("commands.guild.rename.success", guild.getDisplayName()));

        return 1;
    }

    /**
     * Disbands the guild the player is in.
     */
    private static int disbandGuild(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var player = Objects.requireNonNull(source.getPlayer());

        // Disband the guild using the guild manager.
        var guild = GuildManager.getGuild(player);
        if (guild == null) {
            source.sendMessage(Text.translatable("commands.guild.not_in_guild"));
            return 0;
        }

        // Check if the player is the guild owner.
        if (!guild.isOwner(player)) {
            source.sendError(Text.translatable("commands.guild.no_permission"));
            return 0;
        }

        guild.disband();

        return 1;
    }

    /**
     * Opens the guild bank GUI for the player.
     */
    private static int openGuildBank(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var player = Objects.requireNonNull(source.getPlayer());

        // Check if the player is in an allowed dimension.
        if (!Players.inAllowedWorld(player)) {
            source.sendMessage(Text.translatable("commands.guild.bank.not_allowed")
                .formatted(Formatting.RED));
            return 0;
        }

        // Open the guild bank using the guild manager.
        var guild = GuildManager.getGuild(player);
        if (guild == null) {
            source.sendMessage(Text.translatable("commands.guild.not_in_guild")
                .formatted(Formatting.RED));
            return 0;
        }

        // Open the guild bank GUI.
        GuildBankSelectorGui.open(guild, player);

        return 1;
    }

    /**
     * Sets the amount of experience to split between guild members.
     */
    private static int experienceSplit(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var player = Objects.requireNonNull(source.getPlayer());
        var model = Players.getModel(player);

        // Open the guild bank using the guild manager.
        var guild = GuildManager.getGuild(player);
        if (guild == null) {
            source.sendMessage(Text.translatable("commands.guild.not_in_guild")
                .formatted(Formatting.RED));
            return 0;
        }

        // Parse the split.
        var amount = context.getArgument("amount", Integer.class);
        if (amount < 0 || amount > 100) {
            source.sendMessage(Text.translatable("commands.guild.split.invalid_amount")
                .formatted(Formatting.RED));
            return 0;
        }

        model.setGuildSplit(amount);
        model.save();

        source.sendMessage(Text.translatable("commands.guild.split.set", String.valueOf(amount))
            .formatted(Formatting.AQUA));

        return 1;
    }
}
