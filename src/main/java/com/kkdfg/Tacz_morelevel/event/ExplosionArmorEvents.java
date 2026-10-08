package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.ExplosionArmorReduction;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 防爆防弹套：对 {@link DamageTypeTags#IS_EXPLOSION} 按盔甲槽累加 {@code armor.toml}
 * {@code explosionReductionPerPiece}（爆破 / 爆炎 / 动力机甲 / 特种作战 / 冲击波）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ExplosionArmorEvents {

    private ExplosionArmorEvents() {}

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        if (!event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }
        float reduction = ExplosionArmorReduction.total(target);
        if (reduction <= 0f) {
            return;
        }
        event.setAmount(event.getAmount() * (1f - reduction));
    }
}
