package moe.seikimo.mwhrd;

import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.h2.H2Backend;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.commands.DebugCommand;
import moe.seikimo.mwhrd.commands.LootCommand;
import moe.seikimo.mwhrd.commands.PartyCommand;
import moe.seikimo.mwhrd.commands.ReturnCommand;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import moe.seikimo.mwhrd.providers.PlayerVaultNumberProvider;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.predicate.LightPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.gen.structure.StructureKeys;
import org.geysermc.geyser.api.GeyserApi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class MyWellHasRunDry implements DedicatedServerModInitializer {
    // TODO: Add database system and keep track of if player has seen changelog.
    private static final List<Text> CHANGELOG = List.of(
        Text.literal("My Well Has Run Dry: ")
            .formatted(Formatting.BOLD, Formatting.AQUA)
            .append(Text.literal("v" + BuildConfig.VERSION)
                .formatted(Formatting.YELLOW)),
        Text.literal("Minecraft: Luck and Luxury")
            .formatted(Formatting.ITALIC, Formatting.GOLD),
        Text.empty(),
        Text.literal(" - Overhauled ominous trial chambers")
            .formatted(Formatting.DARK_GRAY),
        Text.literal(" - Adventure mode is enforced in trial chambers")
            .formatted(Formatting.DARK_GRAY),
        Text.literal(" - New powerful beacons have been added")
            .formatted(Formatting.DARK_GRAY),
        Text.literal("   -> Materials can be found from trial chambers")
            .formatted(Formatting.DARK_GRAY),
        Text.literal(" - Vaults have a 50% chance to double-reward")
            .formatted(Formatting.DARK_GRAY),
        Text.literal(" - Standing near heavy-armored players causes debuff")
            .formatted(Formatting.DARK_GRAY),
        Text.literal(" - Removed Bedrock player attack cooldown")
            .formatted(Formatting.DARK_GRAY)
    );

    public static LootNumberProviderType PLAYER_VAULT;

    @Getter private static final Random random = new Random();

    @Getter private static MinecraftServer server;
    @Getter private static MongoServer mongoServer;
    @Getter private static Datastore playerStore;

    @Getter private static LocationPredicate trialChamberPredicate;
    @Getter private static Registry<Enchantment> enchantmentRegistry;

    @Getter private static final Map<UUID, List<ItemStack>> playerLoot = new ConcurrentHashMap<>();

    private static final Set<Item> BLACKLISTED = Set.of(
        Items.SPAWNER,
        Items.ALLAY_SPAWN_EGG,
        Items.WITHER_SPAWN_EGG,
        Items.ENDER_DRAGON_SPAWN_EGG,
        Items.ENDERMAN_SPAWN_EGG,
        Items.BAT_SPAWN_EGG
    );

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

        try {
            // Create the mod configuration directory.
            Files.createDirectories(Path.of("config/mwhrd"));
        } catch (Exception ignored) {
            log.debug("Unable to create configuration directory.");
        }

        // Initialize MongoDB.
        MyWellHasRunDry.mongoServer = new MongoServer(new H2Backend("config/mwhrd/database.mv"));
        MyWellHasRunDry.mongoServer.bind();

        // Initialize Morphia.
        MyWellHasRunDry.playerStore = Morphia.createDatastore(MongoClients.create(), "mwhrd");

        // Register the item despawn thread.
        TrialChamberLoot.ItemThread.initialize();

        // Register registry entries.
        MyWellHasRunDry.PLAYER_VAULT = Registry.register(
            Registries.LOOT_NUMBER_PROVIDER_TYPE,
            Identifier.of("mwhrd", "player_vault"),
            new LootNumberProviderType(PlayerVaultNumberProvider.CODEC));

        // Register commands.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            LootCommand.register(dispatcher);
            DebugCommand.register(dispatcher);
            PartyCommand.register(dispatcher);
            ReturnCommand.register(dispatcher);
        });

        // Wait for the server to start.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MyWellHasRunDry.server = server;

            MyWellHasRunDry.enchantmentRegistry = server
                .getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT);

            var trialChamber = server
                .getRegistryManager()
                .get(RegistryKeys.STRUCTURE)
                .entryOf(StructureKeys.TRIAL_CHAMBERS);
            MyWellHasRunDry.trialChamberPredicate = LocationPredicate.Builder.create()
                .light(LightPredicate.Builder.create()
                    .light(NumberRange.IntRange.atLeast(1)))
                .structure(RegistryEntryList.of(trialChamber))
                .build();
        });

        // Wait for server ticks.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                MyWellHasRunDry.setAdventureMode(player);
                DebuffManager.applyDebuffs(player);
            }
        });

        // Prevent blocks from being broken/placed.
        UseBlockCallback.EVENT.register(DebuffManager::blockPlaceCheck);
        PlayerBlockBreakEvents.BEFORE.register(DebuffManager::blockBreakCheck);

        // Prevent certain blacklisted items from being used.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            var item = player.getStackInHand(hand);

            if (item.getItem() == Items.BEACON) {
                return BeaconManager.handleBeacon(item, world, hitResult);
            }

            return BLACKLISTED.contains(item.getItem()) ?
                ActionResult.FAIL : ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            var item = player.getStackInHand(hand);
            var pass = BLACKLISTED.contains(item.getItem());
            return pass ?
                TypedActionResult.fail(item) :
                TypedActionResult.pass(item);
        });

        // Wait for players to join.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            if (!GeyserApi.api().isBedrockPlayer(player.getUuid())) {
                CHANGELOG.forEach(player::sendMessage);
            } else {
                // Apply Bedrock player buff.
                DebuffManager.applyBedrockBuff(player);
            }
        });
    }

    /**
     * Sets the player to adventure mode.
     *
     * @param player The player to set to adventure mode.
     */
    private static void setAdventureMode(
        ServerPlayerEntity player
    ) {
        var condPlayer = (IPlayerConditions) player;

        var interactionManager = player.interactionManager;
        if (!interactionManager.isSurvivalLike()) {
            condPlayer.mwhrd$setOminous(false);
            condPlayer.mwhrd$setInTrialChamber(false);
            return;
        }

        var passed = MyWellHasRunDry.trialChamberPredicate.test(
            player.getServerWorld(), player.getX(),
            player.getY(), player.getZ());

        condPlayer.mwhrd$setInTrialChamber(passed);
        if (!passed) {
            condPlayer.mwhrd$setOminous(false);
        }
    }
}
