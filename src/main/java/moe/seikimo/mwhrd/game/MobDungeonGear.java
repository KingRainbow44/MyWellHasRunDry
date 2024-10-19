package moe.seikimo.mwhrd.game;

import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

public final class MobDungeonGear {
    /** The dungeon gear for monsters. */
    public final static ItemStack
        HELMET, CHESTPLATE, LEGGINGS, BOOTS, SWORD, BOW, ARROWS;

    static {
        HELMET = new ItemStack(Items.DIAMOND_HELMET);
        CHESTPLATE = new ItemStack(Items.DIAMOND_CHESTPLATE);
        LEGGINGS = new ItemStack(Items.DIAMOND_LEGGINGS);
        BOOTS = new ItemStack(Items.DIAMOND_BOOTS);
        Utils.enchant(BOOTS, Enchantments.DEPTH_STRIDER, 4);

        SWORD = new ItemStack(Items.NETHERITE_SWORD);
        Utils.enchant(SWORD, Enchantments.SHARPNESS, 4);

        BOW = new ItemStack(Items.BOW);
        Utils.enchant(BOW, Enchantments.POWER, 10);
        Utils.enchant(BOW, Enchantments.PUNCH, 1);

        ARROWS = new ItemStack(Items.TIPPED_ARROW);
        ARROWS.set(DataComponentTypes.POTION_CONTENTS,
            new PotionContentsComponent(Potions.STRONG_HARMING));

        applyArmor(HELMET);
        applyArmor(CHESTPLATE);
        applyArmor(LEGGINGS);
        applyArmor(BOOTS);
    }

    /**
     * Equips armor to the given mob.
     *
     * @param mob The mob to equip armor to.
     */
    public static void applyArmor(MobEntity mob) {
        mob.equipStack(EquipmentSlot.HEAD, MobDungeonGear.HELMET.copy());
        mob.equipStack(EquipmentSlot.CHEST, MobDungeonGear.CHESTPLATE.copy());
        mob.equipStack(EquipmentSlot.LEGS, MobDungeonGear.LEGGINGS.copy());
        mob.equipStack(EquipmentSlot.FEET, MobDungeonGear.BOOTS.copy());
    }

    /**
     * Applies armor enchantments to the given stack.
     *
     * @param stack The stack to apply the enchantments to.
     */
    private static void applyArmor(ItemStack stack) {
        Utils.enchant(stack, Enchantments.PROTECTION, 3);
        Utils.enchant(stack, Enchantments.FIRE_PROTECTION, 1);
        Utils.enchant(stack, Enchantments.PROJECTILE_PROTECTION, 1);
        Utils.enchant(stack, Enchantments.BLAST_PROTECTION, 1);
    }
}
