package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 去掉动力机甲物品 tooltip 中的创造模式页签名（「TaCZ 更多等级」）。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PowerMechItemTooltipEvents {

    private static final Component MAIN_TAB = Component.translatable("itemGroup.tacz_morelevel.main");

    private PowerMechItemTooltipEvents() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof PowerMechBulletproofArmorItem)) {
            return;
        }
        event.getToolTip()
                .removeIf(
                        line ->
                                line.getContents().equals(MAIN_TAB.getContents())
                                        || line.getString().equals(MAIN_TAB.getString()));
    }
}
