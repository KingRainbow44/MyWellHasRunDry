package moe.seikimo.mwhrd.game.lightrealm;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomEntities;
import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.custom.CustomWorlds;
import moe.seikimo.mwhrd.custom.entities.GuardianOfLight;
import moe.seikimo.mwhrd.interfaces.ITimeTraveler;
import moe.seikimo.mwhrd.interfaces.game.IRespawnableMob;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Ticks;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.utils.items.ItemBuilder;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;

@Slf4j
public final class TheRealmOfLight extends RuntimeWorld {
    private static final long MAX_TICKS = Ticks.ofMinutes(2);
    private static final long TRANSPORT_WAIT = Ticks.ofSeconds(1);

    private static final Vec3d BOSS_SPAWN_POS = new Vec3d(1, 410, 0);

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

    /**
     * Opens a new instance of The Realm of Light.
     *
     * @param fantasy The fantasy instance.
     * @return The handle to the world.
     */
    public static RuntimeWorldHandle open(Fantasy fantasy) {
        var handle = fantasy.openTemporaryWorld(CustomWorlds.REALM_OF_LIGHT);
        if (handle.asWorld() instanceof TheRealmOfLight realm) {
            realm.setHandle(handle);
        }

        return handle;
    }

    /**
     * Fetches the world instance.
     *
     * @return The Realm of Light.
     */
    @SuppressWarnings("resource")
    public static TheRealmOfLight getWorld() {
        var world = MyWellHasRunDry.getRealmOfLight().asWorld();
        if (world instanceof TheRealmOfLight realm) {
            return realm;
        }

        throw new IllegalStateException("The Realm of Light is not loaded.");
    }

    private final Set<ServerPlayerEntity> queued = new HashSet<>();
    private final LightTowerGenerator generator;

    private long ticksAlive = 0;

    @Getter
    private boolean closed = false;

    @Setter
    private transient RuntimeWorldHandle handle;

    public TheRealmOfLight(
        MinecraftServer server, RegistryKey<World> registryKey,
        RuntimeWorldConfig config, Style style
    ) {
        super(server, registryKey, config, style);

        this.generator = new LightTowerGenerator(this);
    }

    /**
     * Invoked when the world is first constructed.
     */
    private void initialize() {
        // Prepare the world border.
        var border = this.getWorldBorder();
        border.setSize(35);
        border.setDamagePerBlock(1);

        // Generate the world.
        this.generate();
    }

    /**
     * Generates and spawns all entities.
     */
    private void generate() {
        try {
            // Place the world's islands.
            this.generator.generate(24);
            this.generator.placeIslands();

            // Place entities on the islands.
            this.spawnEntities();

            Players.broadcast(Text.translatable("text.mwhrd.dimension.rol.ready")
                .formatted(Formatting.LIGHT_PURPLE), true);
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
            if (++spawned == 1) continue;

            // Check if the node is a boss island.
            if (spawned == this.generator.getNodes().size()) {
                // Create the boss entity.
                var entity = new GuardianOfLight(CustomEntities.GUARDIAN_OF_LIGHT, this);

                // Set the entity's position.
                entity.setPersistent();
                entity.setPosition(BOSS_SPAWN_POS);
                // noinspection ConstantValue
                if ((Object) entity instanceof IRespawnableMob respawnable) {
                    respawnable.mwhrd$setSpawnPoint(Utils.blockPos(BOSS_SPAWN_POS));
                }

                // Spawn the entity.
                this.spawnEntity(entity);
            } else for (var i = 0; i < 3; i++) {
                // Make a normal entity.
                var entity = this.makeEntity(spawned, i);

                // Set the entity's position.
                entity.setPosition(Vec3d.of(node));
                if (entity instanceof IRespawnableMob respawnable) {
                    respawnable.mwhrd$setSpawnPoint(Utils.blockPos(node));
                }

                this.spawnEntity(entity);
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
            case 0 -> new ZombieEntity(this);
            case 1 -> {
                var skeleton = new SkeletonEntity(EntityType.SKELETON, this);

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
            case 2 -> new BreezeEntity(EntityType.BREEZE, this);
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
     * Teleports all players in the teleport queue.
     */
    private void doTeleport() {
        // Get all players in the queue.
        var copy = new HashSet<>(this.queued);
        for (var player : copy) {
            if (!(player instanceof ITimeTraveler traveler)) {
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

        // Clear the queue.
        this.queued.clear();
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
     * Queues the world for deletion.
     * Sends all players back to the overworld.
     */
    public void destroy() {
        this.destroy(Reason.COLLAPSED);
    }

    /**
     * Queues the world for deletion.
     * Sends all players back to the overworld.
     *
     * @param reason The reason the world is being destroyed.
     */
    public void destroy(Reason reason) {
        log.info("The Realm of Light is resetting...");

        // Restore all players.
        var players = new HashSet<>(this.getPlayers());
        players.forEach(player -> {
            RealmOfLightLogic.respawn(player);

            if (reason == Reason.DEFEATED) {
                // Send the player a completion message.
                player.sendMessage(Text.translatable("text.mwhrd.dimension.rol.completed")
                    .formatted(Formatting.GREEN));

                var item = new ItemStack(
                    CustomItems.SOUL_OF_LIGHT,
                    Utils.random(1, 5));
                player.getInventory().offerOrDrop(item);
            }
        });

        // Delete the world.
        this.handle.delete();
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        if (this.ticksAlive == 0) {
            this.initialize();
        }

        this.ticksAlive++;

        // Disable fall damage for all entities.
        for (var entity : this.iterateEntities()) {
            entity.fallDistance = 0;
        }

        // Transport all players in the teleport queue.
        if (this.ticksAlive % TRANSPORT_WAIT == 0) {
            this.doTeleport();
        }

        // Destroy the world if it has been alive for one hour.
        if (this.ticksAlive >= MAX_TICKS) {
            this.destroy();
        }

        super.tick(shouldKeepTicking);
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        super.close();
    }

    public enum Reason {
        COLLAPSED,
        DEFEATED
    }
}
