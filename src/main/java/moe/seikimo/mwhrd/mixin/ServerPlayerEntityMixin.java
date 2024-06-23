package moe.seikimo.mwhrd.mixin;

import com.mojang.authlib.GameProfile;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements IPlayerConditions {
    @Unique private boolean trialChamber = false, ominous = false;
    @Unique private long closedCooldown = 0;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public boolean canFoodHeal() {
        return super.canFoodHeal() && !(this.mwhrd$isInTrialChamber() && this.mwhrd$isOminous());
    }

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
}
