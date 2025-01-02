package moe.seikimo.mwhrd.custom;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;

public interface CustomStructures {
    Identifier BLOSSOM_TREE = Identifier.of("mwhrd", "blossom_tree");

    RegistryKey<Structure> LIGHT_SHACK = of("light_shack");

    /**
     * Converts an identifier into a registry key.
     *
     * @param id The identifier to convert.
     * @return The registry key.
     */
    private static RegistryKey<Structure> of(String id) {
        return RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.of("mwhrd", id));
    }
}
