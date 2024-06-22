package moe.seikimo.mwhrd.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class CustomItems {
    public static final ItemStack ADVANCED_BEACON;

    static {
        ADVANCED_BEACON = new ItemStack(Items.BEACON);
        ADVANCED_BEACON.set(DataComponentTypes.ITEM_NAME, Text.literal("Beacon")
            .setStyle(Style.EMPTY.withItalic(false))
            .formatted(Formatting.LIGHT_PURPLE));
        ADVANCED_BEACON.set(DataComponentTypes.LORE, new LoreComponent(List.of(
            Text.literal("This beacon emits special radiation!")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.AQUA)
        )));
        ADVANCED_BEACON.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));

        var beaconNbt = new NbtCompound();
        beaconNbt.putInt("advanced_beacon", 1);
        ADVANCED_BEACON.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(beaconNbt));
    }
}
