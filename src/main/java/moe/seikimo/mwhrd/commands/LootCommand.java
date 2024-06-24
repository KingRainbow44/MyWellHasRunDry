package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.gui.TrialChamberLootGui;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

@Slf4j
public final class LootCommand {
    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var loot = dispatcher.register(literal("dungeonloot")
            .executes(LootCommand::openGui));
        dispatcher.register(literal("trialloot").redirect(loot));
    }

    private static int openGui(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("You must be a player to use this command."));
            return 1;
        }

        try {
            TrialChamberLootGui.open(player);
        } catch (Exception exception) {
            context.getSource().sendError(Text.literal("An error occurred while opening the GUI."));
            log.warn("An error occurred while opening the GUI.", exception);
        }

        return 1;
    }

    private LootCommand() {

    }
}
