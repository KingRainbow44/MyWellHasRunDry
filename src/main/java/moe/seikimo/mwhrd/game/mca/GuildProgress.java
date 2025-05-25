package moe.seikimo.mwhrd.game.mca;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

@Getter
@RequiredArgsConstructor
public enum GuildProgress {
    NEEDS_BASE(Items.BEACON),
    NEEDS_CITIZENS(Items.EMERALD),
    NEEDS_JOBS(Items.SMITHING_TABLE),
    NEEDS_LOCATIONS(Items.ENDER_EYE),
    NEEDS_HALL(Items.JUNGLE_DOOR),
    COMPLETED(Items.AIR);

    final Item icon;
}
