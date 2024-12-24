package moe.seikimo.mwhrd.mixin;

import com.mojang.authlib.GameProfile;
import moe.seikimo.data.DatabaseUtils;
import moe.seikimo.mwhrd.beacon.BeaconEffect;
import moe.seikimo.mwhrd.events.PlayerMoveEvent;
import moe.seikimo.mwhrd.interfaces.*;
import moe.seikimo.mwhrd.interfaces.player.ICallbackPlayer;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.block.Portal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
    extends PlayerEntity
    implements IPlayerConditions,
    IDBObject<PlayerModel>,
    ISelectionPlayer,
    ITrialPlayer,
    ITimeTraveler,
    ICallbackPlayer {
    @Shadow
    public abstract void sendMessage(Text message);

    @Shadow
    public abstract boolean isCreative();

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract void sendMessage(Text message, boolean overlay);

    @Shadow
    protected abstract void worldChanged(ServerWorld origin);

    @Unique private PlayerModel model;
    @Unique private boolean unbreakable = false;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    /// <editor-fold desc="Mixin" defaultstate="collapsed">

    @Override
    public boolean canFoodHeal() {
        return super.canFoodHeal() &&
            !(this.mwhrd$isInTrialChamber() && this.mwhrd$isOminous()) &&
            (this.model == null || !this.model.isHardcore());
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        super.move(movementType, movement);

        PlayerMoveEvent.EVENT.invoker()
            .onMove(this.getWorld(), this.getBlockPos(), this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.model == null) {
            this.mwhrd$loadData();
        }

        if (this.model != null && this.model.isHardcore()) {
            // Check if the player is out of hardcore.
            if (System.currentTimeMillis() > this.model.getHardcoreUntil()) {
                this.model.unsetHardcore(true);
            }
        }
    }

    @Inject(method = "onDeath", at = @At("RETURN"))
    public void onDeath(DamageSource source, CallbackInfo ci) {
        if (this.mwhrd$isHardcore()) {
            this.model.banPlayer(Duration.ofHours(24));
            this.model.unsetHardcore(false);
        }
    }

    @Inject(method = "teleportTo", at = @At("HEAD"))
    public void onTeleport(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        // Remove all beacon effects when a player teleports.
        Arrays.stream(BeaconEffect.values())
            .forEach(e -> e.remove(this.getWorld(), this));
    }

    @Redirect(method = "damage", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"
    ))
    public boolean damage(
        PlayerEntity instance, ServerWorld serverWorld, DamageSource source, float amount
    ) {
        if (this.mwhrd$isHardcore() &&
            !(source.getAttacker() instanceof PlayerEntity)) {
            return super.damage(serverWorld, source, amount *
                (this.mwhrd$isInTrialChamber() ? 2 : 3));
        }

        return super.damage(serverWorld, source, amount);
    }

    /// </editor-fold>

    /// <editor-fold desc="Database Object">

    @Override
    public PlayerModel mwhrd$getData() {
        return this.model;
    }

    @Override
    public void mwhrd$loadData() {
        this.model = DatabaseUtils.fetch(
            PlayerModel.class, "_id", this.getUuidAsString());
        if (this.model == null) {
            this.model = new PlayerModel();
            this.model.setPlayerUuid(this.getUuidAsString());
        }

        this.model.setHandle((ServerPlayerEntity) (Object) this);
    }

    /// </editor-fold>

    /// <editor-fold desc="Selection Player">

    @Unique private BlockPos pos1, pos2;

    @Override
    public BlockPos mwhrd$getPos1() {
        return this.pos1;
    }

    @Override
    public BlockPos mwhrd$getPos2() {
        return this.pos2;
    }

    @Override
    public void mwhrd$setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    @Override
    public void mwhrd$setPos2(BlockPos pos) {
        this.pos2 = pos;
    }

    /// </editor-fold>

    /// <editor-fold desc="Player Conditions">

    @Unique private boolean trialChamber = false, ominous = false;
    @Unique private long closedCooldown = 0;

    @Override
    public void mwhrd$setOminous(boolean ominous) {
        this.ominous = ominous;
    }

    @Override
    public boolean mwhrd$isOminous() {
        return this.ominous;
    }

    @Override
    public void mwhrd$setInTrialChamber(boolean inTrialChamber) {
        if (inTrialChamber != this.trialChamber) {
            if (inTrialChamber && this.loseKills != -1 &&
                System.currentTimeMillis() > this.loseKills) {
                this.loseKills = -1;
                this.mobKills = 0;
            } else if (!mwhrd$isInTrialChamber()) {
                this.loseKills = (long) (System.currentTimeMillis() + 30e3);
            } else {
                this.loseKills = -1;
            }
        }
        this.trialChamber = inTrialChamber;
    }

    @Override
    public boolean mwhrd$isInTrialChamber() {
        return this.trialChamber;
    }

    @Override
    public void mwhrd$setClosedCooldown(long until) {
        this.closedCooldown = until;
    }

    @Override
    public long mwhrd$getClosedCooldown() {
        return this.closedCooldown;
    }

    @Override
    public void mwhrd$setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public boolean mwhrd$isUnbreakable() {
        return this.unbreakable;
    }

    @Override
    public boolean mwhrd$isHardcore() {
        return this.model != null && this.model.isHardcore();
    }

    @Override
    public boolean mwhrd$finishedHardcore() {
        return this.model != null && this.model.isSurvivedHardcore();
    }

    /// </editor-fold>

    /// <editor-fold desc="Trial Player">

    @Unique private int mobKills = 0;
    @Unique private long loseKills = -1;

    @Override
    public void mwhrd$addMobKill() {
        this.mobKills++;
    }

    @Override
    public int mwhrd$getMobKills() {
        return this.mobKills;
    }

    /// </editor-fold>

    /// <editor-fold desc="Time Traveler">

    @Unique private Pair<Portal, BlockPos> queuedPortal = null;

    @Unique
    @Override
    public Pair<Portal, BlockPos> mwhrd$getQueuedPortal() {
        return this.queuedPortal;
    }

    @Unique
    @Override
    public void mwhrd$setQueuedPortal(Pair<Portal, BlockPos> portal) {
        this.queuedPortal = portal;
    }

    @Unique
    @Override
    public void mwhrd$restoreInventory() {
        if (!this.model.isStoredInventory() && !this.isSpectator()) {
            this.sendMessage(Text.translatable("text.mwhrd.inventory.not_stored")
                .formatted(Formatting.RED));

            if (this.hasPermissionLevel(3)) {
                this.sendMessage(Text.translatable("text.mwhrd.inventory.admin_override")
                    .formatted(Formatting.YELLOW));
            }

            return;
        }

        var storage = this.model.getStorage();

        // Restore the entirety of the player's inventory.
        this.getInventory().clear();

        for (var stack : storage.getArmor()) {
            if (!(stack.getItem() instanceof ArmorItem item)) continue;
            this.equipStack(Utils.getSlot(item), stack);
        }

        // Add all items to the player's inventory.
        var inventory = this.getInventory();
        for (var stack : storage.getInventory()) {
            inventory.offerOrDrop(stack);
        }

        this.setStackInHand(Hand.OFF_HAND, storage.getOffHand().get(0));

        // Clear the player's stored inventory.
        this.model.setStoredInventory(false);
        this.model.save();
    }

    @Unique
    @Override
    public void mwhrd$storeInventory(boolean clear) {
        // Check if the player needs to restore their inventory.
        if (this.model.isStoredInventory()) {
            this.sendMessage(Text.translatable("text.mwhrd.inventory.stored")
                .formatted(Formatting.RED));
            return;
        }

        var storage = this.model.getStorage();
        storage.clear();

        // Store the entirety of the player's inventory.
        var inventory = this.getInventory();

        inventory.armor.forEach(storage.getArmor()::offer);
        inventory.main.forEach(storage.getInventory()::offer);
        inventory.offHand.forEach(storage.getOffHand()::offer);

        // Clear the player's existing inventory.
        if (clear) {
            this.getInventory().clear();
        }

        this.model.setStoredInventory(true);
        this.model.save();
    }

    /// </editor-fold>

    /// <editor-fold desc="Callback Player">

    @Unique private Consumer<World> worldCallback = null;

    @Inject(method = "worldChanged", at = @At("TAIL"))
    public void worldChanged(ServerWorld origin, CallbackInfo ci) {
        if (this.worldCallback != null) {
            this.worldCallback.accept(origin);
            this.worldCallback = null;
        }
    }

    @Override
    public void mwhrd$onDimensionChange(Consumer<World> world) {
        this.worldCallback = world;
    }

    /// </editor-fold>
}
