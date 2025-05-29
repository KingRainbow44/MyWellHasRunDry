package moe.seikimo.mwhrd.custom.blocks;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import moe.seikimo.mwhrd.custom.CustomEntities;
import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.custom.entities.AdvancedBeaconBlockEntity;
import moe.seikimo.mwhrd.interfaces.IAdvancedBeacon;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Collections;
import java.util.List;

public final class AdvancedBeaconBlock extends BlockWithEntity implements Stainable, PolymerBlock {
    public static final MapCodec<AdvancedBeaconBlock> CODEC = AdvancedBeaconBlock.createCodec(AdvancedBeaconBlock::new);

    public AdvancedBeaconBlock(Settings settings) {
        super(
            settings
                .mapColor(MapColor.DIAMOND_BLUE)
                .instrument(NoteBlockInstrument.HAT)
                .strength(3.0f)
                .luminance(state -> 15)
                .nonOpaque()
                .solidBlock(Blocks::never)
        );
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedBeaconBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return AdvancedBeaconBlock.validateTicker(type, CustomEntities.ADVANCED_BEACON, AdvancedBeaconBlockEntity::tick);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.BEACON.getDefaultState();
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof AdvancedBeaconBlockEntity blockEntity) {
            player.openHandledScreen(blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_BEACON);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AdvancedBeaconBlockEntity beacon) {
            var item = beacon.createItem();

            var itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);

            // Disable beacon effects on players.
            beacon.destroy();
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        return Collections.emptyList();
    }
}
