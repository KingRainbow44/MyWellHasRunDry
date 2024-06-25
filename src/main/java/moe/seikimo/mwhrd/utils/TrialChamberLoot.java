package moe.seikimo.mwhrd.utils;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

import java.awt.*;
import java.util.UUID;

public interface TrialChamberLoot {
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
}
