package moe.seikimo.mwhrd.custom.items.tools.sword;

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

public final class EnlightenedDiamondSword extends Item implements PolymerItem, EnlightenedItem {
    public EnlightenedDiamondSword(Settings settings) {
        super(
            settings
                .sword(CustomToolMaterials.ENLIGHTENED, 1.5f, -2.4f)
                .maxCount(1)
                .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                    .set("upgrade_tier", 0)
                    .build())
                .component(DataComponentTypes.RARITY, Rarity.RARE)
        );
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.IRON_SWORD;
    }

    @Override
    @Nullable
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, PlayerEntity player) {
        super.onCraftByPlayer(stack, player);
        this.upgrade(stack, player);
    }

    @Override
    public void applyUpgrades(ItemStack stack, int tier, Identifier attributeId) {
        // For all tiers 8+, the player can gain 0.1 attack speed.
        var attackSpeed = tier >= 8 ? Math.min(0.5, (tier - 7) * 0.1) : 0;

        var components = AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.ATTACK_DAMAGE,
                Attributes.add(BASE_ATTACK_DAMAGE_MODIFIER_ID, Math.min(9, tier * 1.5)),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.ATTACK_SPEED,
                Attributes.add(BASE_ATTACK_SPEED_MODIFIER_ID, -2.4f + attackSpeed),
                AttributeModifierSlot.MAINHAND
            );

        // For all tiers 5+, the player can gain 0.1 attack range.
        if (tier >= 5) {
            components.add(
                EntityAttributes.ENTITY_INTERACTION_RANGE,
                Attributes.add(attributeId, Math.min(0.5, (tier - 4) * 0.1)),
                AttributeModifierSlot.MAINHAND
            );
        }

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, components.build());
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ITEM.getId(this);
    }
}
