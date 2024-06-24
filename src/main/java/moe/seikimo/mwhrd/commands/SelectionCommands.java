package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import moe.seikimo.mwhrd.interfaces.ISelectionPlayer;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public final class SelectionCommands {
    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var pos1 = dispatcher.register(literal("/pos1")
            .executes(SelectionCommands::select1));
        dispatcher.register(literal("/1").redirect(pos1));

        var pos2 = dispatcher.register(literal("/pos2")
            .executes(SelectionCommands::select2));
        dispatcher.register(literal("/2").redirect(pos2));
    }

    private static int select1(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player instanceof ISelectionPlayer select) {
            var position = player.getBlockPos();

            select.mwhrd$setPos1(position);
            context.getSource().sendMessage(Text.literal(
                "Position 1 set at " + Utils.serialize(position) + ".")
                .formatted(Formatting.LIGHT_PURPLE));
        }

        return 1;
    }

    private static int select2(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player instanceof ISelectionPlayer select) {
            var position = player.getBlockPos();

            select.mwhrd$setPos2(position);
            context.getSource().sendMessage(Text.literal(
                    "Position 2 set at " + Utils.serialize(position) + ".")
                .formatted(Formatting.LIGHT_PURPLE));
        }
        return 1;
    }
}
