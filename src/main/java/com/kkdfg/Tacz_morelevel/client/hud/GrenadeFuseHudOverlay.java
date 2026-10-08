package com.kkdfg.Tacz_morelevel.client.hud;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.event.GrenadeControlEvents;
import com.kkdfg.Tacz_morelevel.item.custom.FlashbangItem;
import com.kkdfg.Tacz_morelevel.item.custom.GrenadeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 破片手雷捏雷引信 HUD。贴图：{@code textures/entity/grenade/fuse_*.png}。
 * 闪光弹见 {@link FlashbangFuseHudOverlay}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GrenadeFuseHudOverlay {

    private static final ResourceLocation FUSE_FRAME =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/entity/grenade/fuse_frame.png");
    private static final ResourceLocation FUSE_BAR =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/entity/grenade/fuse_bar.png");

    public static final int FRAME_WIDTH = FuseHudRenderer.FRAME_WIDTH;
    public static final int FRAME_HEIGHT = FuseHudRenderer.FRAME_HEIGHT;
    public static final int BAR_WIDTH = FuseHudRenderer.BAR_WIDTH;
    public static final int BAR_HEIGHT = FuseHudRenderer.BAR_HEIGHT;

    private GrenadeFuseHudOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (!GrenadeControlEvents.isClientPriming()) {
            return;
        }
        if (!isHoldingFragmentGrenade(mc.player)) {
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

    private static boolean isHoldingFragmentGrenade(Player player) {
        return isFragment(player.getMainHandItem()) || isFragment(player.getOffhandItem());
    }

    private static boolean isFragment(ItemStack stack) {
        return stack.getItem() instanceof GrenadeItem && !(stack.getItem() instanceof FlashbangItem);
    }
}
