package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RidingMoveControl implements Control {
   public static final float MIN_SPEED = 5.0E-4F;
   public static final float MIN_SPEED_SQR = 2.5000003E-7F;
   protected static final int MAX_TURN = 90;
   private static final double COLLISION_EPSILON = 1.0E-7D;
   private static final double MAX_STRAFE_CHECK_DISTANCE = 2.0D;
   protected final RidingPathSource rider;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;
   protected double speedModifier;
   protected float strafeForwards;
   protected float strafeRight;
   protected RidingMoveControl.Operation operation = RidingMoveControl.Operation.WAIT;

   public RidingMoveControl(RidingPathSource rider) {
      this.rider = rider;
   }

   /** 这一拍骑着的马。没骑、或坐骑不是马类时为空。 */
   @Nullable
   private AbstractHorse currentMount() {
      if (this.rider instanceof Mob mob && mob.getVehicle() instanceof AbstractHorse horse) {
         return horse;
      }
      return null;
   }

   /**
    * @return If the mob is currently trying to go somewhere
    */
   public boolean hasWanted() {
      return this.operation == RidingMoveControl.Operation.MOVE_TO;
   }

   public double getSpeedModifier() {
      return this.speedModifier;
   }

   /**
    * Sets the speed and location to move to
    */
   public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
      if (this.currentMount() == null) {
         return;
      }
      this.wantedX = pX;
      this.wantedY = pY;
      this.wantedZ = pZ;
      this.speedModifier = pSpeed;
      if (this.operation != RidingMoveControl.Operation.JUMPING) {
         this.operation = RidingMoveControl.Operation.MOVE_TO;
      }

   }

   public void strafe(float pForward, float pStrafe) {
      this.strafe(pForward, pStrafe, 1.0D);
   }

   public void strafe(float pForward, float pStrafe, double pSpeed) {
      if (this.currentMount() == null) {
         return;
      }
      this.operation = RidingMoveControl.Operation.STRAFE;
      this.strafeForwards = pForward;
      this.strafeRight = pStrafe;
      this.speedModifier = pSpeed;
   }

   public void tick() {
      AbstractHorse mount = this.currentMount();
      if (mount == null) {
         this.operation = RidingMoveControl.Operation.WAIT;
         return;
      }
      if (this.operation == RidingMoveControl.Operation.STRAFE) {
         float f = (float)mount.getAttributeValue(Attributes.MOVEMENT_SPEED);
         float f1 = (float)this.speedModifier * f;
         float f2 = this.strafeForwards;
         float f3 = this.strafeRight;
         float f4 = Mth.sqrt(f2 * f2 + f3 * f3);
         if (f4 < 1.0F) {
            f4 = 1.0F;
         }

         f4 = f1 / f4;
         f2 *= f4;
         f3 *= f4;
         float f5 = Mth.sin(mount.getYRot() * ((float)Math.PI / 180F));
         float f6 = Mth.cos(mount.getYRot() * ((float)Math.PI / 180F));
         float f7 = f2 * f6 - f3 * f5;
         float f8 = f3 * f6 + f2 * f5;
         if (!this.isWalkable(mount, f7, f8)) {
             this.strafeForwards = 0.0F;
             this.strafeRight = 0.0F;
             float yaw = Mth.wrapDegrees(mount.getYRot() + 10.0F);
             mount.setYRot(yaw);
             mount.setYHeadRot(yaw);
             mount.yBodyRot = yaw;
         }

         mount.setSpeed(f1);
         mount.setZza(this.strafeForwards);
         mount.setXxa(this.strafeRight);
         this.operation = RidingMoveControl.Operation.WAIT;
      } else if (this.operation == RidingMoveControl.Operation.MOVE_TO) {
         this.operation = RidingMoveControl.Operation.WAIT;
         double d0 = this.wantedX - mount.getX();
         double d1 = this.wantedZ - mount.getZ();
         double d2 = this.wantedY - mount.getY();
         double d3 = d0 * d0 + d2 * d2 + d1 * d1;
         if (d3 < (double)2.5000003E-7F) {
            mount.setZza(0.0F);
            return;
         }

         float f9 = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
         mount.setYRot(this.rotlerp(mount.getYRot(), f9, 90.0F));
         mount.setSpeed((float)(this.speedModifier * mount.getAttributeValue(Attributes.MOVEMENT_SPEED)));
         BlockPos blockpos = mount.blockPosition();
         BlockState blockstate = mount.level().getBlockState(blockpos);
         VoxelShape voxelshape = blockstate.getCollisionShape(mount.level(), blockpos);
         if (d2 > (double)mount.getStepHeight() && d0 * d0 + d1 * d1 < (double)Math.max(1.0F, mount.getBbWidth()) || !voxelshape.isEmpty() && mount.getY() < voxelshape.max(Direction.Axis.Y) + (double)blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
            mount.getJumpControl().jump();
            this.operation = RidingMoveControl.Operation.JUMPING;
         }
      } else if (this.operation == RidingMoveControl.Operation.JUMPING) {
         mount.setSpeed((float)(this.speedModifier * mount.getAttributeValue(Attributes.MOVEMENT_SPEED)));
         if (mount.onGround()) {
            this.operation = RidingMoveControl.Operation.WAIT;
         }
      } else {
         mount.setZza(0.0F);
      }

   }

   /** 检查马沿横移方向的通行空间、跨步净空及落脚支撑。 */
   private boolean isWalkable(AbstractHorse mount, float pRelativeX, float pRelativeZ) {
      PathNavigation pathnavigation = this.rider.ridingPathNavigation();
      if (pathnavigation == null || !(this.rider instanceof Mob riderMob)) {
         return true;
      }

      NodeEvaluator nodeevaluator = pathnavigation.getNodeEvaluator();
      if (nodeevaluator == null) {
         return true;
      }

      double distanceSqr = (double)pRelativeX * pRelativeX + (double)pRelativeZ * pRelativeZ;
      if (!Double.isFinite(distanceSqr) || distanceSqr > MAX_STRAFE_CHECK_DISTANCE * MAX_STRAFE_CHECK_DISTANCE) {
         return false;
      }

      int x = Mth.floor(mount.getX() + (double)pRelativeX);
      int y = mount.getBlockY();
      int z = Mth.floor(mount.getZ() + (double)pRelativeZ);
      BlockPathTypes here = nodeevaluator.getBlockPathType(mount.level(), x, y, z);
      // 实心障碍可以尝试跨步，水等负代价地形直接排除。
      if (here != BlockPathTypes.BLOCKED && RidingWalkNodeEvaluator.pathMalus(riderMob, here) < 0.0F) {
         return false;
      }
      // 平地沿用路径类型。空气和实心障碍才进入跳跃、落下的碰撞箱。
      if (here != BlockPathTypes.OPEN && RidingWalkNodeEvaluator.pathMalus(riderMob, here) >= 0.0F) {
         return true;
      }

      AABB body = mount.getBoundingBox();
      AABB searchBox = body.expandTowards(pRelativeX, 0.0D, pRelativeZ)
            .expandTowards(0.0D, 1.0D, 0.0D)
            .expandTowards(0.0D, -1.0D - COLLISION_EPSILON * 2.0D, 0.0D);
      List<VoxelShape> collisions = new ArrayList<>();
      mount.level().getBlockCollisions(mount, searchBox).forEach(collisions::add);

      AABB landing = findStrafeLanding(body, pRelativeX, pRelativeZ, 1.0D, 1.0D, collisions);
      if (landing == null) {
         return false;
      }
      // 半砖等非整格表面用上方的路径格，实际支撑已由碰撞形状确认。
      int landingY = Mth.ceil(landing.minY - COLLISION_EPSILON);
      BlockPathTypes landingType = nodeevaluator.getBlockPathType(mount.level(), x, landingY, z);
      return RidingWalkNodeEvaluator.pathMalus(riderMob, landingType) >= 0.0F;
   }

   /** 各段沿原版的分轴碰撞流程检查，避免大包围盒将台阶本体误判为障碍。 */
   @Nullable
   private static AABB findStrafeLanding(
         AABB body, double dx, double dz, double stepHeight, double fallDistance, List<VoxelShape> collisions) {
      double rise = 0.0D;
      if (!canMoveHorizontally(body, dx, dz, collisions)) {
         // 先计入前方顶板，允许在低顶下跨过半砖；再尝试只在起点抬升。
         rise = Shapes.collide(Direction.Axis.Y, body.expandTowards(dx, 0.0D, dz), collisions, stepHeight);
         if (rise <= COLLISION_EPSILON || !canMoveHorizontally(body.move(0.0D, rise, 0.0D), dx, dz, collisions)) {
            rise = Shapes.collide(Direction.Axis.Y, body, collisions, stepHeight);
            if (rise <= COLLISION_EPSILON || !canMoveHorizontally(body.move(0.0D, rise, 0.0D), dx, dz, collisions)) {
               return null;
            }
         }
      }

      AABB moved = body.move(dx, rise, dz);
      double downward = -(rise + fallDistance + COLLISION_EPSILON * 2.0D);
      double drop = Shapes.collide(Direction.Axis.Y, moved, collisions, downward);
      if (drop <= downward + COLLISION_EPSILON) {
         return null;
      }
      AABB landing = moved.move(0.0D, drop, 0.0D);
      VoxelShape landingShape = Shapes.create(landing);
      for (VoxelShape collision : collisions) {
         if (Shapes.joinIsNotEmpty(collision, landingShape, BooleanOp.AND)) {
            return null;
         }
      }
      return landing;
   }

   private static boolean canMoveHorizontally(AABB body, double dx, double dz, List<VoxelShape> collisions) {
      if (Math.abs(dx) >= Math.abs(dz)) {
         return Math.abs(Shapes.collide(Direction.Axis.X, body, collisions, dx) - dx) <= COLLISION_EPSILON
               && Math.abs(Shapes.collide(Direction.Axis.Z, body.move(dx, 0.0D, 0.0D), collisions, dz) - dz)
                     <= COLLISION_EPSILON;
      }
      return Math.abs(Shapes.collide(Direction.Axis.Z, body, collisions, dz) - dz) <= COLLISION_EPSILON
            && Math.abs(Shapes.collide(Direction.Axis.X, body.move(0.0D, 0.0D, dz), collisions, dx) - dx)
                  <= COLLISION_EPSILON;
   }

   /**
    * Attempt to rotate the first angle to become the second angle, but only allow overall direction change to at max be
    * third parameter
    */
   protected float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
      float f = Mth.wrapDegrees(pTargetAngle - pSourceAngle);
      if (f > pMaximumChange) {
         f = pMaximumChange;
      }

      if (f < -pMaximumChange) {
         f = -pMaximumChange;
      }

      float f1 = pSourceAngle + f;
      if (f1 < 0.0F) {
         f1 += 360.0F;
      } else if (f1 > 360.0F) {
         f1 -= 360.0F;
      }

      return f1;
   }

   public double getWantedX() {
      return this.wantedX;
   }

   public double getWantedY() {
      return this.wantedY;
   }

   public double getWantedZ() {
      return this.wantedZ;
   }

   protected static enum Operation {
      WAIT,
      MOVE_TO,
      STRAFE,
      JUMPING;
   }
}
