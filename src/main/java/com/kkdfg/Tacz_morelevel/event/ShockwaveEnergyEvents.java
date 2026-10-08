package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill;
import com.kkdfg.Tacz_morelevel.effect.ShockwaveImpactEffect;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 冲击波：玩家每 tick 回能/蓄力（含充能粒子，能量耗尽自动释放）；tick 末冲击态爆炸。
 * NPC（如炸弹视野）不走本事件，见 {@link com.kkdfg.Tacz_morelevel.entity.ai.bomb_sight.BombSightDashGoal}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShockwaveEnergyEvents {

    private ShockwaveEnergyEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        ShockwaveDashSkill.tickCharge(event.player);
        ShockwaveImpactEffect.tickImpact(event.player);
    }
}
