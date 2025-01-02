package moe.seikimo.mwhrd.custom.items;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import moe.seikimo.mwhrd.custom.CustomBlocks;
import moe.seikimo.mwhrd.gui.item.AtlasLookupGui;
import moe.seikimo.mwhrd.utils.Portal;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.utils.items.ItemNbt;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

public final class TheAtlas extends SimplePolymerItem {
    public TheAtlas(Settings settings) {
        super(
            settings
                .maxCount(1)
                .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true),
            Items.ENCHANTED_BOOK
        );
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        var nbt = ItemNbt.wrap(stack);

        // If the stack contains the "dest" key, set it as a compass item.
        return nbt.contains("dest") ? Items.COMPASS : super.getPolymerItem(stack, context);
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
        var nbt = ItemNbt.wrap(stack);

        // If the stack doesn't contain the "dest" key, we can move on.
        if (!nbt.contains("dest")) {
            out.set(
                DataComponentTypes.LORE,
                new LoreComponent(List.of(
                    Text.literal("Reveal the secrets of the world-")
                        .formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("Right click to open The Atlas Lookup!")
                        .setStyle(Style.EMPTY.withItalic(false))
                        .formatted(Formatting.YELLOW)
                ))
            );

            return;
        }

        // Otherwise, set the compass location.
        var destination = nbt.getGlobalPos("dest");
        out.set(
            DataComponentTypes.LODESTONE_TRACKER,
            new LodestoneTrackerComponent(Optional.of(destination), true)
        );

        out.set(
            DataComponentTypes.LORE,
            new LoreComponent(List.of(
                Text.literal("Currently tracking %s."
                        .formatted(Utils.serialize(destination.pos())))
                    .formatted(Formatting.GRAY),
                Text.empty(),
                Text.literal("Right click to stop tracking.")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .formatted(Formatting.YELLOW)
            ))
        );
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var nbt = ItemNbt.wrap(stack);

        if (nbt.contains("dest")) {
            // If the NBT contains the "dest" key, remove it.
            nbt.remove("dest");
        } else {
            // Otherwise, open the GUI.
            AtlasLookupGui.open(stack, (ServerPlayerEntity) user);
        }

        return ActionResult.SUCCESS;
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
        optional.get().createPortal(context.getWorld(), CustomBlocks.LIGHT_PORTAL);
        // Play a sound.
        context.getWorld().playSound(
            null, context.getBlockPos(),
            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
            SoundCategory.BLOCKS,
            1.0F, 1.0F);

        return ActionResult.CONSUME;
    }
}
