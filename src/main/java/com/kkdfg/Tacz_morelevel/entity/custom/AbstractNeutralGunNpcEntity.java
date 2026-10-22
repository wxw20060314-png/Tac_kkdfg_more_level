package com.kkdfg.Tacz_morelevel.entity.custom;

import com.google.common.collect.ImmutableList;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.GunEquippedMob;
import com.kkdfg.Tacz_morelevel.api.JumpActingMob;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob;
import com.kkdfg.Tacz_morelevel.api.TaczCombatCrawlMob;
import com.kkdfg.Tacz_morelevel.api.TaczCrawlCapableMob;
import com.kkdfg.Tacz_morelevel.api.TaczGunSoundInvestigatingMob;
import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.kkdfg.Tacz_morelevel.entity.SoldierBackpack;
import com.kkdfg.Tacz_morelevel.entity.SoldierTaczCrawlState;
import com.kkdfg.Tacz_morelevel.entity.SoldierWeaponArrangeHost;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSightHolder;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunCombatMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunIdleMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeBodyRotationControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierMoveControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeLookControl;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadCapableMob;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadCapableSupport;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadInfo;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SoldierLocomotionMode;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBox;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxHolder;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxReloadAccess;
import com.kkdfg.Tacz_morelevel.integration.tacz.SoldierTaczCrawlHelper;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierGroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 中立持枪 NPC 基类：无 target 时枪在副手，有 target 时换主手；
 * 射击/近战 Goal 由子类常驻挂载，靠 {@code canUse} 区分有枪远距离 / 无枪近距离。
 */
