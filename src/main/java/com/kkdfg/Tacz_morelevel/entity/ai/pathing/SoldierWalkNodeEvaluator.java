package com.kkdfg.Tacz_morelevel.entity.ai.pathing;

import com.kkdfg.Tacz_morelevel.api.JumpActingMob;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.Target;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoldierWalkNodeEvaluator extends NodeEvaluator {
   public static final double SPACE_BETWEEN_WALL_POSTS = 0.5D;
   private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125D;
   /** 扩邻居时最多往上试探的整数格数（防零重力等把 j 撑爆）。 */
   private static final int MAX_PATH_VERTICAL_DELTA = 4;
   /** 姿态 BB 真源：趴下（Player SWIMMING_BB）；站立用实体 {@link Pose#STANDING} 真实尺寸。 */ //测试代码
   private static final float VIRTUAL_CRAWL_WIDTH = Player.SWIMMING_BB_WIDTH; //测试代码
   private static final float VIRTUAL_CRAWL_HEIGHT = Player.SWIMMING_BB_HEIGHT; //测试代码
   /** 趴边允许的非跳地板上升（半砖以下）；不抬整格（crawlJ 仍为 0）。 */ //测试代码
   private static final double CRAWL_STEP_MAX_RISE = 0.3D; //测试代码
   /** 仅趴可通过时叠在 base malus 上的代价（base ≥ 0 才加）。 */ //测试代码
   public static final float CRAWL_ONLY_MALUS = 8.0F; //测试代码
   /**
    * 以自身为中心 7×7 窗口的切比雪夫半径（{@code max(|dx|,|dz|) ≤ 3}）。
    * 供 {@link #expandReachable7x7(Mob)} / {@link #expandReachableInWindow} 使用。
    */
   public static final int REACH_WINDOW_7X7_CHEBYSHEV = 3;
   /** 7×7 扩点默认节点上限（含多层高度冗余）。 */
   public static final int REACH_WINDOW_7X7_MAX_NODES = 49 * 3;

   private final boolean crawlPathfinding; //测试代码
   private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();
   /** 趴体积下的 PathType 缓存，与站立缓存分离。 */ //测试代码
   private final Long2ObjectMap<BlockPathTypes> crawlPathTypesByPosCache = new Long2ObjectOpenHashMap<>(); //测试代码
   /** 站立体积下的 PathType 缓存。 */ //测试代码
   private final Long2ObjectMap<BlockPathTypes> standPathTypesByPosCache = new Long2ObjectOpenHashMap<>(); //测试代码
   private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();
   /** 格点判定形式缓存；不可达不写入。 */ //测试代码
   private final Long2ObjectMap<PoseMode> poseModeByPos = new Long2ObjectOpenHashMap<>(); //测试代码

   /** 该格用哪种体积形式判定通过：站高 / 趴高。 */ //测试代码
   public enum PoseMode { //测试代码
      STAND, //测试代码
      CRAWL //测试代码
   } //测试代码

   /**
    * 统一体积：整格扫格 + 浮点 BB（上跳 AABB）。
    * 整格 = floor(bb + 1)，与 {@link NodeEvaluator#prepare} 同式。
    */ //测试代码
   public record EvalSize(int width, int height, int depth, float bbWidth, float bbHeight) { //测试代码
   } //测试代码

   public SoldierWalkNodeEvaluator() {
      this(false);
   }

   public SoldierWalkNodeEvaluator(boolean crawlPathfinding) { //测试代码
      this.crawlPathfinding = crawlPathfinding; //测试代码
   }

   public boolean isCrawlPathfinding() { //测试代码
      return this.crawlPathfinding; //测试代码
   }

   public void prepare(PathNavigationRegion pLevel, Mob pMob) {
      super.prepare(pLevel, pMob);
      pMob.onPathfindingStart();
   }

   /**
    * This method is called when all nodes have been processed and PathEntity is created.
    */
   public void done() {
      this.mob.onPathfindingDone();
      this.pathTypesByPosCache.clear();
      this.crawlPathTypesByPosCache.clear(); //测试代码
      this.standPathTypesByPosCache.clear(); //测试代码
      this.collisionCache.clear();
      this.poseModeByPos.clear(); //测试代码
      super.done();
   }

   public Node getStart() {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int i = this.mob.getBlockY();
      BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
      if (!this.mob.canStandOnFluid(blockstate.getFluidState())) {
         if (this.canFloat() && this.mob.isInWater()) {
            while(true) {
               if (!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                  --i;
                  break;
               }

               ++i;
               blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
            }
         } else if (this.mob.onGround()) {
            i = Mth.floor(this.mob.getY() + 0.5D);
         } else {
            BlockPos blockpos;
            for(blockpos = this.mob.blockPosition(); (this.level.getBlockState(blockpos).isAir() || this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathComputationType.LAND)) && blockpos.getY() > this.mob.level().getMinBuildHeight(); blockpos = blockpos.below()) {
            }

            i = blockpos.above().getY();
         }
      } else {
         while(this.mob.canStandOnFluid(blockstate.getFluidState())) {
            ++i;
            blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
         }

         --i;
      }

      BlockPos blockpos1 = this.mob.blockPosition();
      if (!this.canStartAt(blockpos$mutableblockpos.set(blockpos1.getX(), i, blockpos1.getZ()))) {
         AABB aabb = this.mob.getBoundingBox();
         if (this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, (double)i, aabb.minZ)) || this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, (double)i, aabb.maxZ)) || this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, (double)i, aabb.minZ)) || this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, (double)i, aabb.maxZ))) {
            return this.getStartNode(blockpos$mutableblockpos);
         }
      }

      return this.getStartNode(new BlockPos(blockpos1.getX(), i, blockpos1.getZ()));
   }

   protected Node getStartNode(BlockPos pPos) {
      Node node = this.getNode(pPos);
      node.type = this.getBlockPathType(this.mob, node.asBlockPos());
      node.costMalus = this.mob.getPathfindingMalus(node.type);
      if (this.crawlPathfinding && node.costMalus >= 0.0F) { //测试代码
         PoseMode mode = this.classifyCell(node.x, node.y, node.z); //测试代码
         if (mode == PoseMode.CRAWL) { //测试代码
            node.costMalus = node.costMalus + CRAWL_ONLY_MALUS; //测试代码
         } //测试代码
      } //测试代码
      return node;
   }

   protected boolean canStartAt(BlockPos pPos) {
      BlockPathTypes blockpathtypes = this.getBlockPathType(this.mob, pPos);
      return blockpathtypes != BlockPathTypes.OPEN && this.mob.getPathfindingMalus(blockpathtypes) >= 0.0F;
   }

   public Target getGoal(double pX, double pY, double pZ) {
      return this.getTargetFromNode(this.getNode(Mth.floor(pX), Mth.floor(pY), Mth.floor(pZ)));
   }

   /**
    * 找邻居（原版八方向壳）。开趴时用当前格 PoseMode：STAND 先站后趴；CRAWL 则 j=0 只趴。
    */ //测试代码
   public int getNeighbors(Node[] pOutputArray, Node pNode) {
      boolean originCrawl = false; //测试代码
      BlockPathTypes above; //测试代码
      BlockPathTypes here; //测试代码
      if (this.crawlPathfinding) { //测试代码
         PoseMode origin = this.classifyCell(pNode.x, pNode.y, pNode.z); //测试代码
         if (origin == null) { //测试代码
            return 0; //测试代码
         } //测试代码
         originCrawl = origin == PoseMode.CRAWL; //测试代码
         above = this.getCachedBlockTypeForEval(pNode.x, pNode.y + 1, pNode.z, originCrawl); //测试代码
         here = this.getCachedBlockTypeForEval(pNode.x, pNode.y, pNode.z, originCrawl); //测试代码
      } else { //测试代码
         above = this.getCachedBlockType(this.mob, pNode.x, pNode.y + 1, pNode.z); //测试代码
         here = this.getCachedBlockType(this.mob, pNode.x, pNode.y, pNode.z); //测试代码
      } //测试代码

      int j = 0;
      if (!originCrawl
            && this.mob.getPathfindingMalus(above) >= 0.0F
            && here != BlockPathTypes.STICKY_HONEY) {
         j = this.getVerticalDeltaLimit();
      }
      boolean fromWater = here == BlockPathTypes.WATER;
      if (fromWater) {
         j = Math.min(j, 1);
      }
      double floor = this.getFloorLevel(new BlockPos(pNode.x, pNode.y, pNode.z));
      int crawlJ = originCrawl ? 0 : j; //测试代码

      int i = 0;
      Node nS = this.resolveNeighbor(pNode.x, pNode.y, pNode.z + 1, crawlJ, floor, Direction.SOUTH, here, fromWater, originCrawl); //测试代码
      if (this.isNeighborValid(nS, pNode)) {
         pOutputArray[i++] = nS;
      }
      Node nW = this.resolveNeighbor(pNode.x - 1, pNode.y, pNode.z, crawlJ, floor, Direction.WEST, here, fromWater, originCrawl); //测试代码
      if (this.isNeighborValid(nW, pNode)) {
         pOutputArray[i++] = nW;
      }
      Node nE = this.resolveNeighbor(pNode.x + 1, pNode.y, pNode.z, crawlJ, floor, Direction.EAST, here, fromWater, originCrawl); //测试代码
      if (this.isNeighborValid(nE, pNode)) {
         pOutputArray[i++] = nE;
      }
      Node nN = this.resolveNeighbor(pNode.x, pNode.y, pNode.z - 1, crawlJ, floor, Direction.NORTH, here, fromWater, originCrawl); //测试代码
      if (this.isNeighborValid(nN, pNode)) {
         pOutputArray[i++] = nN;
      }
      Node nNW = this.resolveNeighbor(pNode.x - 1, pNode.y, pNode.z - 1, crawlJ, floor, Direction.NORTH, here, fromWater, originCrawl); //测试代码
      if (this.isDiagonalValid(pNode, nW, nN, nNW)) {
         pOutputArray[i++] = nNW;
      }
      Node nNE = this.resolveNeighbor(pNode.x + 1, pNode.y, pNode.z - 1, crawlJ, floor, Direction.NORTH, here, fromWater, originCrawl); //测试代码
      if (this.isDiagonalValid(pNode, nE, nN, nNE)) {
         pOutputArray[i++] = nNE;
      }
      Node nSW = this.resolveNeighbor(pNode.x - 1, pNode.y, pNode.z + 1, crawlJ, floor, Direction.SOUTH, here, fromWater, originCrawl); //测试代码
      if (this.isDiagonalValid(pNode, nW, nS, nSW)) {
         pOutputArray[i++] = nSW;
      }
      Node nSE = this.resolveNeighbor(pNode.x + 1, pNode.y, pNode.z + 1, crawlJ, floor, Direction.SOUTH, here, fromWater, originCrawl); //测试代码
      if (this.isDiagonalValid(pNode, nE, nS, nSE)) {
         pOutputArray[i++] = nSE;
      }
      return i;
   }

   /**
    * 便捷 API：临时 Evaluator，以 mob 脚底为中心做 7×7（切比雪夫半径
    * {@link #REACH_WINDOW_7X7_CHEBYSHEV}）内的寻路可达扩点，返回节点表。
    * 内部自行 {@link #prepare}/{@link #done}；不改 Navigation 上正在用的 Evaluator。
    * <p>趴开关与该 mob 导航同源（{@link SoldierGroundPathNavigation#isCrawlPathfinding()}），
    * 避免掩体扩表关趴而日常开趴导致趴态进 COVER 不一致。打分等请在外部对返回表处理。
    */
   public static List<Node> expandReachable7x7(Mob mob) {
      if (mob == null || mob.level().isClientSide()) {
         return List.of();
      }
      SoldierWalkNodeEvaluator eval = new SoldierWalkNodeEvaluator(resolveCrawlPathfinding(mob));
      eval.setCanPassDoors(true);
      BlockPos center = mob.blockPosition();
      int r = REACH_WINDOW_7X7_CHEBYSHEV;
      int yPad = MAX_PATH_VERTICAL_DELTA + 1;
      BlockPos min = center.offset(-r - 1, -yPad, -r - 1);
      BlockPos max = center.offset(r + 1, yPad, r + 1);
      PathNavigationRegion region = new PathNavigationRegion(mob.level(), min, max);
      eval.prepare(region, mob);
      try {
         Node start = eval.getStart();
         if (start == null) {
            return List.of();
         }
         return eval.expandReachableInWindow(
               start, center.getX(), center.getZ(), r, REACH_WINDOW_7X7_MAX_NODES);
      } finally {
         eval.done();
      }
   }

   /**
    * 在<strong>已 {@link #prepare}</strong> 的本实例上，从 {@code start} 用 {@link #getNeighbors} 做 BFS，
    * 只保留切比雪夫窗口内的可达节点；不设置 {@link Node#closed}，避免污染节点缓存语义。
    * <p>调用方负责 prepare/done。一般优先用 {@link #expandReachable7x7(Mob)}。
    *
    * @param chebyshevRadius 例如 3 → 以 (centerX,centerZ) 为中心的 7×7
    * @param maxNodes 扩点硬上限
    */
   public List<Node> expandReachableInWindow(
         Node start, int centerX, int centerZ, int chebyshevRadius, int maxNodes) {
      if (start == null || this.mob == null || maxNodes <= 0 || chebyshevRadius < 0) {
         return List.of();
      }
      if (!isInChebyshevWindow(start.x, start.z, centerX, centerZ, chebyshevRadius)) {
         return List.of();
      }
      List<Node> out = new ArrayList<>(Math.min(maxNodes, 64));
      ArrayDeque<Node> queue = new ArrayDeque<>();
      LongOpenHashSet visited = new LongOpenHashSet();
      Node[] buf = new Node[8];
      long startKey = BlockPos.asLong(start.x, start.y, start.z);
      visited.add(startKey);
      queue.add(start);
      out.add(start);
      while (!queue.isEmpty() && out.size() < maxNodes) {
         Node cur = queue.poll();
         int n = this.getNeighbors(buf, cur);
         for (int i = 0; i < n; i++) {
            Node next = buf[i];
            if (next == null || next.costMalus < 0.0F) {
               continue;
            }
            if (!isInChebyshevWindow(next.x, next.z, centerX, centerZ, chebyshevRadius)) {
               continue;
            }
            long key = BlockPos.asLong(next.x, next.y, next.z);
            if (!visited.add(key)) {
               continue;
            }
            out.add(next);
            queue.add(next);
            if (out.size() >= maxNodes) {
               break;
            }
         }
      }
      return Collections.unmodifiableList(out);
   }

   /** {@code max(|x-cx|, |z-cz|) ≤ radius}。 */
   public static boolean isInChebyshevWindow(int x, int z, int centerX, int centerZ, int radius) {
      return Math.max(Math.abs(x - centerX), Math.abs(z - centerZ)) <= radius;
   }

   /** 与 mob 当前 Navigation Evaluator 的趴开关对齐。 */
   public static boolean resolveCrawlPathfinding(Mob mob) {
      if (mob == null) {
         return false;
      }
      if (mob.getNavigation() instanceof SoldierGroundPathNavigation soldierNav) {
         return soldierNav.isCrawlPathfinding();
      }
      if (mob.getNavigation().getNodeEvaluator() instanceof SoldierWalkNodeEvaluator eval) {
         return eval.isCrawlPathfinding();
      }
      return false;
   }

   /** 单边选点：关趴/原点 CRAWL 一次 find；原点 STAND 先站后趴。 */ //测试代码
   @Nullable
   private Node resolveNeighbor( //测试代码
         int x, int y, int z, int j, double floor, Direction dir, BlockPathTypes pathType, boolean fromWater, boolean originCrawl) { //测试代码
      if (!this.crawlPathfinding || originCrawl) { //测试代码
         return this.findAcceptedNode(x, y, z, j, floor, dir, pathType, fromWater, this.crawlPathfinding && originCrawl); //测试代码
      } //测试代码
      Node stand = this.findAcceptedNode(x, y, z, j, floor, dir, pathType, fromWater, false); //测试代码
      if (stand != null && stand.costMalus >= 0.0F && !stand.closed
            && this.classifyCell(stand.x, stand.y, stand.z) == PoseMode.STAND) { //测试代码
         return stand; //测试代码
      } //测试代码
      return this.findAcceptedNode(x, y, z, 0, floor, dir, pathType, fromWater, true); //测试代码
   }

   protected boolean isNeighborValid(@Nullable Node pNeighbor, Node pNode) {
      return pNeighbor != null && !pNeighbor.closed && (pNeighbor.costMalus >= 0.0F || pNode.costMalus < 0.0F);
   }

   protected boolean isDiagonalValid(Node pRoot, @Nullable Node pXNode, @Nullable Node pZNode, @Nullable Node pDiagonal) {
      if (pDiagonal != null && pZNode != null && pXNode != null) {
         if (pDiagonal.closed) {
            return false;
         } else if (pZNode.y <= pRoot.y && pXNode.y <= pRoot.y) {
            if (pXNode.type != BlockPathTypes.WALKABLE_DOOR && pZNode.type != BlockPathTypes.WALKABLE_DOOR && pDiagonal.type != BlockPathTypes.WALKABLE_DOOR) {
               boolean flag = pZNode.type == BlockPathTypes.FENCE && pXNode.type == BlockPathTypes.FENCE && (double)this.mob.getBbWidth() < 0.5D;
               return pDiagonal.costMalus >= 0.0F && (pZNode.y < pRoot.y || pZNode.costMalus >= 0.0F || flag) && (pXNode.y < pRoot.y || pXNode.costMalus >= 0.0F || flag);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean doesBlockHavePartialCollision(BlockPathTypes pBlockPathType) {
      return pBlockPathType == BlockPathTypes.FENCE || pBlockPathType == BlockPathTypes.DOOR_WOOD_CLOSED || pBlockPathType == BlockPathTypes.DOOR_IRON_CLOSED;
   }

   private boolean canReachWithoutCollision(Node pNode) {
      AABB aabb = this.mob.getBoundingBox();
      Vec3 vec3 = new Vec3((double)pNode.x - this.mob.getX() + aabb.getXsize() / 2.0D, (double)pNode.y - this.mob.getY() + aabb.getYsize() / 2.0D, (double)pNode.z - this.mob.getZ() + aabb.getZsize() / 2.0D);
      int i = Mth.ceil(vec3.length() / aabb.getSize());
      vec3 = vec3.scale((double)(1.0F / (float)i));

      for(int j = 1; j <= i; ++j) {
         aabb = aabb.move(vec3);
         if (this.hasCollisions(aabb)) {
            return false;
         }
      }

      return true;
   }

   protected double getFloorLevel(BlockPos pPos) {
      return (this.canFloat() || this.isAmphibious()) && this.level.getFluidState(pPos).is(FluidTags.WATER) ? (double)pPos.getY() + 0.5D : getFloorLevel(this.level, pPos);
   }

   public static double getFloorLevel(BlockGetter pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      VoxelShape voxelshape = pLevel.getBlockState(blockpos).getCollisionShape(pLevel, blockpos);
      return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(Direction.Axis.Y));
   }
   //此处，水不能作为跳跃因此无法作为跳跃判定

   protected boolean isAmphibious() {
      return false;
   }

   @Nullable
   protected Node findAcceptedNode( //测试代码
         int pX,
         int pY,
         int pZ,
         int pVerticalDeltaLimit,
         double pNodeFloorLevel,
         Direction pDirection,
         BlockPathTypes pPathType,
         boolean fromWater,
         boolean asCrawl) { //测试代码
      Node node = null;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      double d0 = this.getFloorLevel(blockpos$mutableblockpos.set(pX, pY, pZ));
      double maxRise = this.resolveMaxRise(fromWater, asCrawl); //测试代码
      EvalSize evalSize = this.resolveEvalSize(asCrawl); //测试代码
      float evalHeight = evalSize.bbHeight(); //测试代码
      float evalWidth = evalSize.bbWidth(); //测试代码
      if (d0 - pNodeFloorLevel > maxRise) {
         return null;
      } else {
         BlockPathTypes blockpathtypes = this.getCachedBlockTypeForEval(pX, pY, pZ, asCrawl); //测试代码
         float f = this.mob.getPathfindingMalus(blockpathtypes);
         double d1 = (double) evalWidth / 2.0D; //测试代码
         if (f >= 0.0F) {
            node = this.getNodeAndUpdateCostToMax(pX, pY, pZ, blockpathtypes, f);
         }

         if (doesBlockHavePartialCollision(pPathType) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
            node = null;
         }

         if (blockpathtypes != BlockPathTypes.WALKABLE && (!this.isAmphibious() || blockpathtypes != BlockPathTypes.WATER)) {
            if ((node == null || node.costMalus < 0.0F) && pVerticalDeltaLimit > 0 && (blockpathtypes != BlockPathTypes.FENCE || this.canWalkOverFences()) && blockpathtypes != BlockPathTypes.UNPASSABLE_RAIL && blockpathtypes != BlockPathTypes.TRAPDOOR && blockpathtypes != BlockPathTypes.POWDER_SNOW) {
               node = this.findAcceptedNode(pX, pY + 1, pZ, pVerticalDeltaLimit - 1, pNodeFloorLevel, pDirection, pPathType, fromWater, asCrawl); //测试代码
               if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && evalWidth < 1.0F) { //测试代码
                  double d2 = (double)(pX - pDirection.getStepX()) + 0.5D;
                  double d3 = (double)(pZ - pDirection.getStepZ()) + 0.5D;
                  AABB aabb = new AABB(d2 - d1, this.getFloorLevel(blockpos$mutableblockpos.set(d2, (double)(pY + 1), d3)) + 0.001D, d3 - d1, d2 + d1, (double) evalHeight + this.getFloorLevel(blockpos$mutableblockpos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002D, d3 + d1); //测试代码
                  if (this.hasCollisions(aabb)) {
                     node = null;
                  }
               }
            }

            if (!this.isAmphibious() && blockpathtypes == BlockPathTypes.WATER && !this.canFloat()) {
               if (this.getCachedBlockTypeForEval(pX, pY - 1, pZ, asCrawl) != BlockPathTypes.WATER) { //测试代码
                  return this.finishAcceptedNode(node, asCrawl); //测试代码
               }

               while(pY > this.mob.level().getMinBuildHeight()) {
                  --pY;
                  blockpathtypes = this.getCachedBlockTypeForEval(pX, pY, pZ, asCrawl); //测试代码
                  if (blockpathtypes != BlockPathTypes.WATER) {
                     return this.finishAcceptedNode(node, asCrawl); //测试代码
                  }

                  node = this.getNodeAndUpdateCostToMax(pX, pY, pZ, blockpathtypes, this.mob.getPathfindingMalus(blockpathtypes));
               }
            }

            if (blockpathtypes == BlockPathTypes.OPEN) {
               int j = 0;
               int i = pY;

               while(blockpathtypes == BlockPathTypes.OPEN) {
                  --pY;
                  if (pY < this.mob.level().getMinBuildHeight()) {
                     return this.getBlockedNode(pX, i, pZ);
                  }

                  if (j++ >= this.mob.getMaxFallDistance()) {
                     return this.getBlockedNode(pX, pY, pZ);
                  }

                  blockpathtypes = this.getCachedBlockTypeForEval(pX, pY, pZ, asCrawl); //测试代码
                  f = this.mob.getPathfindingMalus(blockpathtypes);
                  if (blockpathtypes != BlockPathTypes.OPEN && f >= 0.0F) {
                     // 趴选点：落底须合法（malus≥0）；再按站高扫格确认底面非非法 //测试代码
                     if (asCrawl) { //测试代码
                        BlockPathTypes standFloor = this.getCachedBlockTypeForEval(pX, pY, pZ, false); //测试代码
                        if (this.mob.getPathfindingMalus(standFloor) < 0.0F) { //测试代码
                           return this.getBlockedNode(pX, pY, pZ); //测试代码
                        } //测试代码
                     } //测试代码
                     node = this.getNodeAndUpdateCostToMax(pX, pY, pZ, blockpathtypes, f);
                     break;
                  }

                  if (f < 0.0F) {
                     return this.getBlockedNode(pX, pY, pZ);
                  }
               }
            }

            if (doesBlockHavePartialCollision(blockpathtypes) && node == null) {
               node = this.getNode(pX, pY, pZ);
               node.closed = true;
               node.type = blockpathtypes;
               node.costMalus = blockpathtypes.getMalus();
            }

            return this.finishAcceptedNode(node, asCrawl); //测试代码
         } else {
            return this.finishAcceptedNode(node, asCrawl); //测试代码
         }
      }
   }

   private double resolveMaxRise(boolean fromWater, boolean asCrawl) { //测试代码
      if (this.crawlPathfinding && asCrawl) { //测试代码
         return CRAWL_STEP_MAX_RISE; //测试代码
      } //测试代码
      if (fromWater) {
         return Math.max(1.0D, (double) this.mob.getStepHeight());
      }
      return this.getMobJumpHeight(asCrawl); //测试代码
   }

   /**
    * 姿态体积：站立用实体 STANDING 真实 BB；趴用虚拟趴 BB；关趴寻路时用当前 BB。
    * 整格 = floor(bb+1)。寻路 prepare 期间可用；依赖 {@link #mob}。
    */ //测试代码
   private EvalSize resolveEvalSize(boolean asCrawl) { //测试代码
      return this.resolveEvalSize(asCrawl, this.mob); //测试代码
   }

   /**
    * MoveControl 等在 {@link #done()} 之后调用：必须传入活着的 mob，勿用 this.mob。
    */ //测试代码
   private EvalSize resolveEvalSize(boolean asCrawl, Mob sizeMob) { //测试代码
      float bbWidth;
      float bbHeight;
      if (!this.crawlPathfinding) { //测试代码
         bbWidth = sizeMob.getBbWidth(); //测试代码
         bbHeight = sizeMob.getBbHeight(); //测试代码
      } else if (asCrawl) { //测试代码
         bbWidth = VIRTUAL_CRAWL_WIDTH; //测试代码
         bbHeight = VIRTUAL_CRAWL_HEIGHT; //测试代码
      } else { //测试代码
         // 站立：真实站立盒（史莱姆等非 0.6×1.8；避免用当前可能已是趴的 getBbHeight）
         EntityDimensions stand = sizeMob.getDimensions(Pose.STANDING); //测试代码
         bbWidth = stand.width; //测试代码
         bbHeight = stand.height; //测试代码
      } //测试代码
      return new EvalSize( //测试代码
            Mth.floor(bbWidth + 1.0F), //测试代码
            Mth.floor(bbHeight + 1.0F), //测试代码
            Mth.floor(bbWidth + 1.0F), //测试代码
            bbWidth, //测试代码
            bbHeight); //测试代码
   }

   @Nullable
   private Node finishAcceptedNode(@Nullable Node node, boolean asCrawl) { //测试代码
      if (!this.crawlPathfinding || node == null || node.closed || node.costMalus < 0.0F) { //测试代码
         return node; //测试代码
      } //测试代码
      PoseMode mode = this.classifyCell(node.x, node.y, node.z); //测试代码
      if (mode == null) { //测试代码
         return null; //测试代码
      } //测试代码
      if (mode == PoseMode.STAND) { //测试代码
         return node; //测试代码
      } //测试代码
      // CRAWL：站立选点否决；趴边叠 +16 //测试代码
      if (!asCrawl) { //测试代码
         return null; //测试代码
      } //测试代码
      node.costMalus = node.costMalus + CRAWL_ONLY_MALUS; //测试代码
      return node; //测试代码
   }

   private double getMobJumpHeight() {
      return this.getMobJumpHeight(false); //测试代码
   }

   private double getMobJumpHeight(boolean asCrawl) { //测试代码
      if (this.crawlPathfinding && asCrawl) { //测试代码
         return 0.0D; //测试代码
      } //测试代码
      double step = (double) this.mob.getStepHeight();
      if (this.mob instanceof JumpActingMob jumper) {
         return Math.max(step, JumpController.pathJumpHeightForMax(jumper, this.mob));
      }
      return Math.max(JumpController.VANILLA_PATH_JUMP_HEIGHT, step);
   }

   /**
    * 扩邻居允许的向上试探层数。
    * {@link JumpActingMob} 与 {@link #getMobJumpHeight()} 同源（最高档寻路净空），
    * 不再用步高把特种兵锁在 1 格；其它仍按原版 stepHeight。
    */
   private int getVerticalDeltaLimit() {
      if (this.mob instanceof JumpActingMob) {
         return Mth.clamp(Math.max(1, Mth.floor(this.getMobJumpHeight())), 1, MAX_PATH_VERTICAL_DELTA);
      }
      return Mth.floor(Math.max(1.0F, this.mob.getStepHeight()));
   }

   private Node getNodeAndUpdateCostToMax(int pX, int pY, int pZ, BlockPathTypes pType, float pCostMalus) {
      Node node = this.getNode(pX, pY, pZ);
      node.type = pType;
      node.costMalus = Math.max(node.costMalus, pCostMalus);
      return node;
   }

   private Node getBlockedNode(int pX, int pY, int pZ) {
      Node node = this.getNode(pX, pY, pZ);
      node.type = BlockPathTypes.BLOCKED;
      node.costMalus = -1.0F;
      return node;
   }

   private boolean hasCollisions(AABB pBoundingBox) {
      return this.collisionCache.computeIfAbsent(pBoundingBox, (p_192973_) -> {
         return !this.level.noCollision(this.mob, pBoundingBox);
      });
   }

   /**
    * 判定形式：站高 PathType 过 → STAND；否则趴高过 → CRAWL；都不过 → null。
    * 禁行仍由 findAcceptedNode 的 PathType/malus 负责。
    */ //测试代码
   @Nullable
   private PoseMode classifyCell(int x, int y, int z) { //测试代码
      long key = BlockPos.asLong(x, y, z); //测试代码
      PoseMode cached = this.poseModeByPos.get(key); //测试代码
      if (cached != null) { //测试代码
         return cached; //测试代码
      } //测试代码
      BlockPathTypes standType = this.getCachedBlockTypeForEval(x, y, z, false); //测试代码
      if (this.mob.getPathfindingMalus(standType) >= 0.0F) { //测试代码
         this.poseModeByPos.put(key, PoseMode.STAND); //测试代码
         return PoseMode.STAND; //测试代码
      } //测试代码
      BlockPathTypes crawlType = this.getCachedBlockTypeForEval(x, y, z, true); //测试代码
      if (this.mob.getPathfindingMalus(crawlType) >= 0.0F) { //测试代码
         this.poseModeByPos.put(key, PoseMode.CRAWL); //测试代码
         return PoseMode.CRAWL; //测试代码
      } //测试代码
      return null; //测试代码
   }

   /** MoveControl：脚下能否站立姿态（站高扫格）。 */ //测试代码
   public boolean isStandPoseClearAt(Mob mob, int x, int y, int z) { //测试代码
      EvalSize size = this.resolveEvalSize(false, mob); //测试代码
      BlockPathTypes standType = this.getBlockPathTypeForSize(mob.level(), x, y, z, mob, size.width(), size.height(), size.depth()); //测试代码
      return mob.getPathfindingMalus(standType) >= 0.0F; //测试代码
   }

   /** MoveControl：该格是否只能趴（站不过、趴过）。 */ //测试代码
   public boolean needsCrawlPoseAt(Mob mob, int x, int y, int z) { //测试代码
      EvalSize stand = this.resolveEvalSize(false, mob); //测试代码
      BlockPathTypes standType = this.getBlockPathTypeForSize(mob.level(), x, y, z, mob, stand.width(), stand.height(), stand.depth()); //测试代码
      if (mob.getPathfindingMalus(standType) >= 0.0F) { //测试代码
         return false; //测试代码
      } //测试代码
      EvalSize crawl = this.resolveEvalSize(true, mob); //测试代码
      BlockPathTypes crawlType = this.getBlockPathTypeForSize(mob.level(), x, y, z, mob, crawl.width(), crawl.height(), crawl.depth()); //测试代码
      return mob.getPathfindingMalus(crawlType) >= 0.0F; //测试代码
   }

   private BlockPathTypes getCachedBlockTypeForEval(int x, int y, int z, boolean asCrawl) { //测试代码
      if (!this.crawlPathfinding) { //测试代码
         return this.getCachedBlockType(this.mob, x, y, z); //测试代码
      } //测试代码
      Long2ObjectMap<BlockPathTypes> cache = asCrawl ? this.crawlPathTypesByPosCache : this.standPathTypesByPosCache; //测试代码
      EvalSize size = this.resolveEvalSize(asCrawl); //测试代码
      return cache.computeIfAbsent(BlockPos.asLong(x, y, z), (ignored) -> { //测试代码
         return this.getBlockPathTypeForSize(this.level, x, y, z, this.mob, size.width(), size.height(), size.depth()); //测试代码
      }); //测试代码
   }

   public BlockPathTypes getBlockPathType(BlockGetter pLevel, int pX, int pY, int pZ, Mob pMob) {
      EnumSet<BlockPathTypes> enumset = EnumSet.noneOf(BlockPathTypes.class);
      BlockPathTypes blockpathtypes = BlockPathTypes.BLOCKED;
      blockpathtypes = this.getBlockPathTypes(pLevel, pX, pY, pZ, enumset, blockpathtypes, pMob.blockPosition());
      if (enumset.contains(BlockPathTypes.FENCE)) {
         return BlockPathTypes.FENCE;
      } else if (enumset.contains(BlockPathTypes.UNPASSABLE_RAIL)) {
         return BlockPathTypes.UNPASSABLE_RAIL;
      } else {
         BlockPathTypes blockpathtypes1 = BlockPathTypes.BLOCKED;

         for(BlockPathTypes blockpathtypes2 : enumset) {
            if (pMob.getPathfindingMalus(blockpathtypes2) < 0.0F) {
               return blockpathtypes2;
            }

            if (pMob.getPathfindingMalus(blockpathtypes2) >= pMob.getPathfindingMalus(blockpathtypes1)) {
               blockpathtypes1 = blockpathtypes2;
            }
         }

         return blockpathtypes == BlockPathTypes.OPEN && pMob.getPathfindingMalus(blockpathtypes1) == 0.0F && this.entityWidth <= 1 ? BlockPathTypes.OPEN : blockpathtypes1;
      }
   }

   /** 按传入体型扫格；与原版 {@link #getBlockPathType(BlockGetter, int, int, int, Mob)} 无关，不改 entity*。 */ //测试代码
   private BlockPathTypes getBlockPathTypeForSize( //测试代码
         BlockGetter pLevel, int pX, int pY, int pZ, Mob pMob, int entityWidth, int entityHeight, int entityDepth) { //测试代码
      EnumSet<BlockPathTypes> enumset = EnumSet.noneOf(BlockPathTypes.class); //测试代码
      BlockPathTypes blockpathtypes = BlockPathTypes.BLOCKED; //测试代码
      blockpathtypes = this.getBlockPathTypesForSize(pLevel, pX, pY, pZ, enumset, blockpathtypes, pMob.blockPosition(), entityWidth, entityHeight, entityDepth); //测试代码
      if (enumset.contains(BlockPathTypes.FENCE)) { //测试代码
         return BlockPathTypes.FENCE; //测试代码
      } else if (enumset.contains(BlockPathTypes.UNPASSABLE_RAIL)) { //测试代码
         return BlockPathTypes.UNPASSABLE_RAIL; //测试代码
      } else { //测试代码
         BlockPathTypes blockpathtypes1 = BlockPathTypes.BLOCKED; //测试代码

         for(BlockPathTypes blockpathtypes2 : enumset) { //测试代码
            if (pMob.getPathfindingMalus(blockpathtypes2) < 0.0F) { //测试代码
               return blockpathtypes2; //测试代码
            } //测试代码

            if (pMob.getPathfindingMalus(blockpathtypes2) >= pMob.getPathfindingMalus(blockpathtypes1)) { //测试代码
               blockpathtypes1 = blockpathtypes2; //测试代码
            } //测试代码
         } //测试代码

         return blockpathtypes == BlockPathTypes.OPEN && pMob.getPathfindingMalus(blockpathtypes1) == 0.0F && entityWidth <= 1 ? BlockPathTypes.OPEN : blockpathtypes1; //测试代码
      } //测试代码
   }

   public BlockPathTypes getBlockPathTypes(BlockGetter pLevel, int pXOffset, int pYOffset, int pZOffset, EnumSet<BlockPathTypes> pOutput, BlockPathTypes pFallbackPathType, BlockPos pPos) {
      for(int i = 0; i < this.entityWidth; ++i) {
         for(int j = 0; j < this.entityHeight; ++j) {
            for(int k = 0; k < this.entityDepth; ++k) {
               int l = i + pXOffset;
               int i1 = j + pYOffset;
               int j1 = k + pZOffset;
               BlockPathTypes blockpathtypes = this.getBlockPathType(pLevel, l, i1, j1);
               blockpathtypes = this.evaluateBlockPathType(pLevel, pPos, blockpathtypes);
               if (i == 0 && j == 0 && k == 0) {
                  pFallbackPathType = blockpathtypes;
               }

               pOutput.add(blockpathtypes);
            }
         }
      }

      return pFallbackPathType;
   }

   /** 按传入体型扫格；与原版 {@link #getBlockPathTypes} 无关，不改 entity*。 */ //测试代码
   private BlockPathTypes getBlockPathTypesForSize( //测试代码
         BlockGetter pLevel,
         int pXOffset,
         int pYOffset,
         int pZOffset,
         EnumSet<BlockPathTypes> pOutput,
         BlockPathTypes pFallbackPathType,
         BlockPos pPos,
         int entityWidth,
         int entityHeight,
         int entityDepth) { //测试代码
      for(int i = 0; i < entityWidth; ++i) { //测试代码
         for(int j = 0; j < entityHeight; ++j) { //测试代码
            for(int k = 0; k < entityDepth; ++k) { //测试代码
               int l = i + pXOffset; //测试代码
               int i1 = j + pYOffset; //测试代码
               int j1 = k + pZOffset; //测试代码
               BlockPathTypes blockpathtypes = this.getBlockPathType(pLevel, l, i1, j1); //测试代码
               blockpathtypes = this.evaluateBlockPathType(pLevel, pPos, blockpathtypes); //测试代码
               if (i == 0 && j == 0 && k == 0) { //测试代码
                  pFallbackPathType = blockpathtypes; //测试代码
               } //测试代码

               pOutput.add(blockpathtypes); //测试代码
            } //测试代码
         } //测试代码
      } //测试代码

      return pFallbackPathType; //测试代码
   }

   protected BlockPathTypes evaluateBlockPathType(BlockGetter pLevel, BlockPos pPos, BlockPathTypes pPathTypes) {
      boolean flag = this.canPassDoors();
      if (pPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
         pPathTypes = BlockPathTypes.WALKABLE_DOOR;
      }

      if (pPathTypes == BlockPathTypes.DOOR_OPEN && !flag) {
         pPathTypes = BlockPathTypes.BLOCKED;
      }

      if (pPathTypes == BlockPathTypes.RAIL && !(pLevel.getBlockState(pPos).getBlock() instanceof BaseRailBlock) && !(pLevel.getBlockState(pPos.below()).getBlock() instanceof BaseRailBlock)) {
         pPathTypes = BlockPathTypes.UNPASSABLE_RAIL;
      }

      return pPathTypes;
   }

   /**
    * Returns a significant cached path node type for specified position or calculates it
    */
   protected BlockPathTypes getBlockPathType(Mob pEntityliving, BlockPos pPos) {
      return this.getCachedBlockType(pEntityliving, pPos.getX(), pPos.getY(), pPos.getZ());
   }

   /**
    * Returns a cached path node type for specified position or calculates it
    */
   protected BlockPathTypes getCachedBlockType(Mob pEntity, int pX, int pY, int pZ) {
      return this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(pX, pY, pZ), (p_265015_) -> {
         return this.getBlockPathType(this.level, pX, pY, pZ, pEntity);
      });
   }

   /**
    * Returns the node type at the specified postion taking the block below into account
    */
   public BlockPathTypes getBlockPathType(BlockGetter pLevel, int pX, int pY, int pZ) {
      return getBlockPathTypeStatic(pLevel, new BlockPos.MutableBlockPos(pX, pY, pZ));
   }

   /**
    * Returns the node type at the specified postion taking the block below into account
    */
   public static BlockPathTypes getBlockPathTypeStatic(BlockGetter pLevel, BlockPos.MutableBlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      BlockPathTypes blockpathtypes = getBlockPathTypeRaw(pLevel, pPos);
      if (blockpathtypes == BlockPathTypes.OPEN && j >= pLevel.getMinBuildHeight() + 1) {
         BlockPathTypes blockpathtypes1 = getBlockPathTypeRaw(pLevel, pPos.set(i, j - 1, k));
         blockpathtypes = blockpathtypes1 != BlockPathTypes.WALKABLE && blockpathtypes1 != BlockPathTypes.OPEN && blockpathtypes1 != BlockPathTypes.WATER && blockpathtypes1 != BlockPathTypes.LAVA ? BlockPathTypes.WALKABLE : BlockPathTypes.OPEN;
         if (blockpathtypes1 == BlockPathTypes.DAMAGE_FIRE) {
            blockpathtypes = BlockPathTypes.DAMAGE_FIRE;
         }

         if (blockpathtypes1 == BlockPathTypes.DAMAGE_OTHER) {
            blockpathtypes = BlockPathTypes.DAMAGE_OTHER;
         }

         if (blockpathtypes1 == BlockPathTypes.STICKY_HONEY) {
            blockpathtypes = BlockPathTypes.STICKY_HONEY;
         }

         if (blockpathtypes1 == BlockPathTypes.POWDER_SNOW) {
            blockpathtypes = BlockPathTypes.DANGER_POWDER_SNOW;
         }

         if (blockpathtypes1 == BlockPathTypes.DAMAGE_CAUTIOUS) {
            blockpathtypes = BlockPathTypes.DAMAGE_CAUTIOUS;
         }
      }

      if (blockpathtypes == BlockPathTypes.WALKABLE) {
         blockpathtypes = checkNeighbourBlocks(pLevel, pPos.set(i, j, k), blockpathtypes);
      }

      return blockpathtypes;
   }

   /**
    * Returns possible dangers in a 3x3 cube, otherwise nodeType
    */
   public static BlockPathTypes checkNeighbourBlocks(BlockGetter pLevel, BlockPos.MutableBlockPos pCenterPos, BlockPathTypes pNodeType) {
      int i = pCenterPos.getX();
      int j = pCenterPos.getY();
      int k = pCenterPos.getZ();

      for(int l = -1; l <= 1; ++l) {
         for(int i1 = -1; i1 <= 1; ++i1) {
            for(int j1 = -1; j1 <= 1; ++j1) {
               if (l != 0 || j1 != 0) {
                  pCenterPos.set(i + l, j + i1, k + j1);
                  BlockState blockstate = pLevel.getBlockState(pCenterPos);
                  BlockPathTypes blockPathType = blockstate.getAdjacentBlockPathType(pLevel, pCenterPos, null, pNodeType);
                  if (blockPathType != null) return blockPathType;
                  FluidState fluidState = blockstate.getFluidState();
                  BlockPathTypes fluidPathType = fluidState.getAdjacentBlockPathType(pLevel, pCenterPos, null, pNodeType);
                  if (fluidPathType != null) return fluidPathType;
                  if (blockstate.is(Blocks.CACTUS) || blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                     return BlockPathTypes.DANGER_OTHER;
                  }

                  if (isBurningBlock(blockstate)) {
                     return BlockPathTypes.DANGER_FIRE;
                  }

                  if (pLevel.getFluidState(pCenterPos).is(FluidTags.WATER)) {
                     return BlockPathTypes.WATER_BORDER;
                  }

                  if (blockstate.is(Blocks.WITHER_ROSE) || blockstate.is(Blocks.POINTED_DRIPSTONE)) {
                     return BlockPathTypes.DAMAGE_CAUTIOUS;
                  }
               }
            }
         }
      }

      return pNodeType;
   }

   protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      BlockPathTypes type = blockstate.getBlockPathType(pLevel, pPos, null);
      if (type != null) return type;
      Block block = blockstate.getBlock();
      if (blockstate.isAir()) {
         return BlockPathTypes.OPEN;
      } else if (!blockstate.is(BlockTags.TRAPDOORS) && !blockstate.is(Blocks.LILY_PAD) && !blockstate.is(Blocks.BIG_DRIPLEAF)) {
         if (blockstate.is(Blocks.POWDER_SNOW)) {
            return BlockPathTypes.POWDER_SNOW;
         } else if (!blockstate.is(Blocks.CACTUS) && !blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
            if (blockstate.is(Blocks.HONEY_BLOCK)) {
               return BlockPathTypes.STICKY_HONEY;
            } else if (blockstate.is(Blocks.COCOA)) {
               return BlockPathTypes.COCOA;
            } else if (!blockstate.is(Blocks.WITHER_ROSE) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) {
               FluidState fluidstate = pLevel.getFluidState(pPos);
               BlockPathTypes nonLoggableFluidPathType = fluidstate.getBlockPathType(pLevel, pPos, null, false);
               if (nonLoggableFluidPathType != null) return nonLoggableFluidPathType;
               if (fluidstate.is(FluidTags.LAVA)) {
                  return BlockPathTypes.LAVA;
               } else if (isBurningBlock(blockstate)) {
                  return BlockPathTypes.DAMAGE_FIRE;
               } else if (block instanceof DoorBlock) {
                  DoorBlock doorblock = (DoorBlock)block;
                  if (blockstate.getValue(DoorBlock.OPEN)) {
                     return BlockPathTypes.DOOR_OPEN;
                  } else {
                     return doorblock.type().canOpenByHand() ? BlockPathTypes.DOOR_WOOD_CLOSED : BlockPathTypes.DOOR_IRON_CLOSED;
                  }
               } else if (block instanceof BaseRailBlock) {
                  return BlockPathTypes.RAIL;
               } else if (block instanceof LeavesBlock) {
                  return BlockPathTypes.LEAVES;
               } else if (!blockstate.is(BlockTags.FENCES) && !blockstate.is(BlockTags.WALLS) && (!(block instanceof FenceGateBlock) || blockstate.getValue(FenceGateBlock.OPEN))) {
                  if (!blockstate.isPathfindable(pLevel, pPos, PathComputationType.LAND)) {
                     return BlockPathTypes.BLOCKED;
                  } else {
                     BlockPathTypes loggableFluidPathType = fluidstate.getBlockPathType(pLevel, pPos, null, true);
                     if (loggableFluidPathType != null) return loggableFluidPathType;
                     return fluidstate.is(FluidTags.WATER) ? BlockPathTypes.WATER : BlockPathTypes.OPEN;
                  }
               } else {
                  return BlockPathTypes.FENCE;
               }
            } else {
               return BlockPathTypes.DAMAGE_CAUTIOUS;
            }
         } else {
            return BlockPathTypes.DAMAGE_OTHER;
         }
      } else {
         return BlockPathTypes.TRAPDOOR;
      }
   }

   /**
    * Checks whether the specified block state can cause burn damage
    */
   public static boolean isBurningBlock(BlockState pState) {
      return pState.is(BlockTags.FIRE) || pState.is(Blocks.LAVA) || pState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(pState) || pState.is(Blocks.LAVA_CAULDRON);
   }
}
//单格 Type：Raw 认本格 → Static 用脚下做 OPEN/WALKABLE 上下转换 → 若已是 WALKABLE 再 check 邻域 3x3（危险/水边等）。带 Mob 时再从脚格角点扫体型盒合并；盒内每格仍走上面三步。
