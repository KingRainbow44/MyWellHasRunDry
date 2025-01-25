package moe.seikimo.mwhrd.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Paths {
    /**
     * This path is from the 'level-name' path.
     */
    Path OVERWORLD_EXPANSE_WORLD = Path.of("dimensions", "mwhrd", "overworld_expanse");

    /**
     * This path is from the root path.
     */
    Path CONFIG = Path.of("config", "mwhrd");

    /**
     * This path is from the root path.
     */
    Path SCRIPTS = Path.of("config", "mwhrd", "scripts");

    /**
     * Creates the necessary paths.
     */
    static void ensurePaths() {
        try {
            Paths.makeDirectory(CONFIG);
            Paths.makeDirectory(SCRIPTS);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create necessary paths", ex);
        }
    }

    /**
     * Creates a directory if it does not exist.
     *
     * @param path The path to create.
     * @throws IOException If an I/O error occurs.
     */
    private static void makeDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }

        Files.createDirectories(path);
    }
}
