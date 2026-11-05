package com.astryxion.ironshulkerbox.compat;

import com.astryxion.ironshulkerbox.common.block.AbstractIronShulkerBoxBlock;
import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.color.ColorKey;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Preview provider that knows the real inventory size of every Iron Shulker Box tier
 * (vanilla shulker boxes and SBT's generic container fallback are hard-coded to 27 slots).
 */
public class IronBoxPreviewProvider implements PreviewProvider {
    /** Fallback used when a tier reports no usable column count. */
    private static final int FALLBACK_ROW_SIZE = 9;

    @Override
    public boolean shouldDisplay(PreviewContext ctx) {
        for (ItemStack stack : getInventory(ctx)) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getInventory(PreviewContext ctx) {
        NonNullList<ItemStack> items = NonNullList.withSize(getInventoryMaxSize(ctx), ItemStack.EMPTY);
        if (ctx.registryLookup() != null) {
            ctx.stack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        }
        return items;
    }

    @Override
    public int getInventoryMaxSize(PreviewContext ctx) {
        int size = AbstractIronShulkerBoxBlock.getTypeFromItem(ctx.stack().getItem()).size;
        return Math.max(1, size);
    }

    @Override
    public int getMaxRowSize(PreviewContext ctx) {
        return rowSize(ctx);
    }

    @Override
    public int getCompactMaxRowSize(PreviewContext ctx) {
        return rowSize(ctx);
    }

    /**
     * Mirrors the column count the block actually uses in its own GUI (9 for the small
     * tiers, 12 for the 108 slot tiers), so the preview has the same shape as the opened
     * box and the whole panel stays on screen.
     */
    private static int rowSize(PreviewContext ctx) {
        int rowLength = AbstractIronShulkerBoxBlock.getTypeFromItem(ctx.stack().getItem()).rowLength;
        return rowLength > 0 ? rowLength : FALLBACK_ROW_SIZE;
    }

    @Override
    public ColorKey getWindowColorKey(PreviewContext ctx) {
        return ColorKey.SHULKER_BOX;
    }

    @Override
    public PreviewRenderer getRenderer() {
        return ScaledPreviewRenderer.INSTANCE;
    }
}
