package com.astryxion.ironshulkerbox.compat;

import com.astryxion.ironshulkerbox.common.block.AbstractIronShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.IronShulkerBoxesTypes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side prediction of QuickShulker's "insert item into a stored shulker box" click.
 *
 * QuickShulker only runs this step on the client for vanilla shulker boxes, so without
 * this helper an Iron Shulker Box click falls through to the vanilla pickup on the
 * client and the box is visibly yanked onto the cursor for one tick.
 */
public final class InsertPrediction {
    private InsertPrediction() {
    }

    public static boolean insertIntoBox(Player player, ItemStack boxStack, ItemStack source, Slot slot) {
        if (boxStack.isEmpty() || source.isEmpty()) {
            return false;
        }
        IronShulkerBoxesTypes type = AbstractIronShulkerBoxBlock.getTypeFromItem(boxStack.getItem());
        if (type == null || type.size <= 0) {
            return false;
        }
        if (slot != null && !slot.mayPickup(player)) {
            return false;
        }
        BoxContainer container = new BoxContainer(boxStack, type.size);
        int moved = insert(container, source);
        if (moved <= 0) {
            return false;
        }
        if (slot != null) {
            slot.safeTake(moved, moved, player);
        } else {
            source.shrink(moved);
        }
        return true;
    }

    static int insert(Container container, ItemStack source) {
        int moved = 0;
        int remaining = source.getCount();
        int size = container.getContainerSize();

        for (int i = 0; i < size && remaining > 0; i++) {
            ItemStack existing = container.getItem(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, source)) {
                continue;
            }
            int limit = Math.min(existing.getMaxStackSize(), container.getMaxStackSize());
            int space = limit - existing.getCount();
            if (space <= 0) {
                continue;
            }
            int amount = Math.min(space, remaining);
            container.setItem(i, existing.copyWithCount(existing.getCount() + amount));
            moved += amount;
            remaining -= amount;
        }

        for (int i = 0; i < size && remaining > 0; i++) {
            if (!container.getItem(i).isEmpty() || !container.canPlaceItem(i, source)) {
                continue;
            }
            int limit = Math.min(source.getMaxStackSize(), container.getMaxStackSize());
            int amount = Math.min(limit, remaining);
            container.setItem(i, source.copyWithCount(amount));
            moved += amount;
            remaining -= amount;
        }

        return moved;
    }
}
