package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 肩炮子弹（含爆炸）不伤自己的宿主。判定在 {@link AbstractShoulderCannonEntity}。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShoulderCannonHostDamageEvents {

    private ShoulderCannonHostDamageEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (AbstractShoulderCannonEntity.hurtsOwnHost(source.getEntity(), victim)
                || AbstractShoulderCannonEntity.hurtsOwnHost(source.getDirectEntity(), victim)) {
            event.setCanceled(true);
        }
    }
}
