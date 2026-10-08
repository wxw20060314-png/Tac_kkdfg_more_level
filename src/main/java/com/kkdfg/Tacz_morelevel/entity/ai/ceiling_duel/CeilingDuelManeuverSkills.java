package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel;

import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * 走位编号：百位为大类（1/2/3），后两位为小类（每类最多 100 个）。
 * <ul>
 *   <li>{@code 100–199}：基础（{@link #BASE}=100 经典枪走位）</li>
 *   <li>{@code 200–299}：衔接（{@link #BRIDGE}=200 空衔接兜底；{@link #CAT_STEP_JUMP}=201 猫步；
 *       {@link #INSTANT_CRAWL}=202 秒趴）</li>
 *   <li>{@code 300–399}：技能（{@link #EMPTY_SKILL}=300；{@link #LATERAL_YANK}=301 横拉+秒蹲；
 *       {@link #LATERAL_YANK_CRAWL}=302 横拉+低频秒趴；{@link #LATERAL_YANK_HOP}=303 横拉+秒蹲+小跳阀；
 *       {@link #STAGGER_STEP}=304 中距颠步；{@link #SLIDE_STEP}=305 中远滑步）</li>
 * </ul>
 * 各大类小类抽奖池互不混抽；BASE→技能时抽到空/未注册/准入失败则不转。
 * 技能结束→衔接：从已注册实体衔接（201+）中抽；池空则 {@link #BRIDGE} 空衔接。
 * 持续时间见 {@link CeilingDuelManeuverTiming}（小类→大类回退 + ±偏移）。
 */
public final class CeilingDuelManeuverSkills {

    /** 大类：基础 / 衔接 / 技能（百位）。 */
    public static final int CATEGORY_BASE = 1;
    public static final int CATEGORY_BRIDGE = 2;
    public static final int CATEGORY_SKILL = 3;

    /** 基础：经典枪走位。 */
    public static final int BASE = 100;
    /** 衔接：空衔接（仅过渡 / 池空兜底，不进实体衔接抽奖池）。 */
    public static final int BRIDGE = 200;
    /** 实体衔接编号起点（须 ≥201 注册）。 */
    public static final int BRIDGE_FIRST = 201;
    /**
     * 衔接：猫步反跳（{@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id201.CeilingDuelCatStepJumpGoal}）。
     */
    public static final int CAT_STEP_JUMP = 201;
    /**
     * 衔接：秒趴（{@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id202.CeilingDuelInstantCrawlGoal}）。
     */
    public static final int INSTANT_CRAWL = 202;
    /** 空技能：视为失败，不进入 SKILL，直接回基础。 */
    public static final int EMPTY_SKILL = 300;
    /** 实体技能编号起点（须 ≥301 注册）。 */
    public static final int SKILL_FIRST = 301;
    /**
     * 技能：左右横拉 + 秒蹲同通道（走位 {@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301.CeilingDuelLateralYankGoal}，
     * 蹲 {@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301.CeilingDuelLateralQuickCrouchGoal}）。
     */
    public static final int LATERAL_YANK = 301;
    /**
     * 技能：左右横拉（12–32）+ 低频秒趴同通道（走位 {@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id302.CeilingDuelLateralYankCrawlGoal}，
     * 趴 {@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id302.CeilingDuelLateralQuickCrawlGoal}）。
     */
    public static final int LATERAL_YANK_CRAWL = 302;
    /**
     * 技能：301 横拉+秒蹲 + 蹲后小跳阀（走位 {@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id303.CeilingDuelLateralYankHopGoal}，
     * 蹲跳 {@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id303.CeilingDuelLateralQuickCrouchHopGoal}）。
     */
    public static final int LATERAL_YANK_HOP = 303;
    /**
     * 技能：中距颠步（走→停蹲/趴→走；距&lt;16 进衔接）（{@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id304.CeilingDuelStaggerStepGoal}）。
     */
    public static final int STAGGER_STEP = 304;
    /**
     * 技能：中远滑步（极快秒蹲→趴冲量→中跳；距&lt;16 进衔接）（{@link
     * com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id305.CeilingDuelSlideStepGoal}）。
     */
    public static final int SLIDE_STEP = 305;

    /** @deprecated 使用 {@link #BASE}。 */
    @Deprecated
    public static final int CLASSIC_GUN_STRAFE = BASE;

    public static final int AGE_CAP = 1_000_000;

    @FunctionalInterface
    public interface SkillEntryPredicate {
        boolean test(CeilingDuelEntity boss);
    }

    public record SkillEntry(int id, int defaultDurationTicks, SkillEntryPredicate canEnter) {}

    /** 技能族实体技能（301–399）；不含 {@link #EMPTY_SKILL}。 */
    private static final Map<Integer, SkillEntry> SKILLS = new LinkedHashMap<>();
    /** 衔接族实体衔接（201–299）；不含 {@link #BRIDGE}。 */
    private static final Map<Integer, SkillEntry> BRIDGES = new LinkedHashMap<>();

    static {
        // 301–303：全距可进；权重见 skillPickWeight
        register(LATERAL_YANK, 50, boss -> true);
        CeilingDuelManeuverTiming.putVariant(
                LATERAL_YANK, CeilingDuelManeuverTiming.TimingSpec.of(36, 0.25F));

        register(LATERAL_YANK_CRAWL, 50, boss -> true);
        CeilingDuelManeuverTiming.putVariant(
                LATERAL_YANK_CRAWL, CeilingDuelManeuverTiming.TimingSpec.of(36, 0.25F));

        register(LATERAL_YANK_HOP, 50, boss -> true);
        CeilingDuelManeuverTiming.putVariant(
                LATERAL_YANK_HOP, CeilingDuelManeuverTiming.TimingSpec.of(36, 0.25F));

        register(STAGGER_STEP, 70, CeilingDuelEntity::isTargetInMidFarPaceRange);
        CeilingDuelManeuverTiming.putVariant(
                STAGGER_STEP, CeilingDuelManeuverTiming.TimingSpec.of(70, 0.25F));

        register(SLIDE_STEP, 70, CeilingDuelEntity::isTargetInMidFarPaceRange);
        CeilingDuelManeuverTiming.putVariant(
                SLIDE_STEP, CeilingDuelManeuverTiming.TimingSpec.of(70, 0.25F));

        registerBridge(CAT_STEP_JUMP, 20, boss -> true);
        CeilingDuelManeuverTiming.putVariant(
                CAT_STEP_JUMP, CeilingDuelManeuverTiming.TimingSpec.of(20, 0.0F));

        registerBridge(INSTANT_CRAWL, 20, boss -> true);
        CeilingDuelManeuverTiming.putVariant(
                INSTANT_CRAWL, CeilingDuelManeuverTiming.TimingSpec.of(20, 0.0F));
    }

    private CeilingDuelManeuverSkills() {}

    public static int categoryOf(int id) {
        return Math.floorDiv(id, 100);
    }

    public static int variantOf(int id) {
        return Math.floorMod(id, 100);
    }

    public static boolean isBaseFamily(int id) {
        return categoryOf(id) == CATEGORY_BASE;
    }

    public static boolean isBridgeFamily(int id) {
        return categoryOf(id) == CATEGORY_BRIDGE;
    }

    public static boolean isSkillFamily(int id) {
        return categoryOf(id) == CATEGORY_SKILL;
    }

    public static boolean isEmptySkill(int id) {
        return id == EMPTY_SKILL;
    }

    public static boolean isEmptyBridge(int id) {
        return id == BRIDGE;
    }

    /** 注册实体技能（必须为 301–399）。 */
    public static void register(int id, int defaultDurationTicks, SkillEntryPredicate canEnter) {
        if (!isSkillFamily(id) || id < SKILL_FIRST) {
            throw new IllegalArgumentException("Skill id must be in 301–399, got " + id);
        }
        SKILLS.put(id, new SkillEntry(id, defaultDurationTicks, canEnter));
    }

    /** 注册实体衔接（必须为 201–299）。 */
    public static void registerBridge(int id, int defaultDurationTicks, SkillEntryPredicate canEnter) {
        if (!isBridgeFamily(id) || id < BRIDGE_FIRST) {
            throw new IllegalArgumentException("Bridge id must be in 201–299, got " + id);
        }
        BRIDGES.put(id, new SkillEntry(id, defaultDurationTicks, canEnter));
    }

    public static boolean isRegisteredSkill(int skillId) {
        return SKILLS.containsKey(skillId);
    }

    public static boolean isRegisteredBridge(int bridgeId) {
        return BRIDGES.containsKey(bridgeId);
    }

    public static Set<Integer> registeredSkillIds() {
        return Collections.unmodifiableSet(SKILLS.keySet());
    }

    public static Set<Integer> registeredBridgeIds() {
        return Collections.unmodifiableSet(BRIDGES.keySet());
    }

    @Nullable
    public static SkillEntry getSkill(int skillId) {
        return SKILLS.get(skillId);
    }

    @Nullable
    public static SkillEntry getBridge(int bridgeId) {
        return BRIDGES.get(bridgeId);
    }

    public static boolean canEnterSkill(int skillId, CeilingDuelEntity boss) {
        if (isEmptySkill(skillId)) {
            return false;
        }
        SkillEntry entry = SKILLS.get(skillId);
        return entry != null && entry.canEnter().test(boss);
    }

    /** 横拉族（301–303）基准抽选权重。 */
    public static final int SKILL_PICK_BASE = 10;

    /**
     * 技能池抽选权重（统一入口）。
     * <ul>
     *   <li>301–303：固定 {@link #SKILL_PICK_BASE}</li>
     *   <li>304/305：{@link #midFarPacePickWeight(double)}</li>
     *   <li>其它：0</li>
     * </ul>
     */
    public static int skillPickWeight(int skillId, CeilingDuelEntity boss) {
        return skillPickWeight(skillId, boss.distanceToTarget());
    }

    /** @see #skillPickWeight(int, CeilingDuelEntity) */
    public static int skillPickWeight(int skillId, double distance) {
        if (skillId == LATERAL_YANK
                || skillId == LATERAL_YANK_CRAWL
                || skillId == LATERAL_YANK_HOP) {
            return SKILL_PICK_BASE;
        }
        if (skillId == STAGGER_STEP || skillId == SLIDE_STEP) {
            return midFarPacePickWeight(distance);
        }
        return 0;
    }

    /**
     * 304/305 距离档权重：16–30→14；30–40→40；40–48→18；≥48→4；&lt;16→0。
     * 边界：≤30 属略高档，≤40 属高档，≤48 属中低档。
     */
    public static int midFarPacePickWeight(double distance) {
        if (distance < 16.0D) {
            return 0;
        }
        if (distance <= 30.0D) {
            return 14;
        }
        if (distance <= 40.0D) {
            return 40;
        }
        if (distance <= 48.0D) {
            return 18;
        }
        return 4;
    }

    public static boolean canEnterBridge(int bridgeId, CeilingDuelEntity boss) {
        if (isEmptyBridge(bridgeId)) {
            return false;
        }
        SkillEntry entry = BRIDGES.get(bridgeId);
        return entry != null && entry.canEnter().test(boss);
    }

    public static int defaultDurationForSkill(int skillId) {
        return CeilingDuelManeuverTiming.resolveMaxTicks(skillId);
    }

    public static boolean isBase(int skillId) {
        return skillId == BASE;
    }

    public static boolean isBridge(int skillId) {
        return skillId == BRIDGE;
    }
}
