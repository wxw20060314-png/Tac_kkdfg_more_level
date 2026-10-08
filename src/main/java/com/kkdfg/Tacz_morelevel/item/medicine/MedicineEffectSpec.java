package com.kkdfg.Tacz_morelevel.item.medicine;

import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffect;

/**
 * 药品效果条目：效果、持续时间、等级、概率与生效阶段。
 */
public record MedicineEffectSpec(
        Supplier<MobEffect> effect,
        int durationTicks,
        int amplifier,
        float applyChance,
        EffectPhase phase,
        boolean showParticles) {

    public enum EffectPhase {
        /** 读条正常结束后生效。 */
        ON_FINISH,
        /** 读条过程中每次 channel 脉冲后生效。 */
        ON_CHANNEL_TICK,
        /** 读条结束且本次用药实际触发过回血时生效。 */
        ON_FINISH_IF_HEALED
    }

    public MedicineEffectSpec {
        applyChance = Math.max(0F, Math.min(1F, applyChance));
    }

    public static MedicineEffectSpec of(
            Supplier<MobEffect> effect, int durationTicks, int amplifier, EffectPhase phase) {
        return new MedicineEffectSpec(effect, durationTicks, amplifier, 1F, phase, true);
    }

    public static MedicineEffectSpec of(
            Supplier<MobEffect> effect,
            int durationTicks,
            int amplifier,
            EffectPhase phase,
            boolean showParticles) {
        return new MedicineEffectSpec(effect, durationTicks, amplifier, 1F, phase, showParticles);
    }

    public static MedicineEffectSpec of(
            Supplier<MobEffect> effect,
            int durationTicks,
            int amplifier,
            float applyChance,
            EffectPhase phase) {
        return new MedicineEffectSpec(effect, durationTicks, amplifier, applyChance, phase, true);
    }

    public static MedicineEffectSpec of(
            Supplier<MobEffect> effect,
            int durationTicks,
            int amplifier,
            float applyChance,
            EffectPhase phase,
            boolean showParticles) {
        return new MedicineEffectSpec(
                effect, durationTicks, amplifier, applyChance, phase, showParticles);
    }
}
