package moe.seikimo.mwhrd.mixin;

import com.mojang.authlib.GameProfile;
import moe.seikimo.data.DatabaseUtils;
import moe.seikimo.mwhrd.interfaces.IDBObject;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import moe.seikimo.mwhrd.interfaces.ISelectionPlayer;
import moe.seikimo.mwhrd.models.PlayerModel;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
    extends PlayerEntity
    implements IPlayerConditions,
    IDBObject<PlayerModel>,
    ISelectionPlayer {
    @Unique private boolean trialChamber = false, ominous = false;
    @Unique private long closedCooldown = 0;

    @Unique private PlayerModel model;

    @Unique private BlockPos pos1, pos2;

    @Unique private boolean unbreakable = false;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public boolean canFoodHeal() {
        return super.canFoodHeal() &&
            !(this.mwhrd$isInTrialChamber() && this.mwhrd$isOminous()) &&
            (this.model == null || !this.model.isHardcore());
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

    @Redirect(method = "damage", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
    ))
    public boolean damage(
        PlayerEntity instance, DamageSource source, float amount
    ) {
        if (this.mwhrd$isHardcore() &&
            !(source.getAttacker() instanceof PlayerEntity)) {
            return super.damage(source, amount *
                (this.mwhrd$isInTrialChamber() ? 2 : 3));
        }

        return super.damage(source, amount);
    }

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
}
