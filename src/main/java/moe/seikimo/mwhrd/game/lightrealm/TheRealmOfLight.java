package moe.seikimo.mwhrd.game.lightrealm;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.general.MapBuilder;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomEntities;
import moe.seikimo.mwhrd.custom.CustomWorlds;
import moe.seikimo.mwhrd.custom.entities.GuardianOfLight;
import moe.seikimo.mwhrd.events.BlockBreakEvent;
import moe.seikimo.mwhrd.events.EntityPreDeathEvent;
import moe.seikimo.mwhrd.events.PlayerCraftEvent;
import moe.seikimo.mwhrd.events.PlayerMoveEvent;
import moe.seikimo.mwhrd.interfaces.ITimeTraveler;
import moe.seikimo.mwhrd.interfaces.game.IRespawnableMob;
import moe.seikimo.mwhrd.utils.*;
import moe.seikimo.mwhrd.utils.items.ItemBuilder;
import moe.seikimo.mwhrd.worldedit.AsyncPool;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Global server instance for the Realm of Light.
 */
@Slf4j
public final class TheRealmOfLight {
    private static final Vec3d BOSS_SPAWN_POS = new Vec3d(1, 410, 1);
    private static final BlockPos BOTTOM_CORNER = new BlockPos(-25, -64, -25);
    private static final BlockPos TOP_CORNER = new BlockPos(24, 410, 24);

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
            .put(Blocks.FLOWERING_AZALEA_LEAVES, new ItemStack(Items.GOLDEN_CARROT, 2))
            .put(Blocks.SHORT_GRASS, new ItemStack(Items.WHEAT_SEEDS))
            .put(Blocks.TALL_GRASS, new ItemStack(Items.WHEAT_SEEDS, 2))
            .put(Blocks.ROOTED_DIRT, new ItemStack(Items.DIRT))
            .put(Blocks.MOSS_CARPET, new ItemStack(Items.COBBLESTONE))
            .put(Blocks.OAK_LOG, new ItemStack(Items.OAK_PLANKS, 8))
            .put(Blocks.WHEAT, new ItemStack(Items.IRON_INGOT))
            .build();

    private static final Map<Pair<Integer, Integer>, Set<Block>> DISALLOWED_BLOCKS =
        MapBuilder.<Pair<Integer, Integer>, Set<Block>>create()
            .put(new Pair<>(-64, 0), Set.of())
            .put(new Pair<>(1, 100), Set.of(Blocks.MOSS_BLOCK))
            .put(new Pair<>(101, 250), Set.of(Blocks.MOSS_BLOCK, Blocks.BONE_BLOCK, Blocks.OAK_PLANKS))
            .put(new Pair<>(251, 270), Set.of(Blocks.MOSS_BLOCK, Blocks.OAK_PLANKS, Blocks.BONE_BLOCK, Blocks.COBBLESTONE, Blocks.DIRT))
            .build();

    @Getter private static final TheRealmOfLight instance = new TheRealmOfLight();

    static {
        ServerTickEvents.START_WORLD_TICK.register(TheRealmOfLight::onWorldTick);
        BlockBreakEvent.EVENT.register(TheRealmOfLight::onBlockBreak);
        UseBlockCallback.EVENT.register(TheRealmOfLight::onBlockPlace);
        PlayerMoveEvent.EVENT.register(TheRealmOfLight::onPlayerMove);
        EntityPreDeathEvent.EVENT.register(TheRealmOfLight::onPreDeath);
        PlayerCraftEvent.EVENT.register(TheRealmOfLight::onCraft);
    }

