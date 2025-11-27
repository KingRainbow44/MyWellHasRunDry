package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.impl.PerWorldBorderListener;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(method = "createWorlds", at = @At("TAIL"))
    private void createWorlds(CallbackInfo ci) {
        this.worlds.forEach((key, world) -> {
            var border = world.getWorldBorder();
            if (key == World.OVERWORLD) return;

            border.addListener(new PerWorldBorderListener(world));
        });
    }

    @Redirect(method = "createWorlds", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/server/PlayerManager;setMainWorld(Lnet/minecraft/server/world/ServerWorld;)V"
    ))
    public void setMainWorld(PlayerManager instance, ServerWorld world) {
        // We prevent the main world from being set.
        // This just adds a world border listener which we don't want.
    }
}
