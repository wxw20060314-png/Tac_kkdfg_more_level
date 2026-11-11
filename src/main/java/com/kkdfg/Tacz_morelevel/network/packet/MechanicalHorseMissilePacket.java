package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseEntity;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/** 玩家按键 → 骑着的机械马朝视线落点发射。不套用牛仔的 10 格限制。 */
public final class MechanicalHorseMissilePacket {

    /** 与 {@code MechanicalHorseMissileLauncher} 的最远距离一致。 */
    private static final double LOOK_RANGE = 256.0D;
    private static final double AIM_ASSIST_HALF_WIDTH = 1.5D;

    public MechanicalHorseMissilePacket() {}

    public static void encode(MechanicalHorseMissilePacket msg, FriendlyByteBuf buf) {}

    public static MechanicalHorseMissilePacket decode(FriendlyByteBuf buf) {
        return new MechanicalHorseMissilePacket();
    }

    public static void handle(MechanicalHorseMissilePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(
                () -> {
                    ServerPlayer sender = ctx.getSender();
                    if (sender != null && sender.getVehicle() instanceof MechanicalHorseEntity horse && horse.isAlive()) {
                        fireAtAim(sender, horse);
                    }
                });
        ctx.setPacketHandled(true);
    }

    private static void fireAtAim(ServerPlayer player, MechanicalHorseEntity horse) {
        Vec3 start = player.getEyePosition();
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 end = start.add(forward.scale(LOOK_RANGE));
        HitResult block = player.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 blockPoint = block.getType() == HitResult.Type.MISS ? end : block.getLocation();

        LivingEntity target = findAimTarget(player, horse, start, blockPoint, forward);
        if (target != null) {
            horse.tryFireMissiles(target);
        } else {
            horse.tryFireMissiles(blockPoint);
        }
    }

    @Nullable
    private static LivingEntity findAimTarget(ServerPlayer player, MechanicalHorseEntity horse,
            Vec3 start, Vec3 end, Vec3 forward) {
        double maxDistance = start.distanceTo(end);
        if (maxDistance == 0.0D) {
            return null;
        }

        AABB search = new AABB(start, end).inflate(AIM_ASSIST_HALF_WIDTH * Math.sqrt(2.0D));
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, search,
                living -> living != player && living.isPickable() && horse.canLockMissileTarget(living));

        LivingEntity direct = null;
        double closestHitSqr = maxDistance * maxDistance;
        for (LivingEntity candidate : candidates) {
            AABB bounds = candidate.getBoundingBox();
            Vec3 hit = bounds.contains(start) ? start : bounds.clip(start, end).orElse(null);
            if (hit != null) {
                double distanceSqr = start.distanceToSqr(hit);
                if (distanceSqr < closestHitSqr) {
                    closestHitSqr = distanceSqr;
                    direct = candidate;
                }
            }
        }
        if (direct != null) {
            return direct;
        }

        // Use the player's yaw for the horizontal axis even when looking straight up or down.
        Vec3 right = Vec3.directionFromRotation(0.0F, player.getYRot() - 90.0F).normalize();
        Vec3 up = right.cross(forward).normalize();
        LivingEntity best = null;
        double bestOffsetSqr = Double.POSITIVE_INFINITY;
        double bestDepth = maxDistance;
        for (LivingEntity candidate : candidates) {
            Vec3 center = candidate.getBoundingBox().getCenter();
            Vec3 offset = center.subtract(start);
            double depth = offset.dot(forward);
            double horizontal = offset.dot(right);
            double vertical = offset.dot(up);
            if (depth <= 0.0D || depth >= maxDistance
                    || Math.abs(horizontal) > AIM_ASSIST_HALF_WIDTH || Math.abs(vertical) > AIM_ASSIST_HALF_WIDTH) {
                continue;
            }

            double offsetSqr = horizontal * horizontal + vertical * vertical;
            if (offsetSqr > bestOffsetSqr || (offsetSqr == bestOffsetSqr && depth >= bestDepth)) {
                continue;
            }
            HitResult obstruction = player.level().clip(new ClipContext(
                    start, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (obstruction.getType() == HitResult.Type.MISS) {
                best = candidate;
                bestOffsetSqr = offsetSqr;
                bestDepth = depth;
            }
        }
        return best;
    }
}
