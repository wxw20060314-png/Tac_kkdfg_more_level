package com.kkdfg.Tacz_morelevel.client.hud;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 玩家持有闪光效果时全屏白屏；透明度随剩余时间衰减（末约 40 tick 内线性淡出）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FlashedScreenOverlay {

    private static final int FADE_TICKS = 40;

    private FlashedScreenOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        MobEffectInstance inst = mc.player.getEffect(ModMobEffects.FLASHED.get());
        if (inst == null) {
            return;
        }
        float alpha = Mth.clamp(inst.getDuration() / (float) FADE_TICKS, 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int color = (a << 24) | 0x00FFFFFF;
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        g.fill(0, 0, w, h, color);
    }
}
