package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.MilitiaAcquireTargetVoice;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 当实现 {@link MilitiaAcquireTargetVoice} 的 {@link Mob} 通过 {@link Mob#setTarget(LivingEntity)} 得到新的非空目标时触发语音。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MilitiaAcquireTargetVoiceEvents {

    private MilitiaAcquireTargetVoiceEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getTargetType() != LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof MilitiaAcquireTargetVoice voice) || !(entity instanceof Mob mob)) {
            return;
        }
        if (mob.level().isClientSide()) {
            return;
        }
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget == null) {
            return;
        }
        LivingEntity previous = mob.getTarget();
        if (newTarget == previous) {
            return;
        }
        voice.playAcquireTargetCombatVoice(mob, newTarget, previous);
    }
}
