package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseEntity;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/** 机械马自己没有仇人、主人还在时，抄主人的目标。不占用控制标记。 */
public class MechanicalHorseShareOwnerTargetGoal extends Goal {

    private final MechanicalHorseEntity horse;

    public MechanicalHorseShareOwnerTargetGoal(MechanicalHorseEntity horse) {
        this.horse = horse;
    }

    @Override
    public boolean canUse() {
        return this.sharedTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        LivingEntity shared = this.sharedTarget();
        if (shared != null) {
            this.horse.setTarget(shared);
        }
    }

    @Nullable
    private LivingEntity sharedTarget() {
        if (this.horse.getTarget() != null) {
            return null;
        }
        if (!(this.livingOwner() instanceof Mob owner)) {
            return null;
        }
        LivingEntity shared = owner.getTarget();
        UUID ownerId = this.horse.getOwnerUUID();
        if (shared == null
                || !shared.isAlive()
                || shared == this.horse
                || (ownerId != null && ownerId.equals(shared.getUUID()))) {
            return null;
        }
        return shared;
    }

    @Nullable
    private LivingEntity livingOwner() {
        if (!(this.horse.level() instanceof ServerLevel server)) {
            return null;
        }
        UUID ownerId = this.horse.getOwnerUUID();
        if (ownerId == null) {
            return null;
        }
        Entity owner = server.getEntity(ownerId);
        return owner instanceof LivingEntity living && living.isAlive() ? living : null;
    }
}
