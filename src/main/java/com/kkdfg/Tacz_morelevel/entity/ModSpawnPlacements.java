package com.kkdfg.Tacz_morelevel.entity;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

/** 自然生成时地面刷怪等规则。 */
public final class ModSpawnPlacements {

    public static void register() {
        SpawnPlacements.register(
                ModEntities.MILITIA.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                ModEntities.REFUGEE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                ModEntities.MERCENARY.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                ModEntities.ASSAULT_SOLDIER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                ModEntities.SLIME_WARRIOR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        // 若要在下界放宽亮度限制（不经 Monster 敌对亮度判定），可改回引用 mercenarySpawnRules，并取消下方注释。
        // SpawnPlacements.register(
        //         ModEntities.MERCENARY.get(),
        //         SpawnPlacements.Type.ON_GROUND,
        //         Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        //         ModSpawnPlacements::mercenarySpawnRules);
    }

    /*
     * 雇佣兵：主世界等沿用 {@link Monster#checkMonsterSpawnRules}；下界改用 {@link net.minecraft.world.entity.Mob#checkMobSpawnRules}，
     * 避免菌光体/岩浆附近亮度过高几乎不刷。（暂未启用）
     *
    private static boolean mercenarySpawnRules(
            net.minecraft.world.entity.EntityType<com.kkdfg.Tacz_morelevel.entity.custom.MercenarySoldierEntity> entityType,
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.entity.MobSpawnType spawnType,
            net.minecraft.core.BlockPos pos,
            net.minecraft.util.RandomSource random) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && serverLevel.dimension() == net.minecraft.world.level.Level.NETHER) {
            if (serverLevel.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
                return false;
            }
            return net.minecraft.world.entity.Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
        }
        return Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random);
    }
    */

    private ModSpawnPlacements() {}
}
