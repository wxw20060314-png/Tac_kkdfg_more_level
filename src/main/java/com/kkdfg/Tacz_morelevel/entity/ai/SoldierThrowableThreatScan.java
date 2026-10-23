package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.FlashbangEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MolotovEntity;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 雷/闪/燃烧瓶扫描工具（忽略自身投出，不扫火）。 */
public final class SoldierThrowableThreatScan {

    private SoldierThrowableThreatScan() {}

    public static void tick(Mob mob, SoldierAlertness alertness, SoldierThrowableThreatSense sense) {
        float ttl = sense.getThrowableScanTtl() - alertness.getAlertness() / 10.0F;
        if (ttl > 0.0F) {
            sense.setThrowableScanTtl(ttl);
            return;
        }
        sense.setThrowableScanTtl(SoldierThrowableThreatSense.THROWABLE_SCAN_TTL_RESET);
        scanAndStamp(mob, sense);
    }

    private static void scanAndStamp(Mob mob, SoldierThrowableThreatSense sense) {
        double radius = SoldierThrowableThreatSense.THROWABLE_SCAN_RADIUS;
        Vec3 core = mob.position().add(0.0D, mob.getBbHeight() * 0.35D, 0.0D);
        AABB box = mob.getBoundingBox().inflate(radius);
        double bestSq = radius * radius;
        ThrowableThreatKind bestKind = ThrowableThreatKind.NONE;
        Vec3 bestPos = null;
        Entity bestEntity = null;

        for (ThrowableItemProjectile t :
                mob.level()
                        .getEntitiesOfClass(
                                ThrowableItemProjectile.class,
                                box,
                                e -> e.isAlive() && !e.isRemoved() && e.getOwner() != mob)) {
            ThrowableThreatKind kind = classify(t);
            if (kind == ThrowableThreatKind.NONE) {
                continue;
            }
            double d2 = core.distanceToSqr(t.position());
            if (d2 < bestSq) {
                bestSq = d2;
                bestKind = kind;
                bestPos = t.position();
                bestEntity = t;
            }
        }

        if (bestEntity == null || bestPos == null) {
            sense.clearThrowableThreat();
            return;
        }
        sense.setThrowableThreat(bestKind, bestPos, bestEntity.getUUID());
    }

    @Nullable
    public static Entity resolveValidThreat(Mob mob, SoldierThrowableThreatSense sense) {
        if (!sense.hasThrowableThreatStamp()) {
            return null;
        }
        if (!(mob.level() instanceof ServerLevel server)) {
            return null;
        }
        Entity e = server.getEntity(sense.getThrowableThreatEntityId());
        if (e == null || !e.isAlive() || e.isRemoved()) {
            return null;
        }
        if (classify(e) != sense.getThrowableThreatKind()) {
            return null;
        }
        return e;
    }

    private static ThrowableThreatKind classify(Entity e) {
        if (e instanceof FlashbangEntity) {
            return ThrowableThreatKind.FLASHBANG;
        }
        if (e instanceof GrenadeEntity) {
            return ThrowableThreatKind.GRENADE;
        }
        if (e instanceof MolotovEntity) {
            return ThrowableThreatKind.MOLOTOV;
        }
        return ThrowableThreatKind.NONE;
    }
}
