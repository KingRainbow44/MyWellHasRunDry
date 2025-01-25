package moe.seikimo.mwhrd.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
     * Extracts a zip file to the given destination.
     *
     * @param url The URL to download from.
     * @return True if the URL is valid, false otherwise.
     */
    static boolean isUrl(String url) {
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
                            entryFile.mkdirs();
                        } else {
                            entryFile.getParentFile().mkdirs();

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
}
