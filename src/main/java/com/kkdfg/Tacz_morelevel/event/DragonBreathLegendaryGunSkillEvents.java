package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code tacz:m1014_dragonbreath}（龙息喷）传说技能：每次枪伤把燃烧瓶燃烧叠到至少
 * {@link #BURN_LEVEL} 级，并在当前剩余时间上加 {@link #ADD_TICKS}。
 * 霰弹枪一发多颗弹丸，每颗各走一次伤害事件。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DragonBreathLegendaryGunSkillEvents {

    public static final ResourceLocation GUN_ID =
            ResourceLocation.fromNamespaceAndPath("tacz", "m1014_dragonbreath");

    /** 显示等级。燃烧瓶效果伤害 = amplifier + 1，所以 IV 级是 amplifier 3、每秒 4 点。 */
    private static final int BURN_LEVEL = 4;
    /** 每次伤害事件追加的燃烧时间。 */
    private static final int ADD_TICKS = 5;

    private DragonBreathLegendaryGunSkillEvents() {}

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
        var burn = ModMobEffects.MOLOTOV_BURN.get();
        MobEffectInstance current = living.getEffect(burn);
        int amplifier = BURN_LEVEL - 1;
        int duration = ADD_TICKS;
        if (current != null) {
            amplifier = Math.max(current.getAmplifier(), amplifier);
            duration = current.getDuration() + ADD_TICKS;
        }
        living.addEffect(new MobEffectInstance(burn, duration, amplifier, false, false, true));
    }
}
