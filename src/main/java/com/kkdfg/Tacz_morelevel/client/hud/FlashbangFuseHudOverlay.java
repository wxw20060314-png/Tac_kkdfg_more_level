package com.kkdfg.Tacz_morelevel.client.hud;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.event.GrenadeControlEvents;
import com.kkdfg.Tacz_morelevel.item.custom.FlashbangItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 闪光弹捏雷引信 HUD。贴图：{@code textures/entity/flashbang/fuse_*.png}。
 * 破片手雷见 {@link GrenadeFuseHudOverlay}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FlashbangFuseHudOverlay {

    private static final ResourceLocation FUSE_FRAME =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/entity/flashbang/fuse_frame.png");
    private static final ResourceLocation FUSE_BAR =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/entity/flashbang/fuse_bar.png");

    private FlashbangFuseHudOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (!GrenadeControlEvents.isClientPriming()) {
            return;
        }
        if (!isHoldingFlashbang(mc.player)) {
            return;
        }
        long start = GrenadeControlEvents.getClientPrimeStartGameTime();
        if (start < 0L) {
            return;
        }
        float elapsed = mc.level.getGameTime() - start + event.getPartialTick();
        FuseHudRenderer.render(
                event.getGuiGraphics(),
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                elapsed,
                FUSE_FRAME,
                FUSE_BAR);
    }

    private static boolean isHoldingFlashbang(Player player) {
        return player.getMainHandItem().getItem() instanceof FlashbangItem
                || player.getOffhandItem().getItem() instanceof FlashbangItem;
    }
}
