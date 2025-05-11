package moe.seikimo.mwhrd.managers;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.game.quest.data.QuestData;
import moe.seikimo.mwhrd.impl.PlayerNpcElement;
import moe.seikimo.mwhrd.impl.holder.StarePlayerHolder;
import moe.seikimo.mwhrd.script.ScriptLoader;
import moe.seikimo.mwhrd.script.ScriptSerializer;
import moe.seikimo.mwhrd.utils.Constants;
import moe.seikimo.mwhrd.utils.IO;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Utils;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
public final class GlobalQuestManager {
    /** Holds a single reference to an NPC in The End. */
    private static final ElementHolder holder = new StarePlayerHolder();

    /** Block coordinates for where to place the 'adventure start' NPC. */
    private static final int ADVENTURE_START_X = 4, ADVENTURE_START_Z = -4;

    /** A set of all known quest data, defined in Lua. */
    @Getter
    private static final Int2ObjectMap<QuestData> knownQuests = new Int2ObjectOpenHashMap<>();

    /** The block position to spawn the 'adventure start' NPC. */
    private static BlockPos startBlock = BlockPos.ORIGIN;

    /**
     * Initializes the quest manager.
     */
    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(GlobalQuestManager::onServerStart);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(GlobalQuestManager::onChangeWorld);

        // Read all known quests.
        GlobalQuestManager.loadQuests();
    }

    /**
     * Fetches the specific quest data for a quest by its ID.
     *
     * @param questId The ID of the quest to fetch.
     * @return The quest data, or {@code null} if not found.
     */
    public static QuestData getQuestData(int questId) {
        return GlobalQuestManager.knownQuests.get(questId);
    }

    /**
     * Invoked when the server starts.
     *
     * @param server The server that started.
     */
    private static void onServerStart(MinecraftServer server) {
        // Resolve the starting Y coordinate for the 'adventure start' NPC.
        var world = server.getWorld(World.END);
        Objects.requireNonNull(world, "The End doesn't exist?");

        // Load the (0, -1) chunk.
        world.getChunk(0, -1);

        var y = world.getTopY(
            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            ADVENTURE_START_X, ADVENTURE_START_Z);
        GlobalQuestManager.startBlock = new BlockPos(ADVENTURE_START_X, y, ADVENTURE_START_Z);

        // Create NPC element.
        var npc = new PlayerNpcElement("Adventurer");
        npc.loadScript("behavior/b_adventures.lua");
        npc.setSkin(
            Constants.ADVENTURE_NPC_SKIN,
            Constants.ADVENTURE_NPC_SIGN);
        GlobalQuestManager.holder.addElement(npc);

        // Create chunk attachment.
        ChunkAttachment.ofTicking(GlobalQuestManager.holder, world, Vec3d.ofBottomCenter(GlobalQuestManager.startBlock));
    }

    /**
     * Invoked when the player changes worlds.
     *
     * @param player The player that changed worlds.
     * @param origin The world the player was in.
     * @param destination The world the player is now in.
     */
    private static void onChangeWorld(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination) {
        // Check if the player is entering the end.
        var entering = Utils.compare(destination, World.END);
        var leaving = Utils.compare(origin, World.END);

        if (leaving) {
            GlobalQuestManager.holder.stopWatching(player);
        }

        if (entering) {
            var questData = Players.getQuestData(player);
            // Ignore players who have already started a quest chain.
            if (questData.isStarted()) {
                return;
            }

            GlobalQuestManager.holder.startWatching(player);
        }
    }

    /**
     * Loads all quests from the resources.
     */
    private static void loadQuests() {
        try {
            var directory = IO.resource("scripts/quest");
            Objects.requireNonNull(directory);

            var files = directory.listFiles();
            Objects.requireNonNull(files);

            // Load all quest data.
            for (var file : files) {
                var stream = IO.streamFile(file);
                var script = ScriptLoader.invoke(stream);

                // Read the 'quest' data field.
                var questData = ScriptSerializer.toObject(script.get("quest"), QuestData.class);
                Objects.requireNonNull(questData);

                // Add the quest to the known quests.
                GlobalQuestManager.knownQuests.put(questData.getId(), questData);
            }

            log.info("Loaded {} quests.", GlobalQuestManager.knownQuests.size());
        } catch (URISyntaxException | IOException ex) {
            log.warn("Unable to load quest data.", ex);
        } catch (NullPointerException ignored) {
            log.warn("No quest data found.");
        }
    }
}
