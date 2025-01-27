package moe.seikimo.mwhrd.script;

import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.events.ScriptCachePurgeEvent;
import moe.seikimo.mwhrd.utils.IO;
import moe.seikimo.mwhrd.utils.Paths;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.script.LuajContext;

import javax.script.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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

    /**
     * This set contains paths to all loaded scripts.
     */
    private static final Set<String> loaded = Collections.synchronizedSet(new HashSet<>());

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

        // Register enums.
        ScriptLoader.register(Hand.class);

        log.info("Initialized Lua script engine.");
    }

    /**
     * Reloads all scripts.
     * This is done by caching scripts which have been previously loaded.
     */
    public static synchronized void reload() {
        for (var path : ScriptLoader.loaded) try {
            var stream = ScriptLoader.getScript(path);
            if (stream == null) {
                log.warn("Failed to reload script: {}", path);
                continue;
            }

            ScriptLoader.compile(stream);
            stream.close();

            log.debug("Reloaded script '{}'", path);
        } catch (Exception ex) {
            log.warn("Failed to reload script: {}", path, ex);
        }

        ScriptCachePurgeEvent.EVENT.invoker().onPurge();
    }

    /**
     * Attempts to resolve a script from all available sources.
     *
     * @param path The path to the script.
     * @return The input stream of the script.
     */
    @Nullable
    @CheckReturnValue
    public static InputStream getScript(String path) {
        // Source 1: The file system.
        var filePath = Paths.SCRIPTS.resolve(path);
        if (Files.exists(filePath)) try {
            return new FileInputStream(filePath.toFile());
        } catch (FileNotFoundException ignored) {
            // This should never happen.
        }

        // Source 2: The classpath/resources.
        var resource = ScriptLoader.class.getResourceAsStream("/scripts/%s".formatted(path));
        if (resource != null) {
            return resource;
        }

        // Source 3: By URL.
        if (IO.isUrl(path)) try {
            return IO.streamUrl(path);
        } catch (IOException ex) {
            log.warn("Failed to download script from URL.", ex);
        } catch (URISyntaxException ignored) {
            // This should never happen.
        }

        return null;
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
        EnumSet.allOf(type).forEach((value) -> {
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
        var bindings = new SimpleBindings();

        try {
            return ScriptLoader.invoke(bindings, stream);
        } catch (NoSuchAlgorithmException ex) {
            log.warn("Failed to load script from input stream.", ex);
            return bindings;
        }
    }

    /**
     * Compiles the script.
     * This will overwrite any existing script with the same hash.
     *
     * @param stream The input stream.
     */
    public static void compile(InputStream stream) throws NoSuchAlgorithmException {
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
        } catch (IOException ex) {
            log.warn("Failed to read script from input stream.", ex);
        } catch (ScriptException ex) {
            throw new RuntimeException("Failed to compile script", ex);
        }
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
    @CheckReturnValue
    public static Bindings invoke(Bindings bindings, InputStream stream) throws NoSuchAlgorithmException {
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

    /**
     * Calls a Lua function with the specified arguments.
     *
     * @param luaFunc The Lua function to call.
     * @param arguments The (Lua-encoded) arguments to pass.
     * @return Any return value from the function.
     */
    public static LuaValue call(Object luaFunc, LuaValue... arguments) {
        if (!(luaFunc instanceof LuaFunction function)) {
            return LuaValue.NIL;
        }

        try {
//            var encoded = new LuaValue[arguments.length + 1];
//            encoded[0] = function;
//            System.arraycopy(arguments, 0, encoded, 1, arguments.length);
//
//            // Reflection call to the Lua function.
//            var resolveFunc = LuaValue.class.getDeclaredMethod("callmt");
//            resolveFunc.setAccessible(true);
//
//            if (!(resolveFunc.invoke(luaFunc) instanceof LuaValue retVal)) {
//                return LuaValue.NIL;
//            }
//            return retVal.invoke(encoded).arg1();

            return switch (arguments.length) {
                case 0 -> function.call();
                case 1 -> function.call(arguments[0]);
                case 2 -> function.call(arguments[0], arguments[1]);
                case 3 -> function.call(arguments[0], arguments[1], arguments[2]);
                default -> throw new IllegalArgumentException("No support for >3 arguments currently");
            };
        } catch (LuaError error) {
            log.warn("Failed to call lua function", error);
            return LuaValue.NIL;
        }
    }
}

