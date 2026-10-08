package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierGunPassthroughSight;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import java.util.List;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 闪光弹：引信同手雷；碰墙/碰人立刻起爆；范围内按视线/朝向/距离施加 {@link ModMobEffects#FLASHED}。
 * 半径、满闪秒数与近点比例 t 见 {@link Config} {@code flashbang}。
 */
public class FlashbangEntity extends GrenadeEntity {

    public static final int MAX_FUSE_TICKS = 140;

    @SuppressWarnings("unchecked")
    public FlashbangEntity(EntityType<? extends FlashbangEntity> type, Level level) {
        super((EntityType<? extends GrenadeEntity>) type, level);
    }

    public FlashbangEntity(Level level, LivingEntity owner) {
        super(ModEntities.FLASHBANG.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FLASHBANG.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            explode();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            explode();
        }
    }

    @Override
    protected void explode() {
        if (this.level().isClientSide) {
            return;
        }
        flashAt(this.position());
        this.discard();
    }

    /** 在指定点施加闪光（过热捏爆也走这里）。 */
    public static void flashAt(Level level, Vec3 center) {
        if (level.isClientSide) {
            return;
        }
        double r = Config.flashbangRadius;
        int baseDurationTicks =
                Math.max(1, Mth.floor(Config.flashbangBaseDurationSeconds * 20.0D));
        double rSq = r * r;
        AABB box =
                new AABB(
                        center.x - r,
                        center.y - r,
                        center.z - r,
                        center.x + r,
                        center.y + r,
                        center.z + r);
        List<LivingEntity> targets =
                level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive);
        for (LivingEntity target : targets) {
            Vec3 eye = target.getEyePosition(1.0F);
            double distSq = eye.distanceToSqr(center);
            if (distSq > rSq) {
                continue;
            }
            if (!SoldierGunPassthroughSight.hasPassthroughLine(level, target, center, eye)) {
                continue;
            }

            double flash;
            double dist = Math.sqrt(distSq);
            double pointBlankDist = r * Mth.clamp(Config.flashbangPointBlankFactor, 0.0D, 1.0D);
            if (dist <= pointBlankDist) {
                // 半径 * t 内：满闪（仍需 LOS）
                flash = 1.0D;
            } else {
                Vec3 look = target.getViewVector(1.0F);
                Vec3 toFlash = center.subtract(eye).scale(1.0D / dist);
                double facing = look.dot(toFlash);
                if (facing < 0.0D) {
                    continue;
                }
                double falloffT = Mth.clamp(dist / r, 0.0D, 1.0D);
                double distFactor = (1.0D - falloffT) * (1.0D - falloffT);
                flash = facing * distFactor;
            }
            if (flash <= 0.0D) {
                continue;
            }
            int duration = Math.max(1, (int) (baseDurationTicks * flash));
            target.addEffect(
                    new MobEffectInstance(
                            ModMobEffects.FLASHED.get(), duration, 0, false, false, false));
        }
        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                ModSounds.SOUND_FLASHBANG.get(),
                SoundSource.BLOCKS,
                2.0F,
                1.0F);
    }

    private void flashAt(Vec3 center) {
        flashAt(this.level(), center);
    }
}
