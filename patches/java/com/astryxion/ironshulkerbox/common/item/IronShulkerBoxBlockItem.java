package com.astryxion.ironshulkerbox.common.item;

import com.astryxion.ironshulkerbox.common.block.AbstractIronShulkerBoxBlock;
import com.astryxion.ironshulkerbox.common.block.IronShulkerBoxesTypes;
import com.astryxion.ironshulkerbox.compat.InsertPrediction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public class IronShulkerBoxBlockItem extends BlockItem {
    protected IronShulkerBoxesTypes type;
    protected DyeColor color;

    public IronShulkerBoxBlockItem(Block block, Item.Properties properties, IronShulkerBoxesTypes type, DyeColor color) {
        super(block, properties);
        this.type = type;
        this.color = color;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(this.getBlock() instanceof AbstractIronShulkerBoxBlock);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (super.overrideOtherStackedOnMe(self, other, slot, action, player, access)) {
            return true;
        }
        if (!shouldPredict(player, action)) {
            return false;
        }
        return InsertPrediction.insertIntoBox(player, self, other, slot);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player) {
        if (super.overrideStackedOnOther(self, slot, action, player)) {
            return true;
        }
        if (!shouldPredict(player, action)) {
            return false;
        }
        return InsertPrediction.insertIntoBox(player, self, slot.getItem(), slot);
    }

    private static boolean shouldPredict(Player player, ClickAction action) {
        return action == ClickAction.SECONDARY
                && player.level().isClientSide()
                && FabricLoader.getInstance().isModLoaded("quickshulker");
    }
}
