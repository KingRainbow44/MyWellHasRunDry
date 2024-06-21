package moe.seikimo.mwhrd.beacon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

@Getter
@RequiredArgsConstructor
public enum BeaconLevel {
    TIER_0(0, 40, 16, Items.IRON_INGOT),
    TIER_1(1, 40, 16, Items.IRON_BLOCK),
    TIER_2(2, 60, 14, Items.GOLD_BLOCK),
    TIER_3(3, 80, 10, Items.DIAMOND_BLOCK),
    TIER_4(4, 100, 8, Items.NETHERITE_BLOCK),
    ;

    final int level;
    final int range;
    final int fuelCost; // This is fuel per hour.
    final Item item;
}
