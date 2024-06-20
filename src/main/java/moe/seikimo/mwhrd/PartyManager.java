package moe.seikimo.mwhrd;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class PartyManager {
    /** Map of Invited Player -> Party Leader */
    private static final Map<UUID, UUID> invites = new ConcurrentHashMap<>();

    private static final Map<UUID, List<ServerPlayerEntity>> parties = new ConcurrentHashMap<>();

    private static final Map<UUID, Position> lastLocations = new ConcurrentHashMap<>();

    /**
     * Finds a player within a party.
     *
     * @param player The player to find.
     * @return The party.
     */
    public static List<ServerPlayerEntity> findParty(ServerPlayerEntity player) {
        return parties.values().stream()
            .filter(party -> party.contains(player))
            .findFirst()
            .orElse(null);
    }

    /**
     * Finds the leader of the party.
     *
     * @param party The party to find the leader for.
     * @return The leader of the party.
     */
    public static UUID findLeader(List<ServerPlayerEntity> party) {
        return party.getFirst().getUuid();
    }

    /**
     * Creates a party with the specified leader.
     *
     * @param leader The leader of the party.
     */
    public static void createParty(ServerPlayerEntity leader) {
        var list = Collections.synchronizedList(new ArrayList<ServerPlayerEntity>());
        list.add(leader); // The leader is always the first element.

        parties.put(leader.getUuid(), list);
    }

    /**
     * Disbands the party of the specified player.
     *
     * @param player The player to disband the party for.
     */
    public static void disbandParty(ServerPlayerEntity player) {
        var players = parties.remove(player.getUuid());

        // Send the disband message.
        for (var member : players) {
            if (!member.equals(player)) {
                member.sendMessage(Text.literal("The party has been disbanded.")
                    .withColor(Color.RED.getRGB()));
            }

            PartyManager.removePlayer(member);
            if (lastLocations.containsKey(member.getUuid())) {
                member.sendMessage(Text.literal("Click to return to your last location.")
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/party return"
                    )))
                    .withColor(Color.YELLOW.getRGB()));
            }
        }
    }

    /**
     * Invites a player to the party.
     *
     * @param leader The party leader.
     * @param player The player to invite.
     */
    public static boolean invitePlayer(ServerPlayerEntity leader, ServerPlayerEntity player) {
        // Check if the party exists.
        if (!parties.containsKey(leader.getUuid())) {
            return false;
        }

        // Check if the player is already in a party.
        if (findParty(player) != null) {
            return false;
        }

        // Check if the player is already invited.
        if (invites.containsKey(player.getUuid())) {
            return false;
        }

        invites.put(player.getUuid(), leader.getUuid());
        player.sendMessage(Text.literal("You have received a party invite from ")
            .append(Text.literal(leader.getGameProfile().getName())
                .withColor(Color.YELLOW.getRGB()))
            .append(Text.literal(". Click to accept!")
                .withColor(Color.CYAN.getRGB()))
            .setStyle(Style.EMPTY.withClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept")
            ).withColor(Color.CYAN.getRGB())));

        return true;
    }

    /**
     * Accepts an invite to the party.
     *
     * @param player The player to accept the invite for.
     * @return Whether the invite was accepted.
     */
    public static boolean acceptInvite(ServerPlayerEntity player) {
        var leader = invites.remove(player.getUuid());
        if (leader == null) {
            return false;
        }

        var party = parties.get(leader);
        if (party == null) {
            return false;
        }

        party.add(player);
        for (var member : party) {
            member.sendMessage(Text.literal("Player ")
                .withColor(Color.GREEN.getRGB())
                .append(Text.literal(player.getGameProfile().getName()))
                .withColor(Color.YELLOW.getRGB())
                .append(Text.literal(" has joined the party."))
                .withColor(Color.GREEN.getRGB()));
            member.sendMessage(Text.literal("Party members: ")
                .withColor(Color.GREEN.getRGB())
                .append(Text.literal(party.stream()
                    .map(p -> p.getGameProfile().getName())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("")))
                .withColor(Color.YELLOW.getRGB()));
        }

        return true;
    }

    /**
     * Leaves the party.
     *
     * @param player The player to leave the party.
     */
    public static void leaveParty(ServerPlayerEntity player) {
        var party = findParty(player);
        if (party == null) {
            return;
        }

        party.remove(player);
        PartyManager.removePlayer(player);
        if (parties.containsKey(player.getUuid())) {
            // Reassign the party to the new leader.
            var leader = party.getFirst();
            parties.put(leader.getUuid(), party);
            parties.remove(player.getUuid());
        }

        // Teleport the player to their last location.
        PartyManager.returnPlayer(player);

        if (party.isEmpty()) {
            disbandParty(player);
        } else {
            for (var member : party) {
                member.sendMessage(Text.literal("Player ")
                    .withColor(Color.RED.getRGB())
                    .append(Text.literal(player.getGameProfile().getName()))
                    .withColor(Color.YELLOW.getRGB())
                    .append(Text.literal(" has left the party."))
                    .withColor(Color.RED.getRGB()));
                member.sendMessage(Text.literal("Party members: ")
                    .withColor(Color.GREEN.getRGB())
                    .append(Text.literal(party.stream()
                        .map(p -> p.getGameProfile().getName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("")))
                    .withColor(Color.YELLOW.getRGB()));
            }
        }
    }

    /**
     * Kicks a player from the party.
     *
     * @param leader The leader of the party.
     * @param target The player to kick.
     */
    public static void kickPlayer(ServerPlayerEntity leader, ServerPlayerEntity target) {
        var party = findParty(leader);
        if (party == null) {
            return;
        }

        party.remove(target);
        PartyManager.removePlayer(target);

        target.sendMessage(Text.literal("You have been removed from the party.")
            .withColor(Color.RED.getRGB()));

        for (var member : party) {
            member.sendMessage(Text.literal("Player ")
                .withColor(Color.RED.getRGB())
                .append(Text.literal(target.getGameProfile().getName()))
                .withColor(Color.YELLOW.getRGB())
                .append(Text.literal(" has been kicked from the party."))
                .withColor(Color.RED.getRGB()));
            member.sendMessage(Text.literal("Party members: ")
                .withColor(Color.GREEN.getRGB())
                .append(Text.literal(party.stream()
                    .map(p -> p.getGameProfile().getName())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("")))
                .withColor(Color.YELLOW.getRGB()));
        }
    }

    /**
     * Declines an invite to the party.
     *
     * @param player The player to decline the invite for.
     */
    public static boolean declineInvite(ServerPlayerEntity player) {
        return invites.remove(player.getUuid()) != null;
    }

    /**
     * Warps the party to the leader.
     *
     * @param player The player to warp.
     * @return Whether the party was warped.
     */
    public static boolean warpParty(ServerPlayerEntity player) {
        var party = findParty(player);
        if (party == null) {
            return false;
        }

        // Check if the player is the leader.
        var leaderId = findLeader(party);
        if (!leaderId.equals(player.getUuid())) {
            return false;
        }

        // Store the last location of each player.
        for (var member : party) {
            if (lastLocations.containsKey(member.getUuid())) {
                continue;
            }

            lastLocations.put(member.getUuid(), new Position(
                member.getServerWorld(),
                member.getBlockPos()
            ));
        }

        // Teleport all players to the leader.
        var leader = party.getFirst();
        var server = MyWellHasRunDry.getServer();

        for (var member : party) {
            if (!member.equals(leader)) {
                member.sendMessage(Text.literal("You have been warped to the party leader.")
                    .withColor(Color.GREEN.getRGB()));
                member.sendMessage(Text.literal("You can return to your previous location by leaving.")
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/party leave"
                    )))
                    .withColor(Color.YELLOW.getRGB()));
            }

            member.teleport(
                leader.getServerWorld(),
                leader.getX(), leader.getY(), leader.getZ(),
                Collections.emptySet(), leader.getYaw(), leader.getPitch()
            );

            // Create a scoreboard team for the player.
            // This is used to change the player's glow color.
            var memberName = member.getGameProfile().getName();
            var scoreboard = server.getScoreboard();
            if (scoreboard.getTeam(memberName) != null) {
                scoreboard.removeTeam(scoreboard.getTeam(memberName));
            }

            var team = scoreboard.addTeam(memberName);
            team.setColor(switch (Math.abs(memberName.hashCode()) % 6) {
                case 0 -> Formatting.RED;
                case 1 -> Formatting.GREEN;
                case 2 -> Formatting.BLUE;
                case 3 -> Formatting.YELLOW;
                case 4 -> Formatting.AQUA;
                case 5 -> Formatting.LIGHT_PURPLE;
                default -> Formatting.WHITE;
            });
            scoreboard.addScoreHolderToTeam(memberName, team);

            // Set the player glowing.
            member.setGlowing(true);
        }

        return true;
    }

    /**
     * Returns the player to their last location.
     *
     * @param player The player to return.
     */
    public static boolean returnPlayer(ServerPlayerEntity player) {
        player.setGlowing(false);
        PartyManager.removePlayer(player);

        var lastLocation = lastLocations.remove(player.getUuid());
        if (lastLocation != null) {
            var position = lastLocation.getPos();
            player.teleport(
                lastLocation.getWorld(),
                position.getX(), position.getY(), position.getZ(),
                Collections.emptySet(), player.getYaw(), player.getPitch()
            );
            return true;
        }

        return false;
    }

    /**
     * Removes the player's unique colored team.
     */
    public static void removePlayer(ServerPlayerEntity player) {
        var scoreboard = MyWellHasRunDry.getServer().getScoreboard();
        var team = scoreboard.getTeam(player.getGameProfile().getName());
        if (team != null) {
            scoreboard.removeTeam(team);
        }
    }
}
