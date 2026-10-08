package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionInventoryCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CompanionCapabilityEvents {

    public static final ResourceLocation COMPANION_INVENTORY_CAPABILITY =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "companion_inventory");

    private CompanionCapabilityEvents() {}

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof AbstractFriendlyCompanionEntity companion) {
            event.addCapability(
                    COMPANION_INVENTORY_CAPABILITY, new CompanionInventoryCapabilityProvider(companion));
        }
    }
}
