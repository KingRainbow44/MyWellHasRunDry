package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.script.ScriptLoader;
import moe.seikimo.mwhrd.utils.Scripts;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Slf4j
public final class ScriptCommand {
    private static final DynamicCommandExceptionType INVALID_SCRIPT = new DynamicCommandExceptionType(s -> Text.translatable("commands.script.invalid", s));
    private static final DynamicCommandExceptionType FAILED_TO_CLOSE = new DynamicCommandExceptionType(s -> Text.translatable("commands.script.close", s));

    /**
     * Registers the 'script' command.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var script = dispatcher.register(literal("script")
            .then(literal("run")
                .then(argument("script", greedyString())
                    .executes(ScriptCommand::runScript))));
        dispatcher.register(literal("lua").redirect(script));
    }

    /**
     * Runs a script from a given path.
     */
    private static int runScript(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var script = context.getArgument("script", String.class);

        // Fetch the script's stream.
        var stream = ScriptLoader.getScript(script);
        if (stream == null) {
            throw INVALID_SCRIPT.create(script);
        }

        // Run the script.
        var bindings = ScriptLoader.invoke(stream);

        // Print the result.
        var result = Scripts.toString(bindings);
        log.info("Script executed with result:\n{}", result);

        try {
            // Close the stream.
            stream.close();
        } catch (IOException ex) {
            throw FAILED_TO_CLOSE.create(ex.getMessage());
        }

        context.getSource().sendFeedback(() -> Text.translatable("commands.script.run", script), true);

        return Command.SINGLE_SUCCESS;
    }
}
