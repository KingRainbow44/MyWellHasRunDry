package moe.seikimo.mwhrd.custom.entities;

import lombok.Getter;
import lombok.Setter;
import moe.seikimo.data.DatabaseUtils;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomEntities;
import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.game.beacon.*;
import moe.seikimo.mwhrd.game.beacon.powers.EffectsPower;
import moe.seikimo.mwhrd.gui.beacon.AdvancedBeaconGui;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.models.BeaconModel;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.utils.items.ItemStorage;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BeamEmitter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static moe.seikimo.mwhrd.game.beacon.BeaconManager.FUEL_TIME;

@Getter
public final class AdvancedBeaconBlockEntity
    extends BlockEntity
    implements BeamEmitter, NamedScreenHandlerFactory, IDBObject<BeaconModel>
{
    private static final Map<BeaconEffect, BeaconPower.Initializer> DEFAULT_POWERS = new HashMap<>() {{
        this.put(BeaconEffect.EFFECTS, EffectsPower::new);
        this.put(BeaconEffect.DISABLE_SPAWNS, BeaconPower.Empty::new);
        this.put(BeaconEffect.UNBREAKING_TOOLS, BeaconPower.Empty::new);
    }};

    /**
     * Called once per block entity tick.
     */
    public static void tick(World world, BlockPos pos, BlockState state, AdvancedBeaconBlockEntity blockEntity) {
        var blockPos = pos;

        // Check Y position & recalculate minimum.
        if (blockEntity.minY < pos.getY()) {
            blockEntity.$beamSegments = new ArrayList<>();
            blockEntity.minY = blockPos.getY() - 1;
        } else {
            blockPos = new BlockPos(pos.getX(), blockEntity.minY + 1, pos.getZ());
        }

        // Calculate beam segments.
        var lastSegment = !blockEntity.$beamSegments.isEmpty() ?
            blockEntity.$beamSegments.getLast()
            : null;
        var maxY = world.getTopY(Heightmap.Type.WORLD_SURFACE,
            blockPos.getX(), blockPos.getZ());

        // This loop will set the color of the beam segments to the world height.
        for (var i = 0; i < 10 && blockPos.getY() <= maxY; i++) {
            loop: {
                BlockState target;
                loop2: {
                    int color;

                    loop3: {
                        target = world.getBlockState(blockPos);
                        var block = target.getBlock();

                        // If the block is not stainable, go to the next block.
                        if (!(block instanceof Stainable stainable)) {
                            break loop2;
                        }

                        color = stainable.getColor().getEntityColor();
                        if (blockEntity.$beamSegments.size() > 1) {
                            break loop3;
                        }

                        lastSegment = new BeamSegment(color);
                        blockEntity.$beamSegments.add(lastSegment);
                        break loop;
                    }

                    if (lastSegment == null) {
                        break loop;
                    }
                    if (color == lastSegment.getColor()) {
                        lastSegment.increaseHeight();
                    } else {
                        lastSegment = new BeamSegment(ColorHelper.average(
                            lastSegment.getColor(), color));
                        blockEntity.$beamSegments.add(lastSegment);
                    }

                    break loop;
                }

                if (lastSegment != null && (target.getOpacity() < 15 || target.isOf(Blocks.BEDROCK))) {
                    lastSegment.increaseHeight();
                } else {
                    blockEntity.$beamSegments.clear();;
                    blockEntity.minY = maxY;
                    break;
                }
            }

            blockPos = blockPos.up();
            blockEntity.minY++;
        }

        // Do beacon update.
        var oldLevel = blockEntity.level;
        if (world.getTime() % 80L == 0L) {
            // Use the method from the original beacon to update the level.
            if (!blockEntity.beamSegments.isEmpty()) {
                blockEntity.level = BeaconBlockEntity.updateLevel(
                    world, pos.getX(), pos.getY(), pos.getZ());
            }

            // Do the beacon effects update.
            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
                // Update the beacon's effects.
                AdvancedBeaconBlockEntity.applyEffects(world, pos, blockEntity.level, blockEntity);

                // Play the beacon sound effect.
                world.playSound(
                    null, pos, SoundEvents.BLOCK_BEACON_AMBIENT,
                    SoundCategory.BLOCKS, 1f, 1f
                );

                // Update listeners.
                if (blockEntity.world != null) {
                    blockEntity.world.updateListeners(blockEntity.getPos(), state, state, Block.NOTIFY_ALL);
                }
            }
        }

        // Validate segments & award advancement.
        if (blockEntity.minY >= maxY) {
            blockEntity.minY = world.getBottomY() - 1;
            blockEntity.beamSegments = blockEntity.$beamSegments;

            var oldEnabled = oldLevel > 0;
            var nowEnabled = blockEntity.level > 0;

            if (!oldEnabled && nowEnabled) {
                // Play the activate sound.
                world.playSound(
                    null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE,
                    SoundCategory.BLOCKS, 1f, 1f
                );

                // If the level has changed, award the advancement.
                var box = new Box(
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX(), pos.getY() - 4, pos.getZ()
                ).expand(10f, 5f, 10f);
                for (var player : world.getNonSpectatingEntities(ServerPlayerEntity.class, box)) {
                    Criteria.CONSTRUCT_BEACON.trigger(player, blockEntity.level);
                }
            } else if (oldEnabled && !nowEnabled) {
                // Play the deactivate sound.
                world.playSound(
                    null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE,
                    SoundCategory.BLOCKS, 1f, 1f
                );
            }
        }

        // Run custom beacon logic.
        AdvancedBeaconBlockEntity.tick(world, blockEntity);
    }

    /**
     * Runs the custom beacon logic.
     */
    private static void tick(World world, AdvancedBeaconBlockEntity blockEntity) {
        var level = blockEntity.level;

        // Load the beacon's data.
        if (blockEntity.data == null) {
            blockEntity.mwhrd$loadData();
        }

        // Initialize the beacon.
        if (!blockEntity.initialized) {
            blockEntity.initialized = true;

            // Add default powers & initialize.
            blockEntity.getPowers().putAll(blockEntity.defaultPowers());
            blockEntity.getPowers().forEach((effect, power) -> power.init(blockEntity, world));

            // Add the beacon to the map.
            BeaconManager.getAllBeacons().put(blockEntity.getPos(), blockEntity);
        }

        // Remove effects from nearby players.
        if (level == 0 || blockEntity.beamSegments.isEmpty()) {
            for (var playerUuid : new ArrayList<>(blockEntity.getLastPlayers())) {
                var player = world.getPlayerByUuid(playerUuid);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    blockEntity.getPowers().forEach((effect, power) ->
                        power.remove(world, serverPlayer));
                }
            }
        }

        // Update the beacon's fuel.
        if (world.getTime() % FUEL_TIME == 0) {
            // Decrement fuel.
            var fuel = blockEntity.getFuel();
            if (fuel == 0) {
                return;
            }

            if (level < 0 || level > 4) return;
            var beaconTier = BeaconLevel.valueOf("TIER_" + level);
            var fuelCost = beaconTier.getFuelCost();

            if (fuel <= fuelCost) {
                blockEntity.fuel = 0;
            } else {
                blockEntity.fuel = fuel - fuelCost;
            }

            blockEntity.getPowers().forEach((effect, power) ->
                power.fuelTick(blockEntity.getFuel()));
        }
    }

    /**
     * Applies special effects to nearby players.
     */
    private static void applyEffects(World world, BlockPos pos, int beaconLevel, AdvancedBeaconBlockEntity self) {
        var level = BeaconLevel.valueOf("TIER_" + beaconLevel);
        var players = Utils.getNearbyPlayers(world, pos, level.getRange());
        var lastPlayers = self.getLastPlayers();

        // For any players in last that aren't in players, remove the effect.
        for (var playerUuid : new HashSet<>(lastPlayers)) {
            var player = world.getPlayerByUuid(playerUuid);
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                continue;
            }

            if (!players.contains(player)) {
                self.getPowers().forEach((effect, power) ->
                    power.remove(world, serverPlayer));
                lastPlayers.remove(playerUuid);
            }
        }

        // Apply the effects to the players.
        for (var player : players) {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                continue;
            }

            self.getPowers().forEach((effect, power) ->
                power.apply(world, beaconLevel, serverPlayer));

            lastPlayers.add(player.getUuid());
        }
    }

    private BeaconModel data;
    private boolean initialized = false;

    private final Set<UUID> lastPlayers = new HashSet<>();
    private final Map<BeaconEffect, BeaconPower> powers = new HashMap<>();

    private int level;
    @Setter private int fuel;

    private int minY;
    public List<BeamSegment> beamSegments = new ArrayList<>();
    private List<BeamSegment> $beamSegments = new ArrayList<>();

    public AdvancedBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(CustomEntities.ADVANCED_BEACON, pos, state);
    }

    /**
     * Saves the beacon's data.
     */
    public void save() {
        // Save external database data.
        if (this.data != null) {
            this.data.save();
        }

        // Get world.
        var world = this.getWorld();
        if (world == null) {
            return; // No world, cannot save.
        }

        // Get chunk with beacon.
        var chunk = world.getChunk(this.getPos());
        if (chunk != null) {
            // Force the chunk to save.
            chunk.markNeedsSaving();
        }
    }

    /**
     * @return The beacon's fuel as an enum.
     */
    public BeaconFuel getFuelLevel() {
        return BeaconFuel.getFuel(this.fuel);
    }

    /**
     * @return The players affected by the beacon.
     */
    public List<ServerPlayerEntity> getLastOnlinePlayers() {
        var server = MyWellHasRunDry.getServer();
        return this.getLastPlayers().stream()
            .map(server.getPlayerManager()::getPlayer)
            .toList();
    }

    /**
     * @return A list of all present effects.
     */
    public List<BeaconEffect> getEffectList() {
        return this.powers.keySet().stream().toList();
    }

    /**
     * @return The beacon's storage.
     */
    public ItemStorage getStorage() {
        if (!(this.mwhrd$getData() instanceof BeaconModel model))
            throw new RuntimeException("why are you doing.");
        return model.getItemStorage();
    }

    /**
     * Fetches the instance of a power from the beacon.
     *
     * @param power The class of the power to fetch.
     * @return The power instance, or null if not present.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getPower(Class<T> power) {
        return (T) this.getPowers().values().stream()
            .filter(power::isInstance)
            .findFirst()
            .orElse(null);
    }

    /**
     * Adds an effect to the beacon.
     *
     * @param effect The effect to add.
     */
    public void addEffect(BeaconEffect effect) {
        var instance = effect.create(this.getPos());
        instance.init(this, this.getWorld());
        instance.add(this.getWorld());

        this.powers.put(effect, instance);
    }

    /**
     * This method should be invoked before the entity is destroyed.
     */
    public void destroy() {
        var world = this.getWorld();
        if (world == null) return;

        // Disable beacon effects on players.
        this.getLastPlayers().stream()
            .map(world::getPlayerByUuid)
            .map(ServerPlayerEntity.class::cast)
            .forEach(beaconPlayer -> this.powers.values().forEach(
                power -> power.remove(world, beaconPlayer)));

        BeaconManager.getAllBeacons().remove(this.getPos());
    }

    /**
     * @return Creates an item stack representing the beacon.
     */
    public ItemStack createItem() {
        var world = this.getWorld();
        if (world == null) {
            return ItemStack.EMPTY;
        }

        var item = new ItemStack(CustomItems.ADVANCED_BEACON);
        BlockItem.setBlockEntityData(item, CustomEntities.ADVANCED_BEACON, this.serialize());

        return item;
    }

    /**
     * @return A map of all default beacon powers.
     */
    private Map<BeaconEffect, BeaconPower> defaultPowers() {
        var existing = this.getPowers();
        var map = new HashMap<BeaconEffect, BeaconPower>();
        for (var entry : DEFAULT_POWERS.entrySet()) {
            if (existing.containsKey(entry.getKey())) continue;
            map.put(entry.getKey(), entry.getValue().create(this.getPos()));
        }

        return map;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.fuel = nbt.getInt("fuel", 0);

        // Read beacon powers.
        var powers = nbt.getCompoundOrEmpty("powers");
        for (var key : powers.getKeys()) {
            var effect = BeaconEffect.getById(key);
            if (effect == null) {
                continue;
            }

            var power = effect.create(this.getPos());
            power.init(this, this.getWorld());
            power.read(this.getWorld(), powers.getCompoundOrEmpty(key));

            this.powers.put(effect, power);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        var data = this.serialize();
        for (var key : data.getKeys()) {
            nbt.put(key, data.get(key));
        }
    }

    /**
     * Serializes the beacon data into a compound tag.
     *
     * @return The serialized beacon data.
     */
    public NbtCompound serialize() {
        var serialized = new NbtCompound();
        serialized.putInt("fuel", this.fuel);

        // Write legacy NBT tags.
        serialized.putInt("Levels", this.level);

        // Serialize powers.
        var powers = new NbtCompound();
        for (var entry : this.getPowers().entrySet()) {
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
    public List<BeamSegment> getBeamSegments() {
        return this.level == 0 ? List.of() : this.beamSegments;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        var registry = Objects.requireNonNull(this.getWorld()).getRegistryManager();
        return new BlockEntityUpdateS2CPacket(
            this.getPos(),
            BlockEntityType.BEACON,
            this.toInitialChunkDataNbt(registry)
        );
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return null; // Not a server player, cannot open GUI.
        }

        return AdvancedBeaconGui.create(this, syncId, playerInventory, serverPlayer);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.advanced_beacon");
    }

    @Override
    public void markRemoved() {
        if (this.world != null) {
            this.world.playSound(
                null, this.getPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.BLOCKS, 1f, 1f
            );
        }

        this.destroy();

        super.markRemoved();
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.minY = world.getBottomY() - 1;
    }

    @Override
    public BeaconModel mwhrd$getData() {
        return this.data;
    }

    @Override
    public void mwhrd$loadData() {
        this.data = DatabaseUtils.fetch(
            BeaconModel.class, "_id", this.getPos().asLong());
        if (this.data == null) {
            this.data = new BeaconModel();
            this.data.setBlockPos(this.getPos().asLong());
        }

        this.data.setHandle(this);
    }
}
