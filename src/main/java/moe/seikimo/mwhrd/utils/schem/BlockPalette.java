package moe.seikimo.mwhrd.utils.schem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import java.util.regex.Pattern;

public interface BlockPalette {
    UnboundedMapCodec<BlockState, Integer> CODEC = Codec.unboundedMap(Entry.CODEC, Codec.INT);

    /**
     * A generic helper method to update the block state.
     */
    static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> prop, String value) {
        return state.with(prop, prop.parse(value).orElseThrow());
    }

    interface Entry {
        Codec<BlockState> CODEC = Codec.STRING.comapFlatMap(Entry::deserialize, Entry::serialize);

        /**
         * Attempts to deserialize a block state from a string.
         *
         * @param value The string to deserialize.
         * @return The deserialized block state.
         */
        static DataResult<BlockState> deserialize(String value) {
            // Check if the state has data tags.
            if (!value.contains("[") && !value.contains("]")) {
                // If this is true, we can just look up the name by the identifier.
                var identifier = Identifier.of(value);
                var block = Registries.BLOCK.get(identifier);
                return DataResult.success(block.getDefaultState());
            }

            // If this is false, we need to parse the string and get the block state.
            var identifier = Identifier.of(value.substring(0, value.indexOf("[")));
            var block = Registries.BLOCK.get(identifier);
            var state = block.getDefaultState();

            // Parse each state value.
            var pattern = Pattern.compile("([a-zA-Z0-9_]+)=([a-zA-Z0-9_]+)");
            var matcher = pattern.matcher(value);

            while (matcher.find()) {
                var propKey = matcher.group(1);
                var propValue = matcher.group(2);

                var property = block
                    .getStateManager()
                    .getProperty(propKey);
                if (property == null) {
                    return DataResult.error(() -> "Unknown property: " + propKey);
                }

                try {
                    // Update the state with the property.
                    state = BlockPalette.with(state, property, propValue);
                } catch (Exception ignored) {
                    return DataResult.error(() -> "Failed to set property: " + propKey + " with value: " + propValue);
                }
            }

            return DataResult.success(state);
        }

        /**
         * Attempts to serialize a block state into a string.
         *
         * @param state The block state to serialize.
         * @return The serialized block state.
         */
        static String serialize(BlockState state) {
            var identifier = Registries.BLOCK.getId(state.getBlock());
            var builder = new StringBuilder(identifier.toString());

            // Add properties.
            var iter = state.getProperties().iterator();
            var open = true;

            while (iter.hasNext()) {
                // Create a state 'open' if the builder is empty.
                if (open) {
                    builder.append("[");
                    open = false;
                }

                // Serialize the property.
                var property = iter.next();
                builder
                    .append(property.getName())
                    .append("=");

                var propertyValue = state.get(property);
                if (propertyValue instanceof Enum<?>) {
                    var stringId = (StringIdentifiable) propertyValue;
                    builder.append(stringId.asString());
                } else {
                    builder.append(propertyValue.toString());
                }

                // Add a trailing comma if needed.
                if (iter.hasNext()) {
                    builder.append(",");
                }
            }

            // Close the state if it was opened.
            if (!open) {
                builder.append("]");
            }

            // Return the string.
            return builder.toString();
        }
    }
}
