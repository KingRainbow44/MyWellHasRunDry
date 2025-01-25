package moe.seikimo.mwhrd.custom;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.providers.PlayerVaultNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface CustomProviders {
    LootNumberProviderType PLAYER_VAULT_PROVIDER = Registry.register(
        Registries.LOOT_NUMBER_PROVIDER_TYPE,
        Identifier.of(MyWellHasRunDry.MOD_ID, "player_vault"),
        new LootNumberProviderType(PlayerVaultNumberProvider.CODEC)
    );

    /**
     * No-op method to register the custom providers.
     */
    static void register() {}
}
