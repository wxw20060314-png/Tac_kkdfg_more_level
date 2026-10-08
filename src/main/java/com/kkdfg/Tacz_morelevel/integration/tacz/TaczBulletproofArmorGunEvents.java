package com.kkdfg.Tacz_morelevel.integration.tacz;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.entity.EntityKineticBullet;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * TaCZ 按子弹 {@code armorIgnore} 拆分穿甲段；在 {@link EntityHurtByGunEvent.Pre} 中根据全身
 * {@link BulletproofArmor#sumPenetrationReduction}（Config 动态档位）从 {@code armorIgnore} 直接减去（小于 0 按 0），
 * 在 Post / Kill 上恢复原值。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TaczBulletproofArmorGunEvents {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<Entity, Float> PENDING_ARMOR_IGNORE_RESTORE = new IdentityHashMap<>();

    private static Field armorIgnoreField;

    static {
        try {
            Field f = EntityKineticBullet.class.getDeclaredField("armorIgnore");
            f.setAccessible(true);
            armorIgnoreField = f;
        } catch (NoSuchFieldException e) {
            LOGGER.warn(
                    "[{}] TaCZ EntityKineticBullet.armorIgnore not found; bullet penetration reduction on armor will not apply.",
                    Tacz_MoreLevel.MOD_ID);
        }
    }

    private TaczBulletproofArmorGunEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGunHurtPre(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (event.isCanceled() || armorIgnoreField == null) {
            return;
        }
        Entity bullet = event.getBullet();
        if (!(bullet instanceof EntityKineticBullet)) {
            return;
        }
        Entity hurt = event.getHurtEntity();
        if (!(hurt instanceof LivingEntity living)) {
            return;
        }
        float reduce = BulletproofArmor.sumPenetrationReduction(living);
        if (reduce <= 0f) {
            return;
        }
        if (PENDING_ARMOR_IGNORE_RESTORE.containsKey(bullet)) {
            restoreBulletArmorIgnore(bullet);
        }
        try {
            float current = armorIgnoreField.getFloat(bullet);
            PENDING_ARMOR_IGNORE_RESTORE.put(bullet, current);
            float adjusted = Mth.clamp(current - reduce, 0f, 1f);
            armorIgnoreField.setFloat(bullet, adjusted);
        } catch (IllegalAccessException e) {
            LOGGER.warn("[{}] Could not adjust bullet armorIgnore", Tacz_MoreLevel.MOD_ID, e);
            PENDING_ARMOR_IGNORE_RESTORE.remove(bullet);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGunHurtPost(EntityHurtByGunEvent.Post event) {
        restoreBulletArmorIgnore(event.getBullet());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGunKill(EntityKillByGunEvent event) {
        restoreBulletArmorIgnore(event.getBullet());
    }

    private static void restoreBulletArmorIgnore(Entity bullet) {
        if (armorIgnoreField == null || bullet == null) {
            return;
        }
        Float original = PENDING_ARMOR_IGNORE_RESTORE.remove(bullet);
        if (original == null) {
            return;
        }
        try {
            armorIgnoreField.setFloat(bullet, original);
        } catch (IllegalAccessException e) {
            LOGGER.warn("[{}] Could not restore bullet armorIgnore", Tacz_MoreLevel.MOD_ID, e);
        }
    }
}
