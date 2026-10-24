package com.kkdfg.Tacz_morelevel.api;

/**
 * 交火 {@code MoveControl#strafe} 速度倍率（已含全局 {@code (x + a) * b} 映射）。
 * 供 {@link com.kkdfg.Tacz_morelevel.entity.ai.SoldierMoveControl} 在 {@code strafe} 时现读。
 */
public interface GunStrafeSpeedMob {
    /** @return STRAFE 的 {@code speedModifier}；建议范围约 0.05～2，默认 0.5（原版写死 0.25） */
    double gunStrafeSpeedModifier();
}
