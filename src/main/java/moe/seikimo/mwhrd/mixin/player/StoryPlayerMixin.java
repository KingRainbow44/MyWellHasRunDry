package moe.seikimo.mwhrd.mixin.player;

import com.mojang.authlib.GameProfile;
import moe.seikimo.mwhrd.game.quest.PlayerQuestManager;
import moe.seikimo.mwhrd.interfaces.player.IStoryPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin provides the base implementation for the {@link IStoryPlayer} interface.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class StoryPlayerMixin extends PlayerEntity implements IStoryPlayer {
    @Unique private PlayerQuestManager questManager;

    public StoryPlayerMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
        this.questManager = new PlayerQuestManager((ServerPlayerEntity) (Object) this);
    }

    @Override
    public PlayerQuestManager mwhrd$getQuestManager() {
        return this.questManager;
    }
}
