package com.kkdfg.Tacz_morelevel.entity.ai;

/**
 * 顶层实体每拍刷新一次交火看见。Goal 仍用各自原来的视线，读这个布尔值要等对上原来的判定。
 */
public interface GunCombatSightHolder {

    /** 本拍对当前 {@code getTarget()} 的交火看见结果。 */
    boolean hasGunCombatSight();
}
