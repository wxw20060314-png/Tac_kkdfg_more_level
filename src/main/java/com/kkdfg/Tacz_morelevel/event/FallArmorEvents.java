package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.FallArmorReduction;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 摔落减伤：对 {@link DamageTypeTags#IS_FALL} 按盔甲槽累加（深渊 / 冲击波每件 25% 等），见 {@link FallArmorReduction}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FallArmorEvents {

    private FallArmorEvents() {}

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurtFall(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        if (!event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }
        float reduction = FallArmorReduction.total(target);
        if (reduction <= 0f) {
            return;
        }
        event.setAmount(event.getAmount() * (1f - reduction));
    }
}
