package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.armor.skill.power_mech.PowerMechLegendarySkills;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechFlightState;
import com.kkdfg.Tacz_morelevel.power_mech.war_machine.WarMachineFlightEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 战争机器动力机甲技能：满能量低血过载/否则飞行、战斗中概率进入飞行、能量消耗/回复（与玩家默认基础一致，暂不含速度倍率）。
 */
public final class WarMachinePowerMechSkillGoal extends net.minecraft.world.entity.ai.goal.Goal {

    private final WarMachineEntity mob;
    private int flightRollCooldown;

    public WarMachinePowerMechSkillGoal(WarMachineEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return PowerMechFlightState.hasChestplate(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.mob.level().isClientSide()) {
            return;
        }

        LivingEntity target = this.mob.getTarget();
        ItemStack chest = this.mob.getItemBySlot(EquipmentSlot.CHEST);
        int energy = PowerMechBulletproofArmorItem.getFlightEnergy(chest);
        int pieces = PowerMechBulletproofArmorItem.countEquippedPieces(this.mob);

        if (pieces > 0 && energy < PowerMechBulletproofArmorItem.FLIGHT_ENERGY_MAX) {
            energy += PowerMechBulletproofArmorItem.FLIGHT_REGEN_PER_PIECE * pieces;
        }
        if (this.mob.isPowerMechFlying()) {
            energy -= PowerMechBulletproofArmorItem.FLIGHT_DRAIN_PER_TICK;
        }
        energy =
                Mth.clamp(
                        energy, 0, PowerMechBulletproofArmorItem.FLIGHT_ENERGY_MAX);
        PowerMechBulletproofArmorItem.setFlightEnergy(chest, energy);

        float threshold = WarMachineEntity.FLIGHT_ENERGY_THRESHOLD;
        int thresholdEnergy =
                Mth.floor(PowerMechBulletproofArmorItem.FLIGHT_ENERGY_MAX * threshold);
        boolean hasTarget = target != null && target.isAlive();
        boolean energyFull = energy >= PowerMechBulletproofArmorItem.FLIGHT_ENERGY_MAX;
        boolean energyAboveThreshold = energy > thresholdEnergy;

        if (this.mob.isPowerMechFlying()) {
            WarMachineFlightEffects.tickEmit(this.mob);
            if (!hasTarget || energy <= thresholdEnergy || energy <= 0) {
                this.mob.setPowerMechFlying(false);
            }
            return;
        }

        if (!hasTarget || energy <= 0) {
            this.flightRollCooldown = 0;
            return;
        }

        if (energyFull) {
            if (this.mob.getHealth() < WarMachineEntity.OVERLOAD_HEALTH_THRESHOLD) {
                PowerMechLegendarySkills.tryOverload(this.mob);
            } else {
                this.mob.setPowerMechFlying(true);
            }
            this.flightRollCooldown = 0;
            return;
        }

        if (!energyAboveThreshold) {
            this.flightRollCooldown = 0;
            return;
        }

        if (this.flightRollCooldown > 0) {
            this.flightRollCooldown--;
            return;
        }
        this.flightRollCooldown = WarMachineEntity.FLIGHT_ROLL_INTERVAL_TICKS;

        if (this.mob.getRandom().nextDouble() < WarMachineEntity.FLIGHT_ROLL_CHANCE) {
            this.mob.setPowerMechFlying(true);
        }
    }
}
