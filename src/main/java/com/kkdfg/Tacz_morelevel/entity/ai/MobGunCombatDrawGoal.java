package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.SoldierBackpack;
import com.kkdfg.Tacz_morelevel.entity.SoldierCombatItemPreference;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractHolsterGunSoldierEntity;
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
 * 枪套战斗整理：有存活目标时，最高战斗偏好物品放主手，副手清空（塞包 / 丢最低分）。
 * 主手基础等级 &gt; 40 且副手已空则不扫。
 */
public final class MobGunCombatDrawGoal extends Goal {

    private static final int DROP_ATTEMPTS_MAX = SoldierBackpack.SLOTS + 4;
    private static final EquipmentSlot DEST = EquipmentSlot.MAINHAND;
    private static final EquipmentSlot OTHER = EquipmentSlot.OFFHAND;

    private final AbstractHolsterGunSoldierEntity mob;

    public MobGunCombatDrawGoal(AbstractHolsterGunSoldierEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !this.mob.canOperateWeaponSwap()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || this.isReady()) {
            return false;
        }
        int mainGrade =
                SoldierCombatItemPreference.baseGrade(
                        this.mob.getMainHandItem(), this.mob.getPreferredMainGunDataId());
        if (mainGrade > SoldierCombatItemPreference.GRADE_OTHER_GUN
                && this.mob.getOffhandItem().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        if (this.isReady()) {
            return;
        }
        boolean hadMainGunBefore = IGun.mainHandHoldGun(this.mob);
        this.mob.compactSoldierInventory();
        ResourceLocation preferred = this.mob.getPreferredMainGunDataId();
        ResourceLocation keeper = this.resolveKeeperGunId(preferred);
        this.moveBestToDest(preferred, keeper);
        this.clearOtherHand(preferred, keeper);
        if (IGun.mainHandHoldGun(this.mob)) {
            this.mob.applyCombatDrawEffects();
        } else if (hadMainGunBefore) {
            this.mob.reassessWeaponGoal();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean isReady() {
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
        return heldScore == this.maxScore(preferred, keeper);
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
        NonNullList<ItemStack> bag = this.mob.soldierBackpackSlots();
        for (int i = 0; i < bag.size(); i++) {
            int s = SoldierCombatItemPreference.score(bag.get(i), preferred, keeper);
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
            this.mob.swapHandWithSoldierBackpackSlot(DEST, bestBag);
        }
    }

    private void clearOtherHand(
            @Nullable ResourceLocation preferred, @Nullable ResourceLocation keeper) {
        for (int n = 0; n < DROP_ATTEMPTS_MAX; n++) {
            ItemStack stack = this.mob.getItemBySlot(OTHER);
            if (stack.isEmpty()) {
                return;
            }
            if (this.mob.tryPutIntoSoldierBackpack(stack)) {
                this.mob.setItemSlot(OTHER, ItemStack.EMPTY);
                return;
            }
            if (!this.dropLowest(preferred, keeper)) {
                this.mob.spawnAtLocation(stack.copy());
                this.mob.setItemSlot(OTHER, ItemStack.EMPTY);
                return;
            }
        }
    }

    private boolean dropLowest(
            @Nullable ResourceLocation preferred, @Nullable ResourceLocation keeper) {
        int lowest = Integer.MAX_VALUE;
        int bagIdx = -1;
        boolean dropOtherHand = false;

        ItemStack otherStack = this.mob.getItemBySlot(OTHER);
        if (!otherStack.isEmpty()) {
            int s = SoldierCombatItemPreference.score(otherStack, preferred, keeper);
            if (s < lowest) {
                lowest = s;
                dropOtherHand = true;
                bagIdx = -1;
            }
        }
        NonNullList<ItemStack> bag = this.mob.soldierBackpackSlots();
        for (int i = 0; i < bag.size(); i++) {
            ItemStack stack = bag.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            int s = SoldierCombatItemPreference.score(stack, preferred, keeper);
            if (s < lowest) {
                lowest = s;
                dropOtherHand = false;
                bagIdx = i;
            }
        }
        if (lowest == Integer.MAX_VALUE) {
            return false;
        }
        if (dropOtherHand) {
            this.mob.spawnAtLocation(otherStack.copy());
            this.mob.setItemSlot(OTHER, ItemStack.EMPTY);
            return true;
        }
        ItemStack dropped = bag.get(bagIdx).copy();
        bag.set(bagIdx, ItemStack.EMPTY);
        this.mob.spawnAtLocation(dropped);
        return true;
    }

    private int maxScore(
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
        for (ItemStack stack : this.mob.soldierBackpackSlots()) {
            max = Math.max(max, SoldierCombatItemPreference.score(stack, preferred, keeper));
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
        for (ItemStack stack : this.mob.soldierBackpackSlots()) {
            IGun gun = IGun.getIGunOrNull(stack);
            if (gun != null) {
                return gun.getGunId(stack);
            }
        }
        return null;
    }

    private boolean hasGunId(ResourceLocation gunId) {
        return this.gunMatches(this.mob.getMainHandItem(), gunId)
                || this.gunMatches(this.mob.getOffhandItem(), gunId)
                || SoldierBackpack.containsGun(this.mob.soldierBackpackSlots(), gunId);
    }

    private boolean gunMatches(ItemStack stack, ResourceLocation gunId) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun != null && gunId.equals(gun.getGunId(stack));
    }
}
