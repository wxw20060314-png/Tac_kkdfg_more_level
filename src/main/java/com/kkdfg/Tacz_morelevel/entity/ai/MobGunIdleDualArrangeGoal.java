package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.SoldierBackpack;
import com.kkdfg.Tacz_morelevel.entity.SoldierCombatItemPreference;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractDualGunSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * 双枪闲置整理：无存活目标连续 TTL 后，整理主副并尽力纠到偏好顺序。
 * {@link AbstractDualGunSoldierEntity#isPreferredDualOrderReady()} 则拒绝。
 */
public final class MobGunIdleDualArrangeGoal extends Goal {

    private static final int DROP_ATTEMPTS_MAX = SoldierBackpack.SLOTS + 4;
    private static final int OFFHAND_MIN_SCORE = SoldierCombatItemPreference.GRADE_PREFERRED_GUN;
    private static final int OFFHAND_JUNK_BELOW = SoldierCombatItemPreference.GRADE_OTHER_GUN;

    private final AbstractDualGunSoldierEntity mob;
    private int noTargetTicks;

    public MobGunIdleDualArrangeGoal(AbstractDualGunSoldierEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !this.mob.canOperateWeaponSwap()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            this.noTargetTicks = 0;
            return false;
        }
        this.noTargetTicks++;
        boolean delay = false;
        if (this.noTargetTicks >= AbstractPlayerLikeSoldierEntity.NO_TARGET_WEAPON_ARRANGE_TTL) {
            delay = true;
            this.noTargetTicks = 0;
        }
        return delay && !this.mob.isPreferredDualOrderReady();
    }

    @Override
    public void start() {
        if (this.mob.isPreferredDualOrderReady()) {
            return;
        }
        this.arrange();
        this.mob.afterIdleMainhandArrange();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void arrange() {
        this.mob.compactSoldierInventory();
        ResourceLocation prefMain = this.mob.getPreferredMainGunDataId();
        ResourceLocation prefOff = this.mob.getPreferredOffhandGunDataId();
        this.moveHighestToMain(prefMain, prefOff);
        ResourceLocation keeperMain = gunDataId(this.mob.getMainHandItem());
        ResourceLocation keeperOff = partnerPreferred(keeperMain, prefMain, prefOff);
        this.clearOffhandIfJunk(prefMain, prefOff, keeperMain, keeperOff);
        this.moveSecondPreferredToOffhand(prefMain, prefOff, keeperMain, keeperOff);
        if (this.mob.hasPreferredPairInHands() && !this.mob.isPreferredDualOrderReady()) {
            this.mob.swapMainAndOffhand();
        }
    }

    private void moveHighestToMain(
            @Nullable ResourceLocation prefMain, @Nullable ResourceLocation prefOff) {
        int bestScore = SoldierCombatItemPreference.GRADE_EMPTY;
        int bestBag = -1;
        EquipmentSlot bestHand = null;

        int mainScore =
                SoldierCombatItemPreference.scoreDual(
                        this.mob.getMainHandItem(), prefMain, prefOff, null, null);
        if (mainScore > bestScore) {
            bestScore = mainScore;
            bestHand = EquipmentSlot.MAINHAND;
            bestBag = -1;
        }
        int offScore =
                SoldierCombatItemPreference.scoreDual(
                        this.mob.getOffhandItem(), prefMain, prefOff, null, null);
        if (offScore > bestScore
                || (offScore == bestScore
                        && bestHand != EquipmentSlot.MAINHAND
                        && isPreferredMain(this.mob.getOffhandItem(), prefMain))) {
            bestScore = offScore;
            bestHand = EquipmentSlot.OFFHAND;
            bestBag = -1;
        }
        NonNullList<ItemStack> bag = this.mob.soldierBackpackSlots();
        for (int i = 0; i < bag.size(); i++) {
            ItemStack stack = bag.get(i);
            int s =
                    SoldierCombatItemPreference.scoreDual(
                            stack, prefMain, prefOff, null, null);
            boolean preferMainTie =
                    s == bestScore
                            && bestHand != EquipmentSlot.MAINHAND
                            && isPreferredMain(stack, prefMain);
            if (s > bestScore || preferMainTie) {
                bestScore = s;
                bestHand = null;
                bestBag = i;
            }
        }
        if (bestScore == SoldierCombatItemPreference.GRADE_EMPTY) {
            return;
        }
        if (bestHand == EquipmentSlot.MAINHAND) {
            return;
        }
        if (bestHand == EquipmentSlot.OFFHAND) {
            this.mob.swapMainAndOffhand();
            return;
        }
        if (bestBag >= 0) {
            this.mob.swapHandWithSoldierBackpackSlot(EquipmentSlot.MAINHAND, bestBag);
        }
    }

    private void moveSecondPreferredToOffhand(
            @Nullable ResourceLocation prefMain,
            @Nullable ResourceLocation prefOff,
            @Nullable ResourceLocation keeperMain,
            @Nullable ResourceLocation keeperOff) {
        int bestScore = SoldierCombatItemPreference.GRADE_EMPTY;
        int bestBag = -1;
        boolean bestIsOff = false;

        int offScore =
                SoldierCombatItemPreference.scoreDual(
                        this.mob.getOffhandItem(), prefMain, prefOff, keeperMain, keeperOff);
        if (offScore >= OFFHAND_MIN_SCORE) {
            bestScore = offScore;
            bestIsOff = true;
        }
        NonNullList<ItemStack> bag = this.mob.soldierBackpackSlots();
        for (int i = 0; i < bag.size(); i++) {
            int s =
                    SoldierCombatItemPreference.scoreDual(
                            bag.get(i), prefMain, prefOff, keeperMain, keeperOff);
            if (s >= OFFHAND_MIN_SCORE && s > bestScore) {
                bestScore = s;
                bestBag = i;
                bestIsOff = false;
            }
        }
        if (bestScore < OFFHAND_MIN_SCORE || bestIsOff) {
            return;
        }
        if (bestBag >= 0) {
            this.clearOffhandIfJunk(prefMain, prefOff, keeperMain, keeperOff);
            this.mob.swapHandWithSoldierBackpackSlot(EquipmentSlot.OFFHAND, bestBag);
        }
    }

    private void clearOffhandIfJunk(
            @Nullable ResourceLocation prefMain,
            @Nullable ResourceLocation prefOff,
            @Nullable ResourceLocation keeperMain,
            @Nullable ResourceLocation keeperOff) {
        for (int n = 0; n < DROP_ATTEMPTS_MAX; n++) {
            ItemStack stack = this.mob.getOffhandItem();
            if (stack.isEmpty()) {
                return;
            }
            int s =
                    SoldierCombatItemPreference.scoreDual(
                            stack, prefMain, prefOff, keeperMain, keeperOff);
            if (s >= OFFHAND_JUNK_BELOW) {
                return;
            }
            if (this.mob.tryPutIntoSoldierBackpack(stack)) {
                this.mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                return;
            }
            if (!this.dropLowest(prefMain, prefOff, keeperMain, keeperOff)) {
                this.mob.spawnAtLocation(stack.copy());
                this.mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                return;
            }
        }
    }

    private boolean dropLowest(
            @Nullable ResourceLocation prefMain,
            @Nullable ResourceLocation prefOff,
            @Nullable ResourceLocation keeperMain,
            @Nullable ResourceLocation keeperOff) {
        int lowest = Integer.MAX_VALUE;
        int bagIdx = -1;
        boolean dropOff = false;

        ItemStack off = this.mob.getOffhandItem();
        if (!off.isEmpty()) {
            int s =
                    SoldierCombatItemPreference.scoreDual(
                            off, prefMain, prefOff, keeperMain, keeperOff);
            if (s < lowest) {
                lowest = s;
                dropOff = true;
                bagIdx = -1;
            }
        }
        NonNullList<ItemStack> bag = this.mob.soldierBackpackSlots();
        for (int i = 0; i < bag.size(); i++) {
            ItemStack stack = bag.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            int s =
                    SoldierCombatItemPreference.scoreDual(
                            stack, prefMain, prefOff, keeperMain, keeperOff);
            if (s < lowest) {
                lowest = s;
                dropOff = false;
                bagIdx = i;
            }
        }
        if (lowest == Integer.MAX_VALUE) {
            return false;
        }
        if (dropOff) {
            this.mob.spawnAtLocation(off.copy());
            this.mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            return true;
        }
        ItemStack dropped = bag.get(bagIdx).copy();
        bag.set(bagIdx, ItemStack.EMPTY);
        this.mob.spawnAtLocation(dropped);
        return true;
    }

    @Nullable
    private static ResourceLocation partnerPreferred(
            @Nullable ResourceLocation mainId,
            @Nullable ResourceLocation prefMain,
            @Nullable ResourceLocation prefOff) {
        if (mainId == null) {
            return null;
        }
        if (prefMain != null && prefMain.equals(mainId)) {
            return prefOff;
        }
        if (prefOff != null && prefOff.equals(mainId)) {
            return prefMain;
        }
        return null;
    }

    private static boolean isPreferredMain(
            ItemStack stack, @Nullable ResourceLocation prefMain) {
        if (prefMain == null) {
            return false;
        }
        ResourceLocation id = gunDataId(stack);
        return prefMain.equals(id);
    }

    @Nullable
    private static ResourceLocation gunDataId(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun == null ? null : gun.getGunId(stack);
    }
}
