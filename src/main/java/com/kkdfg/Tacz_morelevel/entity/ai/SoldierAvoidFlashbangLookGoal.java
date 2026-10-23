package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.FlashbangEntity;
import java.util.EnumSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * 背闪：识别到闪光戳后锁定该闪 UUID，最多跟踪 {@link #TRACK_TICKS} tick；
 * 用 {@link net.minecraft.world.entity.ai.control.LookControl#setLookAt} 转向背对方向（非强制写朝向）。
 * 实体消失或超时结束；一次只处理一颗闪。
 */
public final class SoldierAvoidFlashbangLookGoal<T extends PathfinderMob & SoldierThrowableThreatSense>
        extends Goal {

    private static final int TRACK_TICKS = 5;

    private final T mob;
    @Nullable private UUID flashUuid;
    private int ticksLeft;

    public SoldierAvoidFlashbangLookGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !this.mob.isEffectiveAi()) {
            return false;
        }
        if (this.mob.getThrowableThreatKind() != ThrowableThreatKind.FLASHBANG) {
            return false;
        }
        Entity threat = SoldierThrowableThreatScan.resolveValidThreat(this.mob, this.mob);
        return threat instanceof FlashbangEntity;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.ticksLeft <= 0 || this.flashUuid == null) {
            return false;
        }
        return this.findTrackedFlash() != null;
    }

    @Override
    public void start() {
        Entity threat = SoldierThrowableThreatScan.resolveValidThreat(this.mob, this.mob);
        if (threat instanceof FlashbangEntity flash) {
            this.flashUuid = flash.getUUID();
            this.ticksLeft = TRACK_TICKS;
            this.lookAwayFromFlash();
        } else {
            this.flashUuid = null;
            this.ticksLeft = 0;
        }
    }

    @Override
    public void tick() {
        this.ticksLeft--;
        this.lookAwayFromFlash();
    }

    @Override
    public void stop() {
        this.flashUuid = null;
        this.ticksLeft = 0;
    }

    @Nullable
    private FlashbangEntity findTrackedFlash() {
        if (this.flashUuid == null || !(this.mob.level() instanceof ServerLevel server)) {
            return null;
        }
        Entity e = server.getEntity(this.flashUuid);
        if (e instanceof FlashbangEntity flash && flash.isAlive() && !flash.isRemoved()) {
            return flash;
        }
        return null;
    }

    private void lookAwayFromFlash() {
        FlashbangEntity flash = this.findTrackedFlash();
        if (flash == null) {
            return;
        }
        Vec3 eyes = this.mob.getEyePosition();
        Vec3 away = eyes.subtract(flash.position());
        if (away.lengthSqr() < 1.0E-6D) {
            away = this.mob.getLookAngle().scale(-1.0D);
        }
        Vec3 look = eyes.add(away.normalize().scale(8.0D));
        this.mob.getLookControl().setLookAt(look.x, look.y, look.z, 30.0F, 30.0F);
    }
}
