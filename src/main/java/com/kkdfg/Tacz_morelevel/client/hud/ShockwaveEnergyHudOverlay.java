package com.kkdfg.Tacz_morelevel.client.hud;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveEnergyState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 冲击波能量 HUD（从动力机甲飞行能量条复制，独立贴图路径便于后改）。
 * 仅全套冲击波时显示。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ShockwaveEnergyHudOverlay {

    public static final int FRAME_WIDTH = 9;
    public static final int FRAME_HEIGHT = 42;
    public static final int BAR_WIDTH = 7;
    public static final int BAR_HEIGHT = 40;
    public static final int BAR_INSET_X = (FRAME_WIDTH - BAR_WIDTH) / 2;
    public static final int BAR_INSET_Y = (FRAME_HEIGHT - BAR_HEIGHT) / 2;

    public static int marginRight = 8;
    public static float anchorYFraction = 0.5f;
    public static float alpha = 1.0f;

    private static final ResourceLocation ENERGY_FRAME =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "textures/gui/shockwave/energy_frame.png");
    private static final ResourceLocation ENERGY_BAR =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "textures/gui/shockwave/energy_bar.png");

    private ShockwaveEnergyHudOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (!ShockwaveEnergyState.hasFullSet(mc.player)) {
            return;
        }
        render(
                event.getGuiGraphics(),
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                ShockwaveEnergyState.getEnergyRatio(mc.player));
    }

    public static void render(
            GuiGraphics graphics, int screenWidth, int screenHeight, float energyRatio) {
        int frameX = screenWidth - marginRight - FRAME_WIDTH;
        int frameY = (int) (screenHeight * anchorYFraction) - FRAME_HEIGHT / 2;

        int clipX = frameX + BAR_INSET_X;
        int clipY = frameY + BAR_INSET_Y;
        int fillHeight = Mth.ceil(BAR_HEIGHT * Mth.clamp(energyRatio, 0.0f, 1.0f));

        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        if (fillHeight > 0) {
            int fillTop = clipY + BAR_HEIGHT - fillHeight;
            graphics.enableScissor(clipX, fillTop, clipX + BAR_WIDTH, clipY + BAR_HEIGHT);
            graphics.blit(
                    ENERGY_BAR,
                    clipX,
                    clipY,
                    0,
                    0,
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    BAR_WIDTH,
                    BAR_HEIGHT);
            graphics.disableScissor();
        }
        graphics.blit(
                ENERGY_FRAME,
                frameX,
                frameY,
                0,
                0,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                FRAME_WIDTH,
                FRAME_HEIGHT);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
