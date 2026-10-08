package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.plugin.RadiationResistArmorPlateItem;
import com.kkdfg.Tacz_morelevel.effect.ComprehensiveImmunityEffect;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 每 {@link ComprehensiveImmunityEffect#PURGE_INTERVAL_TICKS} tick：盔甲/插件 + 综合免疫折算
 * &gt; 辐射等级 a（amplifier+1）则清除辐射。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RadiationResistArmorPlateEvents {

    private RadiationResistArmorPlateEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity.tickCount % ComprehensiveImmunityEffect.PURGE_INTERVAL_TICKS != 0) {
            return;
        }
        MobEffectInstance radiation = entity.getEffect(ModMobEffects.RADIATION.get());
        if (radiation == null) {
            return;
        }
        float resist = RadiationResistArmorPlateItem.totalResistance(entity);
        int radiationLevel = Math.max(1, radiation.getAmplifier() + 1);
        if (resist > radiationLevel) {
            entity.removeEffect(ModMobEffects.RADIATION.get());
        }
    }
}
