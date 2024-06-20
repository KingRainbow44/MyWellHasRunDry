package moe.seikimo.mwhrd.impl;

import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;

@RequiredArgsConstructor
public final class ShulkerListener implements ScreenHandlerListener {
    private final Listener listener;

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        this.listener.onSlotUpdate(handler, slotId, stack);
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
        // No op
    }

    public interface Listener {
        void onSlotUpdate(ScreenHandler handler, int slot, ItemStack stack);
    }
}
