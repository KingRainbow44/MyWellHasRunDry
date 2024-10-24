package moe.seikimo.mwhrd.custom;

import moe.seikimo.mwhrd.custom.blocks.BlossomSaplingBlock;
import moe.seikimo.mwhrd.custom.blocks.LightPortalBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface CustomBlocks {
    Block LIGHT_PORTAL = Registry.register(Registries.BLOCK, Identifier.of("mwhrd", "light_portal"), new LightPortalBlock());
    Block BLOSSOM_SAPLING = Registry.register(Registries.BLOCK, Identifier.of("mwhrd", "blossom_sapling"), new BlossomSaplingBlock());

    /**
     * No-op method to trigger the static block.
     */
    static void register() {}
}
