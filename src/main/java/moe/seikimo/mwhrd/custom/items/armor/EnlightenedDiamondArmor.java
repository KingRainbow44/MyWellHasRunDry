package moe.seikimo.mwhrd.custom.items.armor;

import eu.pb4.polymer.core.api.item.PolymerItem;
import moe.seikimo.mwhrd.custom.interfaces.EnlightenedItem;
import moe.seikimo.mwhrd.utils.Attributes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class EnlightenedDiamondArmor
    extends ArmorItem
    implements PolymerItem, EnlightenedItem {
    private static final NbtCompound DEFAULT_NBT = new NbtCompound();

    static {
        DEFAULT_NBT.putInt(EnlightenedItem.UPGRADE_TIER, 0);
    }

    private final Item baseItem;

    public EnlightenedDiamondArmor(ArmorItem baseItem) {
        super(
            ArmorMaterials.DIAMOND,
            baseItem.getType(),
            new Settings()
                .maxCount(1).maxDamage(100)
                // Custom armor data component.
                .attributeModifiers(AttributeModifiersComponent.builder()
                    .add(
                        EntityAttributes.GENERIC_ARMOR,
                        Attributes.add(Identifier.ofVanilla("armor." + baseItem.getType().getName()), 0),
                        AttributeModifierSlot.forEquipmentSlot(baseItem.getSlotType())
                    )
                    .build())
                .component(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(DEFAULT_NBT))
        );

        this.baseItem = baseItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.baseItem;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return 0x9bb3e8;
    }

    @Override
    public int getProtection() {
        return 0;
    }

    @Override
    public float getToughness() {
        return 0;
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
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.GENERIC_ARMOR,
                Attributes.add(attributeId, tier),
                AttributeModifierSlot.forEquipmentSlot(this.getSlotType())
            )
            .add(
                EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                Attributes.add(attributeId, Math.round(4f * (tier / 10f))),
                AttributeModifierSlot.forEquipmentSlot(this.getSlotType())
            )
            .build());
    }
}
