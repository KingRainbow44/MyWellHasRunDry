package moe.seikimo.mwhrd.custom.items.tools.sword;

import eu.pb4.polymer.core.api.item.PolymerItem;
import moe.seikimo.mwhrd.custom.interfaces.EnlightenedItem;
import moe.seikimo.mwhrd.utils.Attributes;
import moe.seikimo.mwhrd.utils.items.NbtBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class EnlightenedDiamondSword
    extends SwordItem
    implements PolymerItem, EnlightenedItem {
    public EnlightenedDiamondSword() {
        super(ToolMaterials.DIAMOND, new Settings()
            .maxCount(1).maxDamage(100)
            .attributeModifiers(AttributeModifiersComponent.builder()
                .add(
                    EntityAttributes.GENERIC_ATTACK_SPEED,
                    Attributes.add(BASE_ATTACK_SPEED_MODIFIER_ID, -2.4f),
                    AttributeModifierSlot.MAINHAND
                )
                .build())
            .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                .set("upgrade_tier", 0)
                .build())
            .component(DataComponentTypes.RARITY, Rarity.RARE));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.IRON_SWORD;
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);
        this.upgrade(stack, player);
    }

    @Override
    public void applyUpgrades(ItemStack stack, int tier, Identifier attributeId) {
        var components = AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                Attributes.add(attributeId, Math.min(9, tier * 1.5)),
                AttributeModifierSlot.MAINHAND
            );

        // For all tiers above 5, the player can gain 0.1 attack range.
        if (tier >= 5) {
            components.add(
                EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                Attributes.add(attributeId, Math.min(0.5, (tier - 4) * 0.1)),
                AttributeModifierSlot.MAINHAND
            );
        }

        // For all tiers above 8, the player can gain 0.1 attack speed.
        if (tier >= 8) {
            components.add(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                Attributes.add(attributeId, Math.min(0.5, (tier - 7) * 0.1)),
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
