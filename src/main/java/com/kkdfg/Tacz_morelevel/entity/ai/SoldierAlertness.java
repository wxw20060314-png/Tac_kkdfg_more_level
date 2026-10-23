package com.kkdfg.Tacz_morelevel.entity.ai;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * 警惕值。扫描侧自行读取；无目标收拢到 {@link #ALERT_MIN}，有目标保底 {@link #ALERT_BASE}。
 */
public interface SoldierAlertness {

    int ALERT_MIN = 10;
    int ALERT_BASE = 10;
    int ALERT_MAX = 100;
    int ALERT_SEE_DELTA = -1;
    int ALERT_BLIND_DELTA = 1;
    int ALERT_IDLE_DELTA = -1;

    int getAlertness();

    void setAlertness(int value);

    default void addAlertness(int delta) {
        this.setAlertness(this.getAlertness() + delta);
    }

    default void tickAlertness(@Nullable LivingEntity target, boolean canSeeTarget) {
        if (target == null || !target.isAlive()) {
            int cur = this.getAlertness();
            if (cur > ALERT_MIN) {
                this.setAlertness(Math.max(ALERT_MIN, cur + ALERT_IDLE_DELTA));
            } else if (cur < ALERT_MIN) {
                this.setAlertness(ALERT_MIN);
            }
            return;
        }
        int v = Math.max(ALERT_BASE, this.getAlertness());
        if (canSeeTarget) {
            v = Math.max(ALERT_BASE, v + ALERT_SEE_DELTA);
        } else {
            v = Math.min(ALERT_MAX, v + ALERT_BLIND_DELTA);
        }
        this.setAlertness(Mth.clamp(v, ALERT_BASE, ALERT_MAX));
    }
}
