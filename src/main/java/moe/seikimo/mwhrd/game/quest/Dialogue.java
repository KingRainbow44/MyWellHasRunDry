package moe.seikimo.mwhrd.game.quest;

import lombok.Getter;
import moe.seikimo.mwhrd.events.ScriptCachePurgeEvent;
import moe.seikimo.mwhrd.impl.script.ScriptContext;
import moe.seikimo.mwhrd.script.ScriptLoader;
import moe.seikimo.mwhrd.script.ScriptObject;
import moe.seikimo.mwhrd.script.ScriptSerializer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import javax.script.Bindings;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Dialogue implements Iterator<DialogueLine>, ScriptObject {
    private final String location;
    private Bindings script = null;

    private final List<DialogueLine> lines = new ArrayList<>();
    @Getter private String displayName = null;

    private int index = 0;

    /**
     * Creates a dialogue instance.
     *
     * @param location The path to the dialogue script.
     */
    public Dialogue(String location) {
        this.location = location;
        this.loadScript();

        ScriptCachePurgeEvent.EVENT.register(this::loadScript);
    }

    /**
     * Loads the dialogue script.
     */
    private void loadScript() {
        var script = ScriptLoader.getScript(this.location);
        this.script = ScriptLoader.invoke(script);

        // Read data from the script.
        var dialogue = this.script.get("dialogue");
        if (!(dialogue instanceof LuaTable table)) {
            throw new RuntimeException("Dialogue script must contain a 'dialogue' table.");
        }

        // Read the dialogue lines.
        this.displayName = table.get("name").tojstring();

        var lines = ScriptSerializer.toList(table.get("text"), DialogueLine.class);
        this.lines.addAll(lines);
    }

    /**
     * This will return true until the dialogue has been fully read.
     */
    @Override
    public boolean hasNext() {
        return this.index < this.lines.size();
    }

    @Override
    public DialogueLine next() {
        if (!this.hasNext()) {
            return null;
        }

        // Get the next dialogue line.
        if (this.index >= this.lines.size()) {
            // We are out of lines.
            return null;
        }

        return this.lines.get(this.index++);
    }

    /**
     * Invoked before the dialogue is read.
     *
     * @param player The player reading the dialogue.
     */
    public void startReading(ServerPlayerEntity player) {
        // Resolve the event handler method.
        if (!(this.script.get("before_dialogue") instanceof LuaValue luaFunc)) {
            return;
        }

        // Create the script context.
        var context = ScriptContext.dialogue(player, this);
        // Invoke the function.
        ScriptLoader.call(luaFunc, context);
    }

    /**
     * Invoked when the dialogue has been fully read.
     *
     * @param player The player reading the dialogue.
     */
    public void finishReading(ServerPlayerEntity player) {
        // Resolve the event handler method.
        if (!(this.script.get("after_dialogue") instanceof LuaValue luaFunc)) {
            return;
        }

        // Create the script context.
        var context = ScriptContext.dialogue(player, this);
        // Invoke the function.
        ScriptLoader.call(luaFunc, context);
    }
}
