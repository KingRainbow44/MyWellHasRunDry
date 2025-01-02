package moe.seikimo.mwhrd.custom.items;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.interfaces.ISelectionPlayer;
import moe.seikimo.mwhrd.interfaces.nbt.IItemNbtWrapper;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.utils.items.NbtBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public final class WorldEditWand extends SimplePolymerItem {
    public WorldEditWand(Settings settings) {
        super(
            settings
                .maxCount(1)
                .rarity(Rarity.UNCOMMON)
                .component(
                    DataComponentTypes.CUSTOM_DATA,
                    new NbtBuilder()
                        .set("selection_type", 1)
                        .build()
                ),
            Items.STRUCTURE_VOID
        );
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // Get the item stack.
        var stack = user.getStackInHand(hand);
        var nbt = ((IItemNbtWrapper) (Object) stack).mwhrd$asNbt();

        // Cycle through the selection types.
        var selectionType = nbt.getInt("selection_type");
        selectionType = selectionType == 1 ? 2 : 1;

        // Update the selection type.
        nbt.set("selection_type", selectionType);

        // Send the feedback message.
        user.sendMessage(
            Text.literal("Selection type set to %s.".formatted(selectionType))
                .formatted(Formatting.LIGHT_PURPLE),
            true
        );

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Check if the player has selections.
        if (!(context.getPlayer() instanceof ISelectionPlayer selectionPlayer)) {
            return null;
        }

        // Get the block position selected.
        var blockPos = context.getBlockPos();

        // Set the position based on the selection type.
        var stack = context.getStack();
        var nbt = ((IItemNbtWrapper) (Object) stack).mwhrd$asNbt();

        var selectionType = nbt.getInt("selection_type");
        switch (selectionType) {
            case 1 -> selectionPlayer.mwhrd$setPos1(blockPos);
            case 2 -> selectionPlayer.mwhrd$setPos2(blockPos);
        }

        // Send the feedback message.
        context.getPlayer().sendMessage(
            Text.literal("Position %s set at %s."
                .formatted(selectionType, Utils.serialize(blockPos)))
                .formatted(Formatting.LIGHT_PURPLE),
            false
        );

        return ActionResult.SUCCESS;
    }
}
