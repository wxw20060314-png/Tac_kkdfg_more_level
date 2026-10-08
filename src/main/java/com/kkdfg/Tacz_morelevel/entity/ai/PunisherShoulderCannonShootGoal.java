package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.PunisherShoulderCannonEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.PunisherShoulderCannonShoot;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** 宿主有仇恨/交战目标时：炮口朝向并打 RPG。 */
public final class PunisherShoulderCannonShootGoal extends Goal {

    private final PunisherShoulderCannonEntity cannon;

    public PunisherShoulderCannonShootGoal(PunisherShoulderCannonEntity cannon) {
        this.cannon = cannon;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.resolve() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.resolve() != null;
    }

    @Override
    public void stop() {
        this.cannon.setTarget(null);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity host = this.cannon.getHost();
        LivingEntity target = this.resolve();
        if (host == null || target == null) {
            return;
        }
        this.cannon.setTarget(target);
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.cannon, target);
        this.cannon.yBodyRot = this.cannon.getYRot();
        boolean seen = PunisherShoulderCannonShoot.canSee(this.cannon, target);
        this.cannon.tickPlayerTargetLos(host, target, seen);
        if (!seen) {
            return;
        }
        if (this.cannon.tickCount == 0
                || this.cannon.tickCount % PunisherShoulderCannonEntity.FIRE_INTERVAL_TICKS != 0) {
            return;
        }
        PunisherShoulderCannonShoot.tryFire(this.cannon, host);
    }

    private LivingEntity resolve() {
        LivingEntity host = this.cannon.getHost();
        if (host == null) {
            return null;
        }
        return PunisherShoulderCannonShoot.resolveTarget(this.cannon, host);
    }
}
