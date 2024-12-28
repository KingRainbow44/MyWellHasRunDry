package moe.seikimo.mwhrd.custom.items.tools.shovel;

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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public final class EnlightenedDiamondShovel extends ShovelItem implements PolymerItem, EnlightenedItem {
    public EnlightenedDiamondShovel(Settings settings) {
        super(
            CustomToolMaterials.ENLIGHTENED,
            0, -3f,
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
        return Items.IRON_SHOVEL;
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
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);
        this.upgrade(stack, player);
    }

    @Override
    public void applyUpgrades(ItemStack stack, int tier, Identifier attributeId) {
        var components = AttributeModifiersComponent
            .builder()
            .add(
                EntityAttributes.ATTACK_DAMAGE,
                Attributes.add(BASE_ATTACK_DAMAGE_MODIFIER_ID, Math.min(7.5, tier * 1.5)),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.ATTACK_SPEED,
                Attributes.add(BASE_ATTACK_SPEED_MODIFIER_ID, -3f + tier * 0.1),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.BLOCK_INTERACTION_RANGE,
                Attributes.add(attributeId, tier * 0.2),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.ENTITY_INTERACTION_RANGE,
                Attributes.add(attributeId, tier * 0.17),
                AttributeModifierSlot.OFFHAND
            );

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, components.build());
    }
}
