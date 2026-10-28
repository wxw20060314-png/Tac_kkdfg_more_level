package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.GunEquippedMob;
import com.kkdfg.Tacz_morelevel.api.JumpActingMob;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob;
import com.kkdfg.Tacz_morelevel.api.TaczGunSoundInvestigatingMob;
import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.kkdfg.Tacz_morelevel.entity.SoldierBackpack;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBox;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxHolder;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxReloadAccess;
import com.kkdfg.Tacz_morelevel.entity.SoldierTaczCrawlState;
import com.kkdfg.Tacz_morelevel.entity.SoldierWeaponArrangeHost;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunCombatMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunIdleMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSightHolder;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeBodyRotationControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierFleeFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierMoveControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeLookControl;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczOrdinaryGunCatalog;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierGroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 史莱姆实验体抽象基类：独立于士兵 / 中立 / 友方树。
 * <p>
 * 三代分裂：代际只改血量/scale/能否再裂；二命与碎片均按表写 HP+scale。
 * root 认主 / 根成员表 / TTL 拆树、root 超距拉回；枪池由子类固定；碰撞与渲染共用 scale；
 * 指挥官树不远距离卸载；真死走实体战利品表。声音对齐原版史莱姆；开门 + 枪声调查。
 * 根彻底死亡时向成员表发 {@link #onRootFinalDeath}（默认无掉落移除）；二命不通知。
 * <p>
 * 具体实体负责挂枪 Goal / 近战 Goal / 索敌；本类提供装枪、射击、分裂与链路。
 */
public abstract class AbstractSlimeExperimentEntity extends Monster
        implements RangedAttackMob,
                GunEquippedMob,
                GunCombatSightHolder,
                TaczPlayerLikeMob,
                PlayerLikeHeadBodyYawMob,
                TaczGunSoundInvestigatingMob,
                JumpActingMob,
                SoldierWeaponArrangeHost,
                MobCreativeAmmoBoxHolder,
                com.kkdfg.Tacz_morelevel.api.GunStrafeSpeedMob {

    public static final String TEXTURE_FOLDER = "slime_experiment";
    public static final ResourceLocation SKIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "textures/entity/" + TEXTURE_FOLDER + "/skin_0.png");

    /** 基准碰撞（scale=1）：接近玩家宽高，便于持枪判定。 */
    public static final float BASE_WIDTH = 0.6F;
    public static final float BASE_HEIGHT = 1.8F;

    public static final int LINK_TIMEOUT_TICKS = MarshalFactionHelper.LINK_TIMEOUT_TICKS;
    public static final double RECALL_DIST_IDLE = MarshalFactionHelper.RECALL_DIST_IDLE;
    public static final double RECALL_DIST_COMBAT = MarshalFactionHelper.RECALL_DIST_COMBAT;
    public static final double GUN_SOUND_HEAR_RANGE_BLOCKS = 40.0;
    private static final int GUN_SOUND_MEMORY_TICKS = 200;

    /** 额外背包格数（与 {@link SoldierBackpack#SLOTS} 一致）。 */
    public static final int SOLDIER_BACKPACK_SLOTS = SoldierBackpack.SLOTS;

    private static final String TAG_GENERATION = "SlimeExpGeneration";
    private static final String TAG_ROOT = "SlimeExpRoot";
    private static final String TAG_PARENT = "SlimeExpParent";
    private static final String TAG_LINK_TTL = "SlimeExpLinkTtl";
    private static final String TAG_ROOT_IS_COMMANDER = "SlimeExpRootIsCommander";
    private static final String TAG_TREE_MEMBERS = "SlimeExpTreeMembers";
    private static final String TAG_PREFERRED_MAIN_GUN_DATA_ID = "TaczMoreLevelPreferredMainGunDataId";

    private static final EntityDataAccessor<Integer> DATA_GENERATION =
            SynchedEntityData.defineId(AbstractSlimeExperimentEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(AbstractSlimeExperimentEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_ROOT_UUID =
            SynchedEntityData.defineId(
                    AbstractSlimeExperimentEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_PARENT_UUID =
            SynchedEntityData.defineId(
                    AbstractSlimeExperimentEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    /**
     * 分裂规格表：index = generation（0..2）。大小/能否分裂/数量在此；
     * 血量以 {@link Config#slimeCommanderSplitMaxHealth}（bosses.toml {@code slimeCommanderSplit}）为准。
     */
    public static final SplitSpec[] SPLIT_TABLE = {
        new SplitSpec(300.0F, 1.0F, 3, 4, true),
        new SplitSpec(150.0F, 0.7F, 3, 4, true),
        new SplitSpec(75.0F, 0.4F, 0, 0, false),
    };

    /** 当前代血量（配置 + Boss 全局 maxHealthScale）。 */
    public static float splitMaxHealth(int generation) {
        int g = Mth.clamp(generation, 0, Config.slimeCommanderSplitMaxHealth.length - 1);
        return Config.slimeCommanderSplitMaxHealth[g];
    }

    /** 偏好主武器标记（非槽位）；默认 {@code null}，固定枪时写入。 */
    @Nullable private ResourceLocation preferredMainGunDataId;

    /** 额外 {@link #SOLDIER_BACKPACK_SLOTS} 格；仅服务端武器整理使用。 */
    private final NonNullList<ItemStack> soldierBackpack = SoldierBackpack.createSlots();

    /** 一格全类型创造弹药箱，供 TaCZ {@code reload()} 扫描。 */
    private final NonNullList<ItemStack> creativeAmmoBox = MobCreativeAmmoBox.createSlot();

    private final MobCreativeAmmoBoxReloadAccess creativeAmmoReload =
            new MobCreativeAmmoBoxReloadAccess(this.creativeAmmoBox);

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

    private int linkTtl = LINK_TIMEOUT_TICKS;
    /** 树根为指挥官（含指挥官自身及其直接/间接分裂）：不远距离刷新。 */
    private boolean rootIsCommander;
    /**
     * 仅根启用：同树成员 UUID 表。非根忽略。服务端字段，不进 SynchedEntityData。
     */
    private final Set<UUID> treeMemberIds = new HashSet<>();
    /** 二命处理中，防止递归致死。 */
    private boolean handlingSecondLife;
    @Nullable
    private BlockPos investigateGunSoundPos;
    private int investigateGunSoundUntilTick;

    /** 下一次 {@link #getJumpPower()} 用的档；消费后归 {@link #defaultJumpTier()}。子类可细改。 */
    private int currentJumpTier;
    /** 本拍对当前目标的交火看见。默认头→身→脚。 */
    private boolean gunCombatSight;

    protected AbstractSlimeExperimentEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.lookControl = new SoldierPlayerLikeLookControl(this);
        this.moveControl = new SoldierMoveControl(this);
        this.applyNoDropChances();
        this.setLeftHanded(false);
        if (this.getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
            ground.setCanPassDoors(true);
        }
        this.setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, 0.0F);
        this.goalSelector.addGoal(0, new SoldierFleeFireGoal(this));
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

    @Override
    protected BodyRotationControl createBodyControl() {
        return new SoldierPlayerLikeBodyRotationControl(this);
    }

    /**
     * 每 tick 将躯干限制在头 ±{@link MobGunPlayerLikeAimHelper#MAX_HEAD_BODY_YAW_DEG}° 内；
     * 交火时头由 Gun Goal {@link MobGunPlayerLikeAimHelper#applyPlayerLikeView} 对准目标。
     */
    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        MobGunPlayerLikeAimHelper.tickHeadTurnBodyFollow(this, this.yHeadRot);
        return animStep;
    }

    /** 战士默认属性：移速等抄特种兵 Config；血量取 gen0 配置（spawn 时再按代际覆盖）。 */
    public static AttributeSupplier.Builder createBaseAttributes() {
        Config.NpcAttributeStats stats = Config.npcAttributesFor(SoldierSpawnArmorProfile.SPECIAL_FORCES);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, splitMaxHealth(0))
                .add(Attributes.MOVEMENT_SPEED, stats.movementSpeed)
                .add(Attributes.FOLLOW_RANGE, stats.followRange)
                .add(Attributes.ATTACK_DAMAGE, stats.attackDamage);
    }

    /** 交火属性段（指挥官 / 战士各用自己的 Boss 配置）。 */
    protected abstract Config.NpcAttributeStats slimeCombatAttributes();

    @Override
    public double gunStrafeSpeedModifier() {
        return this.slimeCombatAttributes().gunStrafeSpeedModifier;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_GENERATION, 0);
        this.entityData.define(DATA_SCALE, SPLIT_TABLE[0].scale);
        this.entityData.define(DATA_ROOT_UUID, Optional.empty());
        this.entityData.define(DATA_PARENT_UUID, Optional.empty());
    }

    // region —— 分裂表 / 代际 ——

    public SplitSpec currentSpec() {
        return SPLIT_TABLE[Mth.clamp(this.getGeneration(), 0, SPLIT_TABLE.length - 1)];
    }

    public int getGeneration() {
        return this.entityData.get(DATA_GENERATION);
    }

    protected void setGeneration(int generation) {
        this.entityData.set(DATA_GENERATION, Mth.clamp(generation, 0, SPLIT_TABLE.length - 1));
    }

    public float getSlimeScale() {
        return this.entityData.get(DATA_SCALE);
    }

    protected void setSlimeScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
        this.refreshDimensions();
    }

    public boolean canSecondLifeSplit() {
        return this.currentSpec().canSplit;
    }

    // endregion

    // region —— 树 / root ——

    public boolean isTreeRoot() {
        UUID root = this.getRootUuid();
        return root == null || root.equals(this.getUUID());
    }

    @Nullable
    public UUID getRootUuid() {
        return this.entityData.get(DATA_ROOT_UUID).orElse(null);
    }

    public void setRootUuid(@Nullable UUID uuid) {
        this.entityData.set(DATA_ROOT_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getParentUuid() {
        return this.entityData.get(DATA_PARENT_UUID).orElse(null);
    }

    public void setParentUuid(@Nullable UUID uuid) {
        this.entityData.set(DATA_PARENT_UUID, Optional.ofNullable(uuid));
    }

    /** 若尚未写 root，则自己为根。 */
    public void ensureRootIdentity() {
        if (this.getRootUuid() == null) {
            this.setRootUuid(this.getUUID());
        }
    }

    public void resetLinkTimeout() {
        this.linkTtl = LINK_TIMEOUT_TICKS;
    }

    /** 指挥官实体覆写为 true。 */
    protected boolean isCommanderEntity() {
        return false;
    }

    /** 指挥官树（本人或其分裂谱系）：不远距离刷新。 */
    public boolean isRootCommanderTree() {
        return this.rootIsCommander || this.isCommanderEntity();
    }

    public void setRootIsCommander(boolean rootIsCommander) {
        this.rootIsCommander = rootIsCommander || this.isCommanderEntity();
    }

    @Nullable
    public AbstractSlimeExperimentEntity findRootEntity() {
        this.ensureRootIdentity();
        UUID rootId = this.getRootUuid();
        if (rootId == null) {
            return null;
        }
        if (rootId.equals(this.getUUID())) {
            return this;
        }
        if (!(this.level() instanceof ServerLevel server)) {
            return null;
        }
        Entity entity = server.getEntity(rootId);
        return entity instanceof AbstractSlimeExperimentEntity slime && slime.isAlive() ? slime : null;
    }

    public boolean isSameTree(@Nullable LivingEntity other) {
        if (!(other instanceof AbstractSlimeExperimentEntity slime)) {
            return false;
        }
        UUID a = this.getRootUuid();
        UUID b = slime.getRootUuid();
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    /** 根：登记同树成员。非根无操作。 */
    public void registerTreeMember(UUID memberId) {
        if (!this.isTreeRoot() || memberId == null || memberId.equals(this.getUUID())) {
            return;
        }
        this.treeMemberIds.add(memberId);
    }

    /** 根：从表移除成员。非根无操作。 */
    public void unregisterTreeMember(UUID memberId) {
        if (!this.isTreeRoot() || memberId == null) {
            return;
        }
        this.treeMemberIds.remove(memberId);
    }

    /** 向真正的树根登记自己（分裂出碎片后调用）。 */
    protected void registerSelfWithTreeRoot() {
        if (this.isTreeRoot() || this.level().isClientSide()) {
            return;
        }
        AbstractSlimeExperimentEntity root = this.findRootEntity();
        if (root != null) {
            root.registerTreeMember(this.getUUID());
        }
    }

    /** 从树根表注销自己。 */
    protected void unregisterSelfFromTreeRoot() {
        if (this.isTreeRoot() || this.level().isClientSide()) {
            return;
        }
        AbstractSlimeExperimentEntity root = this.findRootEntity();
        if (root != null) {
            root.unregisterTreeMember(this.getUUID());
        }
    }

    /**
     * 根彻底死亡：通知表内仍存活成员，然后清表。二命不走这里。
     */
    protected void notifyTreeMembersRootFinalDeath() {
        if (!this.isTreeRoot() || this.level().isClientSide()) {
            return;
        }
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        for (UUID id : new ArrayList<>(this.treeMemberIds)) {
            Entity entity = server.getEntity(id);
            if (entity instanceof AbstractSlimeExperimentEntity member && member.isAlive()) {
                member.onRootFinalDeath(this);
            }
        }
        this.treeMemberIds.clear();
    }

    /**
     * 收到根彻底死亡通知。默认：无掉落移除（对齐失联 TTL 到期）。
     */
    protected void onRootFinalDeath(AbstractSlimeExperimentEntity root) {
        MarshalFactionHelper.discardWithoutDrops(this);
    }

    /**
     * 群体仇恨：根 {@link #setTarget} 时下推成员表；非根→根由
     * {@link com.kkdfg.Tacz_morelevel.entity.ai.SlimeShareTargetToRootGoal} 上送（对齐列夫护卫）。
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (this.level().isClientSide() || !this.isTreeRoot()) {
            return;
        }
        if (target != null && target.isAlive() && this.wantsToAttack(target)) {
            this.alertTreeMembersToTarget(target);
        }
    }

    /** 根：把目标推给表内无目标（或目标已死）的成员。 */
    protected void alertTreeMembersToTarget(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server) || target == null) {
            return;
        }
        for (UUID id : new ArrayList<>(this.treeMemberIds)) {
            Entity entity = server.getEntity(id);
            if (!(entity instanceof AbstractSlimeExperimentEntity member) || !member.isAlive()) {
                continue;
            }
            LivingEntity memberTarget = member.getTarget();
            if (memberTarget != null && memberTarget.isAlive()) {
                continue;
            }
            if (member.wantsToAttack(target)) {
                member.setTarget(target);
            }
        }
    }

    /** 同树成员不当作敌对目标。 */
    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isSameTree(target) && super.canAttack(target);
    }

    /** 同树视为同盟，避免 HurtBy / 队伍判定互殴。 */
    @Override
    public boolean isAlliedTo(Entity other) {
        if (other instanceof LivingEntity living && this.isSameTree(living)) {
            return true;
        }
        return super.isAlliedTo(other);
    }

    // endregion

    // region —— 尺寸 / 贴图 / 人形渲染 ——

    public ResourceLocation getSlimeSkinTexture() {
        return SKIN_TEXTURE;
    }

    /** 人形皮肤路径（Steve UV）；与 {@link #SKIN_TEXTURE} 相同。 */
    @Override
    public ResourceLocation getSoldierSkinTextureLocation() {
        return SKIN_TEXTURE;
    }

    @Override
    public boolean isGunCombatAiming() {
        return IGun.mainHandHoldGun(this) && this.isAggressive();
    }

    @Override
    public boolean isTaczCrawlingSynced() {
        return false;
    }

    @Override
    public boolean isTaczCrawling() {
        return false;
    }

    @Override
    public SoldierTaczCrawlState getTaczCrawlState() {
        return SoldierTaczCrawlState.STANDING;
    }

    @Override
    public boolean setTaczCrawling(boolean crawling) {
        return false;
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float s = this.getSlimeScale();
        return EntityDimensions.scalable(BASE_WIDTH * s, BASE_HEIGHT * s);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.85F;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_SCALE.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    // endregion

    // region —— 生成 / 属性应用 ——

    /**
     * 按代际严格写血量与 scale（碰撞/渲染）；可选满血、换枪。
     * 不改移速/走位（由子类固定配置）；枪池由 {@link #gunPoolId()} 决定。
     */
    public void applyGenerationStats(int generation, boolean healToFull, boolean reequipGun) {
        this.setGeneration(generation);
        SplitSpec spec = this.currentSpec();
        this.setSlimeScale(spec.scale);

        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(splitMaxHealth(this.getGeneration()));
        }

        if (healToFull) {
            this.setHealth(this.getMaxHealth());
        }
        if (reequipGun && !this.level().isClientSide()) {
            this.equipFromCurrentPool();
        }
        this.onGenerationStatsApplied(spec);
    }

    /** 子类可在代际变化后刷新 Goal 点射间隔等。 */
    protected void onGenerationStatsApplied(SplitSpec spec) {}

    /** 本实体固定枪池 id（与 generation 无关）。 */
    protected abstract String gunPoolId();

    public void equipFromCurrentPool() {
        if (this.level().isClientSide()) {
            return;
        }
        ResourceLocation preferred = this.getPreferredMainGunDataId();
        if (preferred != null) {
            this.equipTaCZGunMainHand(preferred);
            return;
        }
        ResourceLocation gunId =
                TaczOrdinaryGunCatalog.pickRandomForPoolId(this.getRandom(), this.gunPoolId());
        this.equipTaCZGunMainHand(gunId);
    }

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
        this.ensureRootIdentity();
        if (this.isCommanderEntity()) {
            this.setRootIsCommander(true);
        }
        if (this.getParentUuid() == null && this.isTreeRoot()) {
            this.applyGenerationStats(0, true, true);
        }
        this.applyNoDropChances();
        return spawnData;
    }

    // endregion

    // region —— 二命分裂 ——

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.isOwnTaczProjectileDamage(source)) {
            return false;
        }
        if (this.shouldBlockSameTreeDamage(source)) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            amount *= 5.0F;
        }
        return super.hurt(source, amount);
    }

    /**
     * 二命在盔甲/魔抗/吸收减免之后判定（{@code actuallyHurt} 收到的已是最终伤害）。
     * 可裂/分裂数量读<strong>当前代</strong> {@link SplitSpec}，不是下一级。
     */
    @Override
    protected void actuallyHurt(DamageSource source, float damageAmount) {
        if (!this.level().isClientSide()
                && !this.handlingSecondLife
                && damageAmount > 0.0F
                && damageAmount >= this.getHealth()
                && this.trySecondLife(source)) {
            return;
        }
        super.actuallyHurt(source, damageAmount);
    }

    /** 自己的 TaCZ 动能弹 / 自伤来源不结算（对齐毒蝎）。 */
    protected boolean isOwnTaczProjectileDamage(DamageSource source) {
        if (this.isOwnKineticBullet(source.getEntity()) || this.isOwnKineticBullet(source.getDirectEntity())) {
            return true;
        }
        Entity causing = source.getEntity();
        Entity direct = source.getDirectEntity();
        return causing == this || direct == this;
    }

    private boolean isOwnKineticBullet(@Nullable Entity entity) {
        return entity instanceof EntityKineticBullet bullet && bullet.getOwner() == this;
    }

    protected boolean shouldBlockSameTreeDamage(DamageSource source) {
        Entity attacker = resolveDamagingActor(source);
        return attacker instanceof LivingEntity living && this.isSameTree(living);
    }

    @Nullable
    private static Entity resolveDamagingActor(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof EntityKineticBullet bullet && bullet.getOwner() != null) {
            return bullet.getOwner();
        }
        Entity causing = source.getEntity();
        if (causing instanceof EntityKineticBullet bullet && bullet.getOwner() != null) {
            return bullet.getOwner();
        }
        return causing != null ? causing : direct;
    }

    /**
     * 致死二命：按<strong>当前代</strong> roll 碎片数并刷出；本体升下一档（满血+scale，不换枪）。
     *
     * @return 是否已拦截死亡
     */
    protected boolean trySecondLife(DamageSource source) {
        if (!this.canSecondLifeSplit() || !(this.level() instanceof ServerLevel server)) {
            return false;
        }
        this.handlingSecondLife = true;
        try {
            int nextGen = this.getGeneration() + 1;
            if (nextGen >= SPLIT_TABLE.length) {
                return false;
            }
            // 可裂数量读当前档，再升代
            int fragmentCount = this.currentSpec().rollSplitCount(this.getRandom());
            this.applyGenerationStats(nextGen, true, false);
            this.spawnSplitFragments(server, nextGen, fragmentCount);
            this.playSplitSound();
            return true;
        } finally {
            this.handlingSecondLife = false;
        }
    }

    /**
     * @param childGeneration 碎片代际（血量/scale）
     * @param count 碎片数量（由<strong>父体当前代</strong> SplitSpec 决定）
     */
    protected void spawnSplitFragments(ServerLevel server, int childGeneration, int count) {
        this.ensureRootIdentity();
        UUID rootId = this.getRootUuid();
        for (int i = 0; i < count; i++) {
            AbstractSlimeExperimentEntity child = this.createSplitInstance(server);
            if (child == null) {
                continue;
            }
            double ox = (this.getRandom().nextDouble() - 0.5D) * 2.0D;
            double oz = (this.getRandom().nextDouble() - 0.5D) * 2.0D;
            child.moveTo(this.getX() + ox, this.getY(), this.getZ() + oz, this.getYRot(), 0.0F);
            child.setRootUuid(rootId);
            child.setParentUuid(this.getUUID());
            child.setRootIsCommander(this.isRootCommanderTree());
            child.resetLinkTimeout();
            child.applyGenerationStats(childGeneration, true, true);
            child.finalizeSpawn(
                    server,
                    server.getCurrentDifficultyAt(child.blockPosition()),
                    MobSpawnType.MOB_SUMMONED,
                    null,
                    null);
            // finalizeSpawn 可能把 root 重置为自身；碎片必须跟树根
            child.setRootUuid(rootId);
            child.setParentUuid(this.getUUID());
            child.setRootIsCommander(this.isRootCommanderTree());
            child.applyGenerationStats(childGeneration, true, false);
            server.addFreshEntity(child);
            AbstractSlimeExperimentEntity root =
                    this.isTreeRoot() ? this : this.findRootEntity();
            if (root != null) {
                root.registerTreeMember(child.getUUID());
            } else {
                child.registerSelfWithTreeRoot();
            }
        }
    }

    /**
     * 创建分裂实例（尚未 addFresh）。默认刷战士，指挥官不会被分裂出来。
     */
    @Nullable
    protected AbstractSlimeExperimentEntity createSplitInstance(ServerLevel server) {
        Entity created =
                com.kkdfg.Tacz_morelevel.entity.ModEntities.SLIME_WARRIOR.get().create(server);
        return created instanceof AbstractSlimeExperimentEntity slime ? slime : null;
    }

    protected void playSplitSound() {
        this.playSound(
                this.getSlimeScale() < 0.6F ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH,
                1.0F,
                (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F + 1.0F);
    }

    // endregion

    // region —— root 拉回 / TTL ——

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
        if (this.level().isClientSide()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null
                && (!target.isAlive()
                        || (target instanceof Player player
                                && (player.isCreative() || player.isSpectator())))) {
            this.setTarget(null);
        }
        this.ensureRootIdentity();
        if (this.isTreeRoot()) {
            this.tickRootRecallMinions();
        } else {
            this.tickFollowerLink();
        }
        this.tickShareRootTarget();
    }

    /** 根：把过远的同树成员末影拉回（优先成员表，顺带清理失效 UUID）。 */
    protected void tickRootRecallMinions() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        double limit = this.getTarget() != null && this.getTarget().isAlive()
                ? RECALL_DIST_COMBAT
                : RECALL_DIST_IDLE;
        double limitSq = limit * limit;
        Iterator<UUID> it = this.treeMemberIds.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            Entity entity = server.getEntity(id);
            if (!(entity instanceof AbstractSlimeExperimentEntity member) || !member.isAlive()) {
                it.remove();
                continue;
            }
            if (!this.isSameTree(member)) {
                it.remove();
                continue;
            }
            if (this.distanceToSqr(member) > limitSq) {
                recallNearRoot(member, this);
                member.resetLinkTimeout();
            }
        }
    }

    /** 非根：与 root 保活；失联 TTL 耗尽则无掉落删除。 */
    protected void tickFollowerLink() {
        AbstractSlimeExperimentEntity root = this.findRootEntity();
        if (root != null) {
            this.resetLinkTimeout();
            return;
        }
        this.linkTtl--;
        if (this.linkTtl <= 0) {
            MarshalFactionHelper.discardWithoutDrops(this);
        }
    }

    /** 协同：非根抄 root 的仇恨（root 无目标时不抢自己的）。 */
    protected void tickShareRootTarget() {
        if (this.isTreeRoot()) {
            return;
        }
        AbstractSlimeExperimentEntity root = this.findRootEntity();
        if (root == null) {
            return;
        }
        LivingEntity rootTarget = root.getTarget();
        if (rootTarget != null && rootTarget.isAlive() && this.wantsToAttack(rootTarget)) {
            LivingEntity selfTarget = this.getTarget();
            if (selfTarget == null || !selfTarget.isAlive()) {
                this.setTarget(rootTarget);
            }
        }
    }

    protected boolean wantsToAttack(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target == this) {
            return false;
        }
        return !this.isSameTree(target) && this.canAttack(target);
    }

    protected static void recallNearRoot(AbstractSlimeExperimentEntity member, LivingEntity root) {
        if (!(member.level() instanceof ServerLevel level)) {
            return;
        }
        RandomSource random = member.getRandom();
        for (int i = 0; i < 10; i++) {
            int dx = Mth.nextInt(random, -3, 3);
            int dz = Mth.nextInt(random, -3, 3);
            int dy = Mth.nextInt(random, -1, 1);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            double x = root.getX() + dx;
            double y = root.getY() + dy;
            double z = root.getZ() + dz;
            if (!level.noCollision(member.getType().getAABB(x, y, z))) {
                continue;
            }
            MarshalFactionHelper.enderTeleport(member, x, y, z);
            return;
        }
        MarshalFactionHelper.enderTeleport(member, root.getX(), root.getY(), root.getZ());
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide() && !this.handlingSecondLife) {
            if (this.isTreeRoot()) {
                this.notifyTreeMembersRootFinalDeath();
            } else {
                this.unregisterSelfFromTreeRoot();
            }
        }
        super.die(source);
    }

    // endregion

    // region —— 枪 ——

    @Override
    public void equipTaCZGunMainHand(ResourceLocation gunDataId) {
        if (this.level().isClientSide()) {
            return;
        }
        TaczGunMobHelper.equipMainHand(this, gunDataId);
        this.onGunEquipped();
    }

    public void setPreferredMainGunDataId(@Nullable ResourceLocation gunDataId) {
        this.preferredMainGunDataId = gunDataId;
    }

    @Nullable
    public ResourceLocation getPreferredMainGunDataId() {
        return this.preferredMainGunDataId;
    }

    /** 主手是否为偏好主武器；未设置偏好时视为 true。 */
    public boolean isHoldingPreferredMainGun() {
        if (this.preferredMainGunDataId == null) {
            return true;
        }
        IGun gun = IGun.getIGunOrNull(this.getMainHandItem());
        return gun != null && this.preferredMainGunDataId.equals(gun.getGunId(this.getMainHandItem()));
    }

    /**
     * 放入背包空格；满则 false。调用方自行清空来源槽。
     */
    @Override
    public boolean tryPutIntoSoldierBackpack(ItemStack stack) {
        if (this.level().isClientSide()) {
            return false;
        }
        return SoldierBackpack.tryPut(this.soldierBackpack, stack);
    }

    /**
     * 从背包取枪到 {@code dest}：优先 {@code preferredOrNull}，为 null 则任意枪；与 dest 当前物品交换。
     */
    public boolean tryTakeGunFromSoldierBackpack(
            @Nullable ResourceLocation preferredOrNull, EquipmentSlot dest) {
        return SoldierBackpack.tryTakeGun(this.soldierBackpack, this, preferredOrNull, dest);
    }

    @Override
    public Mob asArrangeLiving() {
        return this;
    }

    @Override
    public NonNullList<ItemStack> soldierBackpackSlots() {
        return this.soldierBackpack;
    }

    @Override
    public void swapHandWithSoldierBackpackSlot(EquipmentSlot hand, int index) {
        if (this.level().isClientSide()
                || index < 0
                || index >= this.soldierBackpack.size()) {
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
            this.onGunEquipped();
        } else if (hadMainGunBefore) {
            this.onGunEquipped();
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
    }

    /** 叠堆：背包内合并，再把主/副手可叠物品并入背包已有堆；双手同种也并。 */
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

    /** 装枪后钩子：子类刷新 Goal 点射间隔。 */
    protected void onGunEquipped() {}

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
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

    /** 子类挂射击 Goal 时用；点射间隔由子类固定档（与 generation 无关）。 */
    public abstract int mobGunBurstIntervalTicks();

    // region —— 枪声调查 ——

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
        return this.investigateGunSoundPos != null
                && this.tickCount < this.investigateGunSoundUntilTick;
    }

    @Override
    public void clearGunSoundInvestigation() {
        this.investigateGunSoundPos = null;
    }

    @Override
    @Nullable
    public BlockPos getGunSoundInvestigatePos() {
        if (this.investigateGunSoundPos == null
                || this.tickCount >= this.investigateGunSoundUntilTick) {
            return null;
        }
        return this.investigateGunSoundPos;
    }

    /** 通知范围内史莱姆实验体调查枪声（由 TaCZ 开火事件调用）。 */
    public static void alertSlimesInRangeToGunshot(
            Level level, Vec3 soundOrigin, @Nullable LivingEntity shooter) {
        if (level.isClientSide()) {
            return;
        }
        double r = GUN_SOUND_HEAR_RANGE_BLOCKS;
        double rsq = r * r;
        AABB box =
                new AABB(
                        soundOrigin.x - r,
                        soundOrigin.y - r,
                        soundOrigin.z - r,
                        soundOrigin.x + r,
                        soundOrigin.y + r,
                        soundOrigin.z + r);
        for (AbstractSlimeExperimentEntity mob :
                level.getEntitiesOfClass(AbstractSlimeExperimentEntity.class, box, e -> e != shooter)) {
            mob.notifyGunFireHeard(soundOrigin, rsq);
        }
    }

    // endregion

    // region —— 掉落 / 卸载 ——

    protected void applyNoDropChances() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0F);
        }
    }

    /**
     * 指挥官树永不远距离刷新；独立最高级战士（gen0）可被刷掉；其它独立战士也可刷。
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (this.isRootCommanderTree()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // 掉落走实体战利品表（datagen）
    }

    @Override
    protected boolean shouldDropLoot() {
        return true;
    }

    // endregion

    // region —— 声音（史莱姆） ——

    /** 闲置 / 战斗闲置共用同一池 9 条。 */
    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (hasTarget) {
            // 战斗闲置：与闲置相同
            return switch (this.random.nextInt(9)) {
                case 0 -> ModSounds.SOUND_SLIME_IDLE_1.get();
                case 1 -> ModSounds.SOUND_SLIME_IDLE_2.get();
                case 2 -> ModSounds.SOUND_SLIME_IDLE_3.get();
                case 3 -> ModSounds.SOUND_SLIME_IDLE_4.get();
                case 4 -> ModSounds.SOUND_SLIME_IDLE_5.get();
                case 5 -> ModSounds.SOUND_SLIME_IDLE_6.get();
                case 6 -> ModSounds.SOUND_SLIME_IDLE_7.get();
                case 7 -> ModSounds.SOUND_SLIME_IDLE_8.get();
                default -> ModSounds.SOUND_SLIME_IDLE_9.get();
            };
        }
        return switch (this.random.nextInt(9)) {
            case 0 -> ModSounds.SOUND_SLIME_IDLE_1.get();
            case 1 -> ModSounds.SOUND_SLIME_IDLE_2.get();
            case 2 -> ModSounds.SOUND_SLIME_IDLE_3.get();
            case 3 -> ModSounds.SOUND_SLIME_IDLE_4.get();
            case 4 -> ModSounds.SOUND_SLIME_IDLE_5.get();
            case 5 -> ModSounds.SOUND_SLIME_IDLE_6.get();
            case 6 -> ModSounds.SOUND_SLIME_IDLE_7.get();
            case 7 -> ModSounds.SOUND_SLIME_IDLE_8.get();
            default -> ModSounds.SOUND_SLIME_IDLE_9.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.getSlimeScale() < 0.6F ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getSlimeScale() < 0.6F ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return this.getSlimeScale() < 0.6F ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return this.getSlimeScale() < 0.6F ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected SoundEvent getDrinkingSound(ItemStack stack) {
        return null;
    }

    // endregion

    // region —— NBT ——

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_GENERATION, this.getGeneration());
        compound.putFloat("SlimeExpScale", this.getSlimeScale());
        compound.putInt(TAG_LINK_TTL, this.linkTtl);
        compound.putBoolean(TAG_ROOT_IS_COMMANDER, this.isRootCommanderTree());
        UUID root = this.getRootUuid();
        if (root != null) {
            compound.putUUID(TAG_ROOT, root);
        }
        UUID parent = this.getParentUuid();
        if (parent != null) {
            compound.putUUID(TAG_PARENT, parent);
        }
        if (this.isTreeRoot() && !this.treeMemberIds.isEmpty()) {
            ListTag members = new ListTag();
            for (UUID id : this.treeMemberIds) {
                members.add(NbtUtils.createUUID(id));
            }
            compound.put(TAG_TREE_MEMBERS, members);
        }
        if (this.preferredMainGunDataId != null) {
            compound.putString(TAG_PREFERRED_MAIN_GUN_DATA_ID, this.preferredMainGunDataId.toString());
        }
        SoldierBackpack.save(compound, this.soldierBackpack);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setLeftHanded(false);
        if (compound.contains(TAG_PREFERRED_MAIN_GUN_DATA_ID, Tag.TAG_STRING)) {
            this.preferredMainGunDataId =
                    ResourceLocation.tryParse(compound.getString(TAG_PREFERRED_MAIN_GUN_DATA_ID));
        }
        SoldierBackpack.load(compound, this.soldierBackpack);
        int gen = compound.contains(TAG_GENERATION, Tag.TAG_INT) ? compound.getInt(TAG_GENERATION) : 0;
        this.applyGenerationStats(gen, false, false);
        if (compound.contains("SlimeExpScale")) {
            this.setSlimeScale(compound.getFloat("SlimeExpScale"));
        }
        if (compound.contains(TAG_LINK_TTL, Tag.TAG_INT)) {
            this.linkTtl = Math.max(0, compound.getInt(TAG_LINK_TTL));
        }
        if (compound.contains(TAG_ROOT_IS_COMMANDER, Tag.TAG_BYTE)) {
            this.setRootIsCommander(compound.getBoolean(TAG_ROOT_IS_COMMANDER));
        }
        if (compound.hasUUID(TAG_ROOT)) {
            this.setRootUuid(compound.getUUID(TAG_ROOT));
        } else {
            this.setRootUuid(this.getUUID());
        }
        if (compound.hasUUID(TAG_PARENT)) {
            this.setParentUuid(compound.getUUID(TAG_PARENT));
        }
        this.treeMemberIds.clear();
        if (compound.contains(TAG_TREE_MEMBERS, Tag.TAG_LIST)) {
            ListTag members = compound.getList(TAG_TREE_MEMBERS, Tag.TAG_INT_ARRAY);
            for (int i = 0; i < members.size(); i++) {
                this.treeMemberIds.add(NbtUtils.loadUUID(members.get(i)));
            }
        }
        if (this.isCommanderEntity()) {
            this.setRootIsCommander(true);
        }
        this.applyNoDropChances();
    }

    // endregion

    /**
     * 一代规格：血量、缩放；{@code canSplit}/分裂数量表示<strong>本代致死时</strong>能否二命、刷几只
     * （读当前档，不是下一级）。与枪池/走位无关。
     */
    public static final class SplitSpec {
        public final float maxHealth;
        public final float scale;
        public final int splitCountMin;
        public final int splitCountMax;
        public final boolean canSplit;

        public SplitSpec(
                float maxHealth,
                float scale,
                int splitCountMin,
                int splitCountMax,
                boolean canSplit) {
            this.maxHealth = maxHealth;
            this.scale = scale;
            this.splitCountMin = Math.max(0, splitCountMin);
            this.splitCountMax = Math.max(this.splitCountMin, splitCountMax);
            this.canSplit = canSplit;
        }

        public int rollSplitCount(RandomSource random) {
            if (!this.canSplit || this.splitCountMax <= 0) {
                return 0;
            }
            if (this.splitCountMax <= this.splitCountMin) {
                return this.splitCountMin;
            }
            return Mth.randomBetweenInclusive(random, this.splitCountMin, this.splitCountMax);
        }
    }
}
