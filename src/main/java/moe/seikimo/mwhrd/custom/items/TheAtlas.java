package moe.seikimo.mwhrd.custom.items;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.custom.CustomBlocks;
import moe.seikimo.mwhrd.utils.Portal;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

public final class TheAtlas extends SimplePolymerItem {
    public TheAtlas() {
        super(
            new Settings()
                .maxCount(1)
                .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true),
            Items.ENCHANTED_BOOK
        );
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var optional = Portal.getNewPortal(
            context.getWorld(),
            // We need to get the block +1 offset from the normal block position.
            context.getBlockPos().offset(context.getSide(), 1),
            context.getHorizontalPlayerFacing().getAxis(),
            (state, _world, _pos) -> state.isOf(Blocks.GLOWSTONE)
        );

        // If the portal is not present, pass the action.
        if (optional.isEmpty()) return ActionResult.PASS;

        // Otherwise, create the portal.
        optional.get().createPortal(CustomBlocks.LIGHT_PORTAL);
        // Play a sound.
        context.getWorld().playSound(
            null, context.getBlockPos(),
            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
            SoundCategory.BLOCKS,
            1.0F, 1.0F);

        return ActionResult.CONSUME;
    }
}
