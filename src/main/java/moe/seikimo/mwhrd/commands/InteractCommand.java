package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import moe.seikimo.mwhrd.interfaces.player.IStoryPlayer;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class InteractCommand {
    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("interact")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(argument("option", string())
                    .executes(InteractCommand::execute))
        );
    }

    /**
     * Argument command: <code>interact [option]</code>
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        var option = StringArgumentType.getString(context, "option");

        // Check if the player is a story player.
        if (!(player instanceof IStoryPlayer storyPlayer)) {
            return 0;
        }

        // Invoke the dialogue event.
        var questManager = storyPlayer.mwhrd$getQuestManager();
        questManager.invokeOption(option);

        return Command.SINGLE_SUCCESS;
    }
}
