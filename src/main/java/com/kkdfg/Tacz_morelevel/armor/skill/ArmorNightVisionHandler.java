package com.kkdfg.Tacz_morelevel.armor.skill;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BlastSeventhBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.SpecialOpsBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.SpacetimeBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.StealthSeventhArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 时空 / 动力机甲 / 特种作战 / 爆破七级 / 冲击波 / 潜行七级头盔：按键开关夜视。
 * 开关 bool 存在头盔 NBT；开启后每 20 tick 刷新 12s（240 tick）夜视。
 * 摘下头盔仅在「开关为开且身上有夜视」时清除效果。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorNightVisionHandler {

    public static final String TAG_ENABLED = "ArmorNightVision";
    public static final int REFRESH_INTERVAL_TICKS = 20;
    public static final int EFFECT_DURATION_TICKS = 240;

    private ArmorNightVisionHandler() {}

    /**
     * 共用判定：时空 / 动力机甲 / 特种作战 / 爆破七级 / 冲击波 / 潜行七级（任意部位物品类型）。
     * 头盔场景再叠加 {@link ArmorItem.Type#HELMET}。
     */
    public static boolean isNightVisionArmorSet(Item item) {
        return item instanceof SpacetimeBulletproofArmorItem
                || item instanceof PowerMechBulletproofArmorItem
                || item instanceof SpecialOpsBulletproofArmorItem
                || item instanceof BlastSeventhBulletproofArmorItem
                || item instanceof ShockwaveBulletproofArmorItem
                || item instanceof StealthSeventhArmorItem;
    }

    /** 是否为可开夜视的头盔。 */
    public static boolean isEligibleHelmet(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ArmorItem armor
                && armor.getType() == ArmorItem.Type.HELMET
                && isNightVisionArmorSet(item);
    }

    public static ItemStack getHelmet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD);
    }

    public static boolean isEnabled(ItemStack helmet) {
        if (!isEligibleHelmet(helmet)) {
            return false;
        }
        CompoundTag tag = helmet.getTag();
        return tag != null && tag.getBoolean(TAG_ENABLED);
    }

    public static void setEnabled(ItemStack helmet, boolean enabled) {
        if (!isEligibleHelmet(helmet)) {
            return;
        }
        if (enabled) {
            helmet.getOrCreateTag().putBoolean(TAG_ENABLED, true);
            return;
        }
        CompoundTag tag = helmet.getTag();
        if (tag != null) {
            tag.remove(TAG_ENABLED);
            if (tag.isEmpty()) {
                helmet.setTag(null);
            }
        }
    }

    /** 服务端切换当前头盔上的开关。 */
    public static void toggle(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        ItemStack helmet = getHelmet(player);
        if (!isEligibleHelmet(helmet)) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.armor_night_vision.need_helmet")
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }
        boolean next = !isEnabled(helmet);
        setEnabled(helmet, next);
        if (next) {
            applyNightVision(player);
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.armor_night_vision.on")
                            .withStyle(ChatFormatting.AQUA),
                    true);
        } else {
            clearNightVision(player);
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.armor_night_vision.off")
                            .withStyle(ChatFormatting.GRAY),
                    true);
        }
    }

    public static void applyNightVision(LivingEntity entity) {
        entity.addEffect(
                new MobEffectInstance(
                        MobEffects.NIGHT_VISION, EFFECT_DURATION_TICKS, 0, false, false, true));
    }

    public static void clearNightVision(LivingEntity entity) {
        entity.removeEffect(MobEffects.NIGHT_VISION);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }
        ItemStack helmet = getHelmet(player);
        if (!isEnabled(helmet)) {
            return;
        }
        if (player.level().getGameTime() % REFRESH_INTERVAL_TICKS != 0) {
            return;
        }
        applyNightVision(player);
    }

    /**
     * 摘下/换下头盔：仅当旧头盔开关为开、且玩家当前有夜视时才清除。
     * 没打开、或没有夜视效果 → 不动。
     */
    @SubscribeEvent
    public static void onHelmetChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.HEAD || event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack from = event.getFrom();
        if (!isEnabled(from)) {
            return;
        }
        if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
            return;
        }
        if (isEnabled(event.getTo())) {
            return;
        }
        clearNightVision(player);
    }
}
