package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.PunisherShoulderCannonShoot;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/** 判罚者肩炮火箭：爆炸点 3×3×3 缓慢 V，5 秒。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PunisherShoulderCannonSlowEvents {

    public static final int SLOW_DURATION_TICKS = 100;
    public static final int SLOW_AMPLIFIER = 4;
    public static final double BOX = 3.0D;

    private static final Set<UUID> APPLIED_THIS_TICK = new HashSet<>();
    private static long appliedGameTime = Long.MIN_VALUE;

    private PunisherShoulderCannonSlowEvents() {}

    @SubscribeEvent
    public static void onGunHurtPost(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        applyAround(event.getBullet(), event.getAttacker());
    }

    @SubscribeEvent
    public static void onGunKill(EntityKillByGunEvent event) {
        if (event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        applyAround(event.getBullet(), event.getAttacker());
    }

    private static void applyAround(Entity bullet, Entity attacker) {
        if (!PunisherShoulderCannonShoot.isPunisherShoulderBullet(bullet)) {
            return;
        }
        long time = bullet.level().getGameTime();
        if (appliedGameTime != time) {
            APPLIED_THIS_TICK.clear();
            appliedGameTime = time;
        }
        if (!APPLIED_THIS_TICK.add(bullet.getUUID())) {
            return;
        }
        AABB box =
                new AABB(
                        bullet.getX() - BOX * 0.5D,
                        bullet.getY() - BOX * 0.5D,
                        bullet.getZ() - BOX * 0.5D,
                        bullet.getX() + BOX * 0.5D,
                        bullet.getY() + BOX * 0.5D,
                        bullet.getZ() + BOX * 0.5D);
        for (LivingEntity living : bullet.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (!living.isAlive()) {
                continue;
            }
            if (living instanceof AbstractShoulderCannonEntity) {
                continue;
            }
            if (attacker != null && living.is(attacker)) {
                continue;
            }
            living.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION_TICKS, SLOW_AMPLIFIER, false, true));
        }
    }
}
