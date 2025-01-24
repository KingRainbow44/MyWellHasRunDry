package moe.seikimo.mwhrd.custom;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.game.botw.BreathOfTheWild;
import moe.seikimo.mwhrd.game.lightrealm.RealmOfLightLogic;
import moe.seikimo.mwhrd.game.lightrealm.TheRealmOfLight;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
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

    RegistryKey<DimensionType> OVERWORLD_EXPANSE_TYPE = RegistryKey.of(
        RegistryKeys.DIMENSION_TYPE, Identifier.of("mwhrd", "overworld_expanse")
    );

    RuntimeWorldConfig REALM_OF_LIGHT = new RuntimeWorldConfig()
        .setDimensionType(TOWER_TYPE)
        .setDifficulty(Difficulty.NORMAL)
        .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
        .setGenerator(VOID_GENERATOR)
        .setWorldConstructor(TheRealmOfLight::new);

    RuntimeWorldConfig OVERWORLD_EXPANSE = new RuntimeWorldConfig()
        .setDimensionType(OVERWORLD_EXPANSE_TYPE)
        .setDifficulty(Difficulty.HARD)
        .setGameRule(GameRules.RANDOM_TICK_SPEED, 0)
        .setGameRule(GameRules.DO_FIRE_TICK, false)
        .setGameRule(GameRules.DO_MOB_GRIEFING, false)
        .setGameRule(GameRules.DO_MOB_SPAWNING, false)
        .setGenerator(VOID_GENERATOR)
        .setWorldConstructor(BreathOfTheWild::new);

    /**
     * No-op method to register the custom worlds.
     */
    static void register() {
        RealmOfLightLogic.initialize();
    }
}
