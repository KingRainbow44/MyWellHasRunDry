package moe.seikimo.mwhrd.interfaces;

import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.beacon.BeaconFuel;
import moe.seikimo.mwhrd.beacon.BeaconPower;
import moe.seikimo.mwhrd.beacon.powers.EffectsPower;
import moe.seikimo.mwhrd.models.BeaconModel;
import moe.seikimo.mwhrd.utils.ItemStorage;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface IAdvancedBeacon {
    Map<BeaconEffect, BeaconPower.Initializer> DEFAULT_POWERS = new HashMap<>() {{
        this.put(BeaconEffect.EFFECTS, EffectsPower::new);
        this.put(BeaconEffect.DISABLE_SPAWNS, BeaconPower.Empty::new);
        this.put(BeaconEffect.UNBREAKING_TOOLS, BeaconPower.Empty::new);
    }};

    World mwhrd$getWorld();
    BlockPos mwhrd$getPos();

    NbtCompound mwhrd$serialize();

    /**
     * Deserializes a beacon from an NBT component.
     */
    void mwhrd$deserialize(NbtCompound nbt);

    boolean mwhrd$hasInitialized();
    void mwhrd$setInitialized(boolean initialized);

    void mwhrd$save();
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
     * @return The beacon's fuel status.
     */
    default BeaconFuel mwhrd$fuel() {
        return BeaconFuel.getFuel(this.mwhrd$getFuel());
    }

    /**
     * @return Serializes the beacon to an NBT component.
     */
    default NbtComponent mwhrd$serializeComponent() {
        return NbtComponent.of(this.mwhrd$serialize());
    }

    /**
     * @return The default powers for the beacon.
     */
    default Map<BeaconEffect, BeaconPower> mwhrd$default() {
        var existing = this.mwhrd$getEffectMap();
        var map = new HashMap<BeaconEffect, BeaconPower>();
        for (var entry : DEFAULT_POWERS.entrySet()) {
            if (existing.containsKey(entry.getKey())) continue;
            map.put(entry.getKey(), entry.getValue().create(this.mwhrd$getPos()));
        }

        return map;
    }

    /**
     * @return The players that were last seen in range of the beacon.
     */
    default List<ServerPlayerEntity> mwhrd$getPlayers() {
        var server = this.mwhrd$getWorld().getServer();
        if (server == null) return Collections.emptyList();

        return this.mwhrd$getLastPlayers().stream()
            .map(server.getPlayerManager()::getPlayer)
            .toList();
    }

    /**
     * Fetches the instance of a power from the beacon.
     *
     * @param power The class of the power to fetch.
     * @return The power instance, or null if not present.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T> T mwhrd$getPower(Class<T> power) {
        return (T) this.mwhrd$getEffectMap().values().stream()
            .filter(power::isInstance)
            .findFirst()
            .orElse(null);
    }

    /**
     * @return The storage of the beacon.
     */
    default ItemStorage mwhrd$getStorage() {
        if (!(this instanceof IDBObject<?> dbObj))
            throw new RuntimeException("what are you doing.");
        if (!(dbObj.mwhrd$getData() instanceof BeaconModel model))
            throw new RuntimeException("why are you doing.");
        return model.getItemStorage();
    }
}
