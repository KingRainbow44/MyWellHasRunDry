package moe.seikimo.mwhrd.utils;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.concurrent.CompletableFuture;

public interface Worlds {
    /** Sourced from 'pop4959/Chunky'. */
    ChunkTicketType<Unit> CHUNK_TICKET = ChunkTicketType.create("mwhrd", (unit, unit1) -> 0);

    /**
     * Force loads a chunk.
     *
     * @param world The world to force load the chunk in.
     * @param chunkX The X coordinate of the chunk.
     * @param chunkY The Y coordinate of the chunk.
     */
    static CompletableFuture<Void> forceLoad(ServerWorld world, int chunkX, int chunkY) {
        var chunkPos = new ChunkPos(chunkX, chunkY);

        var manager = world.getChunkManager();
        manager.addTicket(CHUNK_TICKET, chunkPos, 0, Unit.INSTANCE);

        return CompletableFuture.allOf(manager
            .getChunkFutureSyncOnMainThread(chunkX, chunkY, ChunkStatus.FULL, true));
    }

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
