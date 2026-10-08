package com.kkdfg.Tacz_morelevel.client.hud;

import com.kkdfg.Tacz_morelevel.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Shared fuse bar blit — colors come from the PNGs, not {@code setColor} RGB tint. */
final class FuseHudRenderer {

    static final int FRAME_WIDTH = 102;
    static final int FRAME_HEIGHT = 12;
    static final int BAR_WIDTH = 100;
    static final int BAR_HEIGHT = 10;
    static final int BAR_INSET_X = (FRAME_WIDTH - BAR_WIDTH) / 2;
    static final int BAR_INSET_Y = (FRAME_HEIGHT - BAR_HEIGHT) / 2;

    private FuseHudRenderer() {}

    static void render(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            float elapsed,
            ResourceLocation frame,
            ResourceLocation bar) {
        int frameX = (screenWidth - FRAME_WIDTH) / 2;
        int frameY = (int) (screenHeight * Config.grenadeFuseHudYFractionFromTop);

        int clipX = frameX + BAR_INSET_X;
        int clipY = frameY + BAR_INSET_Y;
        int barX = clipX - (int) elapsed;

        float alpha = Config.grenadeFuseHudAlpha;
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.enableScissor(clipX, clipY, clipX + BAR_WIDTH, clipY + BAR_HEIGHT);
        graphics.blit(bar, barX, clipY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        graphics.disableScissor();
        graphics.blit(frame, frameX, frameY, 0, 0, FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
