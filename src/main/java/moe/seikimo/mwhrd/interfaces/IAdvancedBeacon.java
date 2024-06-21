package moe.seikimo.mwhrd.interfaces;

import moe.seikimo.mwhrd.beacon.BeaconEffect;
import net.minecraft.component.type.NbtComponent;

import java.util.List;
import java.util.UUID;

public interface IAdvancedBeacon {
    NbtComponent mwhrd$serialize();
    void mwhrd$destroy();

    void mwhrd$setAdvanced(boolean advanced);
    boolean mwhrd$isAdvanced();

    void mwhrd$setEffects(List<BeaconEffect> effects);
    List<BeaconEffect> mwhrd$getEffects();

    List<UUID> mwhrd$getLastPlayers();

    void mwhrd$setFuel(int fuel);
    int mwhrd$getFuel();
}
