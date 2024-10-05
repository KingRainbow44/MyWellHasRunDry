package moe.seikimo.mwhrd.custom;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface CustomWorlds {
    RegistryKey<World> REALM_OF_LIGHT = RegistryKey.of(
        RegistryKeys.WORLD, Identifier.of("mwhrd", "realm_of_light")
    );

    RegistryKey<World> RUINS = RegistryKey.of(
        RegistryKeys.WORLD, Identifier.of("mwhrd", "ruins")
    );
}
