package moe.seikimo.mwhrd.utils;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;

public final class MobGear {
    public static ItemStack HELMET, CHESTPLATE, LEGGINGS, BOOTS, SWORD, BOW, ARROWS;

    static {
        HELMET = new ItemStack(Items.DIAMOND_HELMET);
        CHESTPLATE = new ItemStack(Items.DIAMOND_CHESTPLATE);
        LEGGINGS = new ItemStack(Items.DIAMOND_LEGGINGS);
        BOOTS = new ItemStack(Items.DIAMOND_BOOTS);
        enchant(BOOTS, Enchantments.DEPTH_STRIDER, 4);

        SWORD = new ItemStack(Items.NETHERITE_SWORD);
        enchant(SWORD, Enchantments.SHARPNESS, 4);
        enchant(SWORD, Enchantments.KNOCKBACK, 1);

        BOW = new ItemStack(Items.BOW);
        enchant(BOW, Enchantments.POWER, 8);
        enchant(BOW, Enchantments.PUNCH, 1);

        ARROWS = new ItemStack(Items.TIPPED_ARROW);
        ARROWS.set(DataComponentTypes.POTION_CONTENTS,
            new PotionContentsComponent(Potions.STRONG_HARMING));

        applyArmor(HELMET);
        applyArmor(CHESTPLATE);
        applyArmor(LEGGINGS);
        applyArmor(BOOTS);
    }

    /**
     * Applies armor enchantments to the given stack.
     *
     * @param stack The stack to apply the enchantments to.
     */
    private static void applyArmor(ItemStack stack) {
        enchant(stack, Enchantments.PROTECTION, 3);
        enchant(stack, Enchantments.FIRE_PROTECTION, 1);
        enchant(stack, Enchantments.PROJECTILE_PROTECTION, 1);
        enchant(stack, Enchantments.BLAST_PROTECTION, 1);
    }

    /**
     * Enchants the given stack with the given enchantment at the given level.
     *
     * @param stack The stack to enchant.
     * @param enchantment The enchantment to apply.
     * @param level The level of the enchantment.
     */
    private static void enchant(ItemStack stack, RegistryKey<Enchantment> enchantment, int level) {
        var key = MyWellHasRunDry.getEnchantmentRegistry().entryOf(enchantment);
        stack.addEnchantment(key, level);
    }
}
