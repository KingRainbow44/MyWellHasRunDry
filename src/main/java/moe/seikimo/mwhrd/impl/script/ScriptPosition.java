package moe.seikimo.mwhrd.impl.script;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomWorlds;
import moe.seikimo.mwhrd.script.ScriptObject;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.util.regex.Pattern;

/**
 * Represents a position in a world.
 * Includes a pitch/yaw component for rotation.
 *
 * @param x The x coordinate.
 * @param y The y coordinate.
 * @param z The z coordinate.
 * @param pitch The pitch.
 * @param yaw The yaw.
 * @param dimension The dimension.
 */
public record ScriptPosition(
    double x, double y, double z,
    float pitch, float yaw,
    String dimension
) implements ScriptObject {
    private static final Pattern REGEX = Pattern.compile("<([A-Z].*)>");

    /**
     * Resolves the dimension from the dimension name.
     *
     * @return The resolved dimension.
     */
    public ServerWorld resolveDimension() {
        // Check if the specified dimension is ephemeral.
        var matcher = REGEX.matcher(this.dimension);
        if (matcher.matches()) try {
            // Resolve the type name.
            var typeName = matcher.group(1);
            var type = CustomWorlds.class
                .getField(typeName)
                .get(null);

            if (!(type instanceof RuntimeWorldConfig config)) {
                throw new RuntimeException("Invalid dimension type");
            }

            // Create an ephemeral world.
            var handle = MyWellHasRunDry.getFantasy()
                .openTemporaryWorld(config);

            return MyWellHasRunDry.getServer()
                .getWorld(handle.getRegistryKey());
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // Fallback to the overworld.
            return MyWellHasRunDry.getDefaultWorld();
        }

        // Resolve the dimension by name.
        var identifier = Identifier.of(this.dimension);
        var key = RegistryKey.of(RegistryKeys.WORLD, identifier);

        return MyWellHasRunDry.getServer().getWorld(key);
    }
}
