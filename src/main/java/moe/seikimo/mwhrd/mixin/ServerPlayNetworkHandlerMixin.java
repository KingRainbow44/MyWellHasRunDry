package moe.seikimo.mwhrd.mixin;

import moe.seikimo.mwhrd.impl.ShulkerListener;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Unique
    private static final Map<UUID, Pair<ScreenHandler, ItemStack>> SHULKERS = new ConcurrentHashMap<>();

    /**
     * Sends the player's current screen handler.
     */
    @Unique
    private static void sendScreenHandler(ServerPlayerEntity player) {
        var screenHandler = player.currentScreenHandler;
        player.networkHandler.sendPacket(new InventoryS2CPacket(
            screenHandler.syncId, screenHandler.nextRevision(),
            screenHandler.getStacks(), screenHandler.getCursorStack()
        ));
    }

    @Unique
    private static boolean isShulkerBox(ItemStack stack) {
        var itemType = stack.getItem();
        return itemType instanceof BlockItem blockItem &&
            blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onClickSlot", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V",
        shift = At.Shift.AFTER
    ), cancellable = true)
    private void handleSlotClick(ClickSlotC2SPacket packet, CallbackInfo ci) {
        var screenHandler = this.player.currentScreenHandler;
        if (!(screenHandler instanceof GenericContainerScreenHandler) &&
            !(screenHandler instanceof PlayerScreenHandler)) {
            return;
        }

        if (screenHandler instanceof GenericContainerScreenHandler &&
            SHULKERS.containsKey(this.player.getUuid())) {
            var slot = packet.getSlot();
            if (slot == -999) return;

            var item = screenHandler.getSlot(slot).getStack();
            if (item.getItem().canBeNested()) {
                return;
            }

            ServerPlayNetworkHandlerMixin.sendScreenHandler(this.player);
            ci.cancel();
            return;
        }

        var slot = packet.getSlot();
        if (slot == -999) return;

        var button = packet.getButton();
        if (button != 1) return;

        var action = packet.getActionType();
        if (action != SlotActionType.QUICK_MOVE) return;

        // Determine the slot's item stack.
        var itemStack = screenHandler.getSlot(slot).getStack();
        var itemType = itemStack.getItem();

        if (itemType != Items.ENDER_CHEST && !isShulkerBox(itemStack)) {
            return;
        }

        if (itemType == Items.ENDER_CHEST) {
            var inventory = this.player.getEnderChestInventory();
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, inventory),
                Text.translatable("container.enderchest")));
            player.incrementStat(Stats.OPEN_ENDERCHEST);
        } else {
            var container = itemStack.get(DataComponentTypes.CONTAINER);
            if (container == null) return;

            var shulkerInventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
            container.copyTo(shulkerInventory);

            // Create a copy of the shulker box to work on.
            var workingStack = itemStack.copy();
            SHULKERS.put(this.player.getUuid(), new Pair<>(screenHandler, workingStack));

            // Remove the item stack from the player's inventory.
            itemStack.setCount(0);

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
                var screen = GenericContainerScreenHandler.createGeneric9x3(i, playerInventory);
                for (var a = 0; a <= shulkerInventory.size() - 1; a++) {
                    screen.setStackInSlot(a, 0, shulkerInventory.get(a));
                }

                screen.addListener(new ShulkerListener((_handler, _slot, stack) -> {
                    if (_slot == -999) return;
                    if (_slot > shulkerInventory.size() - 1) return;

                    // Update the shulker box's inventory.
                    shulkerInventory.set(_slot, stack);

                    var newContainer = ContainerComponent.fromStacks(shulkerInventory);
                    workingStack.set(DataComponentTypes.CONTAINER, newContainer);
                }));
                return screen;
            }, Text.of(workingStack.getName())));
        }

        ServerPlayNetworkHandlerMixin.sendScreenHandler(this.player);
        ci.cancel();
    }

    @Inject(method = "onCloseHandledScreen", at = @At("TAIL"))
    private void sgui$executeClosing(CloseHandledScreenC2SPacket packet, CallbackInfo info) {
        // Check if the player has a shulker box open.
        var workingStack = SHULKERS.remove(this.player.getUuid());
        if (workingStack == null) return;

        // Add the stack to the screen handler's inventory.
        var screenHandler = workingStack.getLeft();
        var itemStack = workingStack.getRight();

        // Find the first empty slot in the screen handler.
        var emptySlot = screenHandler.slots.stream()
            .filter(slot -> slot.getStack().isEmpty())
            .findFirst()
            .orElse(null);
        if (emptySlot != null && !(screenHandler instanceof PlayerScreenHandler)) {
            emptySlot.setStack(itemStack);
        } else {
            // If there are no empty slots, add the stack to the player's inventory.
            this.player.getInventory().offerOrDrop(itemStack);
        }
    }
}
