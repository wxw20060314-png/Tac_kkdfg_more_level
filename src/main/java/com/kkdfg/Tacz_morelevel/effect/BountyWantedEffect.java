package com.kkdfg.Tacz_morelevel.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 悬赏通缉：等级由 {@link MobEffectInstance#getAmplifier()} + 1 表示。 */
public final class BountyWantedEffect extends MobEffect {

    public BountyWantedEffect() {
        super(MobEffectCategory.NEUTRAL, 0x4A4A4A);
    }
}
