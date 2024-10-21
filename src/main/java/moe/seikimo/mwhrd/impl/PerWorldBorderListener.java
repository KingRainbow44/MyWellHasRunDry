package moe.seikimo.mwhrd.impl;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;

import java.util.function.Consumer;

@RequiredArgsConstructor
public final class PerWorldBorderListener implements WorldBorderListener {
    private final ServerWorld world;

    /**
     * Runs the consumer as each player in the world.
     *
     * @param consumer The consumer to run.
     */
    private void runAs(Consumer<ServerPlayerEntity> consumer) {
        this.world.getPlayers().forEach(consumer);
    }

    @Override
    public void onSizeChange(WorldBorder border, double size) {
        this.runAs(player -> player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(border)));
    }

    @Override
    public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
        this.runAs(player -> player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(border)));
    }

    @Override
    public void onCenterChanged(WorldBorder border, double centerX, double centerZ) {
        this.runAs(player -> player.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(border)));
    }

    @Override
    public void onWarningTimeChanged(WorldBorder border, int warningTime) {
        this.runAs(player -> player.networkHandler.sendPacket(new WorldBorderWarningTimeChangedS2CPacket(border)));
    }

    @Override
    public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) {
        this.runAs(player -> player.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(border)));
    }

    @Override
    public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) {}

    @Override
    public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) {}
}
