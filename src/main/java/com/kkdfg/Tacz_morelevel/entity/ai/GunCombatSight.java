package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.ai.war_machine.WarMachineGunSight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 交火视线：Fire / Move 构造传入；旧 Sight 类不改，只包一层。
 */
@FunctionalInterface
public interface GunCombatSight {

    boolean hasLineOfSight(Mob mob, LivingEntity target);

    /** 普通士兵：头→身→脚穿透。 */
    GunCombatSight COMBAT = MobGunPlayerLikeAimHelper::hasCombatLineOfSight;

    /** 炸弹宝宝：爆破清障视线。 */
    GunCombatSight BLAST = BombBabySight::hasLineOfSight;

    /** 战争机器加特林：严格眼射线。 */
    GunCombatSight WAR_STRICT = WarMachineGunSight::hasStrictLineOfSight;

    /** 战争机器 RPG：穿透档（≤ {@link WarMachineGunSight#RPG_MAX_SOLID_BLOCKS} 实心）。 */
    GunCombatSight WAR_RPG = WarMachineGunSight::hasRpgLineOfSight;

    /** 战争机器走位停走：严格或 RPG 任一看见即可。 */
    GunCombatSight WAR_MOVE =
            (mob, target) -> {
                WarMachineGunSight.Result r = WarMachineGunSight.evaluate(mob, target);
                return r.strictLineOfSight() || r.rpgLineOfSight();
            };
}
