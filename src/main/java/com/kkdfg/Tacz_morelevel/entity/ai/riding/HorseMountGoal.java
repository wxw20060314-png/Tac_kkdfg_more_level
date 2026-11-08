package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.entity.SoldierWeaponArrangeHost;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofHorseArmorItem;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 士兵和中立单位上马。每 20 tick 扫脚下 5×5×2。
 * 马已经穿着防弹马甲就直接上。否则先查默认 6 格装备和 6 格背包里有没有防弹马甲。
 */
public class HorseMountGoal extends Goal {

    private static final int INTERVAL = 20;
    private static final double HALF_HORIZONTAL = 2.5D;
    private static final double HEIGHT = 2.0D;

    private final Mob mob;
    private final SoldierWeaponArrangeHost gear;
    private int cooldown;
    private AbstractHorse horse;

    public HorseMountGoal(Mob mob) {
        if (!(mob instanceof SoldierWeaponArrangeHost gear)) {
            throw new IllegalArgumentException("horse mount requires a soldier backpack");
        }
        this.mob = mob;
        this.gear = gear;
    }

    @Override
    public boolean canUse() {
        if (this.mob.isPassenger()
                || !(this.mob instanceof HorseRidingNpc npc)
                || !npc.adoptsHorseRiding()) {
            return false;
        }
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        this.cooldown = INTERVAL;
        List<AbstractHorse> horses = this.findHorses();
        this.horse = this.nearestWearingArmor(horses);
        if (this.horse != null) {
            return true;
        }
        if (!this.hasBulletproofHorseArmor()) {
            return false;
        }
        this.horse = this.nearest(horses);
        return this.horse != null;
    }

    @Override
    public void start() {
        AbstractHorse mount = this.horse;
        this.horse = null;
        if (mount != null && mount.isAlive() && !mount.isVehicle()) {
            this.mob.startRiding(mount, true);
        }
    }

    private boolean hasBulletproofHorseArmor() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (isBulletproofHorseArmor(this.mob.getItemBySlot(slot))) {
                return true;
            }
        }
        for (ItemStack stack : this.gear.soldierBackpackSlots()) {
            if (isBulletproofHorseArmor(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBulletproofHorseArmor(ItemStack stack) {
        return stack.getItem() instanceof BulletproofHorseArmorItem;
    }

    private List<AbstractHorse> findHorses() {
        double x = this.mob.getX();
        double y = this.mob.getY();
        double z = this.mob.getZ();
        AABB box = new AABB(
                x - HALF_HORIZONTAL,
                y,
                z - HALF_HORIZONTAL,
                x + HALF_HORIZONTAL,
                y + HEIGHT,
                z + HALF_HORIZONTAL);
        return this.mob.level().getEntitiesOfClass(
                AbstractHorse.class,
                box,
                horse -> horse.isAlive() && !horse.isVehicle());
    }

    private AbstractHorse nearestWearingArmor(List<AbstractHorse> horses) {
        AbstractHorse nearest = null;
        double nearestSqr = Double.MAX_VALUE;
        for (AbstractHorse horse : horses) {
            if (!isBulletproofHorseArmor(horse.getItemBySlot(EquipmentSlot.CHEST))) {
                continue;
            }
            double distSqr = this.mob.distanceToSqr(horse);
            if (distSqr < nearestSqr) {
                nearestSqr = distSqr;
                nearest = horse;
            }
        }
        return nearest;
    }

    private AbstractHorse nearest(List<AbstractHorse> horses) {
        AbstractHorse nearest = null;
        double nearestSqr = Double.MAX_VALUE;
        for (AbstractHorse horse : horses) {
            double distSqr = this.mob.distanceToSqr(horse);
            if (distSqr < nearestSqr) {
                nearestSqr = distSqr;
                nearest = horse;
            }
        }
        return nearest;
    }
}