public abstract class AbstractNeutralGunNpcEntity extends PathfinderMob
        implements RangedAttackMob,
                GunEquippedMob,
                PlayerLikeHeadBodyYawMob,
                TaczCombatCrawlMob,
                TaczCrawlCapableMob,
                TaczGunSoundInvestigatingMob,
                TaczPlayerLikeMob,
                JumpActingMob,
                GunCombatSightHolder,
                MobCreativeAmmoBoxHolder,
                SoldierWeaponArrangeHost,
                SimThreadCapableMob {

    private final SimThreadCapableSupport simThreadSupport =
            new SimThreadCapableSupport(this::getCoverModeDurationTicks);

    private static final String TAG_SKIN_INDEX = "TaczMoreLevelSkinIdx";
    private static final String TAG_PREFERRED_MAIN_GUN_DATA_ID = "TaczMoreLevelPreferredMainGunDataId";

    public static final int SOLDIER_SKIN_COUNT = 10;
    public static final double GUN_SOUND_HEAR_RANGE_BLOCKS = 40.0;
    private static final int GUN_SOUND_MEMORY_TICKS = 200;

    private static final EntityDataAccessor<Integer> DATA_SKIN_INDEX =
            SynchedEntityData.defineId(AbstractNeutralGunNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TACZ_CRAWLING =
            SynchedEntityData.defineId(AbstractNeutralGunNpcEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos investigateGunSoundPos;
    private int investigateGunSoundUntilTick;

    /** 下一次 {@link #getJumpPower()} 用的档；消费后归 {@link #defaultJumpTier()}。子类可细改。 */
    private int currentJumpTier;
    /** 本拍对当前目标的交火看见。默认头→身→脚。 */
    private boolean gunCombatSight;

    /** 一格全类型创造弹药箱，供 TaCZ {@code reload()} 扫描。 */
    private final NonNullList<ItemStack> creativeAmmoBox = MobCreativeAmmoBox.createSlot();

    private final MobCreativeAmmoBoxReloadAccess creativeAmmoReload =
            new MobCreativeAmmoBoxReloadAccess(this.creativeAmmoBox);

    /** 换枪整理用的 6 格背包，与士兵相同。 */
    private final NonNullList<ItemStack> soldierBackpack = SoldierBackpack.createSlots();

    @Nullable
    private ResourceLocation preferredMainGunDataId;

    @Override
    public NonNullList<ItemStack> mobCreativeAmmoBox() {
        return this.creativeAmmoBox;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> ammo = this.creativeAmmoReload.getCapability(cap, side, this.isAlive());
        return ammo != null ? ammo : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.creativeAmmoReload.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.creativeAmmoReload.revive();
    }

    protected AbstractNeutralGunNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.lookControl = new SoldierPlayerLikeLookControl(this);
        this.moveControl = new SoldierMoveControl(this);
        this.applyEquipmentDropChances();
        if (this.getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
            ground.setCanPassDoors(true);
        }
        this.setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, 0.0F);
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(1, new MobGunCombatMainhandArrangeGoal(this));
        this.goalSelector.addGoal(1, new MobGunIdleMainhandArrangeGoal(this));
        this.resetJumpTierToDefault();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SoldierGroundPathNavigation(this, level);
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int currentJumpTier() {
        return this.currentJumpTier;
    }

    @Override
    public void setCurrentJumpTier(int tier) {
        int last = JumpController.JUMP_TIERS.size() - 1;
        if (last < 0) {
            this.currentJumpTier = 0;
            return;
        }
        int i = tier;
        if (i < 0) {
            i = 0;
        } else if (i > last) {
            i = last;
        }
        this.currentJumpTier = i;
    }

    @Override
    protected float getJumpPower() {
        float base = this.jumpPowerAt(this.currentJumpTier(), false);
        float power = base * this.getBlockJumpFactor() + this.getJumpBoostPower();
        this.resetJumpTierToDefault();
        return power;
    }

    @Override
    public boolean requestJump(int tier) {
        if (!this.onGround() || this.isInWater() || this.isInLava()) {
            return false;
        }
        this.setCurrentJumpTier(tier);
        this.getJumpControl().jump();
        return true;
    }

    @Override
    public boolean performJump() {
        return this.requestJump(this.defaultJumpTier());
    }

    @Override
    public boolean performJumpTier(int tier) {
        return this.performJumpTier(tier, true);
    }

    @Override
    public boolean performJumpTier(int tier, boolean respectMin) {
        int i = tier;
        if (respectMin) {
            int[] w = this.normalizedWindow();
            if (i < w[0]) {
                i = w[0];
            }
            if (i > w[1]) {
                i = w[1];
            }
        }
        return this.requestJump(i);
    }

    @Override
    public boolean performJumpPower(float basePower) {
        return this.requestJump(JumpController.nearestTier(basePower));
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float multiplier) {
        return JumpController.calculateFallDamage(this, this, fallDistance, multiplier);
    }

    /**
     * 原版 {@link net.minecraft.world.entity.Mob#finalizeSpawn} 约 5% 左撇子；TaCZ 第三人称枪模型对左主手 NPC 不完整，统一右撇子。
     */
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnReason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
        this.setLeftHanded(false);
        return spawnData;
    }

    protected abstract ResourceLocation pickSpawnGunId(net.minecraft.util.RandomSource random);

    protected abstract String skinTextureFolder();

    protected void registerNeutralGunCombatGoals() {
        this.goalSelector.addGoal(1, new com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidGrenadeAndFireGoal(this));
    }

    protected void reassessCombatGoals() {}

    @Override
    protected BodyRotationControl createBodyControl() {
        return new SoldierPlayerLikeBodyRotationControl(this);
    }

    public boolean isGunCombatAiming() {
        return IGun.mainHandHoldGun(this) && this.isAggressive();
    }

    public boolean isInCombatStance() {
        return IGun.mainHandHoldGun(this);
    }

    public boolean canTradePeacefully() {
        return this.getTarget() == null && !this.isInCombatStance() && !this.isAggressive();
    }

    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        MobGunPlayerLikeAimHelper.tickHeadTurnBodyFollow(this, this.yHeadRot);
        return animStep;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_INDEX, 0);
        this.entityData.define(DATA_TACZ_CRAWLING, false);
    }

    public int getSoldierSkinIndex() {
        return Mth.clamp(this.entityData.get(DATA_SKIN_INDEX), 0, SOLDIER_SKIN_COUNT - 1);
    }

    protected void setSoldierSkinIndex(int index) {
        this.entityData.set(DATA_SKIN_INDEX, Mth.clamp(index, 0, SOLDIER_SKIN_COUNT - 1));
    }

    @Override
    public ResourceLocation getSoldierSkinTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/entity/" + skinTextureFolder() + "/skin_" + getSoldierSkinIndex() + ".png");
    }

    @Override
    public boolean isTaczCrawlingSynced() {
        return this.entityData.get(DATA_TACZ_CRAWLING);
    }

    public void syncTaczCrawlingFlag(boolean crawling) {
        this.entityData.set(DATA_TACZ_CRAWLING, crawling);
    }

    @Override
    public boolean isTaczCrawling() {
        return SoldierTaczCrawlHelper.isCrawling(this);
    }

    @Override
    public SoldierTaczCrawlState getTaczCrawlState() {
        return SoldierTaczCrawlHelper.getCrawlState(this);
    }

    public boolean setTaczCrawling(boolean crawling) {
        return SoldierTaczCrawlHelper.applyCrawl(this, crawling);
    }

    protected void applyEquipmentDropChances() {
        float p = Config.equipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, p);
        this.setDropChance(EquipmentSlot.OFFHAND, p);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, p);
            }
        }
    }

    @Override
    public boolean hasGunCombatSight() {
        return this.gunCombatSight;
    }

    protected boolean evaluateGunCombatSight(LivingEntity target) {
        return MobGunPlayerLikeAimHelper.hasCombatLineOfSight(this, target);
    }

    private void tickGunCombatSight() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.gunCombatSight = false;
            return;
        }
        this.gunCombatSight = this.evaluateGunCombatSight(target);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            this.tickGunCombatSight();
            MobCreativeAmmoBox.ensure(this.creativeAmmoBox, this.tickCount);
        }
        super.tick();
        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            if (target != null
                    && (!target.isAlive()
                            || (target instanceof Player player
                                    && (player.isCreative() || player.isSpectator())))) {
                this.setTarget(null);
            }
            SoldierTaczCrawlHelper.maintainCrawlFromSync(this);
        }
        SoldierTaczCrawlHelper.applyCrawlPose(this);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = this.getTarget();
        super.setTarget(target);
        if (this.level().isClientSide()) {
            return;
        }
        if (target != null && target.isAlive() && previous != target) {
            this.enterCombatStance();
        } else if (target == null && previous != null) {
            this.enterPeacefulStance();
        }
    }

    protected void enterCombatStance() {
        this.drawHolsteredGun();
        this.reassessCombatGoals();
    }

    protected void enterPeacefulStance() {
        this.setAggressive(false);
        this.holsterGun();
        this.reassessCombatGoals();
    }

    public void equipHolsteredGun(ResourceLocation gunDataId) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack gunStack = TaczGunMobHelper.createGunStack(gunDataId);
        this.preferredMainGunDataId = gunStack.isEmpty() ? null : gunDataId;
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, gunStack);
        this.reassessCombatGoals();
    }

    public boolean hasHolsteredGun() {
        return IGun.getIGunOrNull(this.getOffhandItem()) != null;
    }

    public boolean canDrawHolsteredGun() {
        if (this.level().isClientSide() || !this.hasHolsteredGun() || IGun.mainHandHoldGun(this)) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        return op.getSynDrawCoolDown() == 0L
                && !op.getSynIsBolting()
                && !op.getSynReloadState().getStateType().isReloading();
    }

    public boolean drawHolsteredGun() {
        if (!this.canDrawHolsteredGun()) {
            return false;
        }
        ItemStack holstered = this.getOffhandItem().copy();
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.MAINHAND, holstered);
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        op.draw(this::getMainHandItem);
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
        this.setAggressive(true);
        this.reassessCombatGoals();
        return true;
    }

    public boolean holsterGun() {
        if (this.level().isClientSide() || !IGun.mainHandHoldGun(this)) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        op.aim(false);
        op.cancelReload();
        ItemStack main = this.getMainHandItem().copy();
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (this.getOffhandItem().isEmpty()) {
            this.setItemSlot(EquipmentSlot.OFFHAND, main);
        }
        this.reassessCombatGoals();
        return true;
    }

    @Override
    public Mob asArrangeLiving() {
        return this;
    }

    @Override
    @Nullable
    public ResourceLocation getPreferredMainGunDataId() {
        return this.preferredMainGunDataId;
    }

    @Override
    public NonNullList<ItemStack> soldierBackpackSlots() {
        return this.soldierBackpack;
    }

    @Override
    public boolean tryPutIntoSoldierBackpack(ItemStack stack) {
        if (this.level().isClientSide()) {
            return false;
        }
        return SoldierBackpack.tryPut(this.soldierBackpack, stack);
    }

    @Override
    public void swapHandWithSoldierBackpackSlot(EquipmentSlot hand, int index) {
        if (this.level().isClientSide() || index < 0 || index >= this.soldierBackpack.size()) {
            return;
        }
        ItemStack bag = this.soldierBackpack.get(index);
        ItemStack held = this.getItemBySlot(hand);
        this.soldierBackpack.set(index, held.isEmpty() ? ItemStack.EMPTY : held.copy());
        this.setItemSlot(hand, bag);
    }

    @Override
    public void swapMainAndOffhand() {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack main = this.getMainHandItem().copy();
        ItemStack off = this.getOffhandItem().copy();
        this.setItemSlot(EquipmentSlot.MAINHAND, off);
        this.setItemSlot(EquipmentSlot.OFFHAND, main);
    }

    @Override
    public boolean canOperateWeaponSwap() {
        if (this.level().isClientSide()) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        return op.getSynDrawCoolDown() == 0L
                && !op.getSynIsBolting()
                && !op.getSynReloadState().getStateType().isReloading();
    }

    @Override
    public void afterCombatMainhandArrange(boolean hadMainGunBefore) {
        if (IGun.mainHandHoldGun(this)) {
            IGunOperator op = IGunOperator.fromLivingEntity(this);
            op.initialData();
            op.draw(this::getMainHandItem);
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
            this.setAggressive(true);
            this.reassessCombatGoals();
        } else if (hadMainGunBefore) {
            this.reassessCombatGoals();
        }
    }

    @Override
    public void afterIdleMainhandArrange() {
        if (this.level().isClientSide()) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        op.aim(false);
        op.cancelReload();
        this.setAggressive(false);
        this.reassessCombatGoals();
    }

    @Override
    public void compactSoldierInventory() {
        if (this.level().isClientSide()) {
            return;
        }
        SoldierBackpack.compact(this.soldierBackpack);
        this.mergeHandIntoBackpack(EquipmentSlot.MAINHAND);
        this.mergeHandIntoBackpack(EquipmentSlot.OFFHAND);
        ItemStack main = this.getMainHandItem();
        ItemStack off = this.getOffhandItem();
        if (!main.isEmpty()
                && !off.isEmpty()
                && main.isStackable()
                && ItemStack.isSameItemSameTags(main, off)) {
            int move = Math.min(main.getCount(), off.getMaxStackSize() - off.getCount());
            if (move > 0) {
                off.grow(move);
                main.shrink(move);
                if (main.isEmpty()) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                }
            }
        }
        SoldierBackpack.compact(this.soldierBackpack);
    }

    private void mergeHandIntoBackpack(EquipmentSlot hand) {
        ItemStack stack = this.getItemBySlot(hand);
        if (stack.isEmpty()) {
            return;
        }
        if (SoldierBackpack.tryMergeIntoExisting(this.soldierBackpack, stack) && stack.isEmpty()) {
            this.setItemSlot(hand, ItemStack.EMPTY);
        }
    }

    @Override
    public void equipTaCZGunMainHand(ResourceLocation gunDataId) {
        if (this.level().isClientSide()) {
            return;
        }
        TaczGunMobHelper.equipMainHand(this, gunDataId);
        this.reassessCombatGoals();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.simThreadSupport.serverTick(this.level().getGameTime());
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
        if (IGun.mainHandHoldGun(this)) {
            ItemStack main = this.getMainHandItem();
            TaczGunMobHelper.normalizeFireModeIfInvalid(main);
            TaczGunMobHelper.preferAutoFireModeForMobMainHandGun(main);
        }
        if (this.isGunCombatAiming()) {
            this.yHeadRot = this.getYRot();
        }
    }

    @Override
    public boolean canSimThreadEnqueue() {
        return true;
    }

    @Override
    public boolean offerSimThreadInfo(SimThreadInfo info) {
        return this.simThreadSupport.offer(info, this.level().getGameTime());
    }

    @Override
    @Nullable
    public SimThreadInfo peekSimThreadInfo() {
        return this.simThreadSupport.peek();
    }

    @Override
    public void clearSimThreadInfo() {
        this.simThreadSupport.clearMailbox();
    }

    @Override
    public boolean isSimThreadReceiveCooling() {
        return this.simThreadSupport.isReceiveCooling(this.level().getGameTime());
    }

    @Override
    public void tickSimThreadReceiver(long gameTime) {
        // 已在 customServerAiStep → serverTick 推进，避免与 Manager 重复扣 TTL
    }

    @Override
    public SoldierLocomotionMode getLocomotionMode() {
        return this.simThreadSupport.mode();
    }

    @Override
    public void enterCoverMode(@Nullable BlockPos coverPos, boolean crawl) {
        this.simThreadSupport.enterCover(coverPos, crawl);
    }

    @Override
    public void exitCoverMode() {
        this.simThreadSupport.exitCover();
    }

    @Override
    public int getCoverModeTicksLeft() {
        return this.simThreadSupport.coverTicksLeft();
    }

    @Override
    @Nullable
    public BlockPos getPendingCoverPos() {
        return this.simThreadSupport.pendingCoverPos();
    }

    @Override
    public boolean isPendingCoverCrawl() {
        return this.simThreadSupport.pendingCoverCrawl();
    }

    @Override
    public int getCoverModeDurationTicks() {
        return 30;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.level().isClientSide() || !IGun.mainHandHoldGun(this)) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        this.setSprinting(false);
        ShooterDataHolder dh = op.getDataHolder();
        if (dh != null) {
            dh.sprintTimeS = 0.0F;
        }
        ShootResult result = op.shoot(this::getXRot, this::getYRot);
        if (result == ShootResult.IS_SPRINTING && dh != null) {
            dh.sprintTimeS = 0.0F;
            this.setSprinting(false);
            result = op.shoot(this::getXRot, this::getYRot);
        }
        if (result == ShootResult.NOT_DRAW || result == ShootResult.IS_DRAWING) {
            op.draw(this::getMainHandItem);
        } else if (result == ShootResult.NEED_BOLT) {
            op.bolt();
        } else if (result == ShootResult.NO_AMMO) {
            op.reload();
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        Level level = this.level();
        if (level != null && !level.isClientSide() && (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND)) {
            this.reassessCombatGoals();
        }
    }

    @Override
    public void notifyGunFireHeard(Vec3 sourceWorldPos, double hearRangeSq) {
        if (this.level().isClientSide()) {
            return;
        }
        if (this.distanceToSqr(sourceWorldPos.x, sourceWorldPos.y, sourceWorldPos.z) > hearRangeSq) {
            return;
        }
        this.investigateGunSoundPos = BlockPos.containing(sourceWorldPos);
        this.investigateGunSoundUntilTick = this.tickCount + GUN_SOUND_MEMORY_TICKS;
    }

    @Override
    public boolean hasPendingGunSoundInvestigation() {
        return this.investigateGunSoundPos != null && this.tickCount < this.investigateGunSoundUntilTick;
    }

    @Override
    public void clearGunSoundInvestigation() {
        this.investigateGunSoundPos = null;
    }

    @Nullable
    @Override
    public BlockPos getGunSoundInvestigatePos() {
        if (this.investigateGunSoundPos == null || this.tickCount >= this.investigateGunSoundUntilTick) {
            return null;
        }
        return this.investigateGunSoundPos;
    }

    protected void initializeNeutralGunLoadout(net.minecraft.util.RandomSource random) {
        this.setSoldierSkinIndex(random.nextInt(SOLDIER_SKIN_COUNT));
        this.equipHolsteredGun(this.pickSpawnGunId(random));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_SKIN_INDEX, this.getSoldierSkinIndex());
        if (this.preferredMainGunDataId != null) {
            compound.putString(TAG_PREFERRED_MAIN_GUN_DATA_ID, this.preferredMainGunDataId.toString());
        }
        SoldierBackpack.save(compound, this.soldierBackpack);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setLeftHanded(false);
        if (compound.contains(TAG_SKIN_INDEX, Tag.TAG_INT)) {
            this.setSoldierSkinIndex(Mth.clamp(compound.getInt(TAG_SKIN_INDEX), 0, SOLDIER_SKIN_COUNT - 1));
        }
        if (compound.contains(TAG_PREFERRED_MAIN_GUN_DATA_ID, Tag.TAG_STRING)) {
            this.preferredMainGunDataId =
                    ResourceLocation.tryParse(compound.getString(TAG_PREFERRED_MAIN_GUN_DATA_ID));
        }
        SoldierBackpack.load(compound, this.soldierBackpack);
        this.applyEquipmentDropChances();
    }

    /**
     * 站/蹲/趴：对齐玩家碰撞降级（站不下→蹲→趴）；死/睡等姿势直通。
     */
    @Override
    public void setPose(Pose pose) {
        super.setPose(this.resolveCollidingPose(pose));
    }

    /** 站/蹲/趴碰撞降级；非移动姿势原样返回。 */
    private Pose resolveCollidingPose(Pose desired) {
        if (desired == null
                || (desired != Pose.STANDING && desired != Pose.CROUCHING && desired != Pose.SWIMMING)) {
            return desired;
        }
        if (this.level() == null || !this.isAddedToWorld() || this.isPassenger()) {
            return desired;
        }
        if (!this.canEnterPose(Pose.SWIMMING)) {
            return this.getPose();
        }
        if (this.canEnterPose(desired)) {
            return desired;
        }
        if (desired == Pose.STANDING) {
            if (this.canEnterPose(Pose.CROUCHING)) {
                return Pose.CROUCHING;
            }
            return Pose.SWIMMING;
        }
        if (desired == Pose.CROUCHING) {
            return Pose.SWIMMING;
        }
        return this.getPose();
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (pose == Pose.SLEEPING) {
            return super.getDimensions(pose);
        }
        EntityDimensions likePlayer =
                switch (pose) {
                    case STANDING -> Player.STANDING_DIMENSIONS;
                    case CROUCHING -> EntityDimensions.scalable(0.6F, Player.CROUCH_BB_HEIGHT);
                    case SWIMMING, FALL_FLYING, SPIN_ATTACK ->
                            EntityDimensions.scalable(Player.SWIMMING_BB_WIDTH, Player.SWIMMING_BB_HEIGHT);
                    default -> null;
                };
        if (likePlayer != null) {
            return likePlayer.scale(this.getScale());
        }
        return super.getDimensions(pose);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return switch (pose) {
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> 0.4F;
            case CROUCHING -> 1.27F;
            case STANDING -> 1.62F;
            default -> super.getStandingEyeHeight(pose, dimensions);
        };
    }

    @Override
    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
    }
}
