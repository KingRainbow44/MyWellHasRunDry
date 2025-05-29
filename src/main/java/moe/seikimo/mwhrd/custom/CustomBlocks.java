package moe.seikimo.mwhrd.custom;

import moe.seikimo.mwhrd.custom.blocks.AdvancedBeaconBlock;
import moe.seikimo.mwhrd.custom.blocks.BlossomSaplingBlock;
import moe.seikimo.mwhrd.custom.blocks.LightPortalBlock;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public interface CustomBlocks {
    Block LIGHT_PORTAL = Blocks.register(Utils.blockKey("light_portal"), LightPortalBlock::new, Settings.create());
    Block BLOSSOM_SAPLING = Blocks.register(Utils.blockKey("blossom_sapling"), BlossomSaplingBlock::new, Settings.create());
    Block ADVANCED_BEACON = Blocks.register(Utils.blockKey("advanced_beacon"), AdvancedBeaconBlock::new, Settings.create());

    /**
     * No-op method to trigger the static block.
     */
    static void register() {}
}
