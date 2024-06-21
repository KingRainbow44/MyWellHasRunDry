package moe.seikimo.mwhrd.beacon;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public interface BeaconCallback {
    BeaconCallback.Apply APPLY_EMPTY = (world, level, player) -> {};
    BeaconCallback.Remove REMOVE_EMPTY = (world, player) -> {};

    interface Apply {
        void applyEffects(World world, int beaconLevel, ServerPlayerEntity player);
    }

    interface Remove {
        void removeEffects(World world, ServerPlayerEntity player);
    }
}
