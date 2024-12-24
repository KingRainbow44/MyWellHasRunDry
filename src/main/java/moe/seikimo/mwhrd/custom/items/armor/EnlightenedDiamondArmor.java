package moe.seikimo.mwhrd.custom.items.armor;

import eu.pb4.polymer.core.api.item.PolymerItem;
import moe.seikimo.mwhrd.custom.interfaces.EnlightenedItem;
import moe.seikimo.mwhrd.utils.Attributes;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;

public final class EnlightenedDiamondArmor
    extends ArmorItem
    implements PolymerItem, EnlightenedItem {
    private static final NbtCompound DEFAULT_NBT = new NbtCompound();

    static {
        DEFAULT_NBT.putInt(EnlightenedItem.UPGRADE_TIER, 0);
    }

    private final Item baseItem;

    public EnlightenedDiamondArmor(Item baseItem, EquipmentType type, Settings settings) {
        super(
            ArmorMaterials.DIAMOND, type,
            settings
                // Set item data.
                .maxCount(1)
                .maxDamage(100)
                // Custom armor data component.
                .attributeModifiers(AttributeModifiersComponent.builder()
                    .add(
                        EntityAttributes.ARMOR,
                        Attributes.add(Identifier.ofVanilla("armor." + type.getName()), 0),
                        AttributeModifierSlot.forEquipmentSlot(type.getEquipmentSlot())
                    )
                    .build())
                .component(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(DEFAULT_NBT))
        );

        this.baseItem = baseItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.baseItem;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
        var stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);

        // Set armor color.
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x9bb3e8, false));

        return stack;
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ITEM.getId(this);
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);
        this.upgrade(stack, player);
    }

    @Override
    public void applyUpgrades(ItemStack stack, int tier, Identifier attributeId) {
        var slotType = Objects.requireNonNull(this.getComponents()
                .get(DataComponentTypes.EQUIPPABLE),
                "Armor item must have an equippable component")
            .slot();

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.ARMOR,
                Attributes.add(attributeId, tier),
                AttributeModifierSlot.forEquipmentSlot(slotType)
            )
            .add(
                EntityAttributes.ARMOR_TOUGHNESS,
                Attributes.add(attributeId, Math.round(4f * (tier / 10f))),
                AttributeModifierSlot.forEquipmentSlot(slotType)
            )
            .build());
    }
}
