package moe.seikimo.mwhrd.utils;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface Structures {
    /**
     * Pastes an NBT structure at the origin.
     *
     * @param world The world to paste the structure in.
     * @param origin The origin to paste the structure at.
     * @param structure The structure to paste.
     * @param offset The offset to paste the structure at.
     */
    static void paste(
        ServerWorld world, BlockPos origin,
        Identifier structure, BlockPos offset) {
        // Place the structure at the origin.
        var struct = world
            .getStructureTemplateManager()
            .getTemplate(structure)
            .orElse(null);

        if (struct == null) {
            throw new NullPointerException("Structure not found.");
        }

        var pos = origin.add(offset);
        struct.place(
            world, pos, pos,
            new StructurePlacementData(),
            Random.create(), 2
        );
    }
}
