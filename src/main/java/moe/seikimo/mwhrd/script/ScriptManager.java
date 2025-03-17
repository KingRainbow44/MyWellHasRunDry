package moe.seikimo.mwhrd.script;

import moe.seikimo.mwhrd.events.ScriptCachePurgeEvent;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import javax.script.Bindings;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This manages scripts on any object.
 */
public final class ScriptManager {
    private final Set<Bindings> loaded = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> $loaded = Collections.synchronizedSet(new HashSet<>());

    public ScriptManager() {
        ScriptCachePurgeEvent.EVENT.register(this::purge);
    }

    /**
     * Runs the <code>tick(environment)</code> function on loaded scripts.
     */
    public void tick() {

    }

    /**
     * Reloads all loaded scripts.
     */
    public void purge() {
        this.loaded.clear();

        for (var location : this.$loaded) {
            this.$load(location);
        }
    }

    /**
     * Adds the script to the manager.
     *
     * @param location The path to the script.
     */
    public void load(String location) {
        this.$loaded.add(location);
        this.$load(location);
    }

    /**
     * Internal method to load the script.
     *
     * @param location The path to the script.
     */
    private void $load(String location) {
        var script = ScriptLoader.getScript(location);
        var bindings = ScriptLoader.invoke(script);
        this.loaded.add(bindings);
    }

    /**
     * Invokes the specified function in all loaded scripts.
     *
     * @param function The name of the function.
     * @param arguments The arguments to pass to the function.
     */
    public void invoke(String function, ScriptObject... arguments) {
        // Encode all arguments.
        var encoded = ScriptLoader.encode(arguments);

        // Invoke the functions.
        for (var script : this.loaded) {
            var func = script.get(function);
            if (func != null) {
                ScriptLoader.call(func, encoded);
            }
        }
    }
}
