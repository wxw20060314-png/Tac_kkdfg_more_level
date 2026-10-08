package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.client.gui.companion.CompanionInventoryScreen;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.menu.ModMenus;
import com.kkdfg.Tacz_morelevel.entity.client.companion.BeautyBodyguardRenderer;
import com.kkdfg.Tacz_morelevel.entity.client.companion.DemonLordBodyguardRenderer;
import com.kkdfg.Tacz_morelevel.entity.client.companion.EliteBodyguardRenderer;
import com.kkdfg.Tacz_morelevel.entity.client.companion.StandardBodyguardRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CompanionClientSetup {

    private CompanionClientSetup() {}

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BEAUTY_BODYGUARD.get(), BeautyBodyguardRenderer::new);
        event.registerEntityRenderer(ModEntities.STANDARD_BODYGUARD.get(), StandardBodyguardRenderer::new);
        event.registerEntityRenderer(ModEntities.ELITE_BODYGUARD.get(), EliteBodyguardRenderer::new);
        event.registerEntityRenderer(ModEntities.DEMON_LORD_BODYGUARD.get(), DemonLordBodyguardRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                        MenuScreens.register(
                                ModMenus.COMPANION_INVENTORY_MENU.get(),
                                CompanionInventoryScreen::new));
    }
}
