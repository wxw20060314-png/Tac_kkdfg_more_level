package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.entity.SoldierWeaponArrangeHost;
import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;

/** 骑着马时每 10 tick 把两套 6 格里更好的马甲和马胸甲对调。同一档不换。 */
public class HorseArmorEquipGoal extends Goal {

    private static final int INTERVAL = 10;
    /** 马甲是马容器的第 1 格，对应 getSlot(400 + 1)，存档写入 ArmorItem。 */
    private static final int HORSE_ARMOR_SLOT = 401;

    private final Mob mob;
    private final SoldierWeaponArrangeHost gear;
    private int cooldown;
    private EquipmentSlot handSlot;
    private int backpackIndex = -1;

    public HorseArmorEquipGoal(Mob mob) {
        if (!(mob instanceof SoldierWeaponArrangeHost gear)) {
            throw new IllegalArgumentException("horse armor equip requires a soldier backpack");
        }
        this.mob = mob;
        this.gear = gear;
    }

    @Override
    public boolean canUse() {
        if (!(this.mob.getVehicle() instanceof AbstractHorse horse)
                || horse instanceof MechanicalHorseEntity
                || !(this.mob instanceof HorseRidingNpc npc)
                || !npc.adoptsHorseRiding()) {
            return false;
        }
        this.keepShownArmor(horse);
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        this.cooldown = INTERVAL;
        if (HorseArmorRank.of(wornArmor(horse)) >= HorseArmorRank.VARIANT_SEVENTH) {
            return false;
        }
        return this.findBetter(horse);
    }

    @Override
    public void start() {
        if (!(this.mob.getVehicle() instanceof AbstractHorse horse) || horse instanceof MechanicalHorseEntity) {
            this.clearChoice();
            return;
        }
        ItemStack worn = wornArmor(horse).copy();
        if (this.handSlot != null) {
            ItemStack better = this.mob.getItemBySlot(this.handSlot).copy();
            if (HorseArmorRank.of(better) > HorseArmorRank.of(worn) && setWornArmor(horse, better)) {
                this.mob.setItemSlot(this.handSlot, worn);
            }
        } else if (this.backpackIndex >= 0) {
            NonNullList<ItemStack> bag = this.gear.soldierBackpackSlots();
            if (this.backpackIndex < bag.size()) {
                ItemStack better = bag.get(this.backpackIndex).copy();
                if (HorseArmorRank.of(better) > HorseArmorRank.of(worn) && setWornArmor(horse, better)) {
                    bag.set(this.backpackIndex, worn);
                }
            }
        }
        this.clearChoice();
    }

    private boolean findBetter(AbstractHorse horse) {
        this.clearChoice();
        int wornRank = HorseArmorRank.of(wornArmor(horse));
        int bestRank = wornRank;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            int rank = HorseArmorRank.of(this.mob.getItemBySlot(slot));
            if (rank > bestRank) {
                bestRank = rank;
                this.handSlot = slot;
                this.backpackIndex = -1;
            }
        }
        NonNullList<ItemStack> bag = this.gear.soldierBackpackSlots();
        for (int i = 0; i < bag.size(); i++) {
            int rank = HorseArmorRank.of(bag.get(i));
            if (rank > bestRank) {
                bestRank = rank;
                this.handSlot = null;
                this.backpackIndex = i;
            }
        }
        return bestRank > wornRank;
    }

    private void clearChoice() {
        this.handSlot = null;
        this.backpackIndex = -1;
    }

    /** 胸甲槽上还没写入马甲容器的，先补进容器，避免读档被空格盖掉。 */
    private static void keepShownArmor(AbstractHorse horse) {
        if (!(horse instanceof Horse)) {
            return;
        }
        ItemStack shown = wornArmor(horse).copy();
        if (HorseArmorRank.of(shown) > HorseArmorRank.of(horse.getSlot(HORSE_ARMOR_SLOT).get())) {
            setWornArmor(horse, shown);
        }
    }

    private static ItemStack wornArmor(AbstractHorse horse) {
        ItemStack equipment = horse.getItemBySlot(EquipmentSlot.CHEST);
        if (!(horse instanceof Horse)) {
            return equipment;
        }
        ItemStack saved = horse.getSlot(HORSE_ARMOR_SLOT).get();
        return HorseArmorRank.of(equipment) > HorseArmorRank.of(saved) ? equipment : saved;
    }

    private static boolean setWornArmor(AbstractHorse horse, ItemStack stack) {
        if (horse instanceof Horse) {
            return horse.getSlot(HORSE_ARMOR_SLOT).set(stack);
        }
        horse.setItemSlot(EquipmentSlot.CHEST, stack);
        return true;
    }
}
