package com.kkdfg.Tacz_morelevel.api;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 民兵等生物在获得「与先前不同」的敌对 {@link Mob#getTarget()} 时播放反应音。
 *
 * <p>服务端在 {@link Mob#setTarget(LivingEntity)} 触发前派发
 * {@link net.minecraftforge.event.entity.living.LivingChangeTargetEvent}，
 * {@link com.kkdfg.Tacz_morelevel.event.MilitiaAcquireTargetVoiceEvents} 仅在
 * {@code MOB_TARGET}、新目标非空且与当前 {@link Mob#getTarget()} 不同时调用本接口。
 *
 * <p>默认播放 {@link #getAcquireTargetPlaceholderSound()}；子类型可覆写占位音效或整段
 * {@link #playAcquireTargetCombatVoice(Mob, LivingEntity, LivingEntity)}。
 */
public interface MilitiaAcquireTargetVoice {

    /**
     * 仅在服务端、且事件未取消、新目标非空并与旧目标不同时由 {@code MilitiaAcquireTargetVoiceEvents} 调用。
     */
    default void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        SoundEvent sound = this.getAcquireTargetPlaceholderSound();
        self.playSound(sound, 1.0F, self.getVoicePitch());
    }

    /** 占位音效，可改为注册表中的自定义 {@link SoundEvent}。 */
    default SoundEvent getAcquireTargetPlaceholderSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }
}
