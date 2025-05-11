package moe.seikimo.mwhrd.custom.items.tools.axe;

import eu.pb4.polymer.core.api.item.PolymerItem;
import moe.seikimo.mwhrd.custom.interfaces.EnlightenedItem;
import moe.seikimo.mwhrd.custom.items.CustomToolMaterials;
import moe.seikimo.mwhrd.utils.Attributes;
import moe.seikimo.mwhrd.utils.items.NbtBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public final class EnlightenedDiamondAxe extends AxeItem implements PolymerItem, EnlightenedItem {
    public EnlightenedDiamondAxe(Settings settings) {
        super(
            CustomToolMaterials.ENLIGHTENED,
            2f, -3f,
            settings
                .maxCount(1)
                .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                    .set("upgrade_tier", 0)
                    .build())
                .component(DataComponentTypes.RARITY, Rarity.RARE)
        );
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.IRON_AXE;
    }

    @Override
    @Nullable
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ITEM.getId(this);
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, PlayerEntity player) {
        super.onCraftByPlayer(stack, player);
        this.upgrade(stack, player);
    }

    @Override
    public void applyUpgrades(ItemStack stack, int tier, Identifier attributeId) {
        var attackSpeed = tier < 8 ? 0 : (tier - 7) * 0.1;

        var components = AttributeModifiersComponent
            .builder()
            .add(
                EntityAttributes.ATTACK_DAMAGE,
                Attributes.add(BASE_ATTACK_DAMAGE_MODIFIER_ID, Math.min(10, tier * 2)),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.ATTACK_SPEED,
                Attributes.add(BASE_ATTACK_SPEED_MODIFIER_ID, -3f + attackSpeed),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.MINING_EFFICIENCY,
                Attributes.add(attributeId, Math.min(9, tier * 0.1)),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.BLOCK_INTERACTION_RANGE,
                Attributes.add(attributeId, tier * 0.2),
                AttributeModifierSlot.OFFHAND
            );

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, components.build());
    }
}
