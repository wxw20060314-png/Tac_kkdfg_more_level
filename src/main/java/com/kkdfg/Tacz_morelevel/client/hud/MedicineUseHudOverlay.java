package com.kkdfg.Tacz_morelevel.client.hud;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.medicine.ChannelMedicineItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 药品读条 HUD：Action Bar 区域显示外框 + 填充条；标签与百分比绘制在条上方。
 * 贴图 {@code textures/hud/medicine_use_frame.png}（64×8）、
 * {@code textures/hud/medicine_use_bar.png}（62×6），1:1 绘制。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MedicineUseHudOverlay {

    public static final int FRAME_WIDTH = 64;
    public static final int FRAME_HEIGHT = 8;
    public static final int BAR_INSET_X = 1;
    public static final int BAR_INSET_Y = 1;
    public static final int BAR_WIDTH = 62;
    public static final int BAR_HEIGHT = 6;

    public static final int TEX_FRAME_WIDTH = 64;
    public static final int TEX_FRAME_HEIGHT = 8;
    public static final int TEX_BAR_WIDTH = 62;
    public static final int TEX_BAR_HEIGHT = 6;

    /** Action Bar 区域：条底边距屏幕底部的偏移（与原版字幕/Action Bar 接近）。 */
    private static final int BAR_BOTTOM_OFFSET = 55;
    private static final int TEXT_GAP = 2;

    private static final ResourceLocation USE_FRAME =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/hud/medicine_use_frame.png");
    private static final ResourceLocation USE_BAR =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/hud/medicine_use_bar.png");

    private MedicineUseHudOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (!mc.player.isUsingItem()) {
            return;
        }
        ItemStack stack = mc.player.getUseItem();
        if (!(stack.getItem() instanceof ChannelMedicineItem)) {
            return;
        }

        int total = stack.getUseDuration();
        if (total <= 0) {
            return;
        }
        float remaining = mc.player.getUseItemRemainingTicks() - event.getPartialTick();
        float progress = Mth.clamp(1.0F - remaining / (float) total, 0.0F, 1.0F);
        int percent = Math.min(100, Math.round(progress * 100.0F));

        render(
                event.getGuiGraphics(),
                mc.font,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                stack,
                progress,
                percent);
    }

    private static void render(
            GuiGraphics graphics,
            Font font,
            int screenWidth,
            int screenHeight,
            ItemStack stack,
            float progress,
            int percent) {
        int frameX = (screenWidth - FRAME_WIDTH) / 2;
        int frameY = screenHeight - BAR_BOTTOM_OFFSET - FRAME_HEIGHT;
        int textY = frameY - font.lineHeight - TEXT_GAP;

        Component label = Component.translatable("message.tacz_morelevel.medicine.use_label");
        String percentText = percent + "%";

        int labelColor = 0xFFFFFF;
        graphics.drawString(font, label, frameX, textY, labelColor, true);
        int percentWidth = font.width(percentText);
        graphics.drawString(
                font, percentText, frameX + FRAME_WIDTH - percentWidth, textY, labelColor, true);

        int clipX = frameX + BAR_INSET_X;
        int clipY = frameY + BAR_INSET_Y;
        int filledWidth = Mth.ceil(BAR_WIDTH * progress);

        if (filledWidth > 0) {
            graphics.enableScissor(clipX, clipY, clipX + filledWidth, clipY + BAR_HEIGHT);
            graphics.blit(
                    USE_BAR,
                    clipX,
                    clipY,
                    0,
                    0,
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    TEX_BAR_WIDTH,
                    TEX_BAR_HEIGHT);
            graphics.disableScissor();
        }

        graphics.blit(
                USE_FRAME,
                frameX,
                frameY,
                0,
                0,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                TEX_FRAME_WIDTH,
                TEX_FRAME_HEIGHT);
    }
}
