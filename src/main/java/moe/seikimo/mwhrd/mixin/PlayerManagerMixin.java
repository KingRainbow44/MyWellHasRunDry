package moe.seikimo.mwhrd.mixin;

import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import moe.seikimo.mwhrd.BuildConfig;
import moe.seikimo.mwhrd.game.guilds.GuildManager;
import moe.seikimo.mwhrd.game.lightrealm.TheRealmOfLight;
import moe.seikimo.mwhrd.utils.Time;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    /**
     * This is a header for the player list.
     */
    @Unique
    private static final Text HEADER = Utils.list(
        Text.translatable("text.mwhrd.player_list.header.1")
            .formatted(Formatting.AQUA)
            .append(Text.literal(BuildConfig.SERVER_NAME)
                .formatted(Formatting.YELLOW)),
        Text.translatable("text.mwhrd.player_list.header.2")
            .formatted(Formatting.GOLD)
            .append(Text.translatable("text.mwhrd.name")
                .formatted(Formatting.BOLD, Formatting.DARK_AQUA)),
        Text.empty()
    );

    @Unique private int updateTicks = 0;

    @Shadow @Final private MinecraftServer server;

    @Shadow
    public abstract void sendToAll(Packet<?> packet);

    @Inject(method = "sendWorldInfo", at = @At(value = "HEAD"), cancellable = true)
    private void getWorldBorder(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        ci.cancel();

        var worldBorder = world.getWorldBorder();
        player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
        player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(world.getSpawnPoint()));
        if (world.isRaining()) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, 0.0F));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, world.getRainGradient(1.0F)));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, world.getThunderGradient(1.0F)));
        }

        player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.INITIAL_CHUNKS_COMING, 0.0F));
        this.server.getTickManager().sendPackets(player);
    }

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        GuildManager.doPlayerListUpdate();

        this.doPlayerListUpdate();
    }

    @Inject(method = "remove", at = @At("RETURN"))
    public void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        GuildManager.doPlayerListUpdate();

        this.doPlayerListUpdate();
    }

    @Inject(method = "updatePlayerLatency", at = @At("HEAD"))
    public void updateTabList(CallbackInfo ci) {
        if (this.updateTicks < 20) {
            this.updateTicks++;
            return;
        }

        this.updateTicks = 0;

        this.doPlayerListUpdate();
    }

    @Unique
    private void doPlayerListUpdate() {
        try {
            // Update the player list header.
            var spark = SparkProvider.get();

            var tps = Objects.requireNonNull(spark.tps(), "TPS is null");
            var mspt = Objects.requireNonNull(spark.mspt(), "MSPT is null");
            var cpu = spark.cpuSystem();

            var rolTicks = TheRealmOfLight.MAX_TICKS - TheRealmOfLight.getWorld().getTicksAlive();

            this.sendToAll(
                new PlayerListHeaderS2CPacket(
                    HEADER,
                    Utils.list(
                        Text.empty(),
                        Text.literal("The Realm of Light Resets in")
                            .formatted(Formatting.BOLD, Formatting.LIGHT_PURPLE),
                        Time.toString(rolTicks).copy()
                            .formatted(Formatting.WHITE),
                        Text.empty(),
                        Text.literal("TPS (10s): ")
                            .formatted(Formatting.GRAY)
                            .append(Text.literal(
                                "%.2f".formatted(tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10))
                            ).formatted(Formatting.GREEN))
                            .append(Text.literal(" | MSPT (95th): ")
                                .formatted(Formatting.GRAY)
                                .append(Text.literal(
                                    "%.2f".formatted(mspt.poll(StatisticWindow.MillisPerTick.SECONDS_10)
                                        .percentile95th())
                                ).formatted(Formatting.GREEN))
                            )
                            .append(Text.literal(" | CPU (10s): ")
                                .formatted(Formatting.GRAY)
                                .append(Text.literal(
                                    "%.2f%%".formatted(cpu.poll(StatisticWindow.CpuUsage.SECONDS_10) * 100)
                                ).formatted(Formatting.GREEN))
                            )
                    )
                )
            );
        } catch (IllegalStateException ignored) {
            // This occurs if Spark has not loaded.
        }
    }
}