    /**
     * Invoked when a world ticks.
     */
    private static void onWorldTick(ServerWorld world) {
        if (!Utils.compare(world, CustomWorlds.REALM_OF_LIGHT)) return;

        for (var entity : world.iterateEntities()) {
            entity.fallDistance = 0;
        }
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
     * Invoked when a player places a block.
     *
     * @param player The player that placed the block.
     * @param world The world the player is in.
     * @param hand The hand the player used to place the block.
     * @param hitResult The block hit result.
     * @return The result of the block placement.
     */
    private static ActionResult onBlockPlace(
        PlayerEntity player, World world,
        Hand hand, BlockHitResult hitResult
    ) {
        if (world.getRegistryKey() != CustomWorlds.REALM_OF_LIGHT) return ActionResult.PASS;

        var stack = player.getStackInHand(hand);
        var item = stack.getItem();
        var block = Block.getBlockFromItem(item);

        var pos = hitResult.getBlockPos();
        var y = pos.getY();

        // Check if the block type is banned.
        if (block instanceof StairsBlock || block instanceof SlabBlock || block instanceof FenceBlock || block instanceof FenceGateBlock) {
            player.sendMessage(
                Text.translatable("text.mwhrd.dimension.rol.banned")
                    .formatted(Formatting.RED)
            );
            return ActionResult.FAIL;
        }

        // Check if the player is in a disallowed area.
        for (var entry : DISALLOWED_BLOCKS.entrySet()) {
            var range = entry.getKey();
            if (y >= range.getLeft() && y <= range.getRight()) {
                var disallowed = entry.getValue();
                if (disallowed.contains(block)) {
                    player.sendMessage(
                        Text.translatable("text.mwhrd.dimension.rol.disallowed")
                            .formatted(Formatting.RED)
                    );
                    return ActionResult.FAIL;
                }
            }
        }

        return ActionResult.PASS;
    }

    /**
     * Invoked when a player moves.
     *
     * @param world The world the player is in.
     * @param pos The position the player moved to.
     * @param player The player that moved.
     */
    private static void onPlayerMove(World world, BlockPos pos, PlayerEntity player) {
        if (world.getRegistryKey() != CustomWorlds.REALM_OF_LIGHT) return;

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
        if (entity instanceof PlayerEntity player) {
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
        } else if (entity instanceof MobEntity mob) {
            if (!(mob instanceof IRespawnableMob respawnable)) return true;
            if (!Utils.inWorld(mob, CustomWorlds.REALM_OF_LIGHT)) return true;

            // Check if the mob died to the void.
            var position = source.getPosition();
            if ((position != null && position.getY() < -50) ||
                Utils.compare(source, DamageTypes.OUT_OF_WORLD)) {
                // Teleport the mob back to its starting position.
                var spawnPoint = respawnable.mwhrd$getSpawnPoint();
                if (spawnPoint.equals(BlockPos.ORIGIN)) return true;

                mob.fallDistance = 0f;
                mob.teleport(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), false);
                mob.setHealth(mob.getMaxHealth());
                return false;
            }

            // Check what the mob has died to.
            if (source.getAttacker() instanceof PlayerEntity) {
                // Drop the mob's gear.
                var world = mob.getEntityWorld();

                mob.getArmorItems().forEach(item -> {
                    if (item.isEmpty()) return;

                    var itemEntity = new ItemEntity(
                        world, mob.getX(), mob.getY(), mob.getZ(),
                        item.copy()
                    );
                    world.spawnEntity(itemEntity);
                });
            }
        }

        return true;
    }

    /**
     * Invoked when a player crafts an item.
     *
     * @param player The player who crafted the item.
     * @param stack The item that was crafted.
     */
    private static void onCraft(PlayerEntity player, ItemStack stack) {
        if (!Players.inWorld(CustomWorlds.REALM_OF_LIGHT, player)) return;

        // Check if the item is a tool.
        var item = stack.getItem();
        if (item instanceof ToolItem) {
            EnchantmentHelper.apply(stack, builder -> {
                builder.add(Utils.lookup(Enchantments.EFFICIENCY), 5);
                builder.add(Utils.lookup(Enchantments.FORTUNE), 5);
                builder.add(Utils.lookup(Enchantments.UNBREAKING), 3);
            });

            if (item instanceof SwordItem || item instanceof AxeItem) {
                EnchantmentHelper.apply(stack, builder -> {
                    builder.add(Utils.lookup(Enchantments.SHARPNESS), 5);
                    builder.add(Utils.lookup(Enchantments.SWEEPING_EDGE), 3);
                    builder.add(Utils.lookup(Enchantments.LOOTING), 5);
                    builder.add(Utils.lookup(Enchantments.UNBREAKING), 3);
                });
            }
        }
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
    private LightTowerGenerator generator;

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

        // Force-load all chunks.
        CompletableFuture.allOf(
            Worlds.forceLoad(world, 0, 0),
            Worlds.forceLoad(world, -1, 0),
            Worlds.forceLoad(world, 0, -1),
            Worlds.forceLoad(world, -1, -1)
        ).join();

        // Set the world's border.
        var border = this.world.getWorldBorder();
        border.setSize(35);
        border.setDamagePerBlock(1);

        ServerTickEvents.START_SERVER_TICK.register(this::tick);

        this.destroy();
    }

    /**
     * Prepares the world for playability.
     */
    public synchronized void prepare() {
        try {
            this.generator = new LightTowerGenerator(this.world);
            this.generator.generate(24);
            this.generator.placeIslands();

            this.spawnEntities();

            Players.broadcast(Text.translatable("text.mwhrd.dimension.rol.ready")
                .formatted(Formatting.LIGHT_PURPLE), true);

            this.running.set(true);
        } catch (Exception ex) {
            log.error("Failed to prepare The Realm of Light.", ex);
        }
    }

