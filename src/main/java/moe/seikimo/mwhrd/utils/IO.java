package moe.seikimo.mwhrd.utils;

import com.google.common.base.Preconditions;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.schem.Schematic;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public interface IO {
    /**
     * Downloads a file at the URL to the given path.
     *
     * @param url The URL to download from.
     * @param writeDestination The path to write the file to.
     * @throws IOException If an I/O error occurs.
     */
    static void download(
        String url, Path writeDestination
    ) throws IOException {
        try {
            // Open the input stream.
            var uri = new URI(url).toURL();
            var inputStream = Channels.newChannel(uri.openStream());

            // Create the output stream and transfer the data.
            var outputStream = new FileOutputStream(writeDestination.toFile());
            outputStream.getChannel().transferFrom(inputStream, 0, Long.MAX_VALUE);

            // Close the streams.
            inputStream.close();
            outputStream.close();
        } catch (URISyntaxException ignored) {
            throw new RuntimeException("Invalid URL provided");
        }
    }

    /**
     * Opens a stream to the given URL.
     *
     * @param url The URL to open a stream to.
     * @return The input stream.
     * @throws IOException If an I/O error occurs.
     */
    static InputStream streamUrl(String url) throws IOException, URISyntaxException {
        return new URI(url).toURL().openStream();
    }

    /**
     * Opens a steam to the given file.
     *
     * @param file The file to open a stream to.
     * @return The input stream.
     * @throws IOException If an I/O error occurs.
     */
    static InputStream streamFile(File file) throws IOException {
        return new FileInputStream(file);
    }

    /**
     * Extracts a zip file to the given destination.
     *
     * @param url The URL to download from.
     * @return True if the URL is valid, false otherwise.
     */
    static boolean isUrl(String url) {
        // Check if the string matches a regex.
        if (!url.matches("^(http|https)://.*$")) {
            return false;
        }

        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Extracts a zip file to the given destination.
     *
     * @param zipFile The zip file to extract.
     * @param destination The destination to extract to.
     */
    static void extract(Path zipFile, Path destination) {
        try (var zip = new ZipFile(zipFile.toFile())) {
            zip.stream()
                .forEach(entry -> {
                    try {
                        var entryPath = destination.resolve(entry.getName());
                        var entryFile = entryPath.toFile();

                        if (entry.isDirectory()) {
                            Preconditions.checkArgument(entryFile.mkdirs(), "Failed to create directory");
                        } else {
                            Preconditions.checkArgument(entryFile.getParentFile().mkdirs(), "Failed to create directory");

                            try (var input = zip.getInputStream(entry);
                                 var output = new FileOutputStream(entryFile)) {
                                input.transferTo(output);
                            }
                        }
                    } catch (IOException exception) {
                        throw new RuntimeException("Failed to extract the zip file", exception);
                    }
                });
        } catch (IOException exception) {
            throw new RuntimeException("Failed to extract the zip file", exception);
        }
    }

    /**
     * Creates a streamed file system for a resource in the JAR.
     *
     * @param path The path to the resource.
     * @return The path to the resource.
     */
    static File resource(String path) throws URISyntaxException {
        var url = IO.class.getResource("/" + path);
        if (url == null) {
            return null;
        }

        return new File(url.toExternalForm());
    }

    /**
     * Downloads and reads a schematic from the URL.
     *
     * @param url The URL to download the schematic from.
     * @return The schematic sample.
     */
    @Nullable
    static Schematic readSchematic(String url) {
        try {
            InputStream stream;
            if (url.startsWith("resource://")) {
                var path = url.substring(11);
                stream = IO.streamFile(IO.resource(path));
            } else {
                stream = IO.streamUrl(url);
            }

            // Read the NBT from the stream.
            var tracker = NbtSizeTracker.ofUnlimitedBytes();
            var nbt = NbtIo.readCompressed(stream, tracker);

            return Schematic.CODEC
                .decode(NbtOps.INSTANCE, nbt)
                .getOrThrow()
                .getFirst();
        } catch (IOException | URISyntaxException | IllegalStateException ignored) {
            return null;
        }
    }
}
