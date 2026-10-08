package com.kkdfg.Tacz_morelevel.item.medicine;

/**
 * 回血配置：百分比与固定血量可同时生效，最终回血 = 最大生命 × percentMax + flat。
 * 两者均为 0 时表示不回血。
 */
public record HealSpec(
        float percentMax,
        float flat,
        int intervalTicks,
        int durabilityCostPerPulse) {

    public HealSpec {
        percentMax = Math.max(0F, percentMax);
        flat = Math.max(0F, flat);
        durabilityCostPerPulse = Math.max(0, durabilityCostPerPulse);
        if (intervalTicks < 0) {
            intervalTicks = 0;
        }
    }

    public static HealSpec none() {
        return new HealSpec(0F, 0F, 0, 0);
    }

    /**
     * 读条过程中每 {@code intervalTicks} 触发一次（通常 20 = 1 秒）。
     *
     * @param percentMax 最大生命百分比，如 0.12 = 12%
     * @param flat 直接加血量
     */
    public static HealSpec channel(float percentMax, float flat, int intervalTicks, int durabilityCostPerPulse) {
        if (percentMax <= 0F && flat <= 0F) {
            return none();
        }
        return new HealSpec(percentMax, flat, Math.max(1, intervalTicks), durabilityCostPerPulse);
    }

    /**
     * 读条结束后一次性回血（intervalTicks = 0）。
     *
     * @param percentMax 最大生命百分比
     * @param flat 直接加血量
     */
    public static HealSpec onFinish(float percentMax, float flat) {
        if (percentMax <= 0F && flat <= 0F) {
            return none();
        }
        return new HealSpec(percentMax, flat, 0, 0);
    }

    public boolean hasHeal() {
        return percentMax > 0F || flat > 0F;
    }

    public boolean channelsDuringUse() {
        return hasHeal() && intervalTicks > 0;
    }

    public boolean healsOnFinish() {
        return hasHeal() && intervalTicks <= 0;
    }

    /** 计算本次应回血量：百分比项 + 固定项。 */
    public float resolveAmount(float maxHealth) {
        return maxHealth * percentMax + flat;
    }
}
