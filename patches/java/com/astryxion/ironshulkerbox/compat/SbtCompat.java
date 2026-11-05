package com.astryxion.ironshulkerbox.compat;

import com.astryxion.ironshulkerbox.common.item.IronShulkerBoxBlockItem;
import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Registered through ShulkerBoxTooltip's own shulkerboxtooltip entrypoint, so this class
 * is only ever loaded when ShulkerBoxTooltip is installed.
 */
public class SbtCompat implements ShulkerBoxTooltipApi {
    private static final IronBoxPreviewProvider PROVIDER = new IronBoxPreviewProvider();

    @Override
    public void registerProviders(PreviewProviderRegistry registry) {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IronShulkerBoxBlockItem) {
                items.add(item);
            }
        }
        if (!items.isEmpty()) {
            registry.register(Identifier.fromNamespaceAndPath("ironshulkerbox", "iron_shulker_boxes"), PROVIDER, items);
        }
    }
}
