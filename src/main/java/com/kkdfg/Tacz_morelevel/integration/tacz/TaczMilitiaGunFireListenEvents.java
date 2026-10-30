package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractNeutralGunNpcEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.tacz.guns.api.event.common.GunFireEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * TaCZ 每次实际击发会产生 {@link GunFireEvent}（含连发、点射多次）；仅在服务端处理。
 * 通知范围内士兵与中立枪械 NPC 走向枪口；史莱姆实验体不接入。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TaczMilitiaGunFireListenEvents {

    private TaczMilitiaGunFireListenEvents() {}

    @SubscribeEvent
    public static void onGunFire(GunFireEvent event) {
        if (event.isCanceled() || event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        LivingEntity shooter = event.getShooter();
        if (shooter.level().isClientSide()) {
            return;
        }
        Vec3 origin = shooter.getEyePosition(1.0F);
        AbstractPlayerLikeSoldierEntity.alertSoldiersInRangeToGunshot(shooter.level(), origin, shooter);
        AbstractNeutralGunNpcEntity.alertNeutralsInRangeToGunshot(shooter.level(), origin, shooter);
    }
}
