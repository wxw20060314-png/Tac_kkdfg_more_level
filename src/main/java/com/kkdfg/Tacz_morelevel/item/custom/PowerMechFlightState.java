package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.kkdfg.Tacz_morelevel.power_mech.PowerMechFlightSync;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PowerMechFlightState {

    private PowerMechFlightState() {}

    public static boolean hasChestplate(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PowerMechBulletproofArmorItem;
    }

    public static boolean canUseFlightSystem(Player player) {
        return !player.isCreative()
                && !player.isSpectator()
                && hasChestplate(player);
    }

    public static boolean isPowerMechFlying(LivingEntity entity) {
        if (entity instanceof WarMachineEntity warMachine) {
            return warMachine.isPowerMechFlying();
        }
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (!canUseFlightSystem(player) || player.isFallFlying()) {
            return false;
        }
        if (player.level().isClientSide()) {
            return player.getAbilities().flying
                    || PowerMechFlightSync.isFlying(player.getUUID());
        }
        return player.getAbilities().flying;
    }

    public static int getFlightEnergy(LivingEntity entity) {
        if (!hasChestplate(entity)) {
            return 0;
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return PowerMechBulletproofArmorItem.getFlightEnergy(chest);
    }

    public static boolean isFlightEnergyEmpty(LivingEntity entity) {
        return getFlightEnergy(entity) <= 0;
    }

    public static boolean isFlightEnergyFull(LivingEntity entity) {
        return getFlightEnergy(entity) >= PowerMechBulletproofArmorItem.FLIGHT_ENERGY_MAX;
    }

    public static float getFlightEnergyRatio(LivingEntity entity) {
        return getFlightEnergy(entity) / (float) PowerMechBulletproofArmorItem.FLIGHT_ENERGY_MAX;
    }

    public static boolean isRegeneratingFlightEnergy(Player player) {
        if (!canUseFlightSystem(player)) {
            return false;
        }
        if (isFlightEnergyFull(player)) {
            return false;
        }
        return PowerMechBulletproofArmorItem.countEquippedPieces(player) > 0;
    }
}
