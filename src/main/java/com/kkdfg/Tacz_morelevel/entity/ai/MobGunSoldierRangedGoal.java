package com.kkdfg.Tacz_morelevel.entity.ai;

/** 士兵持枪远程 goal 与「穿透透明方块视线」版共用，供 {@code reassessWeaponGoal} 调点射间隔。 */
public interface MobGunSoldierRangedGoal {
    void setMinAttackInterval(int attackCooldown);
}
