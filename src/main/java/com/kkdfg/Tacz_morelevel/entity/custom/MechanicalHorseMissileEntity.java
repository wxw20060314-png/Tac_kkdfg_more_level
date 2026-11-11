package com.kkdfg.Tacz_morelevel.entity.custom;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.ExplodeUtil;
import com.tacz.guns.util.TacHitResult;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class MechanicalHorseMissileEntity extends EntityKineticBullet implements GeoEntity {
    private static final int FLIGHT_TICKS = 180;
    private static final int EXIT_TICKS = 2;
    private static final double CRUISE_SPEED = 1.6D;
    private static final double DIVE_SPEED = 2.1D;
    private static final double SEEK_COS = Math.cos(Math.toRadians(80.0D));
    private static final float EXPLOSION_RADIUS = 6.0F;
    private static final float EXPLOSION_DAMAGE = 24.0F;
    private static final ResourceLocation RPG_GUN = ResourceLocation.fromNamespaceAndPath("tacz", "rpg7");
    private static final ResourceLocation RPG_AMMO = ResourceLocation.fromNamespaceAndPath("tacz", "rpg_rocket");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable private UUID protectedOwner;
    @Nullable private UUID trackingTarget;
    @Nullable private Vec3 aimPoint;
    private Vec3 launchOrigin = Vec3.ZERO;
    private boolean topAttack;
    private boolean diving;
    private boolean acquiredApproach;

    public MechanicalHorseMissileEntity(EntityType<? extends MechanicalHorseMissileEntity> type, Level level) {
        super(type, level);
    }

    void launch(MechanicalHorseEntity horse, Vec3 muzzle, Vec3 direction, Vec3 target,
            @Nullable UUID tracked, boolean top) {
        this.setOwner(horse);
        this.protectedOwner = horse.getOwnerUUID();
        if (this.protectedOwner == null && horse.getFirstPassenger() instanceof LivingEntity rider) {
            this.protectedOwner = rider.getUUID();
        }
        this.launchOrigin = horse.position();
        this.aimPoint = target;
        this.trackingTarget = tracked;
        this.topAttack = top;
        this.setPos(muzzle.x, muzzle.y, muzzle.z);
        this.setDeltaMovement(direction.scale(CRUISE_SPEED));
    }

    @Override
    public ResourceLocation getGunId() {
        return RPG_GUN;
    }

    @Override
    public ResourceLocation getAmmoId() {
        return RPG_AMMO;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && !this.isRemoved() && this.tickCount >= FLIGHT_TICKS) {
            this.detonate(this.position());
        }
    }

    @Override
    protected void onBulletTick() {
        if (!(this.level() instanceof ServerLevel level) || this.isRemoved()) {
            return;
        }

        if (this.trackingTarget != null) {
            Entity entity = level.getEntity(this.trackingTarget);
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                this.detonate(this.position());
                return;
            }
            Vec3 center = living.getBoundingBox().getCenter();
            if (!MechanicalHorseMissileLauncher.inRange(this.launchOrigin, center)) {
                this.detonate(this.position());
                return;
            }
            this.aimPoint = center;
        }

        this.steer(level);
        if (!this.isRemoved()) {
            super.onBulletTick();
        }
    }

    private void steer(ServerLevel level) {
        Vec3 current = this.getDeltaMovement().normalize();
        double speed = this.diving ? DIVE_SPEED : CRUISE_SPEED;
        if (this.tickCount <= EXIT_TICKS || this.aimPoint == null) {
            this.setDeltaMovement(current.scale(speed));
            return;
        }

        Vec3 target = this.aimPoint;
        Vec3 offset = target.subtract(this.position());
        if (this.topAttack && !this.diving) {
            double loft = Mth.clamp(this.launchOrigin.subtract(target).horizontalDistance() * 0.25D, 4.0D, 14.0D);
            double ceiling = level.getMaxBuildHeight() - 1.0D;
            double loftY = Math.min(ceiling, Math.max(target.y, this.launchOrigin.y) + loft);
            double diveHeight = Math.min(3.0D, (loftY - target.y) * 0.75D);
            this.diving = this.tickCount >= 42
                    || (this.tickCount > EXIT_TICKS + 1
                            && offset.horizontalDistance() < 13.0D
                            && this.getY() >= target.y + diveHeight);
            if (!this.diving) {
                offset = new Vec3(target.x, loftY, target.z).subtract(this.position());
            }
        }

        Vec3 desired = offset.normalize();
        double alignment = current.dot(desired);
        boolean finalApproach = !this.topAttack || this.diving;
        double seekLimit = this.diving ? 0.0D : SEEK_COS;
        // Let the missile turn onto its approach before treating an off-axis aim point as lost.
        if (desired.lengthSqr() == 0.0D || (finalApproach && this.acquiredApproach && alignment < seekLimit)) {
            if (this.trackingTarget != null) {
                this.detonate(this.position());
                return;
            }
            this.aimPoint = null;
        } else {
            this.acquiredApproach |= finalApproach && alignment >= seekLimit;
            double turn = Math.toRadians(this.diving ? 28.0D : this.topAttack ? 12.0D : 10.0D);
            current = turnTowards(current, desired, turn);
        }
        double flightSpeed = CRUISE_SPEED;
        if (this.diving) {
            // Keep a close shot from overshooting while it is still turning downward.
            flightSpeed = this.aimPoint == null ? DIVE_SPEED
                    : Mth.lerp(Mth.clamp(alignment, 0.0D, 1.0D), 0.75D, DIVE_SPEED);
        }
        this.setDeltaMovement(current.scale(flightSpeed));
    }

    private static Vec3 turnTowards(Vec3 current, Vec3 desired, double maxAngle) {
        double dot = Mth.clamp(current.dot(desired), -1.0D, 1.0D);
        if (dot >= Math.cos(maxAngle)) {
            return desired;
        }
        Vec3 tangent = desired.subtract(current.scale(dot)).normalize();
        if (tangent.lengthSqr() == 0.0D) {
            Vec3 axis = Math.abs(current.y) < 0.9D ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            tangent = current.cross(axis).normalize();
        }
        return current.scale(Math.cos(maxAngle)).add(tangent.scale(Math.sin(maxAngle))).normalize();
    }

    @Override
    protected void onHitEntity(TacHitResult hit, Vec3 start, Vec3 end) {
        this.detonate(hit.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult hit, Vec3 start, Vec3 end) {
        if (hit.getType() == HitResult.Type.BLOCK) {
            this.detonate(hit.getLocation());
        }
    }

    private void detonate(Vec3 hit) {
        if (this.isRemoved() || this.level().isClientSide()) {
            return;
        }
        ExplodeUtil.createExplosion(this.getOwner(), this, EXPLOSION_DAMAGE,
                EXPLOSION_RADIUS, true, true, hit);
        this.discard();
    }

    @Nullable
    public UUID blastProtectedOwner() {
        return this.protectedOwner;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
