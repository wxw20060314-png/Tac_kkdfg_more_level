package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthHandler;
import com.kkdfg.Tacz_morelevel.item.custom.StealthCloakArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 隐身斗篷技能：只翻开关 bool（{@link ArmorStealthHandler#setToggled}）。
 * 上刻/本刻边沿、清隐形、eligible 见 {@link ArmorStealthHandler}。
 */
public final class StealthCloakSkill implements ArmorLegendarySkill {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "stealth_cloak");

    private static final StealthCloakSkill INSTANCE = new StealthCloakSkill();

    private StealthCloakSkill() {}

    public static void register() {
        ArmorLegendarySkillRegistry.register(INSTANCE);
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return StealthCloakArmorItem.hasFullSet(entity);
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        if (!hasFullSet(entity)) {
            return false;
        }
        return StealthCloakArmorItem.isStealthOnCooldown(
                StealthCloakArmorItem.cloak(entity), entity.level().getGameTime());
    }

    private static boolean isSpectatorBlocked(LivingEntity entity) {
        return entity instanceof Player player && player.isSpectator();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean canActivate(LivingEntity entity) {
        if (isSpectatorBlocked(entity) || !ArmorStealthHandler.isStealthEligible(entity)) {
            return false;
        }
        // 已开启：允许关闭（不看冷却）
        if (ArmorStealthHandler.isToggled(entity)) {
            return true;
        }
        return !isOnCooldown(entity);
    }

    @Override
    public boolean activate(LivingEntity entity) {
        if (isSpectatorBlocked(entity) || !ArmorStealthHandler.isStealthEligible(entity)) {
            return false;
        }
        if (ArmorStealthHandler.isToggled(entity)) {
            ArmorStealthHandler.setToggled(entity, false);
            if (entity instanceof Player player) {
                player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.stealth_cloak.off")
                                .withStyle(ChatFormatting.GRAY),
                        true);
            }
            return true;
        }
        if (isOnCooldown(entity)) {
            if (entity instanceof Player player) {
                player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.stealth_cloak.cooldown")
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            return false;
        }
        ArmorStealthHandler.setToggled(entity, true);
        ArmorStealthHandler.applyInvisibility(entity);
        // 本刻已生效，写入上刻，避免下一 tick 误判边沿
        entity.getPersistentData().putBoolean(ArmorStealthHandler.TAG_WAS_ACTIVE, true);
        if (entity instanceof Player player) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.stealth_cloak.on")
                            .withStyle(ChatFormatting.AQUA),
                    true);
        }
        return true;
    }
}
