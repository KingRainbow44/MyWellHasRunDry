package moe.seikimo.mwhrd.utils;

import moe.seikimo.general.MapBuilder;
import moe.seikimo.mwhrd.game.beacon.BeaconEffect;
import moe.seikimo.mwhrd.game.beacon.BeaconPower;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

/**
 * Function collector for hash maps.
 */
public interface Maps {
    /**
     * @return A map of guild icons.
     */
    static MapBuilder<Formatting, Pair<String, String>> guildIcons() {
        return MapBuilder.create();
    }

    /**
     * @return A map of guild indexes.
     */
    static MapBuilder<Formatting, Integer> guildIndexes() {
        return MapBuilder.create();
    }

    /**
     * @return A map of guild ranges.
     */
    static MapBuilder<Formatting, Pair<Integer, Integer>> guildRanges() {
        return MapBuilder.create();
    }

    /**
     * @return A map of guild names.
     */
    static MapBuilder<Formatting, String> guildNames() {
        return MapBuilder.create();
    }

    /**
     * @return A map of beacon powers.
     */
    static MapBuilder<Class<? extends BeaconPower>, BeaconEffect> beaconPower() {
        return MapBuilder.create();
    }

    /**
     * @return A map of boss bar colors.
     */
    static MapBuilder<Formatting, BossBar.Color> bossBarColors() {
        return MapBuilder.create();
    }
}
