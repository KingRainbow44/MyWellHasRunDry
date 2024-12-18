package moe.seikimo.mwhrd.custom.items.tools.pickaxe;

import eu.pb4.polymer.core.api.item.PolymerItem;
import moe.seikimo.mwhrd.custom.interfaces.EnlightenedItem;
import moe.seikimo.mwhrd.custom.items.CustomToolMaterials;
import moe.seikimo.mwhrd.utils.Attributes;
import moe.seikimo.mwhrd.utils.items.NbtBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public final class EnlightenedDiamondPickaxe extends PickaxeItem implements PolymerItem, EnlightenedItem {
    public EnlightenedDiamondPickaxe(Settings settings) {
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
        return Items.IRON_PICKAXE;
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
    public void applyUpgrades(ItemStack stack, int tier, Identifier attributeId) {
        var components = AttributeModifiersComponent
            .builder()
            .add(
                EntityAttributes.ATTACK_DAMAGE,
                Attributes.add(BASE_ATTACK_DAMAGE_MODIFIER_ID, Math.min(9, tier * 1.5)),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.ATTACK_SPEED,
                Attributes.add(BASE_ATTACK_SPEED_MODIFIER_ID, Math.min(9, tier * 0.1)),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.MINING_EFFICIENCY,
                Attributes.add(attributeId, Math.min(9, tier * 0.1)),
                AttributeModifierSlot.MAINHAND
            );

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, components.build());
    }
}
