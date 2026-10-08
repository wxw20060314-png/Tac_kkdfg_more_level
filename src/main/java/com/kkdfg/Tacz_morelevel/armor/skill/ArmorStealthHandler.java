package com.kkdfg.Tacz_morelevel.armor.skill;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.StealthCloakArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 隐身套效果层（对齐夜视双 bool）：
 * <ul>
 *   <li>胸甲 {@link StealthCloakArmorItem#STEALTH_ENABLED_TAG}：开关；创造栏/新物品无键 = 默认关（同夜视）</li>
 *   <li>{@link #TAG_WAS_ACTIVE}：上刻是否真正生效；本刻边沿清/刷隐形（脱装只清效果，不改开关）</li>
 * </ul>
 * 胸甲池 t（上限 4）：每 5 tick +1。
 * {@link LivingHurtEvent} 护甲前：t−伤害&lt;0 则归满、关开关并冷却。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorStealthHandler {

    /** 上刻是否 enabled∧eligible（边沿检测用，存在实体上）。 */
    public static final String TAG_WAS_ACTIVE = "TaczMorelevelStealthWasActive";

    public static final int REFRESH_INTERVAL_TICKS = 20;
    public static final int EFFECT_DURATION_TICKS = 60;

    public static final double VISIBILITY_FACTOR = 0.05D;

    private ArmorStealthHandler() {}

    public static boolean isStealthEligible(LivingEntity entity) {
        return StealthCloakArmorItem.hasFullSet(entity);
    }

    /** 胸甲开关；默认关。 */
    public static boolean isToggled(LivingEntity entity) {
        return StealthCloakArmorItem.isStealthEnabled(StealthCloakArmorItem.cloak(entity));
    }

    public static boolean wasActiveLastTick(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG_WAS_ACTIVE);
    }

    private static void setWasActive(LivingEntity entity, boolean active) {
        CompoundTag data = entity.getPersistentData();
        if (active) {
            data.putBoolean(TAG_WAS_ACTIVE, true);
        } else {
            data.remove(TAG_WAS_ACTIVE);
        }
    }

    public static void setToggled(LivingEntity entity, boolean toggled) {
        ItemStack cloak = StealthCloakArmorItem.cloak(entity);
        if (!StealthCloakArmorItem.isStealthCloak(cloak)) {
            if (!toggled) {
                clearInvisibility(entity);
                setWasActive(entity, false);
            }
            return;
        }
        StealthCloakArmorItem.setStealthEnabled(cloak, toggled);
        if (!toggled) {
            clearInvisibility(entity);
            setWasActive(entity, false);
        }
    }

    /** 本刻真正生效 = 胸甲开关 ∧ 整套。 */
    public static boolean isEffectivelyStealthed(LivingEntity entity) {
        return isToggled(entity) && isStealthEligible(entity);
    }

    public static void applyInvisibility(LivingEntity entity) {
        entity.addEffect(
                new MobEffectInstance(
                        MobEffects.INVISIBILITY, EFFECT_DURATION_TICKS, 0, false, false, true));
    }

    public static void clearInvisibility(LivingEntity entity) {
        if (ArmorStealthSeventhHandler.isEffectivelyStealthed(entity)) {
            return;
        }
        entity.removeEffect(MobEffects.INVISIBILITY);
    }

    /**
     * 双 bool 边沿：上刻生效→本刻不生效则立刻清隐形；不改胸甲开关（同夜视脱盔只清效果）。
     */
    public static void syncStealthEdge(LivingEntity entity) {
        boolean nowActive = isEffectivelyStealthed(entity);
        boolean wasActive = wasActiveLastTick(entity);
        if (wasActive && !nowActive) {
            clearInvisibility(entity);
        }
        if (nowActive) {
            if (!wasActive || entity.tickCount % REFRESH_INTERVAL_TICKS == 0) {
                applyInvisibility(entity);
            }
        }
        setWasActive(entity, nowActive);
    }

    /** 破隐：关胸甲开关 + 写冷却。 */
    public static void breakStealth(LivingEntity entity) {
        if (!isToggled(entity)) {
            return;
        }
        boolean stillFullSet = StealthCloakArmorItem.hasFullSet(entity);
        setToggled(entity, false);
        if (stillFullSet) {
            StealthCloakArmorItem.startStealthCooldown(entity);
        }
        if (entity instanceof Player player) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.stealth_cloak.broken")
                            .withStyle(ChatFormatting.RED),
                    true);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        StealthCloakArmorItem.tickStealthPoolRegen(entity);
        if (!isToggled(entity) && !wasActiveLastTick(entity)) {
            return;
        }
        syncStealthEdge(entity);
    }

    @SubscribeEvent
    public static void onArmorChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.ARMOR) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!wasActiveLastTick(entity) && !isToggled(entity)) {
            return;
        }
        syncStealthEdge(entity);
    }

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (isEffectivelyStealthed(event.getEntity())) {
            event.modifyVisibility(VISIBILITY_FACTOR);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !isEffectivelyStealthed(entity)) {
            return;
        }
        ItemStack cloak = StealthCloakArmorItem.cloak(entity);
        if (!StealthCloakArmorItem.isStealthCloak(cloak)) {
            return;
        }
        float amount = event.getAmount();
        if (amount <= 0.0F) {
            return;
        }
        float t = StealthCloakArmorItem.getStealthPool(cloak);
        if (t - amount < 0.0F) {
            StealthCloakArmorItem.fillStealthPool(cloak);
            breakStealth(entity);
            return;
        }
        StealthCloakArmorItem.setStealthPool(cloak, t - amount);
    }
}
