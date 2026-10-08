package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MolotovEntity;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** {@link net.minecraft.world.entity.ai.goal.FloatGoal} */
public final class CompanionAvoidGrenadeAndFireGoal extends Goal {
    private static final double TRIGGER_DISTANCE = 9.0D;
    private static final double TRIGGER_DIST_SQ = TRIGGER_DISTANCE * TRIGGER_DISTANCE;
    private static final int FIRE_SCAN_RADIUS = 4;

    private final AbstractFriendEntity mob;
    @Nullable private Vec3 threatPos;

    public CompanionAvoidGrenadeAndFireGoal(AbstractFriendEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !this.mob.isEffectiveAi()) {
            return false;
        }
        this.threatPos = findNearestThreat(this.mob);
        return this.threatPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.level().isClientSide()) {
            return false;
        }
        this.threatPos = findNearestThreat(this.mob);
        return this.threatPos != null;
    }

    @Override
    public void start() {
        tryMoveAway();
    }

    @Override
    public void tick() {
        if (this.mob.getNavigation().isDone() || this.mob.tickCount % 15 == 0) {
            this.threatPos = findNearestThreat(this.mob);
            if (this.threatPos != null) {
                tryMoveAway();
            }
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.threatPos = null;
    }

    private void tryMoveAway() {
        if (this.threatPos == null) {
            return;
        }
        Vec3 dest = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.threatPos);
        if (dest != null) {
            this.mob.getNavigation().moveTo(dest.x, dest.y, dest.z, 1.25D);
        } else {
            Vec3 away = this.mob.position().subtract(this.threatPos);
            if (away.lengthSqr() < 1.0E-6D) {
                away = new Vec3(0.0D, 0.0D, 1.0D);
            }
            away = away.normalize().scale(10.0D);
            Vec3 manual = this.mob.position().add(away);
            this.mob.getNavigation().moveTo(manual.x, manual.y, manual.z, 1.25D);
        }
    }

    @Nullable
    private static Vec3 findNearestThreat(AbstractFriendEntity mob) {
        Level level = mob.level();
        Vec3 core = mob.position().add(0.0D, mob.getBbHeight() * 0.35D, 0.0D);
        Vec3 best = null;
        double bestSq = TRIGGER_DIST_SQ;

        AABB box = mob.getBoundingBox().inflate(TRIGGER_DISTANCE);
        for (GrenadeEntity g : level.getEntitiesOfClass(GrenadeEntity.class, box, e -> e.isAlive() && !e.isRemoved())) {
            double d2 = core.distanceToSqr(g.position());
            if (d2 < bestSq) {
                bestSq = d2;
                best = g.position();
            }
        }
        for (MolotovEntity m : level.getEntitiesOfClass(MolotovEntity.class, box, e -> e.isAlive() && !e.isRemoved())) {
            double d2 = core.distanceToSqr(m.position());
            if (d2 < bestSq) {
                bestSq = d2;
                best = m.position();
            }
        }

        if (shouldScanFireThisTick(mob)) {
            BlockPos origin = mob.blockPosition();
            for (int dx = -FIRE_SCAN_RADIUS; dx <= FIRE_SCAN_RADIUS; dx++) {
                for (int dz = -FIRE_SCAN_RADIUS; dz <= FIRE_SCAN_RADIUS; dz++) {
                    for (int dy = -1; dy <= 3; dy++) {
                        BlockPos p = origin.offset(dx, dy, dz);
                        if (!level.hasChunkAt(p)) {
                            continue;
                        }
                        BlockState s = level.getBlockState(p);
                        if (!isDangerFire(s)) {
                            continue;
                        }
                        Vec3 fp = Vec3.atCenterOf(p);
                        double d2 = core.distanceToSqr(fp);
                        if (d2 < bestSq) {
                            bestSq = d2;
                            best = fp;
                        }
                    }
                }
            }
        }

        return best;
    }

    
    private static boolean shouldScanFireThisTick(AbstractFriendEntity mob) {
        return (mob.tickCount + mob.getId()) % 2 == 0;
    }

    private static boolean isDangerFire(BlockState s) {
        return s.is(Blocks.FIRE)
                || s.is(Blocks.SOUL_FIRE)
                || s.is(ModBlocks.MOLOTOV_FIRE.get());
    }
}
