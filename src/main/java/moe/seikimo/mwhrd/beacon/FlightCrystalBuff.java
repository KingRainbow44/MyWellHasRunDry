package moe.seikimo.mwhrd.beacon;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public interface FlightCrystalBuff {
    static void apply(World world, int beaconLevel, ServerPlayerEntity player) {
        player.getAbilities().allowFlying = true;
        player.sendAbilitiesUpdate();
    }

    static void remove(World world, ServerPlayerEntity player) {
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;
        player.sendAbilitiesUpdate();
    }
}
