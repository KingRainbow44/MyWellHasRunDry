package moe.seikimo.mwhrd;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public interface TrialChamberLoot {
    AtomicReference<Set<ItemDespawn>> ITEMS = new AtomicReference<>();

    /**
     * Adds loot to a player.
     *
     * @param player The player to add loot to.
     * @param item The loot to add.
     */
    static void addLoot(UUID player, ItemStack item) {
        MyWellHasRunDry.getLoot(player).add(item);

        // Try and look up the player.
        var playerInstance = MyWellHasRunDry.getServer()
            .getPlayerManager()
            .getPlayer(player);
        if (playerInstance != null) {
            playerInstance.sendMessage(Text.literal("You received ")
                .withColor(Color.GREEN.getRGB())
                .append("x")
                .append(item.getCount() + " ")
                .append(item.toHoverableText()
                    .copy()
                    .withColor(Color.YELLOW.getRGB())));
        }
    }

    /**
     * Schedules an item entity for despawning.
     *
     * @param entity The entity to despawn.
     * @param in The time in milliseconds to despawn the entity.
     */
    static void scheduleForDespawn(ItemEntity entity, double in) {
        ITEMS.get().add(new ItemDespawn(entity,
            System.currentTimeMillis() + (long) in));
    }

    /**
     * Spawns an item entity in the world.
     *
     * @return The entity spawned.
     */
    static ItemEntity spawnItem(
        World world, ItemStack stack,
        int speed, Direction side, Position pos
    ) {
        double d = pos.getX(), e = pos.getY(), f = pos.getZ();
        e = side.getAxis() == Direction.Axis.Y ? e - 0.125 : e - 0.15625;

        var itemEntity = new ItemEntity(world, d, e, f, stack);
        var g = world.random.nextDouble() * 0.1 + 0.2;
        itemEntity.setVelocity(world.random.nextTriangular((double)side.getOffsetX() * g, 0.0172275 * (double)speed), world.random.nextTriangular(0.2, 0.0172275 * (double)speed), world.random.nextTriangular((double)side.getOffsetZ() * g, 0.0172275 * (double)speed));
        world.spawnEntity(itemEntity);

        return itemEntity;
    }

    final class ItemThread extends Thread {
        /**
         * Creates a new instance of the despawn thread.
         */
        public static void initialize() {
            new ItemThread().start();
        }

        private ItemThread() {
            super("Item Despawn Thread");

            ITEMS.set(Collections.synchronizedSet(new HashSet<>()));
        }

        @Override
        public void run() {
            while (true) {
                var items = ITEMS.get();
                var time = System.currentTimeMillis();

                // Despawn all items that have expired.
                for (var item : items) {
                    if (time >= item.despawnTime()) {
                        item.entity().remove(Entity.RemovalReason.DISCARDED);
                    }
                }

                // Remove all despawned items.
                items.removeIf(item -> item.despawnTime() >= time);

                try {
                    Thread.sleep(100);
                } catch (Exception ignored) {
                    return;
                }
            }
        }
    }

    record ItemDespawn(ItemEntity entity, long despawnTime) { }
}
