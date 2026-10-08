package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel;

/**
 * 对掏天花板走位三大类：基础 / 衔接 / 技能。
 * <p>编号见 {@link CeilingDuelManeuverSkills}（百位 1/2/3 → 1xx / 2xx / 3xx）。
 */
public enum CeilingDuelManeuverPhase {
    /** 基础（100–199）。 */
    BASE,
    /** 衔接（200–299）。 */
    BRIDGE,
    /** 技能（300–399；300 为空技能不进本态）。 */
    SKILL
}
