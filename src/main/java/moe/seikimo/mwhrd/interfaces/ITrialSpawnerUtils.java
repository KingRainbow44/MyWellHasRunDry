package moe.seikimo.mwhrd.interfaces;

import net.minecraft.block.spawner.TrialSpawnerData;

import java.util.Set;
import java.util.UUID;

public interface ITrialSpawnerUtils {
    /**
     * @return {@link TrialSpawnerData#players}
     */
    Set<UUID> mwhrd$getSpawnerPlayers();
}
