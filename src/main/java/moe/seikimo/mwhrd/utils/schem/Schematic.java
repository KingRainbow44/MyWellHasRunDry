package moe.seikimo.mwhrd.utils.schem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Sponge schematic format.
 * @see <a href="https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-2.md">Specification</a>
 */
public record Schematic(
    /*
     * Specifies the format version being used.
     * It may be used to provide validation and auto-conversion from older versions.
     */
    int version,
    /*
     * Specifies the data version of Minecraft that was used to create the schematic.
     * This is to allow for block and entity data to be validated and auto-converted from older versions.
     * This is dependent on the Minecraft version
     */
    int dataVersion,
    /*
     * Provides optional metadata about the schematic.
     */
    Schematic.Metadata metadata,
    /*
     * Specifies the width (the size of the area in the X-axis) of the schematic.
     */
    int width,
    /*
     * Specifies the height (the size of the area in the Y-axis) of the schematic.
     */
    int height,
    /*
     * Specifies the length (the size of the area in the Z-axis) of the schematic.
     */
    int length,
    /*
     * Specifies the relative offset of the schematic from the paster.
     * When pasting, if there is a reasonable location to use as a base position,
     * implementations SHOULD offset the location of the paste by this vector.
     * The default value if not provided is [0, 0, 0].
     */
    Vec3i offset,
    /*
     * Specifies the size of the block palette in number of bytes needed for the maximum palette index. Essentially, this is the max index - 1.
     */
    int paletteMax,
    /*
     * Specifies the block palette. This is a mapping of block states to indices which are local to this schematic.
     * These indices are used to reference the block states from within the BlockData array.
     * It is recommended for maximum data compression that your indices start at zero and skip no values.
     * The maximum index cannot be greater than PaletteMax - 1.
     * While not required it is highly recommended that you include a palette in order to tightly pack the block ids included in the data array.
     */
    Map<BlockState, Integer> palette,
    /*
     * Specifies the main storage array which contains Width * Height * Length entries.
     * Each entry is specified as a varint and refers to an index within the Palette.
     * The entries are indexed by x + z * Width + y * Width * Length.
     */
    ByteBuffer blockData,
    /*
     * Specifies additional data for blocks which require extra data.
     * If no additional data is provided for a block which normally requires extra data
     * then it is assumed that the BlockEntity for the block is initialized to its default state.
     */
    List<NbtCompound> blockEntities,
    /*
     * Specifies entities to be placed in the schematic.
     * If no additional data is provided for an entity type which normally requires extra data,
     * then it is assumed that the Entity is initialized with all defaults.
     */
    List<NbtCompound> entities
) {
    public static final Codec<Schematic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("Version").forGetter(Schematic::version),
        Codec.INT.fieldOf("DataVersion").forGetter(Schematic::dataVersion),
        Metadata.CODEC.optionalFieldOf("Metadata", Metadata.EMPTY).forGetter(Schematic::metadata),
        Codec.INT.fieldOf("Width").forGetter(Schematic::width),
        Codec.INT.fieldOf("Height").forGetter(Schematic::height),
        Codec.INT.fieldOf("Length").forGetter(Schematic::length),
        Vec3i.CODEC.fieldOf("Offset").forGetter(Schematic::offset),
        Codec.INT.fieldOf("PaletteMax").forGetter(Schematic::paletteMax),
        BlockPalette.CODEC.fieldOf("Palette").forGetter(Schematic::palette),
        Codec.BYTE_BUFFER.fieldOf("BlockData").forGetter(Schematic::blockData),
        Codec.list(NbtCompound.CODEC).optionalFieldOf("BlockEntities", List.of()).forGetter(Schematic::blockEntities),
        Codec.list(NbtCompound.CODEC).optionalFieldOf("Entities", List.of()).forGetter(Schematic::entities)
    ).apply(instance, Schematic::new));

    /**
     * Creates a schematic sample.
     *
     * @return A new sample of the schematic.
     */
    public Sample sample() {
        return new Sample(this);
    }

    /**
     * Reads the block data from the schematic as a 3D array.
     *
     * @return A 3D array of integers representing the block data.
     */
    public int[][][] readBlockData() {
        var array = new int[this.width][this.height][this.length];

        // Look up the block data from the buffer.
        var data = this.blockData.array();
        for (var x = 0; x < this.width; x++) {
            for (var y = 0; y < this.height; y++) {
                for (var z = 0; z < this.length; z++) {
                    var index = x + z * this.width + y * this.width * this.length;
                    array[x][y][z] = data[index];
                }
            }
        }

        return array;
    }

    /**
     * An object which provides optional additional meta information about the schematic.
     * The fields outlined here are guidelines to assist with standardization, but it is recommended
     * that any program reading and writing schematics persist all fields found within this object.
     */
    public record Metadata(
        /*
         * The name of the schematic.
         */
        String name,
        /*
         * The name of the author of the schematic.
         */
        String author,
        /*
         * The date that this schematic was created on. This is specified as milliseconds since the UNIX epoch.
         */
        long date,
        /*
         * An array of mod IDs which have blocks which are referenced by this schematic's defined Palette.
         */
        List<String> requiredMods
    ) {
        public static final Metadata EMPTY = new Metadata("", "", 0, List.of());
        public static final Codec<Metadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(Metadata::name),
            Codec.STRING.optionalFieldOf("author", "").forGetter(Metadata::author),
            Codec.LONG.optionalFieldOf("date", 0L).forGetter(Metadata::date),
            Codec.list(Codec.STRING).optionalFieldOf("requiredMods", List.of("minecraft")).forGetter(Metadata::requiredMods)
        ).apply(instance, Metadata::new));
    }
}
