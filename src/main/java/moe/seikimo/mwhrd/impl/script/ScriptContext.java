package moe.seikimo.mwhrd.impl.script;

import moe.seikimo.mwhrd.game.quest.Dialogue;
import moe.seikimo.mwhrd.impl.PlayerNpcElement;
import moe.seikimo.mwhrd.script.ScriptObject;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

/**
 * Fields in this class are potentially null.
 * They store the context of the script that is being executed.
 */
public final class ScriptContext implements ScriptObject {
    public ScriptObject actor, player, dialogue;

    public Hand interact$hand;

    /**
     * Creates a script context object for an interaction.
     *
     * @param actor The actor being interacted with.
     * @param player The player that is interacting.
     * @param with The hand that the player is interacting with.
     * @return The script context object.
     */
    public static ScriptContext interact(PlayerNpcElement actor, ServerPlayerEntity player, Hand with) {
        var context = new ScriptContext();

        context.actor = actor.intoScript();
        context.player = Players.extend(player).intoScript();
        context.interact$hand = with;

        return context;
    }

    /**
     * Creates a script context object for a dialogue event.
     *
     * @param dialogue The dialogue event.
     * @return The script context object.
     */
    public static ScriptContext dialogue(ServerPlayerEntity player, Dialogue dialogue) {
        var context = new ScriptContext();

        context.dialogue = dialogue;
        context.player = Players.extend(player).intoScript();

        return context;
    }
}
