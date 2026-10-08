package com.kkdfg.Tacz_morelevel.api;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

/**
 * 跳跃能力表：全局档位 + 实体软窗口 {@code [minJumpTier, maxJumpTier]}（闭区间）。
 * 业务侧通过 {@link #defaultJumpPower()} / {@link #jumpPowerAt(int)} 读取，勿直接下标翻表。
 * <p>真正起跳力度由实体 {@link #currentJumpTier()} 决定；消费后归 {@link #defaultJumpTier()}。
 * 非法 min/max 时规范为 {@code [0, last]}；{@code respectMin=false} 可破软下沿（技能小跳）。
 * <p>寻路净空用 {@link #landVerticalPhysics(LivingEntity)}：重力读 Forge {@code ENTITY_GRAVITY}
 * （维度模组通常改此属性）；竖直衰减原版 travel 写死，默认 {@link #JUMP_DRAG}。
 */
public interface JumpController {

    /** 全局跳跃竖直初速档（升序）；索引 0..4。 */
    List<Float> JUMP_TIERS = List.of(0.42F, 0.52F, 0.62F, 0.72F, 0.82F);

    float VANILLA_JUMP = 0.42F;

    /** 原版 {@code WalkNodeEvaluator} 默认寻路跳高门槛（普通跳约 1.25×0.9）。 */
    double VANILLA_PATH_JUMP_HEIGHT = 1.125D;

    /**
     * 陆地竖直重力默认值（无实体 / 无属性时）。
     * 实际净空请用 {@link #resolveLandGravity(LivingEntity)}。
     */
    double JUMP_GRAVITY = 0.08D;
    /**
     * 陆地竖直速度衰减默认值（与 {@code LivingEntity#travel} 空中 {@code *0.98} 一致）。
     * 1.20.1 无维度字段；有实体时仍走 {@link #resolveLandDrag(LivingEntity)} 以便日后挂钩。
     */
    double JUMP_DRAG = 0.98D;
    /** 升空高度再乘此系数，略往下估。 */
    double JUMP_CLEARANCE_FACTOR = 0.9D;

    /** 重力≤0 时视为几乎不回落，给寻路一个很大可跨高。 */
    double ZERO_GRAVITY_CLEARANCE = 256.0D;

    /**
     * 与 {@code LivingEntity} 缓降重力修饰 UUID 一致；升空净空估算时剔除，避免落地态把 clearance 估矮。
     */
    UUID SLOW_FALLING_GRAVITY_ID = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA");

    /**
     * 各档寻路可跨高度（格）：按默认 g/μ 预计算；有实体时用 {@link #clearanceBlocksAtTier(int, LivingEntity)}。
     * 与 {@link #JUMP_TIERS} 下标对齐。
     */
    double[] PATH_CLEARANCE_BY_TIER = buildPathClearanceByTier();

    /** 陆地竖直运动参数（离散模型 {@code v←(v-g)μ}）。 */
    record LandVerticalPhysics(double gravity, double drag) {}
    /** 无药水 / 旧 {@code LOW}。 */
    int TIER_LOW = 0;
    /** 跳跃 I / 旧 {@code MID}。 */
    int TIER_MID = 1;
    /** 跳跃 II 偏高 / 旧 {@code HIGH_MID}。 */
    int TIER_HIGH_MID = 2;
    /** 跳跃 amp2 等价（0.72）。 */
    int TIER_AMP2 = 3;
    /** 跳跃 amp3 / 旧 {@code HIGH}。 */
    int TIER_HIGH = 4;

    /** 软窗口下沿（闭区间）。 */
    int minJumpTier();

    /** 软窗口上沿（闭区间），一般为能力最高档。 */
    int maxJumpTier();

    /** 下一次真正起跳（{@code getJumpPower}）读取的档。 */
    int currentJumpTier();

    /**
     * 设当前档。只夹到表下标 {@code [0, last]}，允许低于软 min（技能小跳）。
     * 力度在 {@code getJumpPower} 消费后归默认，勿在 {@code jump()} 后立刻 reset。
     */
    void setCurrentJumpTier(int tier);

