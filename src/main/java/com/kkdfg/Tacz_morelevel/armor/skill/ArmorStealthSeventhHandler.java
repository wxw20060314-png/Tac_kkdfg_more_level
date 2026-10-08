package com.kkdfg.Tacz_morelevel.armor.skill;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.StealthSeventhArmorItem;
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
 * 潜行七级套效果层：与 {@link ArmorStealthHandler} 完全独立（独立 NBT / TAG / 事件）。
 * 胸甲池 t 上限 100；破隐冷却 8s。全套被动：每 10 tick（偏移 5）清除 {@code GLOWING}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorStealthSeventhHandler {

    public static final String TAG_WAS_ACTIVE = "TaczMorelevelStealthSeventhWasActive";

    public static final int REFRESH_INTERVAL_TICKS = 20;
    public static final int EFFECT_DURATION_TICKS = 60;

    public static final double VISIBILITY_FACTOR = 0.05D;

    private ArmorStealthSeventhHandler() {}

    public static boolean isStealthEligible(LivingEntity entity) {
        return StealthSeventhArmorItem.hasFullSet(entity);
    }

    public static boolean isToggled(LivingEntity entity) {
        return StealthSeventhArmorItem.isStealthEnabled(StealthSeventhArmorItem.chest(entity));
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
        ItemStack chest = StealthSeventhArmorItem.chest(entity);
        if (!StealthSeventhArmorItem.isStealthSeventhChest(chest)) {
            if (!toggled) {
                clearInvisibility(entity);
                setWasActive(entity, false);
            }
            return;
        }
        StealthSeventhArmorItem.setStealthEnabled(chest, toggled);
        if (!toggled) {
            clearInvisibility(entity);
            setWasActive(entity, false);
        }
    }

    public static boolean isEffectivelyStealthed(LivingEntity entity) {
        return isToggled(entity) && isStealthEligible(entity);
    }

    public static void applyInvisibility(LivingEntity entity) {
        entity.addEffect(
                new MobEffectInstance(
                        MobEffects.INVISIBILITY, EFFECT_DURATION_TICKS, 0, false, false, true));
    }

    /**
     * 仅清隐形效果；若隐身斗篷系统仍生效则不动（避免两套抢同一 MobEffect）。
     */
    public static void clearInvisibility(LivingEntity entity) {
        if (ArmorStealthHandler.isEffectivelyStealthed(entity)) {
            return;
        }
        entity.removeEffect(MobEffects.INVISIBILITY);
    }

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

    public static void breakStealth(LivingEntity entity) {
        if (!isToggled(entity)) {
            return;
        }
        boolean stillFullSet = StealthSeventhArmorItem.hasFullSet(entity);
        setToggled(entity, false);
        if (stillFullSet) {
            StealthSeventhArmorItem.startStealthCooldown(entity);
        }
        if (entity instanceof Player player) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.stealth_seventh.broken")
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
        StealthSeventhArmorItem.tickStealthPoolRegen(entity);
        tickAntiDetection(entity);
        if (!isToggled(entity) && !wasActiveLastTick(entity)) {
            return;
        }
        syncStealthEdge(entity);
    }

    /** 传说技能二：反探测 — 全套时每 10 tick（偏移 5）清除发光。 */
    private static void tickAntiDetection(LivingEntity entity) {
        if (entity.tickCount % 10 != 5) {
            return;
        }
        if (!StealthSeventhArmorItem.hasFullSet(entity)) {
            return;
        }
        if (entity.hasEffect(MobEffects.GLOWING)) {
            entity.removeEffect(MobEffects.GLOWING);
        }
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
        ItemStack chest = StealthSeventhArmorItem.chest(entity);
        if (!StealthSeventhArmorItem.isStealthSeventhChest(chest)) {
            return;
        }
        float amount = event.getAmount();
        if (amount <= 0.0F) {
            return;
        }
        float t = StealthSeventhArmorItem.getStealthPool(chest);
        if (t - amount < 0.0F) {
            StealthSeventhArmorItem.fillStealthPool(chest);
            breakStealth(entity);
            return;
        }
        StealthSeventhArmorItem.setStealthPool(chest, t - amount);
    }
}
