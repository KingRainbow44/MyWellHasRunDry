package moe.seikimo.mwhrd.enums;

import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

@Getter
public enum ArmorEnum {
    NONE(true, 0, Items.AIR),
    DIAMOND(true, 2, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS),
    NETHERITE(false, 4, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)
    ;

    final boolean requireAll;
    final float value;
    final List<Item> armor;

    ArmorEnum(boolean requireAll, float value, Item... armor) {
        this.requireAll = requireAll;
        this.value = value;
        this.armor = List.of(armor);
    }

    /**
     * Identify the armor set from the given list of armor.
     *
     * @param armor List of armor to identify.
     * @return The identified armor set.
     */
    public static ArmorEnum identify(Iterable<ItemStack> armor) {
        var anyNetherite = false;
        var fullDiamond = true;

        for (var piece : armor) {
            if (!anyNetherite && ArmorEnum.NETHERITE.getArmor().contains(piece.getItem())) {
                anyNetherite = true;
            }
            if (fullDiamond && !ArmorEnum.DIAMOND.getArmor().contains(piece.getItem())) {
                fullDiamond = false;
            }
        }

        return anyNetherite ? ArmorEnum.NETHERITE :
            fullDiamond ? ArmorEnum.DIAMOND : ArmorEnum.NONE;
    }
}
