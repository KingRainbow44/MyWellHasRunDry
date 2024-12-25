package moe.seikimo.mwhrd.game.lightrealm;

import lombok.extern.slf4j.Slf4j;
import moe.seikimo.general.MapBuilder;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.events.BlockBreakEvent;
import moe.seikimo.mwhrd.events.EntityPreDeathEvent;
import moe.seikimo.mwhrd.events.PlayerCraftEvent;
import moe.seikimo.mwhrd.events.PlayerMoveEvent;
import moe.seikimo.mwhrd.interfaces.ITimeTraveler;
import moe.seikimo.mwhrd.interfaces.game.IRespawnableMob;
import moe.seikimo.mwhrd.utils.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.Fantasy;

import java.util.*;

/**
 * Global server instance for the Realm of Light.
 */
@Slf4j
public final class RealmOfLightLogic {
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

    /**
     * Invoked when the server ticks.
     *
     * @param server The server instance.
     * @param world The world instance.
     */
    private static void onUnload(MinecraftServer server, ServerWorld world) {
        // If the world isn't the realm of light, continue.
        if (!Utils.compare(world, MyWellHasRunDry.getRealmOfLight())) return;

        // Get the fantasy instance.
        var fantasy = Fantasy.get(server);

        // Set the new realm of light instance.
        var handle = TheRealmOfLight.open(fantasy);
        MyWellHasRunDry.setRealmOfLight(handle);
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
        if (!Players.inWorld(MyWellHasRunDry.getRealmOfLight(), player)) return ActionResult.PASS;

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
        if (!(world instanceof TheRealmOfLight)) return ActionResult.PASS;

        var stack = player.getStackInHand(hand);
        var item = stack.getItem();
        var block = Block.getBlockFromItem(item);

        var pos = hitResult.getBlockPos();
        var y = pos.getY();

        // Check if the block type is banned.
        if (block instanceof StairsBlock || block instanceof SlabBlock || block instanceof FenceBlock || block instanceof FenceGateBlock) {
            player.sendMessage(
                Text.translatable("text.mwhrd.dimension.rol.banned")
                    .formatted(Formatting.RED), false
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
                            .formatted(Formatting.RED), false
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
        if (!(world instanceof TheRealmOfLight)) return;

        var y = pos.getY();
        if (y <= -70) {
            RealmOfLightLogic.respawn(player);

            // Send a message to the player.
            player.sendMessage(
                Text.literal("Oh no! ")
                    .formatted(Formatting.BOLD, Formatting.RED)
                    .append(Text.translatable("text.mwhrd.dimension.rol.fall")),
                false
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
            if (!Players.inWorld(MyWellHasRunDry.getRealmOfLight(), player)) return true;

            // Respawn the player.
            RealmOfLightLogic.respawn(player);

            // Send a message to the player.
            player.sendMessage(
                Text.literal("Oh no! ")
                    .formatted(Formatting.BOLD, Formatting.RED)
                    .append(Text.translatable("text.mwhrd.dimension.rol.death")),
                false
            );

            return false;
        } else if (entity instanceof MobEntity mob) {
            if (!(mob instanceof IRespawnableMob respawnable)) return true;
            if (!Utils.inWorld(mob, MyWellHasRunDry.getRealmOfLight())) return true;

            // Check if the mob died to the void.
            var position = source.getPosition();
            if ((position != null && position.getY() < -64) ||
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
        if (!Players.inWorld(MyWellHasRunDry.getRealmOfLight(), player)) return;

        // Check if the item is a tool.
        var item = stack.getItem();
        if (item instanceof MiningToolItem) {
            EnchantmentHelper.apply(stack, builder -> {
                builder.add(Utils.lookup(Enchantments.EFFICIENCY), 5);
                builder.add(Utils.lookup(Enchantments.FORTUNE), 5);
                builder.add(Utils.lookup(Enchantments.UNBREAKING), 3);
            });
        }

        if (item instanceof SwordItem || item instanceof AxeItem) {
            EnchantmentHelper.apply(stack, builder -> {
                builder.add(Utils.lookup(Enchantments.SHARPNESS), 5);
                builder.add(Utils.lookup(Enchantments.SWEEPING_EDGE), 3);
                builder.add(Utils.lookup(Enchantments.LOOTING), 5);
                builder.add(Utils.lookup(Enchantments.UNBREAKING), 3);
            });
        }
    }

    /**
     * Invoked when a player disconnects from the server.
     *
     * @param handler The network handler.
     * @param server The server instance.
     */
    private static void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        var player = handler.getPlayer();
        if (!Players.inWorld(MyWellHasRunDry.getRealmOfLight(), player)) return;

        RealmOfLightLogic.respawn(player);
    }

    /**
     * Respawns a player in the overworld.
     *
     * @param player The player to respawn.
     */
    public static void respawn(PlayerEntity player) {
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
     * Initializes the Realm of Light logic.
     */
    public static void initialize() {
        ServerWorldEvents.UNLOAD.register(RealmOfLightLogic::onUnload);
        BlockBreakEvent.EVENT.register(RealmOfLightLogic::onBlockBreak);
        UseBlockCallback.EVENT.register(RealmOfLightLogic::onBlockPlace);
        PlayerMoveEvent.EVENT.register(RealmOfLightLogic::onPlayerMove);
        EntityPreDeathEvent.EVENT.register(RealmOfLightLogic::onPreDeath);
        PlayerCraftEvent.EVENT.register(RealmOfLightLogic::onCraft);
        ServerPlayConnectionEvents.DISCONNECT.register(RealmOfLightLogic::onDisconnect);
    }
}
