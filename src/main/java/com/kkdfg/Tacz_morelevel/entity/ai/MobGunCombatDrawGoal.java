package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.AbstractHolsterGunSoldierEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * 枪套战斗整理：有存活目标时立即整理为主手偏好枪、副手空（偏好 null=任意枪）。
 * 后续丢雷等战斗扩展写在本类。
 */
public final class MobGunCombatDrawGoal extends Goal {

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
        return target != null && target.isAlive() && !this.isReady();
    }

    @Override
    public void start() {
        if (this.isReady()) {
            return;
        }
        // 扫描：副 → 主 → 包；目标手=主手
        boolean hadMainGunBefore = IGun.mainHandHoldGun(this.mob);
        this.ensurePreferredInMainhand();
        this.stashHand(EquipmentSlot.OFFHAND);
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

    /** 副空 + 主手偏好（或任意）枪。 */
    private boolean isReady() {
        return this.mob.getOffhandItem().isEmpty() && this.matchesPreferred(this.mob.getMainHandItem());
    }

    private boolean matchesPreferred(@Nullable ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null || stack == null) {
            return false;
        }
        ResourceLocation preferred = this.mob.getPreferredMainGunDataId();
        return preferred == null || preferred.equals(gun.getGunId(stack));
    }

    private void ensurePreferredInMainhand() {
        EquipmentSlot dest = EquipmentSlot.MAINHAND;
        if (this.matchesPreferred(this.mob.getItemBySlot(dest))) {
            return;
        }
        if (this.moveMatchingTo(dest, EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND, true)) {
            return;
        }
        if (this.mob.tryTakeGunFromSoldierBackpack(this.mob.getPreferredMainGunDataId(), dest)) {
            return;
        }
        if (this.moveMatchingTo(dest, EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND, false)) {
            return;
        }
        this.mob.tryTakeGunFromSoldierBackpack(null, dest);
    }

    private boolean moveMatchingTo(
            EquipmentSlot dest, EquipmentSlot first, EquipmentSlot second, boolean preferredOnly) {
        if (this.handMatches(first, preferredOnly)) {
            if (first != dest) {
                this.mob.swapMainAndOffhand();
            }
            return this.handMatches(dest, preferredOnly);
        }
        if (this.handMatches(second, preferredOnly)) {
            if (second != dest) {
                this.mob.swapMainAndOffhand();
            }
            return this.handMatches(dest, preferredOnly);
        }
        return false;
    }

    private boolean handMatches(EquipmentSlot slot, boolean preferredOnly) {
        ItemStack stack = this.mob.getItemBySlot(slot);
        return preferredOnly ? this.matchesPreferred(stack) : IGun.getIGunOrNull(stack) != null;
    }

    private void stashHand(EquipmentSlot hand) {
        ItemStack stack = this.mob.getItemBySlot(hand);
        if (stack.isEmpty()) {
            return;
        }
        if (this.mob.tryPutIntoSoldierBackpack(stack)) {
            this.mob.setItemSlot(hand, ItemStack.EMPTY);
        }
    }
}
