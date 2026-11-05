package com.astryxion.ironshulkerbox.compat;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.PreviewType;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import com.misterpemodder.shulkerboxtooltip.api.renderer.RenderContext;
import com.misterpemodder.shulkerboxtooltip.impl.renderer.RenderContextImpl;
import net.minecraft.client.Minecraft;

/**
 * Wraps ShulkerBoxTooltip's own renderer and scales the whole preview panel down when a
 * box is too large for the current GUI (e.g. the 108-slot Obsidian/Crystal/Diamond boxes).
 *
 * The scale is derived from the delegate's current size on every query, so reported size
 * and the rendered panel always agree, whatever preview type (COMPACT / FULL) is active.
 */
public final class ScaledPreviewRenderer implements PreviewRenderer {
    public static final ScaledPreviewRenderer INSTANCE = new ScaledPreviewRenderer();
    private static final int SCREEN_MARGIN = 12;
    private static final float MIN_SCALE = 0.25F;
    private static final int FALLBACK_GUI_HEIGHT = 240;
    private static final int FALLBACK_GUI_WIDTH = 320;

    private final PreviewRenderer delegate = PreviewRenderer.getModRendererInstance();

    private ScaledPreviewRenderer() {
    }

    private float currentScale() {
        int baseHeight = this.delegate.getHeight();
        int baseWidth = this.delegate.getWidth();
        if (baseHeight <= 0 || baseWidth <= 0) {
            return 1.0F;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int guiHeight = minecraft == null || minecraft.getWindow() == null
                ? FALLBACK_GUI_HEIGHT
                : minecraft.getWindow().getGuiScaledHeight();
        int guiWidth = minecraft == null || minecraft.getWindow() == null
                ? FALLBACK_GUI_WIDTH
                : minecraft.getWindow().getGuiScaledWidth();
        float scale = 1.0F;
        int availableHeight = Math.max(48, guiHeight - SCREEN_MARGIN);
        if (baseHeight > availableHeight) {
            scale = Math.min(scale, (float) availableHeight / (float) baseHeight);
        }
        int availableWidth = Math.max(48, guiWidth - SCREEN_MARGIN);
        if (baseWidth > availableWidth) {
            scale = Math.min(scale, (float) availableWidth / (float) baseWidth);
        }
        return Math.max(MIN_SCALE, scale);
    }

    @Override
    public int getWidth() {
        return Math.max(1, Math.round(this.delegate.getWidth() * currentScale()));
    }

    @Override
    public int getHeight() {
        return Math.max(1, Math.round(this.delegate.getHeight() * currentScale()));
    }

    @Override
    public void setPreview(PreviewContext ctx, PreviewProvider provider) {
        this.delegate.setPreview(ctx, provider);
    }

    @Override
    public void setPreviewType(PreviewType type) {
        this.delegate.setPreviewType(type);
    }

    @Override
    public void draw(RenderContext ctx) {
        float s = currentScale();
        if (s >= 0.999F) {
            this.delegate.draw(ctx);
            return;
        }
        int x = ctx.x();
        int y = ctx.y();
        var pose = ctx.graphics().pose();
        pose.pushMatrix();
        pose.translate((float) x, (float) y);
        pose.scale(s, s);
        pose.translate((float) -x, (float) -y);
        int mouseX = x + Math.round((ctx.mouseX() - x) / s);
        int mouseY = y + Math.round((ctx.mouseY() - y) / s);
        this.delegate.draw(new RenderContextImpl(x, y, ctx.viewportWidth(), ctx.viewportHeight(),
                ctx.graphics(), ctx.font(), mouseX, mouseY, ctx.tooltipTopX(), ctx.tooltipTopY()));
        pose.popMatrix();
    }
}
