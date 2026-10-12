package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.AbstractHolsterGunSoldierEntity;
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
 * 枪套闲置整理：无存活目标连续 {@link AbstractPlayerLikeSoldierEntity#NO_TARGET_WEAPON_ARRANGE_TTL}
 * tick 后，整理为副手偏好枪、主手空（偏好 null=任意枪）。
 */
public final class MobGunIdleHolsterGoal extends Goal {

    private final AbstractHolsterGunSoldierEntity mob;
    private int noTargetTicks;

    public MobGunIdleHolsterGoal(AbstractHolsterGunSoldierEntity mob) {
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
        return this.noTargetTicks >= AbstractPlayerLikeSoldierEntity.NO_TARGET_WEAPON_ARRANGE_TTL
                && !this.isReady();
    }

    @Override
    public void start() {
        if (this.isReady()) {
            return;
        }
        // 扫描：主 → 副 → 包；目标手=副手
        this.ensurePreferredInOffhand();
        this.stashHand(EquipmentSlot.MAINHAND);
        this.mob.applyIdleHolsterEffects();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** 主空 + 副手偏好（或任意）枪。 */
    private boolean isReady() {
        return this.mob.getMainHandItem().isEmpty() && this.matchesPreferred(this.mob.getOffhandItem());
    }

    private boolean matchesPreferred(@Nullable ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null || stack == null) {
            return false;
        }
        ResourceLocation preferred = this.mob.getPreferredMainGunDataId();
        return preferred == null || preferred.equals(gun.getGunId(stack));
    }

    private void ensurePreferredInOffhand() {
        EquipmentSlot dest = EquipmentSlot.OFFHAND;
        if (this.matchesPreferred(this.mob.getItemBySlot(dest))) {
            return;
        }
        if (this.moveMatchingTo(dest, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, true)) {
            return;
        }
        if (this.mob.tryTakeGunFromSoldierBackpack(this.mob.getPreferredMainGunDataId(), dest)) {
            return;
        }
        if (this.moveMatchingTo(dest, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, false)) {
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
