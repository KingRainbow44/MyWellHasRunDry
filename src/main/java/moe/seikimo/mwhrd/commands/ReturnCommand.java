package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;

import static net.minecraft.server.command.CommandManager.literal;

public final class ReturnCommand {
    /**
     * Registers the command with the dispatcher.
     *
     * @param dispatcher The dispatcher to register the command with.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var back = dispatcher.register(literal("back")
            .executes(ReturnCommand::returnToSpawn));
        dispatcher.register(literal("return").redirect(back));
    }

    private static int returnToSpawn(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) return Command.SINGLE_SUCCESS;

        if (!MyWellHasRunDry.getTrialChamberPredicate().test(
            player.getWorld(), player.getX(),
            player.getY(), player.getZ())) {
            context.getSource().sendError(Text.literal("You cannot return to spawn here."));
        } else {
            if (player.getHealth() != player.getMaxHealth()) {
                context.getSource().sendError(Text.literal("You must be full health to return!"));
                return 1;
            }

            BlockPos spawnPoint;
            if (player.getRespawn() instanceof ServerPlayerEntity.Respawn respawn) {
                spawnPoint = respawn.pos();
            } else {
                spawnPoint = player.getWorld().getSpawnPos();
            }

            player.teleport(
                player.getWorld(),
                spawnPoint.getX(),
                spawnPoint.getY(),
                spawnPoint.getZ(),
                Collections.emptySet(),
                player.getYaw(),
                player.getPitch(),
                true
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
