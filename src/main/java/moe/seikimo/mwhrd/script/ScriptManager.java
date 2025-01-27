package moe.seikimo.mwhrd.script;

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

    /**
     * Runs the <code>tick(environment)</code> function on loaded scripts.
     */
    public void tick() {

    }

    /**
     * Invokes the specified function in all loaded scripts.
     *
     * @param function The name of the function.
     * @param arguments The arguments to pass to the function.
     */
    public void invoke(String function, ScriptObject... arguments) {
        // Encode all arguments.
        var encoded = new LuaValue[arguments.length];
        for (var i = 0; i < arguments.length; i++) {
            var arg = arguments[i];
            encoded[i] = CoerceJavaToLua.coerce(arg);
        }

        // Invoke the functions.
        for (var script : this.loaded) {
            var func = script.get(function);
            if (func != null) {
                ScriptLoader.call(func, encoded);
            }
        }
    }
}
