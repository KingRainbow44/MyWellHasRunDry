package moe.seikimo.mwhrd;

import lombok.Getter;
import moe.seikimo.mwhrd.commands.LootCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MyWellHasRunDry implements DedicatedServerModInitializer {
    @Getter private static MinecraftServer server;
    @Getter private static final Map<UUID, List<ItemStack>> playerLoot = new ConcurrentHashMap<>();

    /**
     * Gets or creates the loot set for the player.
     *
     * @param player The player to get the loot for.
     * @return The loot set for the player.
     */
    public static List<ItemStack> getLoot(ServerPlayerEntity player) {
        return MyWellHasRunDry.getLoot(player.getUuid());
    }

    /**
     * Gets or creates the loot set for the player.
     *
     * @param player The player to get the loot for.
     * @return The loot set for the player.
     */
    public static List<ItemStack> getLoot(UUID player) {
        return playerLoot.computeIfAbsent(player,
            uuid -> Collections.synchronizedList(new ArrayList<>()));
    }

    @Override
    public void onInitializeServer() {
        // Register the item despawn thread.
        TrialChamberLoot.ItemThread.initialize();

        // Register commands.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            LootCommand.register(dispatcher);
        });

        // Wait for the server to start.
        ServerLifecycleEvents.SERVER_STARTED.register(
            server -> MyWellHasRunDry.server = server);
    }
}
