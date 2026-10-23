package com.kkdfg.Tacz_morelevel.entity.ai;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.phys.Vec3;

/**
 * 投掷物威胁打戳。实现方每 tick 调 {@link #tickThrowableThreatScan()}；
 * Goal 只读戳并校验实体。与 {@link SoldierAlertness} 无接口耦合。
 */
public interface SoldierThrowableThreatSense {

    float THROWABLE_SCAN_TTL_RESET = 10.0F;
    double THROWABLE_SCAN_RADIUS = 8.0D;

    ThrowableThreatKind getThrowableThreatKind();

    @Nullable
    Vec3 getThrowableThreatPos();

    @Nullable
    UUID getThrowableThreatEntityId();

    void setThrowableThreat(
            ThrowableThreatKind kind, @Nullable Vec3 pos, @Nullable UUID entityId);

    float getThrowableScanTtl();

    void setThrowableScanTtl(float ttl);

    /** 基类实现：按警惕减 TTL，归零扫描打戳。 */
    void tickThrowableThreatScan();

    default void clearThrowableThreat() {
        this.setThrowableThreat(ThrowableThreatKind.NONE, null, null);
    }

    default boolean hasThrowableThreatStamp() {
        ThrowableThreatKind kind = this.getThrowableThreatKind();
        return kind != null
                && kind != ThrowableThreatKind.NONE
                && this.getThrowableThreatPos() != null
                && this.getThrowableThreatEntityId() != null;
    }
}
