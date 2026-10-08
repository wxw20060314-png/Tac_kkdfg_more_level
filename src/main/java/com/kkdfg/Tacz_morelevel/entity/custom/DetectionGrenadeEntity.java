package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.particle.ModParticles;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DetectionGrenadeEntity extends ThrowableItemProjectile {
    public DetectionGrenadeEntity(EntityType<? extends DetectionGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public DetectionGrenadeEntity(Level level, LivingEntity owner) {
        super(ModEntities.DETECTION_GRENADE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DETECTION_GRENADE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            detonate(result.getLocation());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            detonate(result.getLocation());
        }
    }

    private void detonate(Vec3 center) {
        Level level = this.level();

        AABB scanBox =
                new AABB(
            center.x - Config.detectionGrenadeHorizontalRadius,
            center.y - Config.detectionGrenadeVerticalRadius,
            center.z - Config.detectionGrenadeHorizontalRadius,
            center.x + Config.detectionGrenadeHorizontalRadius,
            center.y + Config.detectionGrenadeVerticalRadius,
            center.z + Config.detectionGrenadeHorizontalRadius);
        List<LivingEntity> targets =
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        scanBox,
                        entity -> entity.isAlive() && entity != this.getOwner());
        for (LivingEntity target : targets) {
            MobEffectInstance glowing =
                new MobEffectInstance(
                        MobEffects.GLOWING, Config.detectionGrenadeGlowingDurationTicks, 0, false, false);
            target.addEffect(glowing);
        }
        AABB scanBox_slow =
            new AABB(
                center.x - Config.detectionGrenadeSlowHorizontalRadius,
                center.y - Config.detectionGrenadeSlowVerticalRadius,
                center.z - Config.detectionGrenadeSlowHorizontalRadius,
                center.x + Config.detectionGrenadeSlowHorizontalRadius,
                center.y + Config.detectionGrenadeSlowVerticalRadius,
                center.z + Config.detectionGrenadeSlowHorizontalRadius);

        List<LivingEntity> targets_slow =
            level.getEntitiesOfClass(
                LivingEntity.class,
                scanBox_slow,
                entity -> entity.isAlive());
        for (LivingEntity target : targets_slow) {
            MobEffectInstance slowEffect =
                new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, Config.detectionGrenadeSlowDurationTicks, 3, false, false);
            target.addEffect(slowEffect);
        }

        this.playSound(
                ModSounds.SOUND_DIANBAO.get(),
                3.0F,
                1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ModParticles.DIANBAO.get(),
                    center.x,
                    center.y,
                    center.z,
                    1,
                    0,
                    0,
                    0,
                    0);
        }
        this.discard();
    }
}