    /** 默认档：软窗口上沿。子类可覆写。 */
    default int defaultJumpTier() {
        return this.normalizedWindow()[1];
    }

    default void resetJumpTierToDefault() {
        this.setCurrentJumpTier(this.defaultJumpTier());
    }

    /** 原版跳跃药水 amplifier → 档位；非法 amp 夹到 [0,3] 再 +1；无药水请直接用 TIER_LOW。 */
    static int tierFromJumpAmplifier(int amplifier) {
        int a = amplifier;
        if (a < 0) {
            a = 0;
        }
        if (a > 3) {
            a = 3;
        }
        return a + 1; // amp0→1 ... amp3→4
    }

    /** 将任意 basePower 映射到最近表档下标。 */
    static int nearestTier(float basePower) {
        if (JUMP_TIERS.isEmpty()) {
            return TIER_LOW;
        }
        int best = 0;
        float bestDiff = Float.MAX_VALUE;
        for (int i = 0; i < JUMP_TIERS.size(); i++) {
            Float v = JUMP_TIERS.get(i);
            if (v == null) {
                continue;
            }
            float d = Math.abs(v - basePower);
            if (d < bestDiff) {
                bestDiff = d;
                best = i;
            }
        }
        return best;
    }

    /** 默认 g/μ 下某档可跨高（预计算表）。 */
    static double clearanceBlocksAtTier(int tier) {
        return clearanceBlocksAtTier(tier, (LivingEntity) null);
    }

    /** 按实体当前陆地竖直物理算某档可跨高。 */
    static double clearanceBlocksAtTier(int tier, @Nullable LivingEntity entity) {
        return clearanceBlocksAtTier(tier, landVerticalPhysics(entity));
    }

    static double clearanceBlocksAtTier(int tier, LandVerticalPhysics physics) {
        if (physics == null) {
            physics = new LandVerticalPhysics(JUMP_GRAVITY, JUMP_DRAG);
        }
        if (isDefaultLandPhysics(physics)) {
            if (PATH_CLEARANCE_BY_TIER.length == 0) {
                return VANILLA_PATH_JUMP_HEIGHT;
            }
            int last = PATH_CLEARANCE_BY_TIER.length - 1;
            int i = tier;
            if (i < 0) {
                i = 0;
            } else if (i > last) {
                i = last;
            }
            return PATH_CLEARANCE_BY_TIER[i];
        }
        return pathClearanceFromJumpPower(tierJumpPower(tier), physics.gravity(), physics.drag());
    }

    /** 寻路最高门槛：窗口上沿对应 clearance（默认物理）。 */
    static double pathJumpHeightForMax(JumpController controller) {
        return pathJumpHeightForMax(controller, (LivingEntity) null);
    }

    static double pathJumpHeightForMax(JumpController controller, @Nullable LivingEntity entity) {
        return clearanceBlocksAtTier(controller.defaultJumpTier(), entity);
    }

    /**
     * 软窗口内能跨过 {@code deltaY} 的最低档；没有则 {@code -1}（默认物理）。
     */
    static int lowestTierForClearance(JumpController controller, double deltaY) {
        return lowestTierForClearance(controller, deltaY, (LivingEntity) null);
    }

    static int lowestTierForClearance(
            JumpController controller, double deltaY, @Nullable LivingEntity entity) {
        LandVerticalPhysics physics = landVerticalPhysics(entity);
        int[] w = controller.normalizedWindow();
        for (int t = w[0]; t <= w[1]; t++) {
            if (clearanceBlocksAtTier(t, physics) + 1.0E-3D >= deltaY) {
                return t;
            }
        }
        return -1;
    }

    /** 从实体解析陆地竖直物理；{@code null} → 默认 0.08 / 0.98。 */
    static LandVerticalPhysics landVerticalPhysics(@Nullable LivingEntity entity) {
        return new LandVerticalPhysics(resolveLandGravity(entity), resolveLandDrag(entity));
    }

