package com.kkdfg.Tacz_morelevel.effect;

import com.kkdfg.Tacz_morelevel.entity.custom.BombSightEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * 冲击波冲击态：不可见标记；持续 {@link #DURATION_TICKS}。
 * 碰撞/爆炸由 {@link #tickImpact} 判定：外扩碰撞箱检测周围方块/生物，爆炸原点为贴合点。
 * 全套半径：玩家及其它实体 2–10；仅 {@link BombSightEntity} 为 5–10，按冲量线性映射。
 */
public final class ShockwaveImpactEffect extends MobEffect {

    public static final int DURATION_TICKS = 40;

    /** 玩家全套：冲量映射下限。 */
    public static final float EXPLOSION_RADIUS_MIN = 2.0F;
    /** 炸弹视野专用：冲量映射下限。 */
    public static final float EXPLOSION_RADIUS_NPC_MIN = 5.0F;
    public static final float EXPLOSION_RADIUS_MAX = 10.0F;

    /** 略微外扩，用于摸到已贴合的方块/生物碰撞箱。 */
    public static final double PROBE_INFLATE = 0.02D;

    public ShockwaveImpactEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55FFFF);
    }

    /** 玩家及其它实体用 {@link #EXPLOSION_RADIUS_MIN}；仅炸弹视野用 {@link #EXPLOSION_RADIUS_NPC_MIN}。 */
    public static float explosionRadiusMinFor(LivingEntity entity) {
        if (entity instanceof BombSightEntity) {
            return EXPLOSION_RADIUS_NPC_MIN;
        }
        return EXPLOSION_RADIUS_MIN;
    }

    /**
     * 爆炸半径：全套冲击波按冲量从实体对应 min 线性映射到 {@link #EXPLOSION_RADIUS_MAX}；
     * 否则固定为该 min。
     */
    public static float resolveExplosionRadius(LivingEntity entity, double impulse) {
        float min = explosionRadiusMinFor(entity);
        if (entity != null && ShockwaveBulletproofArmorItem.hasFullSet(entity)) {
            return radiusFromImpulse(impulse, min, EXPLOSION_RADIUS_MAX);
        }
        return min;
    }

    public static float radiusFromImpulse(double impulse) {
        return radiusFromImpulse(impulse, EXPLOSION_RADIUS_MIN, EXPLOSION_RADIUS_MAX);
    }

    public static float radiusFromImpulse(double impulse, float minRadius, float maxRadius) {
        double maxImpulse =
                ShockwaveBulletproofArmorItem.IMPULSE_K * ShockwaveBulletproofArmorItem.ENERGY_MAX;
        if (maxImpulse <= 1.0E-6D) {
            return minRadius;
        }
        float t = (float) Mth.clamp(impulse / maxImpulse, 0.0D, 1.0D);
        return Mth.lerp(t, minRadius, maxRadius);
    }

    public static int amplifierForRadius(float radius) {
        float clamped = Mth.clamp(radius, EXPLOSION_RADIUS_MIN, EXPLOSION_RADIUS_MAX);
        return Mth.clamp(Math.round(clamped * 10.0F), 20, 100);
    }

    public static float radiusFromAmplifier(int amplifier) {
        return Mth.clamp(amplifier / 10.0F, EXPLOSION_RADIUS_MIN, EXPLOSION_RADIUS_MAX);
    }

    /** 服务端 tick 末：有标记且探针碰到方块/生物 → 在贴合点爆炸。 */
    public static void tickImpact(LivingEntity entity) {
        if (entity.level().isClientSide() || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        MobEffectInstance inst = entity.getEffect(ModMobEffects.SHOCKWAVE_IMPACT.get());
        if (inst == null) {
            return;
        }
        Optional<Vec3> impact = findImpactPoint(entity, level);
        if (impact.isEmpty()) {
            return;
        }
        explodeAndClear(entity, level, radiusFromAmplifier(inst.getAmplifier()), impact.get());
    }

    /**
     * 外扩玩家碰撞箱，检测周围方块 VoxelShape 与其它生物 AABB；取距自身中心最近的贴合点。
     */
    public static Optional<Vec3> findImpactPoint(LivingEntity entity, ServerLevel level) {
        AABB self = entity.getBoundingBox();
        AABB probe = self.inflate(PROBE_INFLATE);
        Vec3 center = self.getCenter();

        Vec3 best = null;
        double bestDist = Double.MAX_VALUE;

        for (VoxelShape shape : level.getBlockCollisions(entity, probe)) {
            if (shape.isEmpty()) {
                continue;
            }
            Optional<Vec3> closest = shape.closestPointTo(center);
            if (closest.isEmpty()) {
                continue;
            }
            double d = closest.get().distanceToSqr(center);
            if (d < bestDist) {
                bestDist = d;
                best = closest.get();
            }
        }

        List<LivingEntity> others =
                level.getEntitiesOfClass(
                        LivingEntity.class, probe, other -> other != entity && other.isAlive());
        for (LivingEntity other : others) {
            Vec3 onOther = closestPointOnAabb(other.getBoundingBox(), center);
            double d = onOther.distanceToSqr(center);
            if (d < bestDist) {
                bestDist = d;
                best = onOther;
            }
        }

        return best == null ? Optional.empty() : Optional.of(best);
    }

    private static Vec3 closestPointOnAabb(AABB box, Vec3 point) {
        return new Vec3(
                Mth.clamp(point.x, box.minX, box.maxX),
                Mth.clamp(point.y, box.minY, box.maxY),
                Mth.clamp(point.z, box.minZ, box.maxZ));
    }

    public static void explodeAndClear(
            LivingEntity entity, ServerLevel level, float radius, Vec3 at) {
        entity.removeEffect(ModMobEffects.SHOCKWAVE_IMPACT.get());
        DamageSource blast = level.damageSources().explosion(entity, entity);
        entity.getPersistentData().putBoolean(GrenadeEntity.TAG_SKIP_EXPLOSION_SELF_DAMAGE, true);
        try {
            level.explode(
                    entity,
                    blast,
                    null,
                    at.x,
                    at.y,
                    at.z,
                    radius,
                    false,
                    Level.ExplosionInteraction.MOB);
        } finally {
            entity.getPersistentData().remove(GrenadeEntity.TAG_SKIP_EXPLOSION_SELF_DAMAGE);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(
                new IClientMobEffectExtensions() {
                    @Override
                    public boolean isVisibleInInventory(MobEffectInstance instance) {
                        return false;
                    }

                    @Override
                    public boolean isVisibleInGui(MobEffectInstance instance) {
                        return false;
                    }
                });
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
