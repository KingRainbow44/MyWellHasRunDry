package moe.seikimo.mwhrd.providers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import org.joml.Math;

public record PlayerVaultNumberProvider(float baseValue, float maxValue, float scale) implements LootNumberProvider {
    public static final MapCodec<PlayerVaultNumberProvider> CODEC =RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.FLOAT.fieldOf("base").forGetter(PlayerVaultNumberProvider::baseValue),
                Codec.FLOAT.fieldOf("max").forGetter(PlayerVaultNumberProvider::maxValue),
                Codec.FLOAT.fieldOf("scale").forGetter(PlayerVaultNumberProvider::scale))
            .apply(instance, PlayerVaultNumberProvider::new));

    @Override
    public float nextFloat(LootContext context) {
        var world = context.getWorld();

        var trialPlayers = 0;
        for (var player : world.getPlayers()) {
            var condPlayer = (IPlayerConditions) player;
            if (condPlayer.mwhrd$isInTrialChamber()) trialPlayers++;
        }

        return Math.lerp(this.baseValue, this.maxValue,
            Math.min(1.0f, trialPlayers / this.scale));
    }

    @Override
    public LootNumberProviderType getType() {
        return MyWellHasRunDry.PLAYER_VAULT;
    }
}
