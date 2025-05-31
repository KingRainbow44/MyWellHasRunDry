package moe.seikimo.mwhrd.managers;

import moe.seikimo.mwhrd.enums.ArmorEnum;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class BuffManager {
    private static final Set<Item> ITEM_WHITELIST = Set.of(
        Items.WATER_BUCKET, Items.BUCKET,
        Items.TNT, Items.FLINT_AND_STEEL
    );
    private static final Set<Block> BLOCK_WHITELIST = Set.of(
        Blocks.WATER, Blocks.TNT, Blocks.FIRE,
        Blocks.COBWEB, Blocks.DECORATED_POT,
        Blocks.WAXED_OXIDIZED_COPPER_GRATE
    );

    private static final Identifier BEDROCK_BUFF = Identifier.of("mwhrd", "bedrock_buff");
    private static final Identifier DEBUFF = Identifier.of("mwhrd", "debuff");

    private static final Identifier LUCK_BUFF = Identifier.of("mwhrd", "luck_buff");

    /**
     * Apply debuffs to the player.
     *
     * @param player The player to apply debuffs to.
     */
    public static void applyDebuffs(ServerPlayerEntity player) {
        var maxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealth == null) return;

        // Check if the player is hardcore.
        var condPlayer = (IPlayerConditions) player;
        if (condPlayer.mwhrd$isHardcore()) {
            // Remove all debuffs.
            if (maxHealth.removeModifier(DEBUFF)) {
                BuffManager.healthModification(player, false);
            }
            return;
        }

        // Check if the player is nearby others.
        var nearbyPlayers = Utils.getNearbyPlayers(player, 15);
        if (nearbyPlayers.size() <= 1) {
            // Remove all debuffs.
            if (maxHealth.removeModifier(DEBUFF)) {
                BuffManager.healthModification(player, false);
            }
            return;
        }

        // Check the nearby players.
        Identifier debuffId = null;
        float debuffValue = 0f;

        var playerArmor = ArmorEnum.identify(Utils.iterate(player.equipment));
        if (playerArmor == ArmorEnum.NONE) {
            // Remove all debuffs.
            if (maxHealth.removeModifier(DEBUFF)) {
                BuffManager.healthModification(player, false);
            }
            return;
        }

        for (var nearby : nearbyPlayers) {
            if (nearby.getUuid().equals(player.getUuid())) continue;

            var nearbyArmor = ArmorEnum.identify(Utils.iterate(nearby.equipment));
            if (nearbyArmor == ArmorEnum.NONE) continue;

            debuffId = DEBUFF;
            debuffValue = -2.0f;
        }

        if (debuffId == null) {
            if (maxHealth.removeModifier(DEBUFF)) {
                BuffManager.healthModification(player, false);
            }
            return;
        }

        // Apply the debuff.
        if (maxHealth.hasModifier(debuffId)) {
            return;
        }
        maxHealth.removeModifier(DEBUFF);

        BuffManager.healthModification(player, true);
        maxHealth.addTemporaryModifier(new EntityAttributeModifier(
            debuffId, debuffValue, EntityAttributeModifier.Operation.ADD_VALUE
        ));
    }

    /**
     * Logic for preventing annoying health modification changes.
     *
     * @param player The player to modify health for.
     * @param add Whether to add or remove the health modification.
     */
    private static void healthModification(PlayerEntity player, boolean add) {
        var min = player.getMaxHealth() - 2f;

        if (add) {
            // The player's health should be modified to -2f if they are at full.
            // Otherwise, they can keep the same health.
            player.setHealth(Math.min(player.getHealth(), min));
        } else {
            // The player's health should be updated if they are at 18f or more.
            if (player.getHealth() >= (min - 0.5f)) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    /**
     * Applies buffs to the player.
     *
     * @param player The player to apply buffs to.
     */
    public static void applyBuffs(ServerPlayerEntity player) {
        var condPlayer = (IPlayerConditions) player;
        if (condPlayer.mwhrd$finishedHardcore()) {
            var luck = player.getAttributeInstance(EntityAttributes.LUCK);
            if (luck != null) {
                luck.addTemporaryModifier(new EntityAttributeModifier(
                    LUCK_BUFF, 5.0f, EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

    /**
     * Applies buffs to the Bedrock player.
     *
     * @param player The Bedrock player to apply buffs to.
     */
    public static void applyBedrockBuff(ServerPlayerEntity player) {
        var attackSpeed = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
        if (attackSpeed == null) return;

        attackSpeed.addTemporaryModifier(new EntityAttributeModifier(
            BEDROCK_BUFF, 20f, EntityAttributeModifier.Operation.ADD_VALUE
        ));
    }

    /**
     * Checks if the player is allowed to place the block.
     */
    public static ActionResult blockPlaceCheck(
        PlayerEntity player, World world,
        Hand hand, BlockHitResult hitResult
    ) {
        var condPlayer = (IPlayerConditions) player;
        if (!condPlayer.mwhrd$isInTrialChamber())
            return ActionResult.PASS;

        // Check if the block we are looking at is a block entity.
        var block = world.getBlockState(hitResult.getBlockPos()).getBlock();
        if (block instanceof DoorBlock ||
            block instanceof BulbBlock ||
            block instanceof TrapdoorBlock ||
            block instanceof ButtonBlock) {
            return ActionResult.PASS;
        }

        var item = player.getStackInHand(hand);
        var blockEntity = world.getBlockEntity(hitResult.getBlockPos());
        if (blockEntity != null && item.getItem() != Items.HOPPER) {
            return ActionResult.PASS;
        }

        // Check if the item originates from outside of Minecraft.
        var identifier = Registries.ITEM.getId(item.getItem());
        if (!identifier.getNamespace().equals("minecraft")) {
            return ActionResult.PASS;
        }

        return ITEM_WHITELIST.contains(item.getItem()) ?
            ActionResult.PASS : ActionResult.FAIL;
    }

    /**
     * Checks if the player is allowed to break the block.
     */
    public static boolean blockBreakCheck(
        World world, PlayerEntity player, BlockPos pos,
        BlockState state, @Nullable BlockEntity blockEntity
    ) {
        var condPlayer = (IPlayerConditions) player;
        if (!condPlayer.mwhrd$isInTrialChamber())
            return true;

        return BLOCK_WHITELIST.contains(state.getBlock());
    }
}
