package moe.seikimo.mwhrd.custom;

import eu.pb4.polymer.core.api.other.PolymerStat;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.util.Identifier;

public interface CustomStats {
    Identifier INTERACT_WITH_ADVANCED_BEACON = CustomStats.register("interact_with_advanced_beacon", StatFormatter.DEFAULT);

    /**
     * Creates and registers a custom statistic.
     *
     * @param name The name of the statistic, used as the registry key.
     * @param formatter The formatter for the statistic, defining how it is displayed.
     * @return The identifier for the registered statistic.
     */
    private static Identifier register(String name, StatFormatter formatter) {
        return PolymerStat.registerStat(Identifier.of(MyWellHasRunDry.MOD_ID, name), formatter);
    }

    /**
     * No-op method to ensure the interface is loaded and the statistics are registered.
     */
    static void register() {}
}
