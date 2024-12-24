package moe.seikimo.mwhrd.custom.items.tools.sword;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.custom.interfaces.EnlightenedItem;
import moe.seikimo.mwhrd.utils.Attributes;
import moe.seikimo.mwhrd.utils.items.NbtBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public final class EnlightenedDiamondSword extends SimplePolymerItem implements EnlightenedItem {
    public EnlightenedDiamondSword(Settings settings) {
        super(
            settings
                .maxCount(1).maxDamage(100)
                .attributeModifiers(AttributeModifiersComponent.builder()
                    .add(
                        EntityAttributes.ATTACK_SPEED,
                        Attributes.add(BASE_ATTACK_DAMAGE_MODIFIER_ID, 1.5f),
                        AttributeModifierSlot.MAINHAND
                    )
                    .add(
                        EntityAttributes.ATTACK_SPEED,
                        Attributes.add(BASE_ATTACK_SPEED_MODIFIER_ID, -2.4f),
                        AttributeModifierSlot.MAINHAND
                    )
                    .build())
                .component(
                    DataComponentTypes.TOOL,
                    new ToolComponent(
                        List.of(
                            ToolComponent.Rule.ofAlwaysDropping(RegistryEntryList.of(Blocks.COBWEB.getRegistryEntry()), 15.0F),
                            ToolComponent.Rule.of(Registries.createEntryLookup(Registries.BLOCK)
                                .getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
                        ),
                        1.0F, 2
                    )
                )
                .component(DataComponentTypes.CUSTOM_DATA, NbtBuilder.of()
                    .set("upgrade_tier", 0)
                    .build())
                .component(DataComponentTypes.RARITY, Rarity.RARE),
            Items.IRON_SWORD,
            false
        );
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.IRON_SWORD;
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);
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

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ITEM.getId(this);
    }
}
