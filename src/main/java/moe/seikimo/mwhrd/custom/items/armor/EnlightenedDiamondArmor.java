package moe.seikimo.mwhrd.custom.items.armor;

import eu.pb4.polymer.core.api.item.PolymerItem;
import moe.seikimo.mwhrd.utils.Attributes;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EnlightenedDiamondArmor extends ArmorItem implements PolymerItem {
    private static final String UPGRADE_TIER = "upgrade_tier";
    private static final NbtCompound DEFAULT_NBT = new NbtCompound();

    static {
        DEFAULT_NBT.putInt(UPGRADE_TIER, 0);
    }

    private final Item baseItem;

    public EnlightenedDiamondArmor(ArmorItem baseItem) {
        super(
            ArmorMaterials.DIAMOND,
            baseItem.getType(),
            new Settings()
                .maxCount(1)
                .maxDamage(100)
                // Custom armor data component.
                .component(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(DEFAULT_NBT))
                .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
                    .add(EntityAttributes.GENERIC_ARMOR, Attributes.ZERO, AttributeModifierSlot.forEquipmentSlot(baseItem.getSlotType()))
                    .build())
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
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);

        // Get the current component for upgrade tier.
        var component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        var nbt = component.copyNbt();

        var upgradeTier = nbt.contains(UPGRADE_TIER) ? nbt.getInt(UPGRADE_TIER) : 0;
        // Check if the item is already at max tier.
        if (upgradeTier >= 10) {
            player.sendMessage(Text.translatable("item.mwhrd.max_tier")
                .formatted(Formatting.RED));

            // Return the upgrade items.
            player.getInventory().offerOrDrop(new ItemStack(Items.NETHERITE_INGOT));
            player.getInventory().offerOrDrop(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));

            return;
        }

        upgradeTier++; // Upgrade the tier.

        // Replace the component with the new upgrade tier.
        nbt.putInt(UPGRADE_TIER, upgradeTier);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt);

        // Apply stack upgrades.
        var modifier = Identifier.of("mwhrd", Registries.ITEM.getId(this).getPath() + "_modifier");

        stack.set(DataComponentTypes.DAMAGE, 0);
        stack.set(DataComponentTypes.MAX_DAMAGE, 100 * upgradeTier);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.GENERIC_ARMOR,
                Attributes.add(modifier, upgradeTier),
                AttributeModifierSlot.forEquipmentSlot(this.getSlotType())
            )
            .add(
                EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                Attributes.add(modifier, Math.round(4f * (upgradeTier / 10f))),
                AttributeModifierSlot.forEquipmentSlot(this.getSlotType())
            )
            .build());
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
            Text.literal("Tier %s".formatted(Utils.toRoman(upgradeTier)))
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.GRAY)
        )));

        player.sendMessage(Text.translatable("item.mwhrd.tier_upgrade",
            stack.toHoverableText().copy()
                .formatted(Formatting.YELLOW),
            Text.literal(Utils.toRoman(upgradeTier)))
            .formatted(Formatting.GREEN));
    }
}
