package moe.seikimo.mwhrd.game.lightrealm;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.general.MapBuilder;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomWorlds;
import moe.seikimo.mwhrd.events.BlockBreakEvent;
import moe.seikimo.mwhrd.events.EntityPreDeathEvent;
import moe.seikimo.mwhrd.events.PlayerMoveEvent;
import moe.seikimo.mwhrd.interfaces.ITimeTraveler;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Ticks;
import moe.seikimo.mwhrd.worldedit.AsyncPool;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Global server instance for the Realm of Light.
 */
@Slf4j
public final class TheRealmOfLight {
    private static final BlockPos BOTTOM_CORNER = new BlockPos(-25, -64, -25);
    private static final BlockPos TOP_CORNER = new BlockPos(24, 320, 24);

    private static final Text[] INFO_MESSAGES = {
        Text.empty(),
        Text.translatable("text.mwhrd.dimension.rol.info.1")
            .formatted(Formatting.YELLOW),
        Text.empty(),
        Text.translatable("text.mwhrd.dimension.rol.info.2")
            .formatted(Formatting.AQUA),
        Text.translatable("text.mwhrd.dimension.rol.info.3")
            .formatted(Formatting.LIGHT_PURPLE),
        Text.empty(),
        Text.translatable("text.mwhrd.dimension.rol.info.4")
            .formatted(Formatting.GREEN),
        Text.empty()
    };

    private static final Map<Block, ItemStack> BLOCK_OVERRIDES =
        MapBuilder.<Block, ItemStack>create()
            .put(Blocks.AZALEA_LEAVES, new ItemStack(Items.BONE_MEAL))
            .put(Blocks.FLOWERING_AZALEA_LEAVES, new ItemStack(Items.GLOW_BERRIES, 2))
            .put(Blocks.SHORT_GRASS, new ItemStack(Items.WHEAT_SEEDS))
            .put(Blocks.TALL_GRASS, new ItemStack(Items.WHEAT_SEEDS, 2))
            .put(Blocks.ROOTED_DIRT, new ItemStack(Items.DIRT))
            .put(Blocks.OAK_LOG, new ItemStack(Items.OAK_PLANKS, 8))
            .build();

    @Getter private static final TheRealmOfLight instance = new TheRealmOfLight();

    static {
        BlockBreakEvent.EVENT.register(TheRealmOfLight::onBlockBreak);
        PlayerMoveEvent.EVENT.register(TheRealmOfLight::onPlayerMove);
        EntityPreDeathEvent.EVENT.register(TheRealmOfLight::onPreDeath);
    }

    /**
     * Invoked when a player breaks a block.
     */
    private static ActionResult onBlockBreak(
        World world, PlayerEntity player,
        BlockPos pos, BlockState state,
        @Nullable BlockEntity blockEntity,
        ItemStack tool
    ) {
        if (!Players.inWorld(CustomWorlds.REALM_OF_LIGHT, player)) return ActionResult.PASS;

        // Check if the block broken has an override.
        var override = BLOCK_OVERRIDES.get(state.getBlock());

        // If the block has an override, drop the override item.
        if (override != null) {
            var itemEntity = new ItemEntity(
                world, pos.getX(), pos.getY(), pos.getZ(),
                override.copy()
            );
            itemEntity.setPickupDelay(10);
            world.spawnEntity(itemEntity);
        }

        // If we dropped an item, we do not drop the block.
        return override == null ? ActionResult.PASS : ActionResult.FAIL;
    }

    /**
     * Invoked when a player moves.
     *
     * @param world The world the player is in.
     * @param pos The position the player moved to.
     * @param player The player that moved.
     */
    private static void onPlayerMove(World world, BlockPos pos, PlayerEntity player) {
        if (!Players.inWorld(CustomWorlds.REALM_OF_LIGHT, player)) return;

        var y = pos.getY();
        if (y <= -70) {
            TheRealmOfLight.respawn(player);

            // Send a message to the player.
            player.sendMessage(
                Text.literal("Oh no! ")
                    .formatted(Formatting.BOLD, Formatting.RED)
                    .append(Text.translatable("text.mwhrd.dimension.rol.fall"))
            );
        }
    }

