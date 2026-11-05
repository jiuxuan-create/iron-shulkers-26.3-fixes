package com.astryxion.ironshulkerbox.compat;

import com.astryxion.ironshulkerbox.common.block.AbstractIronShulkerBoxBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * A SimpleContainer view over the minecraft:container component of an Iron Shulker
 * Box item stack. Every mutation is written back to the stack, exactly like
 * QuickShulker's own item stack inventory.
 */
public class BoxContainer extends SimpleContainer {
    private final ItemStack stack;

    public BoxContainer(ItemStack stack, int size) {
        super(size);
        this.stack = stack;
        NonNullList<ItemStack> loaded = NonNullList.withSize(size, ItemStack.EMPTY);
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(loaded);
        for (int i = 0; i < size; i++) {
            super.setItem(i, loaded.get(i));
        }
    }

    public ItemStack getBackingStack() {
        return this.stack;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        Block block = Block.byItem(stack.getItem());
        return !(block instanceof AbstractIronShulkerBoxBlock) && !(block instanceof ShulkerBoxBlock);
    }
}
