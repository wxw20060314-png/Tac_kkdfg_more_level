package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.SoldierBackpack;
import com.kkdfg.Tacz_morelevel.entity.SoldierCombatItemPreference;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionInventory;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * 护航闲置主手整理：无目标 TTL 脉冲后，每次只扫背包连续 {@link SoldierBackpack#SLOTS} 格 + 双手，
 * 最高偏好放主手；副手尽量塞空格（不叠堆、不 compact、不丢物）。
 */
public final class CompanionIdleMainhandArrangeGoal extends Goal {

    private static final int WINDOW = SoldierBackpack.SLOTS;
    private static final EquipmentSlot DEST = EquipmentSlot.MAINHAND;
    private static final EquipmentSlot OTHER = EquipmentSlot.OFFHAND;

    private final AbstractFriendlyCompanionEntity mob;
    private int noTargetTicks;
    private int windowStart;

    public CompanionIdleMainhandArrangeGoal(AbstractFriendlyCompanionEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide()
                || this.mob.isOrderedToSit()
                || !this.mob.canOperateWeaponSwap()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            this.noTargetTicks = 0;
            return false;
        }
        this.noTargetTicks++;
        if (this.noTargetTicks < AbstractPlayerLikeSoldierEntity.NO_TARGET_WEAPON_ARRANGE_TTL) {
            return false;
        }
        this.noTargetTicks = 0;
        return true;
    }

    @Override
    public void start() {
        if (!this.isReadyForWindow()) {
            ResourceLocation preferred = this.mob.getPreferredMainGunDataId();
            ResourceLocation keeper = this.resolveKeeperGunId(preferred);
            this.moveBestToDest(preferred, keeper);
            this.clearOtherHand();
            this.mob.afterIdleMainhandArrange();
        }
        this.advanceWindow();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean isReadyForWindow() {
        if (!this.mob.getItemBySlot(OTHER).isEmpty()) {
            return false;
        }
        ItemStack held = this.mob.getItemBySlot(DEST);
        if (held.isEmpty()) {
            return false;
        }
        ResourceLocation preferred = this.mob.getPreferredMainGunDataId();
        ResourceLocation keeper = this.resolveKeeperGunId(preferred);
        int heldScore = SoldierCombatItemPreference.score(held, preferred, keeper);
        return heldScore == this.maxScoreInWindow(preferred, keeper);
    }

    private void moveBestToDest(
            @Nullable ResourceLocation preferred, @Nullable ResourceLocation keeper) {
        int bestScore = SoldierCombatItemPreference.GRADE_EMPTY;
        int bestBag = -1;
        EquipmentSlot bestHand = null;

        int mainScore =
                SoldierCombatItemPreference.score(this.mob.getMainHandItem(), preferred, keeper);
        if (mainScore > bestScore) {
            bestScore = mainScore;
            bestHand = EquipmentSlot.MAINHAND;
            bestBag = -1;
        }
        int offScore =
                SoldierCombatItemPreference.score(this.mob.getOffhandItem(), preferred, keeper);
        if (offScore > bestScore) {
            bestScore = offScore;
            bestHand = EquipmentSlot.OFFHAND;
            bestBag = -1;
        }
        CompanionInventory inv = this.mob.getCompanionInventory();
        int end = Math.min(this.windowStart + WINDOW, CompanionInventory.TOTAL_SIZE);
        for (int i = this.windowStart; i < end; i++) {
            int s = SoldierCombatItemPreference.score(inv.getItem(i), preferred, keeper);
            if (s > bestScore) {
                bestScore = s;
                bestHand = null;
                bestBag = i;
            }
        }
        if (bestScore == SoldierCombatItemPreference.GRADE_EMPTY) {
            return;
        }
        if (bestHand == DEST) {
            return;
        }
        if (bestHand != null) {
            this.mob.swapMainAndOffhand();
            return;
        }
        if (bestBag >= 0) {
            this.mob.swapHandWithCompanionInventorySlot(DEST, bestBag);
        }
    }

    private void clearOtherHand() {
        ItemStack stack = this.mob.getItemBySlot(OTHER);
        if (stack.isEmpty()) {
            return;
        }
        if (this.mob.tryPutIntoCompanionInventoryEmptySlot(stack)) {
            this.mob.setItemSlot(OTHER, ItemStack.EMPTY);
        }
    }

    private int maxScoreInWindow(
            @Nullable ResourceLocation preferred, @Nullable ResourceLocation keeper) {
        int max = SoldierCombatItemPreference.GRADE_EMPTY;
        max =
                Math.max(
                        max,
                        SoldierCombatItemPreference.score(
                                this.mob.getMainHandItem(), preferred, keeper));
        max =
                Math.max(
                        max,
                        SoldierCombatItemPreference.score(
                                this.mob.getOffhandItem(), preferred, keeper));
        CompanionInventory inv = this.mob.getCompanionInventory();
        int end = Math.min(this.windowStart + WINDOW, CompanionInventory.TOTAL_SIZE);
        for (int i = this.windowStart; i < end; i++) {
            max = Math.max(max, SoldierCombatItemPreference.score(inv.getItem(i), preferred, keeper));
        }
        return max;
    }

    @Nullable
    private ResourceLocation resolveKeeperGunId(@Nullable ResourceLocation preferred) {
        if (preferred != null && this.hasGunId(preferred)) {
            return preferred;
        }
        ItemStack main = this.mob.getMainHandItem();
        IGun mainGun = IGun.getIGunOrNull(main);
        if (mainGun != null) {
            return mainGun.getGunId(main);
        }
        ItemStack off = this.mob.getOffhandItem();
        IGun offGun = IGun.getIGunOrNull(off);
        if (offGun != null) {
            return offGun.getGunId(off);
        }
        CompanionInventory inv = this.mob.getCompanionInventory();
        int end = Math.min(this.windowStart + WINDOW, CompanionInventory.TOTAL_SIZE);
        for (int i = this.windowStart; i < end; i++) {
            IGun gun = IGun.getIGunOrNull(inv.getItem(i));
            if (gun != null) {
                return gun.getGunId(inv.getItem(i));
            }
        }
        return null;
    }

    private boolean hasGunId(ResourceLocation gunId) {
        if (this.gunMatches(this.mob.getMainHandItem(), gunId)
                || this.gunMatches(this.mob.getOffhandItem(), gunId)) {
            return true;
        }
        CompanionInventory inv = this.mob.getCompanionInventory();
        int end = Math.min(this.windowStart + WINDOW, CompanionInventory.TOTAL_SIZE);
        for (int i = this.windowStart; i < end; i++) {
            if (this.gunMatches(inv.getItem(i), gunId)) {
                return true;
            }
        }
        return false;
    }

    private boolean gunMatches(ItemStack stack, ResourceLocation gunId) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun != null && gunId.equals(gun.getGunId(stack));
    }

    private void advanceWindow() {
        this.windowStart += WINDOW;
        if (this.windowStart >= CompanionInventory.TOTAL_SIZE) {
            this.windowStart = 0;
        }
    }
}
