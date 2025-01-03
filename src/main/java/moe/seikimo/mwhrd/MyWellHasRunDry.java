package moe.seikimo.mwhrd;

import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.h2.H2Backend;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.data.DatabaseUtils;
import moe.seikimo.mwhrd.game.beacon.BeaconEffect;
import moe.seikimo.mwhrd.game.beacon.BeaconManager;
import moe.seikimo.mwhrd.commands.*;
import moe.seikimo.mwhrd.custom.*;
import moe.seikimo.mwhrd.game.lightrealm.TheRealmOfLight;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import moe.seikimo.mwhrd.managers.BuffManager;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.providers.PlayerVaultNumberProvider;
import moe.seikimo.mwhrd.utils.items.ItemStorage;
import moe.seikimo.mwhrd.game.worldedit.AsyncPool;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.predicate.LightPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureKeys;
import org.geysermc.geyser.api.GeyserApi;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public final class MyWellHasRunDry implements DedicatedServerModInitializer {
    public static final List<Text> CHANGELOG = List.of(
        Text.literal(" My Well Has Run Dry: ")
            .formatted(Formatting.BOLD, Formatting.DARK_AQUA)
            .append(Text.literal("v" + BuildConfig.VERSION)
                .formatted(Formatting.GOLD)),
        Text.literal(" Allied Nations Align, Enemy Ideals Falter")
            .formatted(Formatting.RED),
        Text.empty(),
        Text.literal("View the changelog at:")
            .formatted(Formatting.GRAY),
        Text.literal("https://docs.seikimo.moe/mwhrd/changelog")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(
                    ClickEvent.Action.OPEN_URL,
                    "https://docs.seikimo.moe/mwhrd/changelog"
                )))
            .formatted(Formatting.AQUA)
    );

    public static final String MOD_ID = "mwhrd";

    @Getter private static final Random random = new Random();

    public static LootNumberProviderType PLAYER_VAULT;

    @Getter private static MinecraftServer server;
    @Getter private static Fantasy fantasy;

    @Getter private static MongoServer mongoServer;
    @Getter private static Datastore datastore;

    @Getter private static BlockPos defaultSpawn;
    @Getter private static LocationPredicate trialChamberPredicate;

    @Getter private static DynamicRegistryManager registry;
    @Getter private static Registry<Enchantment> enchantmentRegistry;
    @Getter private static Registry<Item> itemRegistry;

    @Getter @Setter
    private static RuntimeWorldHandle realmOfLight;

    /**
     * Gets or creates the loot set for the player.
     *
     * @param player The player to get the loot for.
     * @return The loot set for the player.
     */
    public static ItemStorage getLoot(ServerPlayerEntity player) {
        if (!(player instanceof IDBObject<?> dbObj)) return ItemStorage.EMPTY;
        var model = dbObj.mwhrd$getData();
        if (!(model instanceof PlayerModel data)) return ItemStorage.EMPTY;
        return data.getLoot();
    }

    @Override
    public void onInitializeServer() {
        // Register custom content.
        CustomBlocks.register();
        CustomComponents.register();
        CustomItems.register();
        CustomEntities.register();

        try {
            // Create the mod configuration directory.
            Files.createDirectories(Path.of("config/mwhrd"));
        } catch (Exception ignored) {
            log.debug("Unable to create configuration directory.");
        }

        // Initialize MongoDB.
        MyWellHasRunDry.mongoServer = new MongoServer(new H2Backend("config/mwhrd/database.mv"));
        MyWellHasRunDry.mongoServer.bind("0.0.0.0", 8018);

        // Initialize Morphia.
        var store = MyWellHasRunDry.datastore =
            Morphia.createDatastore(MongoClients.create("mongodb://localhost:8018"), "mwhrd");
        DatabaseUtils.DATASTORE.set(store);

        // Create the async pool.
        AsyncPool.initialize();

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
            HardcoreCommand.register(dispatcher);
            ChangelogCommand.register(dispatcher);

            SelectionCommands.register(dispatcher);
        });

        // Wait for the server to start.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MyWellHasRunDry.server = server;
            var fantasy = MyWellHasRunDry.fantasy = Fantasy.get(server);

            // Resolve registry tables.
            MyWellHasRunDry.registry = server.getRegistryManager();

            MyWellHasRunDry.enchantmentRegistry = server
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);

            MyWellHasRunDry.itemRegistry = server
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ITEM);

            var trialChamber = server
                .getRegistryManager()
                .getOrThrow(RegistryKeys.STRUCTURE)
                .getOrThrow(StructureKeys.TRIAL_CHAMBERS);
            MyWellHasRunDry.trialChamberPredicate = LocationPredicate.Builder.create()
                .light(LightPredicate.Builder.create()
                    .light(NumberRange.IntRange.atLeast(1)))
                .structure(RegistryEntryList.of(trialChamber))
                .build();

            var overworld = server.getWorld(World.OVERWORLD);
            if (overworld == null) {
                throw new IllegalStateException("Overworld is null.");
            }
            MyWellHasRunDry.defaultSpawn = overworld.getSpawnPos();

            // Register the health scoreboard.
            var scoreboard = server.getScoreboard();
            if (scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST) == null) {
                var objective = scoreboard.addObjective(
                    "health", ScoreboardCriterion.HEALTH,
                    Text.literal("Health"), ScoreboardCriterion.RenderType.HEARTS,
                    true, null
                );
                scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.LIST, objective);

                log.info("Configured the scoreboard to show player health!");
            }

            // Initialize custom dimensions.
            CustomWorlds.register();

            MyWellHasRunDry.realmOfLight = TheRealmOfLight.open(fantasy);
        });

        // Wait for server ticks.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                MyWellHasRunDry.setAdventureMode(player);
                BuffManager.applyDebuffs(player);
                BeaconManager.openBeaconMenu(player);
            }
        });

        // Listen for block breaking.
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (!(player instanceof IDBObject<?> dbObj)) return;
            var model = dbObj.mwhrd$getData();
            if (!(model instanceof PlayerModel playerModel)) return;
            if (!playerModel.isSurvivedHardcore()) return;

            // Spawn an experience orb at the block's position.
            ExperienceOrbEntity.spawn((ServerWorld) world, pos.toCenterPos(), 4);
        });

        // Prevent blocks from being broken/placed.
        UseBlockCallback.EVENT.register(BuffManager::blockPlaceCheck);
        PlayerBlockBreakEvents.BEFORE.register(BuffManager::blockBreakCheck);

        // Prevent certain blacklisted items from being used.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            var item = player.getStackInHand(hand);
            var itemType = item.getItem();

            if (
                itemType == Items.BEACON ||
                    itemType == CustomItems.ADVANCED_BEACON
            ) {
                return BeaconManager.handleBeacon(item, world, hitResult);
            }

            return ActionResult.PASS;
        });

        // Wait for players to join.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            if (player instanceof IDBObject<?> model) {
                model.mwhrd$loadData();
            }

            // Apply luck buff.
            BuffManager.applyBuffs(player);

            // Apply Bedrock player buff.
            if (GeyserApi.api().isBedrockPlayer(player.getUuid())) {
                BuffManager.applyBedrockBuff(player);
            }

            // Send the changelog message.
            CHANGELOG.forEach(player::sendMessage);

            // Remove all beacon effects on join.
            Arrays.stream(BeaconEffect.values())
                .forEach(e -> e.remove(player.getWorld(), player));

            // Give players all recipes.
            player.unlockRecipes(server.getRecipeManager().values());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            var player = handler.getPlayer();

            // Save the player's data.
            if (player instanceof IDBObject<?> model) {
                model.mwhrd$getData().save();
            }

            // Remove all beacon effects on disconnect.
            Arrays.stream(BeaconEffect.values())
                .forEach(e -> e.remove(player.getWorld(), player));
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
