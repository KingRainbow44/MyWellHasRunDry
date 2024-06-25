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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class DebuffManager {
    private static final Set<Item> ITEM_WHITELIST = Set.of(
        Items.WATER_BUCKET, Items.BUCKET,
        Items.TNT, Items.FLINT_AND_STEEL
    );
    private static final Set<Block> BLOCK_WHITELIST = Set.of(
        Blocks.WATER, Blocks.TNT, Blocks.FIRE,
        Blocks.LAVA, Blocks.COBWEB, Blocks.DECORATED_POT,
        Blocks.WAXED_OXIDIZED_COPPER_GRATE
    );

    private static final Identifier BEDROCK_BUFF = Identifier.of("mwhrd", "bedrock_buff");
    private static final Identifier DEBUFF = Identifier.of("mwhrd", "debuff");

    /**
     * Apply debuffs to the player.
     *
     * @param player The player to apply debuffs to.
     */
    public static void applyDebuffs(ServerPlayerEntity player) {
        var maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth == null) return;

        // Check if the player is hardcore.
        var condPlayer = (IPlayerConditions) player;
        if (condPlayer.mwhrd$isHardcore()) {
            maxHealth.removeModifier(DEBUFF);
            return;
        }

        // Check if the player is nearby others.
        var nearbyPlayers = Utils.getNearbyPlayers(player, 15);
        if (nearbyPlayers.size() <= 1) {
            // Remove all debuffs.
            maxHealth.removeModifier(DEBUFF);
            return;
        }

        // Check the nearby players.
        Identifier debuffId = null;
        float debuffValue = 0f;

        var playerArmor = ArmorEnum.identify(player.getArmorItems());
        if (playerArmor == ArmorEnum.NONE) {
            // Remove all debuffs.
            maxHealth.removeModifier(DEBUFF);
            return;
        }

        for (var nearby : nearbyPlayers) {
            if (nearby.equals(player)) continue;

            var nearbyArmor = ArmorEnum.identify(nearby.getArmorItems());
            if (nearbyArmor == ArmorEnum.NONE) continue;

            debuffId = DEBUFF;
            debuffValue = -2.0f;
        }

        if (debuffId == null) {
            maxHealth.removeModifier(DEBUFF);
            return;
        }

        // Apply the debuff.
        if (maxHealth.hasModifier(debuffId)) {
            return;
        }
        maxHealth.removeModifier(DEBUFF);

        maxHealth.addTemporaryModifier(new EntityAttributeModifier(
            debuffId, debuffValue, EntityAttributeModifier.Operation.ADD_VALUE
        ));
    }

    /**
     * Applies buffs to the Bedrock player.
     *
     * @param player The Bedrock player to apply buffs to.
     */
    public static void applyBedrockBuff(ServerPlayerEntity player) {
        var attackSpeed = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
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
