package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseMissileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 导弹爆炸不伤发射它的机械马，以及发射时记下的主人。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MechanicalHorseMissileEvents {

    private MechanicalHorseMissileEvents() {}

    @SubscribeEvent
    public static void protectLauncherAndOwner(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDirectSourceEntity() instanceof MechanicalHorseMissileEntity missile) {
            Entity launcher = missile.getOwner();
            event.getAffectedEntities().removeIf(entity ->
                    entity == launcher || entity.getUUID().equals(missile.blastProtectedOwner()));
        }
    }
}
