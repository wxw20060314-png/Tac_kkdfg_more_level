package com.kkdfg.Tacz_morelevel.api;

/**
 * 每 tick 将躯干 yaw 限制在头 yaw ±{@link com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper#MAX_HEAD_BODY_YAW_DEG}°
 * 内（见对应实体 {@code tickHeadTurn} 与 {@link com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeBodyRotationControl}）。
 */
public interface PlayerLikeHeadBodyYawMob {}