    /**
     * Invoked when an entity is about to die.
     *
     * @param entity The entity that is about to die.
     * @param source The source of the damage.
     * @return True to allow dying.
     */
    private static boolean onPreDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof PlayerEntity player)) return true;
        if (!Players.inWorld(CustomWorlds.REALM_OF_LIGHT, player)) return true;

        // Respawn the player.
        TheRealmOfLight.respawn(player);

        // Send a message to the player.
        player.sendMessage(
            Text.literal("Oh no! ")
                .formatted(Formatting.BOLD, Formatting.RED)
                .append(Text.translatable("text.mwhrd.dimension.rol.death"))
        );

        return false;
    }

    /**
     * Respawns a player in the overworld.
     *
     * @param player The player to respawn.
     */
    private static void respawn(PlayerEntity player) {
        // Teleport the player to the overworld.
        Players.respawn(player);

        // Restore the player's inventory.
        if (player instanceof ITimeTraveler traveler) try {
            traveler.mwhrd$restoreInventory();
        } catch (Exception ex) {
            Players.kickPlayer(
                (ServerPlayerEntity) player,
                Text.literal("Unable to restore your inventory. Please contact a server admin."));
            log.error("Failed to restore player inventory.", ex);
        }
    }

    /**
     * All participating players.
     * Resets after the realm collapses.
     */
    private final Set<ServerPlayerEntity> players = new HashSet<>();
    private final Set<ServerPlayerEntity> queued = new HashSet<>();

    private ServerWorld world;

    private long
        transportTicks = 200,
        collapseTicks = Ticks.ofHours(1);

    @Getter private final AtomicBoolean running
        = new AtomicBoolean(false);

    /**
     * Initializes the dimension.
     */
    public void initialize(ServerWorld world) {
        this.world = world;

        ServerTickEvents.START_SERVER_TICK.register(this::tick);

        this.destroy();
    }

    /**
     * Prepares the world for playability.
     */
    public synchronized void prepare() {
        try {
            var generator = new LightTowerGenerator(this.world);
            generator.generate(24);
            generator.placeIslands();

            Players.broadcast(Text.translatable("text.mwhrd.dimension.rol.ready")
                .formatted(Formatting.LIGHT_PURPLE), true);

            this.running.set(true);
        } catch (Exception ex) {
            log.error("Failed to prepare The Realm of Light.", ex);
        }
    }

    /**
     * Invoked when the dimension should tick.
     */
    private void tick(MinecraftServer server) {
        if (!this.running.get()) return;

        if (this.transportTicks-- <= 0) {
            this.transportPlayers();
            this.transportTicks = 200;
        }

        if (this.collapseTicks-- <= 0) {
            this.destroy();
            this.collapseTicks = Ticks.ofHours(1);
        }
    }

    /**
     * Queues a player to be transported to the dimension.
     *
     * @param player The player to queue.
     */
    public void queuePlayer(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        this.queued.add(serverPlayer);
    }

    /**
     * Transports all queued players to the dimension.
     */
    public void transportPlayers() {
        // Get all players in the queue.
        var copy = new HashSet<>(this.queued);
        for (var player : copy) {
            if (!(player instanceof ITimeTraveler traveler)) {
                this.queued.remove(player);
                continue;
            }

            // Store the player's inventory.
            traveler.mwhrd$storeInventory(true);
            // Give the player bone meal.
            player.getInventory().offerOrDrop(
                new ItemStack(Items.BONE_BLOCK, 2)
            );

            // Teleport the player to the realm.
            var portalInfo = traveler.mwhrd$getQueuedPortal();
            if (portalInfo == null) {
                this.queued.remove(player);
                continue;
            }

            var portal = portalInfo.getLeft();
            var pos = portalInfo.getRight();
            player.tryUsePortal(portal, pos);

            // Send info messages.
            Players.bulkSend(player, INFO_MESSAGES);

            // Reset player state.
            traveler.mwhrd$setQueuedPortal(null);
        }

        // Add all players to the realm.
        this.players.addAll(this.queued);
        // Clear the queue.
        this.queued.clear();
    }

    /**
     * Resets the dimension.
     */
    public void destroy() {
        log.info("The Realm of Light is resetting...");

        // Remove all entities in the world.
        for (var entity : this.world.iterateEntities()) {
            if (entity == null) continue;

            if (entity instanceof ServerPlayerEntity player) {
                var spawn = MyWellHasRunDry.getDefaultSpawn();
                player.teleport(
                    MyWellHasRunDry.getServer().getWorld(World.OVERWORLD),
                    spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0
                );
            } else {
                entity.kill();
            }
        }

        // Restore all players.
        this.players.forEach(player -> {
            if (!(player instanceof ITimeTraveler traveler)) return;

            Players.respawn(player);
            traveler.mwhrd$restoreInventory();
        });

        // Destroy all blocks within the world's border.
        var pool = AsyncPool.getInstance();
        var operation = pool.clear(
            null, this.world,
            BOTTOM_CORNER, TOP_CORNER
        );

        this.running.set(false);
        operation.whenComplete((_blocks, exception) -> {
            if (exception != null) {
                log.error("Unable to delete blocks in the realm.", exception);
            } else {
                this.prepare();
                log.info("The Realm of Light has been reset!");
            }
        });
    }
}
