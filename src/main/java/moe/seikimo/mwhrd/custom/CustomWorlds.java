package moe.seikimo.mwhrd.custom;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.game.lightrealm.RealmOfLightLogic;
import moe.seikimo.mwhrd.game.lightrealm.TheRealmOfLight;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public interface CustomWorlds {
    ChunkGenerator VOID_GENERATOR = new VoidChunkGenerator(
        MyWellHasRunDry.getRegistry()
            .getOrThrow(RegistryKeys.BIOME)
            .getEntry(0)
            .orElseThrow()
    );

    RegistryKey<DimensionType> TOWER_TYPE = RegistryKey.of(
        RegistryKeys.DIMENSION_TYPE, Identifier.of("mwhrd", "tower")
    );

    RuntimeWorldConfig REALM_OF_LIGHT = new RuntimeWorldConfig()
        .setDimensionType(TOWER_TYPE)
        .setDifficulty(Difficulty.NORMAL)
        .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
        .setGenerator(VOID_GENERATOR)
        .setWorldConstructor(TheRealmOfLight::new);

    /**
     * No-op method to register the custom worlds.
     */
    static void register() {
        RealmOfLightLogic.initialize();
    }
}
