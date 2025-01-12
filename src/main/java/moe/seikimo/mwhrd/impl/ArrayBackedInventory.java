package moe.seikimo.mwhrd.impl;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Inventory implementation which is backed by an array list of item stacks.
 */
@RequiredArgsConstructor
public final class ArrayBackedInventory implements Inventory {
    private final List<ItemStack> backing;
    
    @Override
    public int size() {
        return this.backing.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backing.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.backing.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        var stack = this.backing.get(slot);
        var result = stack.split(amount);
        
        if (stack.isEmpty()) {
            this.backing.set(slot, ItemStack.EMPTY);
        }
        
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        var stack = this.backing.get(slot).copy();
        this.backing.set(slot, ItemStack.EMPTY);
        
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.backing.set(slot, stack);
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        Collections.fill(this.backing, ItemStack.EMPTY);
    }
}