    /**
     * 仅有世界时：1.20.1 {@code DimensionType} 无重力字段，退回默认。
     * 有实体时请用 {@link #landVerticalPhysics(LivingEntity)}（Forge 属性随维度模组变化）。
     */
    static LandVerticalPhysics landVerticalPhysics(@Nullable Level level) {
        if (level == null) {
            return new LandVerticalPhysics(JUMP_GRAVITY, JUMP_DRAG);
        }
        return new LandVerticalPhysics(JUMP_GRAVITY, JUMP_DRAG);
    }

    /**
     * Forge {@code ENTITY_GRAVITY}（与 {@code LivingEntity#travel} 一致）。
     * 剔除缓降瞬时修饰，按升空段估算。
     */
    static double resolveLandGravity(@Nullable LivingEntity entity) {
        if (entity == null) {
            return JUMP_GRAVITY;
        }
        AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (gravity == null) {
            return JUMP_GRAVITY;
        }
        double g = gravity.getValue();
        AttributeModifier slow = gravity.getModifier(SLOW_FALLING_GRAVITY_ID);
        if (slow != null && slow.getOperation() == AttributeModifier.Operation.ADDITION) {
            g -= slow.getAmount();
        }
        return g;
    }

    /**
     * 竖直衰减：原版 travel 写死 0.98；保留实体入口便于日后按维度挂钩。
     */
    static double resolveLandDrag(@Nullable LivingEntity entity) {
        return JUMP_DRAG;
    }

    static boolean isDefaultLandPhysics(LandVerticalPhysics physics) {
        return physics != null
                && Math.abs(physics.gravity() - JUMP_GRAVITY) < 1.0E-9D
                && Math.abs(physics.drag() - JUMP_DRAG) < 1.0E-9D;
    }

    static float tierJumpPower(int tier) {
        if (JUMP_TIERS.isEmpty()) {
            return VANILLA_JUMP;
        }
        int last = JUMP_TIERS.size() - 1;
        int i = tier;
        if (i < 0) {
            i = 0;
        } else if (i > last) {
            i = last;
        }
        Float v = JUMP_TIERS.get(i);
        return v != null && v > 0.0F ? v : VANILLA_JUMP;
    }

    static double[] buildPathClearanceByTier() {
        double[] out = new double[JUMP_TIERS.size()];
        for (int i = 0; i < out.length; i++) {
            Float power = JUMP_TIERS.get(i);
            out[i] = pathClearanceFromJumpPower(power != null ? power : VANILLA_JUMP);
        }
        return out;
    }

    /**
     * 默认 g/μ。离散竖直模型：{@code v←(v-g)μ}，高度和
     * {@code S(n)=n·v*+(v0-v*)(1-μ^n)/(1-μ)}；对连续 {@code S} 求极大再 ×0.9。
     */
    static double pathClearanceFromJumpPower(double jumpPower) {
        return pathClearanceFromJumpPower(jumpPower, JUMP_GRAVITY, JUMP_DRAG);
    }

    static double pathClearanceFromJumpPower(double jumpPower, double gravity, double drag) {
        if (!(jumpPower > 0.0D)) {
            return 0.0D;
        }
        if (!(gravity > 1.0E-8D)) {
            return ZERO_GRAVITY_CLEARANCE * JUMP_CLEARANCE_FACTOR;
        }
        double g = gravity;
        double mu = drag;
        if (!(mu > 0.0D) || !(mu < 1.0D)) {
            mu = JUMP_DRAG;
        }
        double oneMinusMu = 1.0D - mu;
        double vStar = -g * mu / oneMinusMu;
        double v0 = jumpPower;
        // μ^n* = (g μ) / ((v0 - v*) (-ln μ))
        double denom = (v0 - vStar) * (-Math.log(mu));
        if (!(denom > 0.0D)) {
            return VANILLA_PATH_JUMP_HEIGHT * JUMP_CLEARANCE_FACTOR;
        }
        double muN = (g * mu) / denom;
        if (!(muN > 0.0D) || !(muN < 1.0D)) {
            return VANILLA_PATH_JUMP_HEIGHT * JUMP_CLEARANCE_FACTOR;
        }
        double nStar = Math.log(muN) / Math.log(mu);
        int nLo = (int) Math.floor(nStar);
        int nHi = (int) Math.ceil(nStar);
        double best = 0.0D;
        if (nLo > 0) {
            best = Math.max(best, jumpHeightSum(nLo, v0, vStar, mu));
        }
        if (nHi > 0 && nHi != nLo) {
            best = Math.max(best, jumpHeightSum(nHi, v0, vStar, mu));
        }
        if (!(best > 0.0D)) {
            best = jumpHeightSum(Math.max(1, nHi), v0, vStar, mu);
        }
        return best * JUMP_CLEARANCE_FACTOR;
    }

