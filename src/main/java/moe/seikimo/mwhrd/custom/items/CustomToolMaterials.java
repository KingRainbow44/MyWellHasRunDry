package moe.seikimo.mwhrd.custom.items;

import moe.seikimo.mwhrd.custom.CustomTags;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;

public interface CustomToolMaterials {
    ToolMaterial ENLIGHTENED = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
        100, 8.5f, 0, 10,
        CustomTags.ENLIGHTENED_MATERIALS
    );
}
