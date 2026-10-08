package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 护航继承 {@link net.minecraft.world.entity.PathfinderMob}，不会像 {@link net.minecraft.world.entity.monster.Monster}
 * 那样在 {@code aiStep} 里调 {@link net.minecraft.world.entity.LivingEntity#updateSwingTime()}，客户端
 * {@code attackAnim} 不前进，近战挥臂不播放。仅在客户端为护航补这一帧逻辑。
 */
@OnlyIn(Dist.CLIENT)
public final class CompanionClientSwingAnim {

    private CompanionClientSwingAnim() {}

    /** 与 {@link net.minecraft.world.entity.LivingEntity#updateSwingTime()} 一致。 */
    public static void tick(AbstractFriendlyCompanionEntity entity) {
        entity.oAttackAnim = entity.attackAnim;
        int duration = swingDuration(entity);
        if (entity.swinging) {
            ++entity.swingTime;
            if (entity.swingTime >= duration) {
                entity.swingTime = 0;
                entity.swinging = false;
            }
        } else {
            entity.swingTime = 0;
        }
        entity.attackAnim = (float) entity.swingTime / (float) duration;
    }

    /** 与 {@link net.minecraft.world.entity.LivingEntity#getCurrentSwingDuration()} 一致（该方法为 private）。 */
    private static int swingDuration(AbstractFriendlyCompanionEntity entity) {
        if (MobEffectUtil.hasDigSpeed(entity)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        }
        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            return 6 + (1 + entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2;
        }
        return 6;
    }
}
