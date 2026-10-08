package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.JokerSeventhBulletproofHelmetItem;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 小丑七级头被动传说技能「头部防御」：爆头伤害按正常倍率计算后，再在防弹前将基础伤害 ÷2。
 * <p>天花板的认证。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class JokerSeventhHelmetSkillEvents {

    /** 爆头结算后、防弹前的伤害系数。 */
    public static final float HEADSHOT_PRE_ARMOR_DAMAGE_FACTOR = 0.75F;

    private JokerSeventhHelmetSkillEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGunHurtPre(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (event.isCanceled() || !event.isHeadShot()) {
            return;
        }
        Entity hurt = event.getHurtEntity();
        if (!(hurt instanceof LivingEntity living)) {
            return;
        }
        ItemStack head = living.getItemBySlot(EquipmentSlot.HEAD);
        if (!(head.getItem() instanceof JokerSeventhBulletproofHelmetItem)) {
            return;
        }
        // 保留爆头倍率；最终伤害 = base×倍率×0.5（等价爆头算完再 ÷2）
        event.setBaseAmount(event.getBaseAmount() * HEADSHOT_PRE_ARMOR_DAMAGE_FACTOR);
    }
}