    /**
     * Spawns all realm monsters.
     */
    private void spawnEntities() {
        Objects.requireNonNull(this.generator);

        var spawned = 0;
        for (var node : this.generator.getNodes()) {
            // Skip the first island.
            if (++spawned == 0) continue;

            // Check if the node is a boss island.
            if (spawned == this.generator.getNodes().size()) {
                // Create the boss entity.
                var entity = new GuardianOfLight(CustomEntities.GUARDIAN_OF_LIGHT, this.world);

                // Set the entity's position.
                entity.setPosition(BOSS_SPAWN_POS);
                // noinspection ConstantValue
                if ((Object) entity instanceof IRespawnableMob respawnable) {
                    respawnable.mwhrd$setSpawnPoint(Utils.blockPos(BOSS_SPAWN_POS));
                }

                // Spawn the entity.
                this.world.spawnEntity(entity);
            } else for (var i = 0; i < 3; i++) {
                // Make a normal entity.
                var entity = this.makeEntity(spawned, i);

                // Set the entity's position.
                entity.setPosition(Vec3d.of(node));
                if (entity instanceof IRespawnableMob respawnable) {
                    respawnable.mwhrd$setSpawnPoint(Utils.blockPos(node));
                }

                this.world.spawnEntity(entity);
            }
        }
    }

    /**
     * Creates an entity for the Realm of Light.
     *
     * @param node The node to create the entity at.
     * @param index The index of the entity to create.
     * @return The created entity.
     */
    private HostileEntity makeEntity(int node, int index) {
        var entity = switch (index) {
            case 0 -> new ZombieEntity(this.world);
            case 1 -> {
                var skeleton = new SkeletonEntity(EntityType.SKELETON, this.world);

                // Apply skeleton-specific items.
                ItemBuilder.of(Items.BOW)
                    .enchant(Enchantments.POWER, switch (node) {
                        case 1, 2 -> 0;
                        case 3, 4, 5 -> 1;
                        case 6, 7 -> 2;
                        case 8, 9 -> 3;
                        default -> 5;
                    })
                    .equip(skeleton, EquipmentSlot.MAINHAND);

                yield skeleton;
            }
            case 2 -> new BreezeEntity(EntityType.BREEZE, this.world);
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };

        // Apply gear depending on the current node.
        var protection = switch (node) {
            case 1, 2 -> 0;
            case 3, 4, 5 -> 1;
            case 6, 7 -> 2;
            case 8, 9 -> 3;
            default -> 5;
        };

        ItemBuilder.of(switch (node) {
            case 0, 1, 2 -> Items.LEATHER_HELMET;
            case 3, 4, 5 -> Items.IRON_HELMET;
            case 6, 7, 8, 9 -> Items.DIAMOND_HELMET;
            default -> Items.NETHERITE_HELMET;
        })
            .enchant(Enchantments.PROTECTION, protection)
            .enchant(Enchantments.THORNS, node != 1 ? 1 : 0)
            .unbreakable()
            .equip(entity, EquipmentSlot.HEAD);

        ItemBuilder.of(switch (node) {
                case 0, 1, 2 -> Items.LEATHER_CHESTPLATE;
                case 3, 4, 5 -> Items.IRON_CHESTPLATE;
                case 6, 7, 8, 9 -> Items.DIAMOND_CHESTPLATE;
                default -> Items.NETHERITE_CHESTPLATE;
            })
            .enchant(Enchantments.PROTECTION, protection)
            .enchant(Enchantments.THORNS, node != 1 ? 1 : 0)
            .unbreakable()
            .equip(entity, EquipmentSlot.CHEST);

        ItemBuilder.of(switch (node) {
                case 0, 1, 2 -> Items.LEATHER_LEGGINGS;
                case 3, 4, 5 -> Items.IRON_LEGGINGS;
                case 6, 7, 8, 9 -> Items.DIAMOND_LEGGINGS;
                default -> Items.NETHERITE_LEGGINGS;
            })
            .enchant(Enchantments.PROTECTION, protection)
            .enchant(Enchantments.THORNS, node != 1 ? 1 : 0)
            .unbreakable()
            .equip(entity, EquipmentSlot.LEGS);

        ItemBuilder.of(switch (node) {
                case 0, 1, 2 -> Items.LEATHER_BOOTS;
                case 3, 4, 5 -> Items.IRON_BOOTS;
                case 6, 7, 8, 9 -> Items.DIAMOND_BOOTS;
                default -> Items.NETHERITE_BOOTS;
            })
            .enchant(Enchantments.PROTECTION, protection)
            .enchant(Enchantments.THORNS, node != 1 ? 1 : 0)
            .unbreakable()
            .equip(entity, EquipmentSlot.FEET);

        // Spawn the entity.
        entity.setPersistent();
        return entity;
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

            // Make the player invulnerable.
            player.joinInvulnerabilityTicks = 20 * 10;

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
                if (entity instanceof MobEntity mob) {
                    mob.persistent = false;
                    mob.setHealth(0);
                    mob.damage(mob.getWorld().getDamageSources().magic(), 1);
                }

                entity.discard();
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
