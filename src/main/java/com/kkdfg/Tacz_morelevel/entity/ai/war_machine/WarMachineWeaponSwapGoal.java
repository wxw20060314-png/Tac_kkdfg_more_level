package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 战争机器切枪：严格看见持续 → 加特林；严格看不见持续 → RPG（不设穿透上限）。
 * 寻敌仍由 {@link WarMachineTargetGoal} 用 RPG 穿透档；本 Goal 只看严格视线 + 持续 tick。
 * 认枪用实体偏好标记（main / offhand），不写死常量。
 * 不占 Flag，与 Fire/Move 并行。
 */
public final class WarMachineWeaponSwapGoal extends Goal {

    public static final int STRICT_SEE_TICKS_FOR_MINIGUN = 40;
    public static final int NO_STRICT_SEE_TICKS_FOR_RPG = 10;

    private static final int SWAP_COOLDOWN_TICKS = 4;

    private final WarMachineEntity mob;
    private int swapCooldown;
    private int strictSeeTime;
    private int noStrictSeeTime;
    private boolean lastStrictSee;

    public WarMachineWeaponSwapGoal(WarMachineEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null
                && target.isAlive()
                && IGun.mainHandHoldGun(this.mob)
                && this.mob.hasPreferredPairInHands();
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
    public void start() {
        this.swapCooldown = 0;
        this.strictSeeTime = 0;
        this.noStrictSeeTime = 0;
        this.lastStrictSee = false;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        if (this.swapCooldown > 0) {
            this.swapCooldown--;
        }
        boolean strictSee = WarMachineGunSight.hasStrictLineOfSight(this.mob, target);
        if (strictSee != this.lastStrictSee) {
            this.strictSeeTime = 0;
            this.noStrictSeeTime = 0;
        } else if (strictSee) {
            this.strictSeeTime++;
        } else {
            this.noStrictSeeTime++;
        }
        this.lastStrictSee = strictSee;

        boolean wantMinigun = strictSee && this.strictSeeTime >= STRICT_SEE_TICKS_FOR_MINIGUN;
        boolean wantRpg = !strictSee && this.noStrictSeeTime >= NO_STRICT_SEE_TICKS_FOR_RPG;
        if (this.mob.level().isClientSide()
                || this.swapCooldown > 0
                || !this.mob.canOperateWeaponSwap()
                || !this.mob.hasPreferredPairInHands()) {
            return;
        }
        boolean holdingMinigun = this.mob.isHoldingPreferredMainGun();
        boolean holdingRpg = this.mob.isHoldingPreferredOffhandGun();
        if (wantMinigun && !holdingMinigun && holdingRpg) {
            if (this.mob.swapActiveAndBackGun()) {
                this.swapCooldown = SWAP_COOLDOWN_TICKS;
            }
        } else if (wantRpg && !holdingRpg && holdingMinigun) {
            if (this.mob.swapActiveAndBackGun()) {
                this.swapCooldown = SWAP_COOLDOWN_TICKS;
            }
        }
    }
}
