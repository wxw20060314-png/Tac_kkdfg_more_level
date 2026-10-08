package com.kkdfg.Tacz_morelevel.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 由可主手装备 TaCZ 枪械的生物实现。主手即为“武器栏”上的那把枪（与玩家主手一致）。
 */
public interface GunEquippedMob {

    /**
     * 将 TaCZ 动能枪物品置于主手并写入枪械数据 id，初始化 TaCZ 射击数据并执行一次 {@code draw}。
     * 若 {@code gunDataId} 在 TaCZ 索引中不存在，则主手可能被置为空栈（由实现决定）。
     */
    void equipTaCZGunMainHand(ResourceLocation gunDataId);

    /** 若实体实现了 {@link GunEquippedMob}，则对其调用 {@link #equipTaCZGunMainHand(ResourceLocation)}。 */
    static void applyEquipMainHand(LivingEntity entity, ResourceLocation gunDataId) {
        if (entity instanceof GunEquippedMob gunMob) {
            gunMob.equipTaCZGunMainHand(gunDataId);
        }
    }
}
