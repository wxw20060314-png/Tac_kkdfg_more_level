package com.kkdfg.Tacz_morelevel.entity.custom;

import com.tacz.guns.entity.EntityKineticBullet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

/** 列夫大帅 / 大帅护卫军：链路超时、末影拉回、附近生成共用常量与工具。 */
public final class MarshalFactionHelper {

    public static final int MAX_MINION_SLOTS = 4;
    /** 小弟库上限；未满时按 {@link #RESERVE_REFILL_TTL_TICKS} 回补。 */
    public static final int MAX_MINION_RESERVE = 24;
    /** 小弟库回补间隔（库未满时 TTL 耗尽 +1；15s）。 */
    public static final int RESERVE_REFILL_TTL_TICKS = 20 * 15;
    /** 链路保活超时（5s）；解析不到对方则倒计时，耗尽断连。 */
    public static final int LINK_TIMEOUT_TICKS = 100;
    /** 空槽从库放出间隔（需库 &gt; 0）。 */
    public static final int SPAWN_INTERVAL_TICKS = 10;
    public static final int SPAWN_SCAN_RANGE = 6;
    public static final int SPAWN_ATTEMPTS = 10;
    /** 平时拉回距离。 */
    public static final double RECALL_DIST_IDLE = 64.0D;
    /** 大帅有 target 时拉回距离。 */
    public static final double RECALL_DIST_COMBAT = 32.0D;

    private MarshalFactionHelper() {}

    public static double recallDistanceLimit(@Nullable Mob marshal) {
        if (marshal != null && marshal.getTarget() != null && marshal.getTarget().isAlive()) {
            return RECALL_DIST_COMBAT;
        }
        return RECALL_DIST_IDLE;
    }

    /**
     * 阵营友伤：仅己方——护卫误伤自己的大帅 / 大帅误伤自己的护卫 /
     * 同一大帅名下的护卫互伤（含 TaCZ 子弹射手）。不同大帅之间不免。
     */
    public static boolean shouldBlockFactionDamage(LivingEntity victim, DamageSource source) {
        Entity attacker = resolveDamagingActor(source);
        if (attacker == null || attacker == victim) {
            return false;
        }
        if (victim instanceof LevMarshalEntity marshal) {
            return attacker instanceof MarshalGuardEntity guard
                    && marshal.getUUID().equals(guard.getMarshalOwnerUUID());
        }
        if (victim instanceof MarshalGuardEntity guard) {
            if (attacker instanceof MarshalGuardEntity other) {
                UUID owner = guard.getMarshalOwnerUUID();
                return owner != null && owner.equals(other.getMarshalOwnerUUID());
            }
            return attacker instanceof LevMarshalEntity marshal
                    && marshal.getUUID().equals(guard.getMarshalOwnerUUID());
        }
        return false;
    }

    @Nullable
    private static Entity resolveDamagingActor(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof EntityKineticBullet bullet && bullet.getOwner() != null) {
            return bullet.getOwner();
        }
        Entity causing = source.getEntity();
        if (causing instanceof EntityKineticBullet bullet && bullet.getOwner() != null) {
            return bullet.getOwner();
        }
        return causing != null ? causing : direct;
    }

    /** 无掉落移除（丢弃，非击杀）。 */
    public static void discardWithoutDrops(Mob entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            entity.setDropChance(slot, 0.0F);
        }
        entity.discard();
    }

    public static void enderTeleport(Mob entity, double x, double y, double z) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 from = entity.position();
        playTeleportFx(level, from);
        entity.teleportTo(x, y, z);
        entity.resetFallDistance();
        entity.getNavigation().stop();
        playTeleportFx(level, entity.position());
        level.playSound(
                null,
                x,
                y,
                z,
                SoundEvents.ENDERMAN_TELEPORT,
                entity.getSoundSource(),
                1.0F,
                1.0F);
    }

    public static void playTeleportFx(ServerLevel level, Vec3 pos) {
        level.sendParticles(
                ParticleTypes.PORTAL, pos.x, pos.y + 1.0D, pos.z, 32, 0.25D, 0.5D, 0.25D, 0.0D);
    }

    /** 将小弟拉到主人附近（末影特效）；失败则贴主人坐标强制传送。 */
    public static void recallToMarshal(MarshalGuardEntity guard, LivingEntity marshal) {
        if (!(guard.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos base = marshal.blockPosition();
        RandomSource random = guard.getRandom();
        for (int i = 0; i < 10; i++) {
            int dx = Mth.nextInt(random, -3, 3);
            int dz = Mth.nextInt(random, -3, 3);
            int dy = Mth.nextInt(random, -1, 1);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            BlockPos pos = base.offset(dx, dy, dz);
            if (!level.noCollision(guard.getType().getAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
                continue;
            }
            enderTeleport(guard, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            return;
        }
        enderTeleport(guard, marshal.getX(), marshal.getY(), marshal.getZ());
    }

    /**
     * 在大帅附近生成一只护卫并绑主；最多 {@link #SPAWN_ATTEMPTS} 次采样，失败则大帅脚底强制刷。
     *
     * @return 成功刷出的实体，失败 {@code null}
     */
    @Nullable
    public static MarshalGuardEntity spawnGuardNear(LevMarshalEntity marshal) {
        if (!(marshal.level() instanceof ServerLevel level)) {
            return null;
        }
        RandomSource random = marshal.getRandom();
        Set<BlockPos> seen = new HashSet<>();
        for (int t = 0; t < SPAWN_ATTEMPTS; t++) {
            double x =
                    marshal.getX()
                            + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_SCAN_RANGE;
            double z =
                    marshal.getZ()
                            + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_SCAN_RANGE;
            int y = Mth.floor(marshal.getY()) + random.nextInt(3) - 1;
            BlockPos key = BlockPos.containing(x, y, z);
            if (!seen.add(key)) {
                continue;
            }
            MarshalGuardEntity spawned = tryPlaceGuard(level, marshal, x, y, z, false);
            if (spawned != null) {
                return spawned;
            }
        }
        return tryPlaceGuard(level, marshal, marshal.getX(), marshal.getY(), marshal.getZ(), true);
    }

    @Nullable
    private static MarshalGuardEntity tryPlaceGuard(
            ServerLevel level,
            LevMarshalEntity marshal,
            double x,
            double y,
            double z,
            boolean force) {
        var type = com.kkdfg.Tacz_morelevel.entity.ModEntities.MARSHAL_GUARD.get();
        if (!force && !level.noCollision(type.getAABB(x, y, z))) {
            return null;
        }
        MarshalGuardEntity guard = type.create(level);
        if (guard == null) {
            return null;
        }
        guard.moveTo(x, y, z, marshal.getYRot(), 0.0F);
        guard.setMarshalOwner(marshal);
        guard.resetLinkTimeout();
        if (!force) {
            guard.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(guard.blockPosition()),
                    MobSpawnType.MOB_SUMMONED,
                    null,
                    null);
        } else {
            guard.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)),
                    MobSpawnType.MOB_SUMMONED,
                    null,
                    null);
        }
        if (!level.addFreshEntity(guard)) {
            return null;
        }
        return guard;
    }
}
