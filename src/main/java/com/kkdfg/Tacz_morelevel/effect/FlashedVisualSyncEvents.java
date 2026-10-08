package com.kkdfg.Tacz_morelevel.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.network.packet.FlashedVisualSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 服务端识别 {@link ModMobEffects#FLASHED}，向跟踪该实体的客户端同步头顶特效标记。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FlashedVisualSyncEvents {

    private FlashedVisualSyncEvents() {}

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        MobEffectInstance inst = event.getEffectInstance();
        if (inst.getEffect() != ModMobEffects.FLASHED.get()) {
            return;
        }
        FlashedVisualSyncPacket.sendToTracking(entity, inst.getDuration());
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (event.getEffect() != ModMobEffects.FLASHED.get()) {
            return;
        }
        FlashedVisualSyncPacket.sendToTracking(entity, 0);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        MobEffectInstance inst = event.getEffectInstance();
        if (inst == null || inst.getEffect() != ModMobEffects.FLASHED.get()) {
            return;
        }
        FlashedVisualSyncPacket.sendToTracking(entity, 0);
    }

    /** 新观察者开始跟踪时补发当前闪光状态。 */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer viewer)) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity living)) {
            return;
        }
        MobEffectInstance inst = living.getEffect(ModMobEffects.FLASHED.get());
        if (inst == null) {
            return;
        }
        FlashedVisualSyncPacket.sendToPlayer(viewer, living, inst.getDuration());
    }
}
