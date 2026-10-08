package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthSeventhHandler;
import com.kkdfg.Tacz_morelevel.item.custom.StealthSeventhArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** 潜行七级套技能：独立于隐身斗篷，只翻本套胸甲开关。 */
public final class StealthSeventhSkill implements ArmorLegendarySkill {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "stealth_seventh");

    private static final StealthSeventhSkill INSTANCE = new StealthSeventhSkill();

    private StealthSeventhSkill() {}

    public static void register() {
        ArmorLegendarySkillRegistry.register(INSTANCE);
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return StealthSeventhArmorItem.hasFullSet(entity);
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        if (!hasFullSet(entity)) {
            return false;
        }
        return StealthSeventhArmorItem.isStealthOnCooldown(
                StealthSeventhArmorItem.chest(entity), entity.level().getGameTime());
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
        if (isSpectatorBlocked(entity) || !ArmorStealthSeventhHandler.isStealthEligible(entity)) {
            return false;
        }
        if (ArmorStealthSeventhHandler.isToggled(entity)) {
            return true;
        }
        return !isOnCooldown(entity);
    }

    @Override
    public boolean activate(LivingEntity entity) {
        if (isSpectatorBlocked(entity) || !ArmorStealthSeventhHandler.isStealthEligible(entity)) {
            return false;
        }
        if (ArmorStealthSeventhHandler.isToggled(entity)) {
            ArmorStealthSeventhHandler.setToggled(entity, false);
            if (entity instanceof Player player) {
                player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.stealth_seventh.off")
                                .withStyle(ChatFormatting.GRAY),
                        true);
            }
            return true;
        }
        if (isOnCooldown(entity)) {
            if (entity instanceof Player player) {
                player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.stealth_seventh.cooldown")
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            return false;
        }
        ArmorStealthSeventhHandler.setToggled(entity, true);
        ArmorStealthSeventhHandler.applyInvisibility(entity);
        entity.getPersistentData().putBoolean(ArmorStealthSeventhHandler.TAG_WAS_ACTIVE, true);
        if (entity instanceof Player player) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.stealth_seventh.on")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    true);
        }
        return true;
    }
}
