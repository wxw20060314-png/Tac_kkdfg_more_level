package com.kkdfg.Tacz_morelevel.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

/** 已驯服同伴的 AI 与阵营查询。 */
public interface CompanionMob extends OwnableEntity, CompanionTameable {

    boolean isOrderedToSit();

    void setOrderedToSit(boolean orderedToSit);

    boolean wantsToAttack(LivingEntity target, LivingEntity owner);
}
