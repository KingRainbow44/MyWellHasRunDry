package moe.seikimo.mwhrd.utils.items;

import lombok.RequiredArgsConstructor;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;

import java.util.ArrayList;

@RequiredArgsConstructor(staticName = "of")
public final class ItemBuilder {
    /**
     * Creates a new item builder.
     *
     * @param item The item to build.
     * @return The builder for chaining.
     */
    public static ItemBuilder of(Item item) {
        return new ItemBuilder(new ItemStack(item));
    }

    private final ItemStack stack;

    /**
     * Sets the name of the stack.
     *
     * @param text The name to set.
     * @return The builder for chaining.
     */
    public ItemBuilder name(Text text) {
        this.stack.set(DataComponentTypes.CUSTOM_NAME, Utils.clearFormatting(text));
        return this;
    }

    /**
     * Sets the lore of the stack.
     *
     * @param text The lore to set.
     * @return The builder for chaining.
     */
    public ItemBuilder lore(Text... text) {
        var lore = new ArrayList<Text>();

        // Clear the italics formatting from the lore.
        for (var line : text) {
            lore.add(Utils.clearFormatting(line));
        }

        this.stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return this;
    }

    /**
     * Enchants the stack with the given enchantment at the given level.
     *
     * @param enchantment The enchantment to apply.
     * @param level The level of the enchantment.
     * @return The builder for chaining.
     */
    public ItemBuilder enchant(RegistryKey<Enchantment> enchantment, int level) {
        Utils.enchant(this.stack, enchantment, level);
        return this;
    }

    /**
     * Sets the quantity of the stack.
     *
     * @param quantity The quantity to set.
     * @return The builder for chaining.
     */
    public ItemBuilder quantity(int quantity) {
        this.stack.setCount(quantity);
        return this;
    }

    /**
     * Marks the stack as unbreakable.
     *
     * @return The builder for chaining.
     */
    public ItemBuilder unbreakable() {
        this.stack.set(
            DataComponentTypes.UNBREAKABLE,
            new UnbreakableComponent(false));
        return this;
    }

    /**
     * @return The finished item stack.
     */
    public ItemStack build() {
        return this.stack;
    }

    /**
     * Equips the stack to the given mob in the given slot.
     *
     * @param mob  The mob to equip the stack to.
     * @param slot The slot to equip the stack to.
     */
    public void equip(MobEntity mob, EquipmentSlot slot) {
        mob.equipStack(slot, this.stack);
    }
}
