package com.kkdfg.Tacz_morelevel.entity.ai.riding;

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
import net.minecraft.world.phys.shapes.VoxelShape;

public class RidingMoveControl implements Control {
   public static final float MIN_SPEED = 5.0E-4F;
   public static final float MIN_SPEED_SQR = 2.5000003E-7F;
   protected static final int MAX_TURN = 90;
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
      if (this.currentMount() == null) {
         return;
      }
      this.operation = RidingMoveControl.Operation.STRAFE;
      this.strafeForwards = pForward;
      this.strafeRight = pStrafe;
      this.speedModifier = 1.0D;
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
            this.strafeForwards = 1.0F;
            this.strafeRight = 0.0F;
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

   /**
    * @return true if the mob can walk successfully to a given X and Z
    */
   private boolean isWalkable(AbstractHorse mount, float pRelativeX, float pRelativeZ) {
      PathNavigation pathnavigation = this.rider.ridingPathNavigation();
      if (pathnavigation == null || !(this.rider instanceof Mob riderMob)) {
         return true;
      }

      NodeEvaluator nodeevaluator = pathnavigation.getNodeEvaluator();
      if (nodeevaluator == null) {
         return true;
      }

      int x = Mth.floor(mount.getX() + (double)pRelativeX);
      int y = mount.getBlockY();
      int z = Mth.floor(mount.getZ() + (double)pRelativeZ);
      BlockPathTypes here = nodeevaluator.getBlockPathType(mount.level(), x, y, z);
      if (riderMob.getPathfindingMalus(here) >= 0.0F) {
         return true;
      }

      if (this.canStepUp(mount, nodeevaluator, riderMob, x, y, z, here)) {
         return true;
      }

      if (here != BlockPathTypes.OPEN) {
         return false;
      }

      BlockPathTypes below = nodeevaluator.getBlockPathType(mount.level(), x, y - 1, z);
      return below != BlockPathTypes.OPEN && riderMob.getPathfindingMalus(below) >= 0.0F;
   }

   /** 上一格落差用马当前站立面，超过 1 格就拒绝。栅栏、不可通行铁轨、活板门、细雪不往上探。 */
   private boolean canStepUp(AbstractHorse mount, NodeEvaluator nodeevaluator, Mob riderMob, int x, int y, int z, BlockPathTypes here) {
      if (here == BlockPathTypes.FENCE
         || here == BlockPathTypes.UNPASSABLE_RAIL
         || here == BlockPathTypes.TRAPDOOR
         || here == BlockPathTypes.POWDER_SNOW) {
         return false;
      }

      double origin = RidingWalkNodeEvaluator.getFloorLevel(mount.level(), mount.blockPosition());
      double stepped = RidingWalkNodeEvaluator.getFloorLevel(mount.level(), new BlockPos(x, y + 1, z));
      if (stepped - origin > 1.0D) {
         return false;
      }

      BlockPathTypes above = nodeevaluator.getBlockPathType(mount.level(), x, y + 1, z);
      return riderMob.getPathfindingMalus(above) >= 0.0F;
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
