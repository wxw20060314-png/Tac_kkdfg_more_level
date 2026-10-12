package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 炸弹宝宝榴弹切枪：{@link BombBabySight} 连续不通达 {@link #NO_SIGHT_TICKS} → M320；
 * 视线恢复 → 切回 MK14。不占 Flag，与 Move/Fire 并行。
 */
public final class BombBabyGrenadeWeaponSwapGoal extends Goal {

    public static final int NO_SIGHT_TICKS = 20;

    private final BombBabyEntity mob;
    private int noSightTicks;

    public BombBabyGrenadeWeaponSwapGoal(BombBabyEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
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
        this.noSightTicks = 0;
    }

    @Override
    public void stop() {
        this.noSightTicks = 0;
        if (!this.mob.level().isClientSide() && this.mob.isHoldingGrenadeLauncher()) {
            this.mob.ensurePrimaryGunDrawn();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            this.noSightTicks = 0;
            return;
        }
        boolean sight = BombBabySight.hasLineOfSight(this.mob, target);
        if (sight) {
            this.noSightTicks = 0;
            if (this.mob.isHoldingGrenadeLauncher()) {
                this.mob.ensurePrimaryGunDrawn();
            }
            return;
        }
        this.noSightTicks++;
        if (this.noSightTicks < NO_SIGHT_TICKS && !this.mob.isHoldingGrenadeLauncher()) {
            return;
        }
        if (!this.mob.isHoldingGrenadeLauncher() && this.mob.hasBackGun()) {
            this.mob.ensureGrenadeLauncherDrawn();
        }
    }
}
