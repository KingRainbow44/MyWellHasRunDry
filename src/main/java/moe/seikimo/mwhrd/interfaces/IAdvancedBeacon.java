package moe.seikimo.mwhrd.interfaces;

import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.beacon.BeaconPower;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;

import java.util.*;

public interface IAdvancedBeacon {
    NbtCompound mwhrd$serialize();

    /**
     * Deserializes a beacon from an NBT component.
     */
    void mwhrd$deserialize(NbtCompound nbt);

    void mwhrd$destroy();

    void mwhrd$setAdvanced(boolean advanced);
    boolean mwhrd$isAdvanced();

    /**
     * @return The map of effects to powers. This is mutable.
     */
    Map<BeaconEffect, BeaconPower> mwhrd$getEffectMap();

    /**
     * @return A list of all effects present. This is immutable.
     */
    List<BeaconEffect> mwhrd$getEffectList();

    /**
     * Adds an effect to the beacon.
     * The power is created using the initializer.
     *
     * @param effect The effect to add.
     */
    void mwhrd$addEffect(BeaconEffect effect);

    List<UUID> mwhrd$getLastPlayers();

    void mwhrd$setFuel(int fuel);
    int mwhrd$getFuel();

    /**
     * @return Serializes the beacon to an NBT component.
     */
    default NbtComponent mwhrd$serializeComponent() {
        return NbtComponent.of(this.mwhrd$serialize());
    }
}
