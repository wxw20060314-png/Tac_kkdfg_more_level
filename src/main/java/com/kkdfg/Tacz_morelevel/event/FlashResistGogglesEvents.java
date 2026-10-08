package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.plugin.FlashResistGogglesItem;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 每 10 tick：按已装防闪光护目镜之和缩短 {@link ModMobEffects#FLASHED} 剩余时长。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FlashResistGogglesEvents {

    public static final int TICK_INTERVAL = 10;

    private FlashResistGogglesEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity.tickCount % TICK_INTERVAL != 0) {
            return;
        }
        MobEffectInstance flashed = entity.getEffect(ModMobEffects.FLASHED.get());
        if (flashed == null) {
            return;
        }
        int reduce = FlashResistGogglesItem.sumReduceTicks(entity);
        if (reduce <= 0) {
            return;
        }
        int left = flashed.getDuration() - reduce;
        if (left <= 0) {
            entity.removeEffect(ModMobEffects.FLASHED.get());
            return;
        }
        entity.removeEffect(ModMobEffects.FLASHED.get());
        entity.addEffect(
                new MobEffectInstance(
                        ModMobEffects.FLASHED.get(),
                        left,
                        flashed.getAmplifier(),
                        flashed.isAmbient(),
                        flashed.isVisible(),
                        flashed.showIcon()));
    }
}
