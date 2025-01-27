package moe.seikimo.mwhrd.impl.script;

import lombok.Data;
import lombok.experimental.Accessors;
import moe.seikimo.mwhrd.impl.PlayerNpcElement;
import moe.seikimo.mwhrd.script.ScriptObject;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

/**
 * Fields in this class are potentially null.
 * They store the context of the script that is being executed.
 */
@Data
@Accessors(fluent = true)
public final class ScriptContext implements ScriptObject {
    private ScriptObject actor, player;

    private Hand interact$hand;

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
}
