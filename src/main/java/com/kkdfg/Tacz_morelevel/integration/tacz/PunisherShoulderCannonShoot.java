package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.PunisherShoulderCannonEntity;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.mojang.logging.LogUtils;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.lang.reflect.Field;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/** 判罚者肩炮：借用已有 RPG 火箭，有目标才打；弹道朝目标。 */
public final class PunisherShoulderCannonShoot {

    public static final String TAG_PUNISHER_SHOULDER = "PunisherShoulderCannon";

    public static final ResourceLocation RPG_GUN_ID =
            ResourceLocation.fromNamespaceAndPath("tacz", "rpg7");

    private static final float SMALL_EXPLOSION_RADIUS = 1.5F;
    private static final float SMALL_EXPLOSION_DAMAGE = 24.0F;
    private static final Logger LOGGER = LogUtils.getLogger();

    private PunisherShoulderCannonShoot() {}

    public static boolean isPunisherShoulderBullet(net.minecraft.world.entity.Entity bullet) {
        return bullet != null && bullet.getPersistentData().getBoolean(TAG_PUNISHER_SHOULDER);
    }

    @Nullable
    public static LivingEntity resolveTarget(AbstractShoulderCannonEntity cannon, LivingEntity host) {
        if (host == null || !host.isAlive()) {
            return null;
        }
        if (host instanceof Player player) {
            LivingEntity attacked = player.getLastHurtMob();
            LivingEntity attacker = player.getLastHurtByMob();
            boolean attackedOk = cannon.isValidCombatTarget(host, attacked);
            boolean attackerOk = cannon.isValidCombatTarget(host, attacker);
            if (attackedOk && attackerOk) {
                return player.getLastHurtMobTimestamp() >= player.getLastHurtByMobTimestamp()
                        ? attacked
                        : attacker;
            }
            if (attackedOk) {
                return attacked;
            }
            if (attackerOk) {
                return attacker;
            }
            return null;
        }
        if (host instanceof Mob mob) {
            LivingEntity hate = mob.getTarget();
            return cannon.isValidCombatTarget(host, hate) ? hate : null;
        }
        LivingEntity lastBy = host.getLastHurtByMob();
        return cannon.isValidCombatTarget(host, lastBy) ? lastBy : null;
    }

    public static boolean canSee(AbstractShoulderCannonEntity cannon, LivingEntity target) {
        return MobGunPlayerLikeAimHelper.hasCombatLineOfSight(cannon, target);
    }

    public static void tryFire(AbstractShoulderCannonEntity cannon, LivingEntity host) {
        if (cannon.level().isClientSide() || host == null || !host.isAlive()) {
            return;
        }
        LivingEntity target = resolveTarget(cannon, host);
        if (target == null || !canSee(cannon, target)) {
            return;
        }
        try {
            fireOnce(cannon, host, target);
        } catch (RuntimeException ex) {
            LOGGER.error("Punisher shoulder cannon failed to fire", ex);
        }
    }

    private static void fireOnce(
            AbstractShoulderCannonEntity cannon, LivingEntity host, LivingEntity target) {
        Level level = cannon.level();
        ItemStack gunStack = TaczGunMobHelper.createPlayerGunStack(RPG_GUN_ID);
        if (gunStack.isEmpty()) {
            return;
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(RPG_GUN_ID);
        if (indexOpt.isEmpty()) {
            return;
        }
        GunData gunData = indexOpt.get().getGunData();
        if (gunData == null) {
            return;
        }
        BulletData bulletData = gunData.getBulletData();
        ResourceLocation ammoId = gunData.getAmmoId();
        if (bulletData == null || ammoId == null) {
            return;
        }
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(cannon, target);
        cannon.yBodyRot = cannon.getYRot();
        Vec3 from = cannon.getEyePosition();
        Vec3 dir = cannon.getLookAngle();
        if (dir.lengthSqr() < 1.0E-8D) {
            return;
        }
        dir = dir.normalize();
        EntityKineticBullet bullet =
                new EntityKineticBullet(
                        level, host, gunStack, ammoId, RPG_GUN_ID, false, gunData, bulletData);
        applySmallExplosion(bullet);
        AbstractShoulderCannonEntity.markShoulderBullet(bullet);
        CompoundTag data = bullet.getPersistentData();
        data.putBoolean(TAG_PUNISHER_SHOULDER, true);
        double x = from.x + dir.x * 0.35D;
        double y = from.y + dir.y * 0.35D;
        double z = from.z + dir.z * 0.35D;
        bullet.setPos(x, y, z);
        float jsonSpeed = (float) bulletData.getSpeed();
        float shootSpeed = jsonSpeed > 10.0F ? jsonSpeed / 20.0F : Math.max(0.8F, jsonSpeed);
        shootSpeed = Math.min(4.0F, Math.max(0.8F, shootSpeed));
        bullet.shoot(dir.x, dir.y, dir.z, shootSpeed, 0.0F);
        level.addFreshEntity(bullet);
        spawnExhaustParticles(cannon, dir);
        playRpgShootSound(cannon);
        if (cannon instanceof PunisherShoulderCannonEntity punisherCannon) {
            punisherCannon.playShootAnimation();
        }
    }

    private static void spawnExhaustParticles(AbstractShoulderCannonEntity cannon, Vec3 dir) {
        if (!(cannon.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 center = cannon.position().add(0.0D, cannon.getBbHeight() * 0.5D, 0.0D);
        double back = cannon.getBbWidth() * 0.5D + 0.12D;
        Vec3 exhaust = center.subtract(dir.scale(back));
        Vec3 puff = dir.scale(-0.12D);
        server.sendParticles(
                ParticleTypes.FLAME,
                exhaust.x,
                exhaust.y,
                exhaust.z,
                0,
                puff.x,
                puff.y,
                puff.z,
                1.0D);
        server.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                exhaust.x,
                exhaust.y,
                exhaust.z,
                6,
                0.08D,
                0.06D,
                0.08D,
                0.02D);
    }

    private static void playRpgShootSound(AbstractShoulderCannonEntity cannon) {
        cannon.level()
                .playSound(
                        null,
                        cannon.getX(),
                        cannon.getY(),
                        cannon.getZ(),
                        ModSounds.SOUND_PUNISHER_SHOULDER_CANNON_SHOOT.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
    }

    private static void applySmallExplosion(EntityKineticBullet bullet) {
        setFloat(bullet, "explosionRadius", SMALL_EXPLOSION_RADIUS);
        setFloat(bullet, "explosionDamage", SMALL_EXPLOSION_DAMAGE);
        setBoolean(bullet, "explosionDestroyBlock", false);
        setBoolean(bullet, "explosion", true);
        setBoolean(bullet, "explode", true);
    }

    private static void setFloat(Object target, String field, float value) {
        try {
            Field f = EntityKineticBullet.class.getDeclaredField(field);
            f.setAccessible(true);
            f.setFloat(target, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void setBoolean(Object target, String field, boolean value) {
        try {
            Field f = EntityKineticBullet.class.getDeclaredField(field);
            f.setAccessible(true);
            f.setBoolean(target, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
