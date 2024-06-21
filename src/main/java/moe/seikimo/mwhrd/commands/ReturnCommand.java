package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class ReturnCommand {
    /**
     * Registers the command with the dispatcher.
     *
     * @param dispatcher The dispatcher to register the command with.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("return")
            .requires(ServerCommandSource::isExecutedByPlayer)
            .executes(ReturnCommand::returnToSpawn));
    }

    private static int returnToSpawn(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return 1;

        if (!MyWellHasRunDry.getTrialChamberPredicate().test(
            player.getServerWorld(), player.getX(),
            player.getY(), player.getZ())) {
            context.getSource().sendError(Text.literal("You cannot return to spawn here."));
        } else {
            if (player.getHealth() != player.getMaxHealth()) {
                context.getSource().sendError(Text.literal("You must be full health to return!"));
                return 1;
            }

            var spawnPoint = player.getSpawnPointPosition();
            if (spawnPoint == null) {
                spawnPoint = player.getWorld().getSpawnPos();
            }

            player.teleport(
                player.getServerWorld(),
                spawnPoint.getX(),
                spawnPoint.getY(),
                spawnPoint.getZ(),
                player.getYaw(),
                player.getPitch()
            );
        }

        return 1;
    }
}
