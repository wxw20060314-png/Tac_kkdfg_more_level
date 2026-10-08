package com.kkdfg.Tacz_morelevel.armor.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 盔甲传说技能：适用于任意 {@link LivingEntity}（玩家、NPC、怪物等）。
 * 玩家按键经网络包调用 {@link ArmorLegendarySkillRegistry}；AI / 状态机可直接调用 Registry 或套件专用 API。
 */
public interface ArmorLegendarySkill {

    ResourceLocation id();

    /** 服务端：该实体是否满足本技能固有条件（不含全局 Gate / 事件 / 冷却）。 */
    boolean canActivate(LivingEntity entity);

    /** 服务端：执行技能；返回是否成功。 */
    boolean activate(LivingEntity entity);
}
