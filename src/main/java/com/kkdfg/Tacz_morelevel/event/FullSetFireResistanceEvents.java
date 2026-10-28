package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BlazeflameSeventhBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.ObsidianArmorItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 全套抗火：直接查 {@link LivingEntity} 四格盔甲（玩家与 NPC 均生效），
 * 不依赖盔甲 {@code inventoryTick}（Mob 穿戴时通常不会调用）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FullSetFireResistanceEvents {

    private FullSetFireResistanceEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        BlazeflameSeventhBulletproofArmorItem.tickFullSetFireResistance(entity);
        ObsidianArmorItem.tickFullSetFireResistance(entity);
    }
}
