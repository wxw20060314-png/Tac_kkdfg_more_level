package com.kkdfg.Tacz_morelevel.config;

import java.util.EnumSet;

/** 单位 toml {@code combatGoals} 中实际写出的 Goal 字段分组。 */
public enum CombatGoalSection {
    SNEAK,
    CRAWL,
    JUMP;

    public static EnumSet<CombatGoalSection> all() {
        return EnumSet.allOf(CombatGoalSection.class);
    }

    public static EnumSet<CombatGoalSection> none() {
        return EnumSet.noneOf(CombatGoalSection.class);
    }

    /** 潜行 + 跳，无趴（史莱姆 / 轰炸视界等）。 */
    public static EnumSet<CombatGoalSection> sneakAndJump() {
        return EnumSet.of(SNEAK, JUMP);
    }

    /** 仅跳（元帅）。 */
    public static EnumSet<CombatGoalSection> jumpOnly() {
        return EnumSet.of(JUMP);
    }
}
