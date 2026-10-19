package com.kkdfg.Tacz_morelevel.entity.custom;

import com.google.common.collect.ImmutableList;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.JumpActingMob;
import com.kkdfg.Tacz_morelevel.api.GunEquippedMob;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob;
import com.kkdfg.Tacz_morelevel.api.MilitiaAcquireTargetVoice;
import com.kkdfg.Tacz_morelevel.api.SpawnArmorConfigurableMob;
import com.kkdfg.Tacz_morelevel.api.TaczCombatCrawlMob;
import com.kkdfg.Tacz_morelevel.api.TaczGunSoundInvestigatingMob;
import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.kkdfg.Tacz_morelevel.entity.ConfiguredLivingAttributes;
import com.kkdfg.Tacz_morelevel.entity.SoldierBackpack;
import com.kkdfg.Tacz_morelevel.entity.SoldierTaczCrawlState;
import com.kkdfg.Tacz_morelevel.entity.SoldierWeaponArrangeHost;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunCombatMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunIdleMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSightHolder;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunNearestAttackablePassthroughTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierMoveControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeBodyRotationControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeLookControl;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBox;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxHolder;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxReloadAccess;
import com.kkdfg.Tacz_morelevel.integration.tacz.SoldierTaczCrawlHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunSoldierRangedGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidGrenadeAndFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierUnarmedMeleeGoal;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczOrdinaryGunCatalog;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierGroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 近似玩家的 TaCZ 持枪敌对士兵基类；主手枪械由 {@link #troopGunPoolId()} /
 * {@link com.kkdfg.Tacz_morelevel.integration.tacz.TaczOrdinaryGunCatalog} 决定，
 * 护甲与语音默认与民兵相同，子类可覆盖行为目标。
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>枪械 AI</b>：实现 {@link RangedAttackMob} 与 {@link GunEquippedMob}，通过 {@link MobGunRangedPassthroughAttackGoal}
 *       等远程 goal（与原版弓 AI 类似的走位/开火节奏）。</li>
 *   <li><b>枪声感知</b>：通过 {@link MobInvestigateGunSoundGoal} 在 {@link #GUN_SOUND_HEAR_RANGE_BLOCKS} 格内响应
 *       TaCZ 枪声并前往声源调查。</li>
 *   <li><b>皮肤</b>：{@link #SOLDIER_SKIN_COUNT} 套随机皮肤，索引通过 {@link EntityDataAccessor} 同步，NBT 持久化；
 *       纹理路径 {@code textures/entity/militia/skin_0..9.png}。</li>
 *   <li><b>生成装备</b>：低阶 TaCZ 枪随机持主手；盔甲见 {@link SpawnArmorConfigurableMob#applySpawnArmorFromJson}（{@link #spawnArmorPoolId()} 对应 {@code data/tacz_morelevel/troop_armor/*.json} 权重池）。</li>
 *   <li><b>武器切换</b>：{@link #gunGoal} 与近战经 {@link #mountPermanentCombatGoals} 常驻挂载（priority 2）；
 *       有枪走远程、无枪走近战，靠 {@code canUse} 互斥，禁止运行时动态挂卸；
 *       {@link #reassessWeaponGoal} 仅刷新点射间隔（须在子类字段初始化后调用，勿在父类 {@code registerGoals} 里挂枪）。
 *       优先级 1 为 {@link SoldierAvoidGrenadeAndFireGoal}。</li>
 *   <li><b>子弹</b>：背包直读枪由 {@link TaczGunMobHelper#tickInfiniteMobAmmo} 每 tick 补满弹匣，并强制自动开火模式。</li>
 *   <li><b>碰撞/视角</b>：尺寸、眼高、下马姿态均与 {@link Player} 一致，便于潜行贴墙和受击判定。</li>
 *   <li><b>声音</b>：闲置与首次锁定敌对目标时为 {@link ModSounds} 随机语音；受伤/死亡使用玩家音效；
 *       索敌反应见 {@link MilitiaAcquireTargetVoice}、{@link com.kkdfg.Tacz_morelevel.event.MilitiaAcquireTargetVoiceEvents}。</li>
 *   <li><b>装备掉落</b>：装备槽统一使用 {@link Config#equipmentDropChance}（默认 4%，见 common 配置 {@code equipmentDropChance}）。</li>
 *   <li><b>额外掉落</b>：升级凭证与黑市货币见 {@code loot_tables/entities/<entity_id>.json}。</li>
 *   <li><b>移速与追踪</b>：各子类 {@code createAttributes} 使用 {@link Config#npcAttributesFor(SoldierSpawnArmorProfile)}
 *      （common 配置 {@code [npcAttributes]}）。</li>
 * </ul>
 *
// * @see PlayerLikeHostileRenderer
 * @see MobGunRangedPassthroughAttackGoal
 */
public abstract class AbstractPlayerLikeSoldierEntity extends Monster
        implements RangedAttackMob, GunEquippedMob, MilitiaAcquireTargetVoice, PlayerLikeHeadBodyYawMob,
                SpawnArmorConfigurableMob, TaczCombatCrawlMob, TaczGunSoundInvestigatingMob, TaczPlayerLikeMob,
                JumpActingMob, SoldierWeaponArrangeHost, GunCombatSightHolder, MobCreativeAmmoBoxHolder {

    /** NBT 键：持久化的皮肤索引。 */
    private static final String TAG_SKIN_INDEX = "TaczMoreLevelSkinIdx";
    /** NBT 键：偏好主武器标记（非槽位；枪本体只靠原版装备槽）。 */
    private static final String TAG_PREFERRED_MAIN_GUN_DATA_ID = "TaczMoreLevelPreferredMainGunDataId";

    /** 可用皮肤数量，与 {@code assets/tacz_morelevel/textures/entity/militia/skin_*.png} 数量一致（各兵种暂不区分贴图）。 */
    public static final int SOLDIER_SKIN_COUNT = 10;

    /** 同步数据键：当前皮肤索引（0 .. {@value #SOLDIER_SKIN_COUNT}-1）。 */
    private static final EntityDataAccessor<Integer> DATA_SKIN_INDEX =
            SynchedEntityData.defineId(AbstractPlayerLikeSoldierEntity.class, EntityDataSerializers.INT);

    /** 同步数据键：TaCZ 趴下（服务端 {@link SoldierTaczCrawlHelper} 写入；客户端只读）。 */
    private static final EntityDataAccessor<Boolean> DATA_TACZ_CRAWLING =
            SynchedEntityData.defineId(AbstractPlayerLikeSoldierEntity.class, EntityDataSerializers.BOOLEAN);

    /** 枪械攻击激活半径（格），内部平方后与 distanceToSqr 比较；与 {@link #gunGoal} 的射程一致。 */
    public static final float MOB_GUN_ATTACK_RADIUS_BLOCKS = 20.0F;
    /**
     * 线性距离 ≤ 该比例×{@link #MOB_GUN_ATTACK_RADIUS_BLOCKS} 时视为近战腰射（连续开火）；
     * 增大可减少「贴脸仍点射」现象。
     */
    public static final float MOB_GUN_MELEE_LINEAR_FRAC_OF_RADIUS = 0.65F;

    /** 能听到枪声并试图前往的球半径（格），略大于 {@link Attributes#FOLLOW_RANGE}。 */
    public static final double GUN_SOUND_HEAR_RANGE_BLOCKS = 40.0;
    /** 记住枪声位置并尝试寻路的持续时间（tick），约 10 秒。 */
    private static final int GUN_SOUND_MEMORY_TICKS = 200;

    /**
     * 无存活目标达到该 tick 数后才做闲置武器整理（枪套收枪 / 双枪切回偏好主等）。
     * 有目标时的整理由各自 Goal 立即触发，不走此 TTL。
     */
    public static final int NO_TARGET_WEAPON_ARRANGE_TTL = 20;

    /** 士兵额外背包格数（与 {@link SoldierBackpack#SLOTS} 一致）。 */
    public static final int SOLDIER_BACKPACK_SLOTS = SoldierBackpack.SLOTS;

    /** 子类提供的枪械走位 goal（由 {@link #mountPermanentCombatGoals} 常驻挂载）；须实现 {@link MobGunSoldierRangedGoal} 以调节点射间隔。 */
    protected abstract Goal gunGoal();

    /** 子类提供的近战 goal（经 {@link SoldierUnarmedMeleeGoal} 包装后常驻挂载）。 */
    protected abstract MeleeAttackGoal meleeGoal();

    /** 懒建的常驻射击 Goal；子类可覆盖 {@link #createGunFireGoal()}。 */
    @Nullable private Goal gunFireGoalInstance;

    /** 无枪时可走近战；枪套子类可额外要求副手也无枪。 */
    public boolean canUseUnarmedMeleeGoal() {
        return !IGun.mainHandHoldGun(this);
    }

    /** 射击/近战是否已常驻挂载（只挂一次）。 */
    private boolean combatGoalsMounted;

    /** Config / 枪械池等仍按此 profile；生成护甲 JSON 由 {@link #spawnArmorPoolId()} 指定。 */
    protected abstract SoldierSpawnArmorProfile spawnArmorWeightProfile();

    /**
     * 生成 / 读档后写入的战斗属性（血量、移速、攻击、跟随）。默认按 {@link #spawnArmorWeightProfile()}；
     * Boss / 大帅护卫等有独立配置段的子类应 override。
     */
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.npcAttributesFor(this.spawnArmorWeightProfile());
    }

    /** 将 {@link #configuredAttributeStats()} 写入属性实例（Config 已加载后现读）。 */
    protected void applyConfiguredLivingAttributes(boolean fillHealth) {
        ConfiguredLivingAttributes.apply(this, this.configuredAttributeStats(), fillHealth);
    }

    /**
     * 主手枪械池：{@code data/tacz_morelevel/troop_guns/<id>.json}。
     * 默认与 {@link #spawnArmorWeightProfile()} 小写名一致。
     */
    protected String troopGunPoolId() {
        return this.spawnArmorWeightProfile().name().toLowerCase();
    }

    /** {@code troop_armor/{poolId}.json}；各兵种子类返回自己的 pool id。 */
    @Override
    public abstract String spawnArmorPoolId();

    /** 皮肤贴图目录：{@code textures/entity/{folder}/skin_*.png}。 */
    protected abstract String skinTextureFolder();

    /**
     * 偏好主武器标记（与主手/副手槽无关；换枪不改）。默认 {@code null}，仅 Boss/固定枪装配时写入。
     * 枪的动态状态只存在装备槽，不另存字段。
     */
    @Nullable
    private ResourceLocation preferredMainGunDataId;

    /** 额外 {@link #SOLDIER_BACKPACK_SLOTS} 格；仅服务端武器整理使用，不进装备栏。 */
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

    /** 枪声调查目标位置，{@code null} 表示无调查任务或已过期；仅服务端有效。 */
    @Nullable
    private BlockPos investigateGunSoundPos;
    /** 调查任务的过期 tick 值（{@link #tickCount} + {@link #GUN_SOUND_MEMORY_TICKS}）；仅服务端有效。 */
    private int investigateGunSoundUntilTick;

    /** 下一次 {@link #getJumpPower()} 用的档；消费后归 {@link #defaultJumpTier()}。子类可细改。 */
    private int currentJumpTier;
    /** 本拍对当前目标的交火看见。默认头→身→脚。 */
    private boolean gunCombatSight;

    /**
     * 是否像原版生物一样在远离玩家时被卸载；由 {@code config/tacz_morelevel-common.toml} 中
     * {@code [npcRemoveWhenFarAway]} 按兵种（{@link #spawnArmorWeightProfile()}）分别配置 0 / 1。
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return Config.removeWhenFarAwayFor(this.spawnArmorWeightProfile());
    }

    protected AbstractPlayerLikeSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.lookControl = new SoldierPlayerLikeLookControl(this);
        this.moveControl = new SoldierMoveControl(this, this.allowsCrawlPathfinding());
        this.applyEquipmentDropChances();
        if (this.getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
            ground.setCanPassDoors(true);
        }
        this.setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, 0.0F);
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.resetJumpTierToDefault();
    }

    /** 趴寻路白名单；仅返回常量，避免构造期读未初始化字段。 */
    protected boolean allowsCrawlPathfinding() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SoldierGroundPathNavigation(this, level, this.allowsCrawlPathfinding());
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

    /** 持枪且处于攻击态（{@link #isAggressive()} 同步），Look/Body 控制与渲染按玩家逻辑处理。 */
    public boolean isGunCombatAiming() {
        return IGun.mainHandHoldGun(this) && this.isAggressive();
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

    /** 注册 {@link #DATA_SKIN_INDEX} 同步数据项，默认值 0。 */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_INDEX, 0);
        this.entityData.define(DATA_TACZ_CRAWLING, false);
    }

    /**
     * @return 当前皮肤索引（0 .. {@value #SOLDIER_SKIN_COUNT}-1），已钳位，供渲染器使用。
     */
    public int getSoldierSkinIndex() {
        return Mth.clamp(this.entityData.get(DATA_SKIN_INDEX), 0, SOLDIER_SKIN_COUNT - 1);
    }

    protected void setSoldierSkinIndex(int index) {
        this.entityData.set(DATA_SKIN_INDEX, Mth.clamp(index, 0, SOLDIER_SKIN_COUNT - 1));
    }

    /** 渲染器读取的皮肤资源路径。 */
    public ResourceLocation getSoldierSkinTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/entity/" + skinTextureFolder() + "/skin_" + getSoldierSkinIndex() + ".png");
    }

    /** 客户端：由服务端同步的 TaCZ 趴下标志。 */
    public boolean isTaczCrawlingSynced() {
        return this.entityData.get(DATA_TACZ_CRAWLING);
    }

    /** 服务端写入同步标志；请通过 {@link #setTaczCrawling(boolean)} 或 {@link SoldierTaczCrawlHelper} 调用。 */
    public void syncTaczCrawlingFlag(boolean crawling) {
        this.entityData.set(DATA_TACZ_CRAWLING, crawling);
    }

    /** 是否处于 TaCZ 趴下（双端读同步意图 {@link #DATA_TACZ_CRAWLING}）。 */
    public boolean isTaczCrawling() {
        return SoldierTaczCrawlHelper.isCrawling(this);
    }

    public SoldierTaczCrawlState getTaczCrawlState() {
        return SoldierTaczCrawlHelper.getCrawlState(this);
    }

    /**
     * 服务端：进入/退出趴下。
     *
     * @return 是否成功（进入时须服配置 {@code EnableCrawl}；不要求主手枪）
     */
    public boolean setTaczCrawling(boolean crawling) {
        return SoldierTaczCrawlHelper.applyCrawl(this, crawling);
    }

    @Override
    public boolean hasGunCombatSight() {
        return this.gunCombatSight;
    }

    /** 默认头→身→脚。子类可覆写。 */
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

    /** 将主副手及四个盔甲槽的掉落概率设为 {@link Config#equipmentDropChance}。 */
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

    /**
     * @return 默认属性模板：20HP、0.5 移速、2 近战伤害、24 格跟随距离。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    /**
     * 服务端：听见枪声（在 {@code hearRangeSq} 内）则记住声源，供 {@link MobInvestigateGunSoundGoal} 寻路。
     * <p>
     * 声源位置转为 {@link BlockPos}，过期时间为当前 {@link #tickCount} + {@link #GUN_SOUND_MEMORY_TICKS}。
     *
     * @param sourceWorldPos 开火位置（世界坐标）
     * @param hearRangeSq    听觉判定范围（距离²）
     */
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

    /**
     * @return {@code true} 若仍有未过期的枪声调查记忆。
     */
    public boolean hasPendingGunSoundInvestigation() {
        return this.investigateGunSoundPos != null
                && this.tickCount < this.investigateGunSoundUntilTick;
    }

    /** 清除枪声调查状态（位置与过期时间均重置）。 */
    public void clearGunSoundInvestigation() {
        this.investigateGunSoundPos = null;
    }

    /**
     * @return 调查目标位置（BlockPos），若已过期或未标记则返回 {@code null}。
     */
    @Nullable
    public BlockPos getGunSoundInvestigatePos() {
        if (this.investigateGunSoundPos == null
                || this.tickCount >= this.investigateGunSoundUntilTick) {
            return null;
        }
        return this.investigateGunSoundPos;
    }

    /**
     * 将 {@code soundOrigin} 球半径范围内的所有本类士兵引向该点（常用于 TaCZ 开火事件）；
     * {@code shooter} 自身不计入。
     * <p>
     * 范围 = {@link #GUN_SOUND_HEAR_RANGE_BLOCKS}。
     *
     * @param level       所在世界
     * @param soundOrigin 开火位置（世界坐标）
     * @param shooter     开火者（可为 {@code null}），自身不计入
     */
    public static void alertSoldiersInRangeToGunshot(
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
        for (AbstractPlayerLikeSoldierEntity mob :
                level.getEntitiesOfClass(AbstractPlayerLikeSoldierEntity.class, box, e -> e != shooter)) {
            mob.notifyGunFireHeard(soundOrigin, rsq);
        }
    }

    /**
     * 每 tick 服务端 AI 步进：
     * <ol>
     *   <li>背包直读枪由 {@link TaczGunMobHelper#tickInfiniteMobAmmo} 补满弹匣。</li>
     *   <li>若主手持枪，规范化并锁定为自动开火模式。</li>
     * </ol>
     */
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

    /**
     * 生成时初始化：
     * <ol>
     *   <li>随机选择皮肤索引（范围 0 .. {@value #SOLDIER_SKIN_COUNT}-1）。</li>
     *   <li>若 {@link #shouldEquipRandomTroopGun()}，按 {@link #troopGunPoolId()} 随机装备。</li>
     *   <li>调用 {@link #applyMilitiaSpawnArmor} 处理盔甲生成。</li>
     * </ol>
     * 以上均在服务端执行。
     *
     * @return 生成数据，可链式传递
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
        if (!level.isClientSide()) {
            this.applyConfiguredLivingAttributes(true);
            RandomSource rnd = level.getLevel().getRandom();
            this.entityData.set(DATA_SKIN_INDEX, rnd.nextInt(SOLDIER_SKIN_COUNT));
            if (this.shouldEquipRandomTroopGun()) {
                ResourceLocation gid =
                        TaczOrdinaryGunCatalog.pickRandomForPoolId(rnd, this.troopGunPoolId());
                this.equipTaCZGunMainHand(gid);
            }
            this.applyMilitiaSpawnArmor(rnd);
        }
        return spawnData;
    }

    /**
     * 是否在生成时从兵种枪池随机主手。已有偏好主、或主手已持枪时默认 false。
     * 双枪等自行装配的子类可恒返回 false。
     */
    protected boolean shouldEquipRandomTroopGun() {
        return this.getPreferredMainGunDataId() == null && !IGun.mainHandHoldGun(this);
    }

    /** 生成时按 {@link #spawnArmorPoolId()} 从 JSON 权重池穿甲（见 {@link SpawnArmorConfigurableMob}）。 */
    protected void applyMilitiaSpawnArmor(RandomSource random) {
        if (appliesSpawnArmorOnFinalize()) {
            applySpawnArmorFromJson(random);
        }
    }

    /**
     * 注册索敌等；射击/近战勿在此挂载（子类字段尚未初始化）。
     * 常驻挂载见 {@link #mountPermanentCombatGoals}（由 {@link #reassessWeaponGoal} 在构造体后触发）。
     */
    protected void registerGoals() {
        this.targetSelector.addGoal(5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, WitherBoss.class, false));
        this.targetSelector.addGoal(5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Warden.class, false));
        this.targetSelector.addGoal(2, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(3, new MobGunNearestAttackablePassthroughTargetGoal<>(this, AbstractFriendEntity.class, false));
        this.goalSelector.addGoal(1, new SoldierAvoidGrenadeAndFireGoal(this));
    }

    /**
     * 子类字段就绪后调用：射击(LOOK) + 走位(MOVE) + 徒手近战各挂一次，之后禁止 remove/add。
     * 战争机器等可 override 自行挂载多把射击 Goal。
     */
    protected void mountPermanentCombatGoals() {
        if (this.combatGoalsMounted || this.level() == null || this.level().isClientSide()) {
            return;
        }
        Goal gun = this.gunGoal();
        MeleeAttackGoal melee = this.meleeGoal();
        if (gun == null || melee == null) {
            return;
        }
        this.combatGoalsMounted = true;
        Goal fire = this.gunFireGoal();
        if (fire != null) {
            this.goalSelector.addGoal(2, fire);
        }
        this.goalSelector.addGoal(2, gun);
        this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, melee));
        if (this.mountsMainhandWeaponArrangeGoals()) {
            this.goalSelector.addGoal(1, new MobGunCombatMainhandArrangeGoal(this));
            this.goalSelector.addGoal(1, new MobGunIdleMainhandArrangeGoal(this));
        }
    }

    /**
     * 是否挂主手整理 Goal。默认 true；{@link AbstractHolsterGunSoldierEntity} /
     * {@link AbstractDualGunSoldierEntity} 关掉（各自有枪套 / 双枪逻辑）。
     */
    protected boolean mountsMainhandWeaponArrangeGoals() {
        return true;
    }

    /**
     * 常驻射击 Goal；默认按本档 Config + {@link GunCombatSight#COMBAT}。
     * 对掏/战争机器等可覆盖返回 {@code null}（自行挂载）或特供实例。
     */
    @Nullable
    protected Goal gunFireGoal() {
        if (this.gunFireGoalInstance == null) {
            this.gunFireGoalInstance = this.createGunFireGoal();
        }
        return this.gunFireGoalInstance;
    }

    /**
     * 射击/走位 Goal 是否要求主手为偏好主武器。默认 false；宝宝、战争机器等打开。
     * public：供 {@code entity.ai} 包 Goal 查询。
     */
    public boolean requiresPreferredMainGunForCombat() {
        return false;
    }

    /**
     * 新建射击 Goal；默认头→身→脚 COMBAT。难民/宝宝等覆盖。
     * {@link #requiresPreferredMainGunForCombat()} 为 true 时挂 {@link #isHoldingPreferredMainGun}。
     */
    @Nullable
    protected Goal createGunFireGoal() {
        Config.NpcAttributeStats attrs = this.configuredAttributeStats();
        return new MobGunRangedFireGoal<>(
                this,
                (float) attrs.gunRadius,
                (float) attrs.gunMeleeFrac,
                attrs.gunAttackIntervalMin,
                attrs.seeTimeToShoot,
                this.gunCombatSight(),
                false,
                this.requiresPreferredMainGunForCombat()
                        ? AbstractPlayerLikeSoldierEntity::isHoldingPreferredMainGun
                        : null);
    }

    /** 默认交火视线；宝宝等可覆盖为 {@link GunCombatSight#BLAST}。 */
    protected GunCombatSight gunCombatSight() {
        return GunCombatSight.COMBAT;
    }

    /** 是否已完成常驻战斗 Goal 挂载。 */
    protected final boolean areCombatGoalsMounted() {
        return this.combatGoalsMounted;
    }

    /** 子类自行挂载后标记，避免父类再挂一份。 */
    protected final void markCombatGoalsMounted() {
        this.combatGoalsMounted = true;
    }

    /**
     * 确保战斗 Goal 常驻后，仅刷新点射间隔；禁止运行时动态挂卸。
     */
    public void reassessWeaponGoal() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.mountPermanentCombatGoals();
        int interval = this.mobGunBurstIntervalTicks();
        Goal gun = this.gunGoal();
        if (gun instanceof MobGunSoldierRangedGoal ranged) {
            ranged.setMinAttackInterval(interval);
        }
        Goal fire = this.gunFireGoal();
        if (fire instanceof MobGunSoldierRangedGoal fireRanged) {
            fireRanged.setMinAttackInterval(interval);
        }
    }

    /**
     * 持枪时 {@link #reassessWeaponGoal} 写入远程 goal 的点射间隔（tick）；
     * 读本档 {@code gunAttackIntervalMin}，再经 NPC 全局 a/b 映射。
     */
    protected int mobGunBurstIntervalTicks() {
        return Config.mapNpcGunBurstInterval(
                Config.gunAttackIntervalMinFor(this.spawnArmorWeightProfile()));
    }

    /**
     * 服务端装备 TaCZ 枪械至主手（写入装备槽，与盔甲相同）。
     * 最后调用 {@link #reassessWeaponGoal} 刷新点射间隔。
     *
     * @param gunDataId TaCZ 枪械数据 ID
     */
    @Override
    public void equipTaCZGunMainHand(ResourceLocation gunDataId) {
        if (this.level().isClientSide()) {
            return;
        }
        TaczGunMobHelper.equipMainHand(this, gunDataId);
        this.reassessWeaponGoal();
    }

    /** 偏好主武器标记；换槽不改。 */
    public void setPreferredMainGunDataId(@Nullable ResourceLocation gunDataId) {
        this.preferredMainGunDataId = gunDataId;
    }

    @Nullable
    public ResourceLocation getPreferredMainGunDataId() {
        return this.preferredMainGunDataId;
    }

    /**
     * 主手是否为偏好主武器。未设置偏好时视为 true（不强制纠偏）。
     */
    public boolean isHoldingPreferredMainGun() {
        if (this.preferredMainGunDataId == null) {
            return true;
        }
        IGun gun = IGun.getIGunOrNull(this.getMainHandItem());
        return gun != null && this.preferredMainGunDataId.equals(gun.getGunId(this.getMainHandItem()));
    }

    /**
     * 放入士兵背包空格；满则 false（丢弃后续再做）。调用方自行清空来源槽。
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

    /** 背包是否含指定枪（{@code null} 则任意枪）。 */
    public boolean hasGunInSoldierBackpack(@Nullable ResourceLocation gunIdOrNull) {
        return SoldierBackpack.containsGun(this.soldierBackpack, gunIdOrNull);
    }

    /** 背包格子（整理 / 偏好逻辑读写）。 */
    @Override
    public NonNullList<ItemStack> soldierBackpackSlots() {
        return this.soldierBackpack;
    }

    /** 手槽与背包第 {@code index} 格交换。 */
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
    public Mob asArrangeLiving() {
        return this;
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
            this.reassessWeaponGoal();
        } else if (hadMainGunBefore) {
            this.reassessWeaponGoal();
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
        this.reassessWeaponGoal();
    }

    @Override
    public int noTargetWeaponArrangeTtl() {
        return NO_TARGET_WEAPON_ARRANGE_TTL;
    }

    /**
     * 叠堆：背包内合并，再把主/副手可叠物品并入背包已有堆（不新占空格）；双手之间同种也并。
     */
    @Override
    public void compactSoldierInventory() {
        if (this.level().isClientSide()) {
            return;
        }
        SoldierBackpack.compact(this.soldierBackpack);
        this.mergeHandIntoBackpack(EquipmentSlot.MAINHAND);
        this.mergeHandIntoBackpack(EquipmentSlot.OFFHAND);
        this.mergeHandsTogether();
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

    /** 主手 → 副手（同种可叠则并）。 */
    private void mergeHandsTogether() {
        ItemStack main = this.getMainHandItem();
        ItemStack off = this.getOffhandItem();
        if (main.isEmpty() || off.isEmpty() || !main.isStackable() || !ItemStack.isSameItemSameTags(main, off)) {
            return;
        }
        int move = Math.min(main.getCount(), off.getMaxStackSize() - off.getCount());
        if (move <= 0) {
            return;
        }
        off.grow(move);
        main.shrink(move);
        if (main.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    /**
     * 执行一次远程攻击（由 {@link MobGunRangedPassthroughAttackGoal} 或难民穿透版 goal 驱动）。
     * <p>
     * 近战腰射；中远距由枪械 Goal 维持 {@code aim(true)}，此处不强制关闭瞄准姿态。
     * 同时清除疾跑状态，防止 TaCZ 因 {@code sprintTimeS > 0} 返回 {@link ShootResult#IS_SPRINTING}
     * 而导致无法连发；若仍命中该结果则重试一次。
     * <p>
     * 根据射击结果处理：未拔枪/拉栓中 → 拔枪；需拉栓 → 上膛；无弹药 → 换弹。
     *
     * @param target 攻击目标
     * @param power  攻击力度（未使用）
     */
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

    /**
     * 设置装备槽物品；若为主手且在服务端，调用 {@link #reassessWeaponGoal} 刷新点射间隔。
     *
     * @param slot  装备槽
     * @param stack 物品栈
     */
    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        Level level = this.level();
        if (level != null && !level.isClientSide() && slot == EquipmentSlot.MAINHAND) {
            this.reassessWeaponGoal();
        }
    }

    /**
     * 持久化：皮肤索引 + 偏好主标记 + 背包。枪本体由原版装备槽保存。
     *
     * @param compound 写入目标
     */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_SKIN_INDEX, this.getSoldierSkinIndex());
        if (this.preferredMainGunDataId != null) {
            compound.putString(TAG_PREFERRED_MAIN_GUN_DATA_ID, this.preferredMainGunDataId.toString());
        }
        SoldierBackpack.save(compound, this.soldierBackpack);
    }

    /**
     * 读取：皮肤、偏好主、背包；枪已由 {@code super} 从装备槽还原，此处不补枪。
     *
     * @param compound 读取来源
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setLeftHanded(false);
        if (compound.contains(TAG_SKIN_INDEX, Tag.TAG_INT)) {
            int idx = Mth.clamp(compound.getInt(TAG_SKIN_INDEX), 0, SOLDIER_SKIN_COUNT - 1);
            this.entityData.set(DATA_SKIN_INDEX, idx);
        }
        if (compound.contains(TAG_PREFERRED_MAIN_GUN_DATA_ID, Tag.TAG_STRING)) {
            this.preferredMainGunDataId =
                    ResourceLocation.tryParse(compound.getString(TAG_PREFERRED_MAIN_GUN_DATA_ID));
        }
        SoldierBackpack.load(compound, this.soldierBackpack);
        Level level = this.level();
        if (level != null && !level.isClientSide()) {
            if (IGun.mainHandHoldGun(this) || IGun.getIGunOrNull(this.getOffhandItem()) != null) {
                this.reassessWeaponGoal();
            }
            this.applyConfiguredLivingAttributes(false);
        }
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

    /**
     * 与 {@link Player} 的 {@code POSES} 一致：站立 / 蹲下碰撞箱与原版玩家相同，
     * 便于潜行时贴方块与受击判定一致。
     * <p>
     * 睡眠姿态回退到父类默认。
     *
     * @param pose 当前姿态
     * @return 对应姿态的碰撞尺寸
     */
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

    /**
     * 与 {@link Player#getStandingEyeHeight(Pose, EntityDimensions)} 数值一致，确保视角与碰撞对齐。
     *
     * @param pose       当前姿态
     * @param dimensions 当前碰撞尺寸
     * @return 站立眼高
     */
    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return switch (pose) {
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> 0.4F;
            case CROUCHING -> 1.27F;
            case STANDING -> 1.62F;
            default -> super.getStandingEyeHeight(pose, dimensions);
        };
    }

    /**
     * @return 允许的下马姿态：站立、蹲下、游泳。
     */
    @Override
    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
    }

    /**
     * {@link MilitiaAcquireTargetVoice}：通过 {@link Mob#setTarget} 得到新的非空敌对目标时（服务端）
     * 播放 {@link ModSounds#SOUND_FIRE}。
     *
     * @see com.kkdfg.Tacz_morelevel.event.MilitiaAcquireTargetVoiceEvents
     */
    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        self.playSound(ModSounds.SOUND_FIRE.get(), 1.0F, 1);
    }

    /**
     * @return 闲置随机语音（{@link ModSounds#SOUND_HENHEN} / {@link ModSounds#SOUND_XIANGSHOU}。
     */
    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if(!hasTarget){
            int WhichChoose = random.nextInt(20);
            return switch (WhichChoose) {
                case 0 -> ModSounds.SOUND_SOLDIER_5.get();
                case 1 -> ModSounds.SOUND_XIANGSHOU.get();
                case 2 -> ModSounds.SOUND_SOLDIER_1.get();
                case 3 -> ModSounds.SOUND_SOLDIER_4.get();
                case 4 -> ModSounds.SOUND_SOLDIER_3.get();
                case 5 -> ModSounds.SOUND_SOLDIER_6.get();
                case 6 -> ModSounds.SOUND_HENHEN.get();
                case 7 -> ModSounds.SOUND_XIANGSHOU.get();
                case 8 -> ModSounds.SOUND_XIANGSHOU.get();
                case 9 -> ModSounds.SOUND_XIANGSHOU.get();
                case 10 -> ModSounds.SOUND_XIANGSHOU.get();
                default -> ModSounds.SOUND_HENHEN.get();
            };
        }
        int WhichChoose = random.nextInt(10);
        return switch (WhichChoose) {
            case 0 -> ModSounds.SOUND_SOLDIER_FIGHT_2.get();
            case 1 -> ModSounds.SOUND_SOLDIER_FIGHT1_1.get();
            default ->  ModSounds.SOUND_HENHEN.get();
        };
    }

    /**
     * @return 受伤音效（与玩家一致）。
     */
    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        int WhichChoose = random.nextInt(2);
        return switch (WhichChoose) {
            case 0 -> ModSounds.SOUND_SOLDIER_HURT_2.get();
            case 1 -> ModSounds.SOUND_SOLDIER_HURT_1.get();
            default -> ModSounds.SOUND_SOLDIER_HURT_2.get();
        };
    }

    /**
     * @return 死亡音效（与玩家一致）。
     */
    @Override
    protected SoundEvent getDeathSound() {
        int WhichChoose = random.nextInt(4);
        return switch (WhichChoose) {
            case 0 -> ModSounds.SOUND_SOLDIER_DIED_1.get();
            case 1 -> ModSounds.SOUND_SOLDIER_DIED_2.get();
            case 2 -> ModSounds.SOUND_SOLDIER_DIED_5.get();
            case 3 -> ModSounds.SOUND_SOLDIER_DIED_4.get();
            default -> ModSounds.SOUND_SOLDIER_DIED_1.get();
        };
    }

}
