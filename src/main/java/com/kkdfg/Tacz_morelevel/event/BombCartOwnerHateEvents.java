package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import com.tacz.guns.entity.EntityKineticBullet;
import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 炸弹小车相关：主人枪伤写仇恨；小车伤害主人时统一拦截。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BombCartOwnerHateEvents {

    private BombCartOwnerHateEvents() {}

    /** 认主小车（含爆炸）不对主人造成伤害。 */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurtCancelOwnCart(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (isOwnBombCart(source.getEntity(), victim)
                || isOwnBombCart(source.getDirectEntity(), victim)) {
            event.setCanceled(true);
        }
    }

    private static boolean isOwnBombCart(@Nullable Entity entity, LivingEntity victim) {
        return entity instanceof BombCartEntity cart && cart.isOwnedBy(victim);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) {
            return;
        }
        LivingEntity victim = event.getEntity();
        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null || attacker == victim || !attacker.isAlive()) {
            return;
        }
        if (victim instanceof BombCartEntity cart && cart.isOwnedBy(attacker)) {
            return;
        }
        attacker.setLastHurtMob(victim);
    }

    @Nullable
    private static LivingEntity resolveAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof EntityKineticBullet bullet && bullet.getOwner() instanceof LivingEntity living) {
            return living;
        }
        Entity causing = source.getEntity();
        if (causing instanceof EntityKineticBullet bullet && bullet.getOwner() instanceof LivingEntity living) {
            return living;
        }
        if (causing instanceof LivingEntity living) {
            return living;
        }
        if (direct instanceof LivingEntity living) {
            return living;
        }
        return null;
    }
}
