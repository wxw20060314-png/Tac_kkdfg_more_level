package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code tacz:taurus943_legendary}（列夫手枪）传说技能：命中时有概率为目标叠加缓慢，
 * 持续 {@link #DURATION_TICKS}，最高 {@link #MAX_LEVEL} 级。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Taurus943LegendaryGunSkillEvents {

    public static final ResourceLocation GUN_ID =
            ResourceLocation.fromNamespaceAndPath("tacz", "taurus943_legendary");

    /** 每次命中叠加一级的概率。 */
    private static final float PROC_CHANCE = 0.50F;
    /** 缓慢最高显示等级（amplifier = level - 1）。 */
    private static final int MAX_LEVEL = 3;
    /** 缓慢持续时间：2 秒。 */
    private static final int DURATION_TICKS = 40;

    private Taurus943LegendaryGunSkillEvents() {}

    @SubscribeEvent
    public static void onGunHurtPost(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        if (!GUN_ID.equals(event.getGunId())) {
            return;
        }
        Entity hurt = event.getHurtEntity();
        if (!(hurt instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        Entity attacker = event.getAttacker();
        if (attacker != null && living.is(attacker)) {
            return;
        }
        if (living.getRandom().nextFloat() >= PROC_CHANCE) {
            return;
        }
        int nextAmplifier = 0;
        MobEffectInstance current = living.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (current != null) {
            nextAmplifier = current.getAmplifier() + 1;
        }
        int maxAmplifier = MAX_LEVEL - 1;
        nextAmplifier = Mth.clamp(nextAmplifier, 0, maxAmplifier);
        living.addEffect(
                new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, DURATION_TICKS, nextAmplifier, false, true));
    }
}
