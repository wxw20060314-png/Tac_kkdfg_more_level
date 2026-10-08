package com.kkdfg.Tacz_morelevel.armor.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 传说技能全局附加检测层：在技能 {@link ArmorLegendarySkill#canActivate} 之前执行。
 * 用于状态机、冷却、场景限制等扩展；注册见 {@link ArmorLegendarySkillRegistry#registerGate}。
 */
@FunctionalInterface
public interface ArmorLegendarySkillGate {

    /**
     * @return {@code false} 则 {@link ArmorLegendarySkillRegistry#canActivate} / {@link ArmorLegendarySkillRegistry#tryActivate} 直接失败
     */
    boolean mayActivate(LivingEntity entity, ResourceLocation skillId, ArmorLegendarySkill skill);
}
