package moe.seikimo.mwhrd.mixin.game.hardcore;

import com.mojang.authlib.GameProfile;
import moe.seikimo.mwhrd.interfaces.game.IHardcorePlayer;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class HardcorePlayerMixin extends PlayerEntity implements IHardcorePlayer {
    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Unique private Set<HostileEntity> targets = new HashSet<>();

    public HardcorePlayerMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    /**
     * @return This object as a {@link ServerPlayerEntity}.
     */
    @Unique
    private ServerPlayerEntity asPlayer() {
        return (ServerPlayerEntity) (Object) this;
    }

    /**
     * @return Whether the player is in hardcore mode.
     */
    @Override
    public boolean mwhrd$isKamikaze() {
        return Players.getModel(this.asPlayer()).isHardcoreV2();
    }

    /**
     * @return Whether the player has targets.
     */
    @Override
    public boolean mwhrd$hasTargets() {
        return !this.targets.isEmpty();
    }

    @Override
    public void mwhrd$addTarget(MobEntity target) {
        if (!this.mwhrd$isKamikaze()) {
            return;
        }

        // Check if the target is a hostile mob.
        if (target instanceof HostileEntity hostileEntity) {
            this.targets.add(hostileEntity);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void mwhrd$checkCombat(CallbackInfo ci) {
        if (!this.mwhrd$isKamikaze() || this.targets.isEmpty()) {
            return;
        }

        // Send targeted message.
        this.sendMessage(Text.literal("You are being targeted!")
            .formatted(Formatting.LIGHT_PURPLE), true);

        // Check if the entities are still targeting the player.
        var copy = new HashSet<HostileEntity>();
        for (var target : this.targets) {
            if (target.getTarget() == this.asPlayer() && !target.isDead()) {
                copy.add(target);
            }
        }

        this.targets = copy;
    }

    @ModifyArg(method = "addExperience", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"
    ))
    public int mwhrd$moreExperience(int experience) {
        // If the player is in hardcore mode, triple their experience.
        if (this.mwhrd$isKamikaze()) {
            experience *= 3;
        }

        return experience;
    }
}
