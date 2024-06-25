package moe.seikimo.mwhrd.mixin.beacon;

import com.llamalad7.mixinextras.sugar.Local;
import moe.seikimo.data.DatabaseUtils;
import moe.seikimo.mwhrd.beacon.BeaconManager;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.BeaconModel;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.beacon.BeaconLevel;
import moe.seikimo.mwhrd.beacon.BeaconPower;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static moe.seikimo.mwhrd.beacon.BeaconManager.FUEL_TIME;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin
    extends BlockEntity
    implements IAdvancedBeacon, IDBObject<BeaconModel> {

    @Shadow
    protected abstract void addComponents(ComponentMap.Builder componentMapBuilder);

    /// <editor-fold desc="Beacon Override">

    @Redirect(method = "tick", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/entity/BeaconBlockEntity;applyPlayerEffects(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/registry/entry/RegistryEntry;)V"
    ))
    private static void tick(
        World world, BlockPos pos, int beaconLevel,
        @Nullable RegistryEntry<StatusEffect> primaryEffect,
        @Nullable RegistryEntry<StatusEffect> secondaryEffect,
        @Local(argsOnly = true) BeaconBlockEntity self
    ) {
        if (self instanceof IAdvancedBeacon beacon && beacon.mwhrd$isAdvanced()) {
            applySpecialEffects(world, pos, beaconLevel, beacon);
        } else {
            BeaconBlockEntity.applyPlayerEffects(world, pos, beaconLevel, primaryEffect, secondaryEffect);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private static void tick(
        World world, BlockPos pos, BlockState state,
        BeaconBlockEntity blockEntity, CallbackInfo ci
    ) {
        if (!(blockEntity instanceof IAdvancedBeacon beacon)) return;
        var level = blockEntity.level;

        // Load the beacon's data.
        if (blockEntity instanceof IDBObject<?> dbObj) {
            if (dbObj.mwhrd$getData() == null) {
                dbObj.mwhrd$loadData();
            }
        }

        // Call update if the entity hasn't been initialized.
        if (!beacon.mwhrd$hasInitialized()) {
            beacon.mwhrd$setInitialized(true);

            beacon.mwhrd$getEffectMap().putAll(beacon.mwhrd$default());
            beacon.mwhrd$getEffectMap().forEach(
                (effect, power) -> power.init(beacon, world));

            BeaconManager.getAllBeacons().put(blockEntity.getPos(), beacon);
        }

        // Remove effects from nearby players.
        if (level == 0) {
            for (var playerUuid : new ArrayList<>(beacon.mwhrd$getLastPlayers())) {
                var player = world.getPlayerByUuid(playerUuid);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    beacon.mwhrd$getEffectMap().forEach((effect, power) ->
                        power.remove(world, serverPlayer));
                }
            }
        }

        // Update the beacon's fuel.
        if (world.getTime() % FUEL_TIME == 0) {
            // Decrement fuel.
            var fuel = beacon.mwhrd$getFuel();
            if (fuel == 0) {
                return;
            }

            if (level < 0 || level > 4) return;
            var beaconTier = BeaconLevel.valueOf("TIER_" + level);
            var fuelCost = beaconTier.getFuelCost();

            if (fuel <= fuelCost) {
                beacon.mwhrd$setFuel(0);
            } else {
                beacon.mwhrd$setFuel(fuel - fuelCost);
            }

            beacon.mwhrd$getEffectMap().forEach((effect, power) ->
                power.fuelTick(beacon.mwhrd$getFuel()));
        }
    }

    @Unique
    private static void applySpecialEffects(
        World world, BlockPos pos, int beaconLevel,
        IAdvancedBeacon self
    ) {
        var level = BeaconLevel.valueOf("TIER_" + beaconLevel);
        var players = Utils.getNearbyPlayers(world, pos, level.getRange());
        var lastPlayers = self.mwhrd$getLastPlayers();

        // For any players in last that aren't in players, remove the effect.
        for (var playerUuid : new ArrayList<>(lastPlayers)) {
            var player = world.getPlayerByUuid(playerUuid);
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                continue;
            }

            if (!players.contains(player)) {
                self.mwhrd$getEffectMap().forEach((effect, power) ->
                    power.remove(world, serverPlayer));
                lastPlayers.remove(playerUuid);
            }
        }

        // Apply the effects to the players.
        for (var player : players) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                self.mwhrd$getEffectMap().forEach((effect, power) ->
                    power.apply(world, beaconLevel, serverPlayer));

                if (!lastPlayers.contains(player.getUuid())) {
                    lastPlayers.add(player.getUuid());
                }
            }
        }
    }

    /// </editor-fold>

    @Unique private final List<UUID> lastPlayers = new ArrayList<>();

    @Unique private int fuel = 0;
    @Unique private boolean advanced = false, initialized = false;

    @Unique private final Map<BeaconEffect, BeaconPower> powers = new HashMap<>();

    @Unique private BeaconModel model;

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /// <editor-fold desc="Serialization">

    @Inject(method = "writeNbt", at = @At("RETURN"))
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        var data = this.mwhrd$serialize();
        for (var key : data.getKeys()) {
            nbt.put(key, data.get(key));
        }
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        this.mwhrd$deserialize(nbt);
    }

    /// </editor-fold>

    /// <editor-fold desc="IDBObject">

    @Override
    public void mwhrd$loadData() {
        this.model = DatabaseUtils.fetch(
            BeaconModel.class, "_id", this.getPos().asLong());
        if (this.model == null) {
            this.model = new BeaconModel();
            this.model.setBlockPos(this.getPos().asLong());
        }

        this.model.setHandle((BeaconBlockEntity) (Object) this);
    }

    @Override
    public BeaconModel mwhrd$getData() {
        return this.model;
    }

    /// </editor-fold>

    /// <editor-fold desc="IAdvancedBeacon">

    @Override
    public World mwhrd$getWorld() {
        return this.world;
    }

    @Override
    public BlockPos mwhrd$getPos() {
        return this.getPos();
    }

    @Override
    public boolean mwhrd$hasInitialized() {
        return this.initialized;
    }

    @Override
    public void mwhrd$setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public void mwhrd$addEffect(BeaconEffect effect) {
        var instance = effect.create(this.getPos());
        instance.init(this, this.getWorld());
        instance.add(this.getWorld());

        this.powers.put(effect, instance);
    }

    @Override
    public NbtCompound mwhrd$serialize() {
        var serialized = new NbtCompound();
        serialized.putInt("adv_beacon", this.mwhrd$isAdvanced() ? 1 : 0);
        serialized.putInt("fuel", this.fuel);

        // Serialize powers.
        var powers = new NbtCompound();
        for (var entry : this.mwhrd$getEffectMap().entrySet()) {
            var id = entry.getKey().getId();
            var power = entry.getValue();

            // Serialize the power's data.
            var compound = new NbtCompound();
            power.write(this.getWorld(), compound);

            // Write it using the power enum.
            powers.put(id, compound);
        }
        serialized.put("powers", powers);

        return serialized;
    }

    @Override
    public void mwhrd$deserialize(NbtCompound nbt) {
        var advanced = nbt.getBoolean("adv_beacon");
        var fuel = nbt.getInt("fuel");

        this.mwhrd$setAdvanced(advanced);
        this.mwhrd$setFuel(fuel);

        // Apply powers.
        var powers = nbt.getCompound("powers");
        for (var key : powers.getKeys()) {
            var effect = BeaconEffect.getById(key);
            if (effect == null) {
                continue;
            }

            var power = effect.create(this.getPos());
            power.read(this.getWorld(), powers.getCompound(key));

            this.powers.put(effect, power);
        }
    }

    @Override
    public void mwhrd$save() {
        if (this.mwhrd$getData() != null) {
            this.mwhrd$getData().save();
        }

        var world = this.getWorld();
        if (world == null) return;

        var chunk = world.getChunk(this.getPos());
        if (chunk != null) {
            chunk.setNeedsSaving(true);
        }
    }

    @Override
    public void mwhrd$destroy() {
        var world = this.getWorld();
        if (world == null) return;

        // Disable beacon effects on players.
        this.mwhrd$getLastPlayers().stream()
            .map(world::getPlayerByUuid)
            .map(ServerPlayerEntity.class::cast)
            .forEach(beaconPlayer -> this.powers.values().forEach(
                power -> power.remove(world, beaconPlayer)));

        BeaconManager.getAllBeacons().remove(this.getPos());
    }

    /// </editor-fold>

    /// <editor-fold desc="Getters and Setters">

    @Override
    public List<BeaconEffect> mwhrd$getEffectList() {
        return this.powers.keySet().stream().toList();
    }

    @Override
    public Map<BeaconEffect, BeaconPower> mwhrd$getEffectMap() {
        return this.powers;
    }

    @Override
    public List<UUID> mwhrd$getLastPlayers() {
        return this.lastPlayers;
    }

    @Override
    public void mwhrd$setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    @Override
    public boolean mwhrd$isAdvanced() {
        return this.advanced;
    }

    @Override
    public int mwhrd$getFuel() {
        return this.fuel;
    }

    @Override
    public void mwhrd$setFuel(int fuel) {
        this.fuel = fuel;
    }

    /// </editor-fold>
}