    /** 前 n 项速度和（k = 0 .. n-1）。 */
    static double jumpHeightSum(int n, double v0, double vStar, double mu) {
        if (n <= 0) {
            return 0.0D;
        }
        return n * vStar + (v0 - vStar) * (1.0D - Math.pow(mu, n)) / (1.0D - mu);
    }

    /**
     * 对齐原版 {@code LivingEntity#calculateFallDamage}：安全高度 {@code 3 + f}。
     * {@code f} 取 {@code max(JUMP药水 amp+1, defaultJumpTier())}——表档与旧常驻药映射一致（LOW=0）。
     */
    static int calculateFallDamage(
            LivingEntity self, JumpController jump, float fallDistance, float damageMultiplier) {
        if (self.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        MobEffectInstance potion = self.getEffect(MobEffects.JUMP);
        float potionF = potion == null ? 0.0F : (float) (potion.getAmplifier() + 1);
        float tierF = (float) jump.defaultJumpTier();
        float f = Math.max(potionF, tierF);
        return Mth.ceil((fallDistance - 3.0F - f) * damageMultiplier);
    }

    /**
     * 规范化后的 {@code [a, b]}；非法下标回退 0 / last；{@code a > b} 时交换。
     */
    default int[] normalizedWindow() {
        int last = JUMP_TIERS.size() - 1;
        if (last < 0) {
            return new int[]{0, 0};
        }
        int a = this.minJumpTier();
        int b = this.maxJumpTier();
        if (a < 0 || a > last) {
            a = 0;
        }
        if (b < 0 || b > last) {
            b = last;
        }
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        return new int[]{a, b};
    }

    /**
     * 在软窗口 {@code [min, max]} 内均匀随机一档（闭区间）；{@code min == max} 时即该档。
     */
    default int randomJumpTier(RandomSource random) {
        int[] w = this.normalizedWindow();
        int a = w[0];
        int b = w[1];
        if (a >= b) {
            return a;
        }
        if (random == null) {
            return b;
        }
        return Mth.randomBetweenInclusive(random, a, b);
    }

    /** 默认跳：默认档对应 vy。 */
    default float defaultJumpPower() {
        return this.jumpPowerAt(this.defaultJumpTier(), true);
    }

    /** 按档取 vy，尊重软窗口 {@code [min, max]}。 */
    default float jumpPowerAt(int tier) {
        return this.jumpPowerAt(tier, true);
    }

    /**
     * 按档取 vy。
     *
     * @param respectMin {@code true} 夹到 {@code [min, max]}；{@code false} 可低于 min（仍不超过 max，且落在表下标内）
     */
    default float jumpPowerAt(int tier, boolean respectMin) {
        if (JUMP_TIERS.isEmpty()) {
            return VANILLA_JUMP;
        }
        int last = JUMP_TIERS.size() - 1;
        int[] w = this.normalizedWindow();
        int a = w[0];
        int b = w[1];
        int i = tier;
        if (i < 0) {
            i = 0;
        } else if (i > last) {
            i = last;
        }
        if (respectMin) {
            if (i < a) {
                i = a;
            }
            if (i > b) {
                i = b;
            }
        } else if (i > b) {
            i = b;
        }
        Float v = JUMP_TIERS.get(i);
        return v != null && v > 0.0F ? v : VANILLA_JUMP;
    }
}
