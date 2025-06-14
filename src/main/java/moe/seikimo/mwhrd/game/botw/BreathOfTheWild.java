package moe.seikimo.mwhrd.game.botw;

import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.BuildConfig;
import moe.seikimo.mwhrd.custom.CustomWorlds;
import moe.seikimo.mwhrd.utils.IO;
import moe.seikimo.mwhrd.utils.Paths;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
public final class BreathOfTheWild extends RuntimeWorld {
    /**
     * Opens a persistent world instance for the 'overworld expanse'.
     *
     * @param server The server instance to open the world for.
     * @param fantasy The fantasy instance to open the world for.
     * @return The handle to the opened world.
     */
    public static RuntimeWorldHandle open(MinecraftServer server, Fantasy fantasy) {
        // Check if the download is specified.
        if (BuildConfig.BOTW_DOWNLOAD.isEmpty()) {
            log.debug("No download URL specified for the 'overworld expanse' world.");
            return fantasy.openTemporaryWorld(new RuntimeWorldConfig()
                .setGenerator(CustomWorlds.VOID_GENERATOR));
        }

        // Resolve the world path.
        var worldFolderName = Objects.requireNonNull(server.getWorld(World.OVERWORLD))
            .getChunkManager()
            .chunkLoadingManager
            .getSaveDir();
        var baseFolder = Path.of(worldFolderName);
        var worldFolder = baseFolder.resolve(Paths.OVERWORLD_EXPANSE_WORLD);

        // If the world doesn't exist, we should download it.
        if (!Files.exists(worldFolder)) {
            log.info("Downloading the 'overworld expanse' world...");

            try {
                // Download the world from the server.
                var zipFile = Path.of("cache", "botw.zip");
                if (!Files.exists(zipFile)) {
                    IO.download(BuildConfig.BOTW_DOWNLOAD, zipFile);
                }

                // Extract the world to the world folder.
                log.info("Extracting the 'overworld expanse' world...");
                IO.extract(zipFile, worldFolder);

                log.info("Successfully downloaded the 'overworld expanse' world!");
            } catch (IOException exception) {
                log.error("Failed to download the 'overworld expanse' world!", exception);
                System.exit(1);
            }
        }

        return fantasy.getOrOpenPersistentWorld(
            Identifier.of("mwhrd", "overworld_expanse"),
            CustomWorlds.OVERWORLD_EXPANSE
        );
    }

    public BreathOfTheWild(
        MinecraftServer server, RegistryKey<World> registryKey,
        RuntimeWorldConfig config, Style style
    ) {
        super(server, registryKey, config, style);
    }
}
