package moe.seikimo.mwhrd.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.utils.Debug;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class DebugCommand {
    /**
     * Registers the command with the dispatcher.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("debug")
            .requires(source -> source.hasPermissionLevel(3))
            .then(argument("property", StringArgumentType.word())
                .then(argument("value", StringArgumentType.word())
                    .executes(DebugCommand::set)))
            .then(literal("hardcore")
                .executes(DebugCommand::unsetHardcore))
            .executes(DebugCommand::usage));
    }

    private static int usage(CommandContext<ServerCommandSource> context) {
        context.getSource().sendError(Text.literal(
            "Usage: /debug <property> <value>"
        ));
        return 1;
    }

    private static int set(CommandContext<ServerCommandSource> context) {
        var property = StringArgumentType.getString(context, "property");
        var value = StringArgumentType.getString(context, "value");

        try {
            var field = Debug.class.getDeclaredField(property);
            field.setAccessible(true);

            if (field.getType() == boolean.class) {
                field.set(null, Boolean.parseBoolean(value));
            } else if (field.getType() == int.class) {
                field.set(null, Integer.parseInt(value));
            } else if (field.getType() == String.class) {
                field.set(null, value);
            } else {
                context.getSource().sendError(Text.literal(
                    "Unsupported property type: " + field.getType().getName()
                ));
                return 1;
            }

            field.setAccessible(false);

            context.getSource().sendMessage(Text.literal(
                "Set " + property + " to " + value));
        } catch (Exception exception) {
            context.getSource().sendError(Text.literal(
                "Unknown property: " + property
            ));
            return 1;
        }

        return 1;
    }

    private static int unsetHardcore(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (!(player instanceof IDBObject<?> dbObject)) {
            context.getSource().sendError(Text.literal(
                "Player is not an IDBObject"
            ));
            return 1;
        }

        var data = dbObject.mwhrd$getData();
        if (data instanceof PlayerModel model) {
            model.unsetHardcore(true);
        } else {
            context.getSource().sendError(Text.literal(
                "Player data is not a PlayerModel"
            ));
            return 1;
        }

        return 1;
    }
}
