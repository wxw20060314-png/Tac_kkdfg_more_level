package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import com.kkdfg.Tacz_morelevel.armor.skill.power_mech.PowerMechLegendarySkills;
import com.kkdfg.Tacz_morelevel.armor.skill.power_mech.PowerMechOverloadEffects;
import com.kkdfg.Tacz_morelevel.effect.ModDeffenceEffect;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 动力机甲全套传说技能：过载。 */
public final class PowerMechOverloadSkill implements ArmorLegendarySkill {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "power_mech_overload");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean canActivate(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
        }
        if (!PowerMechLegendarySkills.hasFullSet(entity)) {
            return false;
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return PowerMechBulletproofArmorItem.getFlightEnergy(chest)
                >= PowerMechBulletproofArmorItem.OVERLOAD_ENERGY_COST;
    }

    @Override
    public boolean activate(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        int energy = PowerMechBulletproofArmorItem.getFlightEnergy(chest);
        if (energy < PowerMechBulletproofArmorItem.OVERLOAD_ENERGY_COST) {
            return false;
        }
        PowerMechBulletproofArmorItem.setFlightEnergy(
                chest, energy - PowerMechBulletproofArmorItem.OVERLOAD_ENERGY_COST);
        entity.addEffect(
                new MobEffectInstance(
                    ModMobEffects.MOD_DEFFENCE.get(),
                        PowerMechBulletproofArmorItem.OVERLOAD_ABSORPTION_DURATION_TICKS,
                        PowerMechBulletproofArmorItem.OVERLOAD_ABSORPTION_AMPLIFIER,
                        false,
                        false,
                        false));
        PowerMechOverloadEffects.playActivate(entity);
        return true;
    }
}
