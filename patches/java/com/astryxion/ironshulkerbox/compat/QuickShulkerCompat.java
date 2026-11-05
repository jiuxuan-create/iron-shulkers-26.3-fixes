package com.astryxion.ironshulkerbox.compat;

import com.astryxion.ironshulkerbox.common.block.AbstractIronShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.CopperShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.CrystalShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.DiamondShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.GoldShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.IronShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.IronShulkerBoxesTypes;
import com.astryxion.ironshulkerbox.common.block.ObsidianShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.inventory.IronShulkerBoxMenu;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.RegisterQuickShulker;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * Optional bridge that teaches Quick Shulker about the Iron Shulker Boxes.
 *
 * This class is only ever loaded through Quick Shulker's own quickshulker entrypoint,
 * so it never has to be present when Quick Shulker is not installed.
 */
public final class QuickShulkerCompat implements RegisterQuickShulker {

    @Override
    public void registerProviders() {
        new QuickOpenableRegistry.Builder()
                .setItem(
                        IronShulkerBoxBlock.class,
                        GoldShulkerBoxBlock.class,
                        DiamondShulkerBoxBlock.class,
                        CrystalShulkerBoxBlock.class,
                        CopperShulkerBoxBlock.class,
                        ObsidianShulkerBoxBlock.class)
                .supportsBundleing(Boolean.TRUE)
                .canOpenInHand(true)
                .getSound(QuickShulkerCompat::soundFor)
                .getBundleInv(QuickShulkerCompat::inventoryFor)
                .canBundleInsertItem(QuickShulkerCompat::canInsert)
                .setOpenAction(QuickShulkerCompat::open)
                .register();
    }

    private static SoundEvent soundFor(ItemStack stack, Boolean opening) {
        return Boolean.TRUE.equals(opening) ? SoundEvents.SHULKER_BOX_OPEN : SoundEvents.SHULKER_BOX_CLOSE;
    }

    private static Container inventoryFor(Player player, ItemStack stack) {
        return new ItemStackContainer(stack, AbstractIronShulkerBoxBlock.getTypeFromItem(stack.getItem()).size);
    }

    private static boolean canInsert(Player player, Container container, ItemStack boxStack, ItemStack insertStack) {
        if (insertStack.isEmpty()) {
            return false;
        }
        Block block = Block.byItem(insertStack.getItem());
        return !(block instanceof AbstractIronShulkerBoxBlock) && !(block instanceof ShulkerBoxBlock);
    }

    private static void open(Player player, ItemStack stack) {
        IronShulkerBoxesTypes type = AbstractIronShulkerBoxBlock.getTypeFromItem(stack.getItem());
        Container container = new ItemStackContainer(stack, type.size);
        Component title = stack.getHoverName();
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> menuFor(type, id, inventory, container), title));
    }

    private static AbstractContainerMenu menuFor(IronShulkerBoxesTypes type, int id, Inventory inventory, Container container) {
        return switch (type) {
            case IRON -> IronShulkerBoxMenu.createIronContainer(id, inventory, container);
            case GOLD -> IronShulkerBoxMenu.createGoldContainer(id, inventory, container);
            case DIAMOND -> IronShulkerBoxMenu.createDiamondContainer(id, inventory, container);
            case CRYSTAL -> IronShulkerBoxMenu.createCrystalContainer(id, inventory, container);
            case COPPER -> IronShulkerBoxMenu.createCopperContainer(id, inventory, container);
            case OBSIDIAN -> IronShulkerBoxMenu.createObsidianContainer(id, inventory, container);
            case VANILLA -> IronShulkerBoxMenu.createIronContainer(id, inventory, container);
        };
    }

    /**
     * A SimpleContainer of the right size that reads its contents from, and writes them
     * back into, the minecraft:container component of the shulker box item stack.
     */
    public static final class ItemStackContainer extends SimpleContainer {

        private final ItemStack stack;

        public ItemStackContainer(ItemStack stack, int size) {
            super(size);
            this.stack = stack;
            NonNullList<ItemStack> loaded = NonNullList.withSize(size, ItemStack.EMPTY);
            stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(loaded);
            for (int i = 0; i < size; i++) {
                super.setItem(i, loaded.get(i));
            }
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            Block block = Block.byItem(stack.getItem());
            return !(block instanceof AbstractIronShulkerBoxBlock) && !(block instanceof ShulkerBoxBlock);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (this.stack != null) {
                this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
            }
        }
    }
}
