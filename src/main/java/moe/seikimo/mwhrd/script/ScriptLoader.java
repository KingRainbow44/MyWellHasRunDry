package moe.seikimo.mwhrd.script;

import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.utils.Utils;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.script.LuajContext;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads Lua scripts from input streams.
 */
@Slf4j
@ExtensionMethod(Utils.class)
public final class ScriptLoader {
    private static final ScriptEngineManager manager;
    private static final ScriptEngine engine;

    private static final ScriptLib scriptLib = new ScriptLib();
    private static final LuaValue $scriptLib;

    /**
     * This dictionary caches the script's hash to the compiled script.
     */
    private static final Map<byte[], CompiledScript> cache = new ConcurrentHashMap<>();

    static {
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("luaj");

        // Create the global script library instance.
        $scriptLib = CoerceJavaToLua.coerce(ScriptLoader.scriptLib);
    }

    /**
     * Safe cast method to get the Lua script context.
     *
     * @return An instance of {@link LuajContext}.
     */
    public static LuajContext getContext() {
        if (ScriptLoader.engine.getContext() instanceof LuajContext context) {
            return context;
        } else {
            throw new IllegalStateException("Invalid script engine context.");
        }
    }

    /**
     * Initializes the script loader.
     */
    public static synchronized void initialize() {
        var context = ScriptLoader.getContext();

        // Prepare global context.
        context.globals.set("ScriptLib", ScriptLoader.$scriptLib);

        log.info("Initialized Lua script engine.");
    }

    /**
     * Registers an enum.
     *
     * @param type The class of the enum.
     * @param <T> The type of the enum.
     */
    public static <T extends Enum<T>> void register(Class<T> type) {
        var table = new LuaTable();

        // Enumerate over all enum values.
        // Add them to the Lua table.
        EnumSet.allOf(type).forEach(value -> {
            var name = value.name();
            table.set(name, value.ordinal());
            table.set(name.toUpperCase(), value.ordinal());
        });

        // Register it as a global.
        ScriptLoader.getContext().globals.set(type.getSimpleName(), table);
    }

    /**
     * Loads a script from an input stream.
     * This creates a new instance of a script context.
     *
     * @param stream The input stream.
     * @return The compiled script.
     */
    public static Bindings invoke(InputStream stream) {
        return ScriptLoader.invoke(new SimpleBindings(), stream);
    }

    /**
     * Loads a script from an input stream.
     * This should be used for loading multiple scripts.
     *
     * @param bindings The existing script context.
     * @param stream The input stream.
     * @return The compiled script.
     */
    @Nullable
    @SneakyThrows
    @CheckReturnValue
    public static Bindings invoke(Bindings bindings, InputStream stream) {
        var sha256 = MessageDigest.getInstance("SHA-256");

        try (var digest = new DigestInputStream(stream, sha256)) {
            var reader = new InputStreamReader(digest);

            // Check if the script is already cached.
            var hash = sha256.digest();
            var compiled = ScriptLoader.cache.get(hash);

            // Otherwise, compile the script.
            if (compiled == null) {
                if (!(ScriptLoader.engine instanceof Compilable compilable)) {
                    throw new IllegalStateException("Invalid Lua script engine specified");
                }

                compiled = compilable.compile(reader);
                ScriptLoader.cache.put(hash, compiled);
            }

            // Evaluate the script.
            compiled.eval(bindings);
            reader.close();

            return bindings;
        } catch (ScriptException ignored) {
            // This is likely a syntax error.
        } catch (IOException ex) {
            log.warn("Failed to read script from input stream.", ex);
        }

        return bindings;
    }
}

