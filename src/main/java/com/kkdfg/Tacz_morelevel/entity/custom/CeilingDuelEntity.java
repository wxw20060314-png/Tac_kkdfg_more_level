package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunNearestAttackablePassthroughTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.base.CeilingDuelBaseManeuverGoals;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.base.classic.CeilingDuelClassicGunStrafeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverDirectorGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverTiming;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelGunFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id201.CeilingDuelCatStepJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id202.CeilingDuelInstantCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301.CeilingDuelLateralQuickCrouchGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301.CeilingDuelLateralYankGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id302.CeilingDuelLateralQuickCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id302.CeilingDuelLateralYankCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id303.CeilingDuelLateralQuickCrouchHopGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id303.CeilingDuelLateralYankHopGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id304.CeilingDuelStaggerStepGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id305.CeilingDuelSlideStepGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.google.common.collect.ImmutableMap;
import com.tacz.guns.api.item.attachment.AttachmentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 对掏天花板 Boss：M4 对掏天花板；枪套同大帅（无目标收枪）。
 * 射击常驻 {@link CeilingDuelGunFireGoal}（LOOK）；走位 Goal 仅 MOVE，随阶段切换。
 * 走位由导演驱动 {@link CeilingDuelManeuverPhase}（BASE↔SKILL↔BRIDGE）。
 * <p>BASE→技能：空/准入失败不转；技能失败可用 {@link #failToBase()} 直回基础。
 * <p>死亡装备掉落见 common 配置 {@code [ceilingDuelDeathDrops]}：
 * 头盔默认 100%（{@code helmetDropChance}），其余同其它 Boss 默认 20%（{@code equipmentDropChance}）。
 */
public final class CeilingDuelEntity extends AbstractHolsterGunSoldierEntity {

    public static final ResourceLocation MAIN_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "m4a1_ceiling");

    private static ResourceLocation taczAttachment(String path) {
        return ResourceLocation.fromNamespaceAndPath("tacz", path);
    }

    /** 固定配件：红点、三级扩容、握把、枪托、补偿器、激光（与原 HK416 套装相同）。 */
    private static final Map<AttachmentType, ResourceLocation> MAIN_GUN_ATTACHMENTS =
            ImmutableMap.<AttachmentType, ResourceLocation>builder()
                    .put(AttachmentType.SCOPE, taczAttachment("sight_coyote"))
                    .put(AttachmentType.EXTENDED_MAG, taczAttachment("extended_mag_3"))
                    .put(AttachmentType.GRIP, taczAttachment("grip_vertical_ranger"))
                    .put(AttachmentType.STOCK, taczAttachment("stock_sba3"))
                    .put(AttachmentType.MUZZLE, taczAttachment("muzzle_compensator_trident"))
                    .put(AttachmentType.LASER, taczAttachment("laser_compact"))
                    .build();

    /** 近战带线性距离（格）= gunRadius × gunMeleeFrac。 */
    private static float meleeRange() {
        return (float)
                (Config.ceilingDuelAttributes.gunRadius * Config.ceilingDuelAttributes.gunMeleeFrac);
    }

    /**
     * 枪走位侧移轴系数（传给 {@code MoveControl.strafe} 的前后/左右绝对值）。
     * 基础 100、技能 301、{@link #applyStrafeFromMoveDir()} 共用。
     */
    public static final float GUN_STRAFE_AXIS = 2.0F;

    private static final String TAG_PHASE = "CeilingDuelPhase";
    private static final String TAG_SKILL = "CeilingDuelSkill";
    private static final String TAG_PHASE_ELAPSED = "CeilingDuelPhaseElapsed";
    private static final String TAG_PHASE_MAX = "CeilingDuelPhaseMax";

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.ceiling_duel"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private CeilingDuelManeuverPhase maneuverPhase = CeilingDuelManeuverPhase.BASE;
    private int maneuverSkillId = CeilingDuelManeuverSkills.BASE;
    private int phaseElapsed;
    private int phaseDurationMax = 20;

    /** 相对朝向：{@code [0]} 前后 -1/0/1，{@code [1]} 左右 -1/0/1。 */
    private final int[] moveDir = new int[2];
    private int fwdChangeAge;
    private int strafeChangeAge;

    private boolean maneuverSneaking;
    private int sneakTicks;
    private boolean maneuverCrawling;
    private int crawlTicks;

    private final CeilingDuelClassicGunStrafeGoal gunGoal =
            new CeilingDuelClassicGunStrafeGoal(
                    this,
                    Config.ceilingDuelGunGoalSpeedModifier,
                    Config.ceilingDuelAttributes.gunAttackIntervalMin,
                    (float) Config.ceilingDuelAttributes.gunRadius,
                    (float) Config.ceilingDuelAttributes.gunMeleeFrac,
                    Config.ceilingDuelAttributes.seeTimeToStopMove,
                    (float) Config.ceilingDuelAttributes.gunStrafeOuterRingFrac,
                    (float) Config.ceilingDuelAttributes.gunStrafeInnerRingFrac,
                    Config.ceilingDuelAttributes.seeTimeToShoot);

    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.ceilingDuelAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    CeilingDuelEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    CeilingDuelEntity.this.setAggressive(true);
                }
            };

    public CeilingDuelEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.reassessWeaponGoal();
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
    }

    // ——— 阶段 / 编号 ———

    public CeilingDuelManeuverPhase getManeuverPhase() {
        return this.maneuverPhase;
    }

    public int getManeuverSkillId() {
        return this.maneuverSkillId;
    }

    public void ensureBaseSkillId() {
        this.maneuverPhase = CeilingDuelManeuverPhase.BASE;
        this.maneuverSkillId = CeilingDuelManeuverSkills.BASE;
    }

    public int getPhaseElapsed() {
        return this.phaseElapsed;
    }

    public int getPhaseDurationMax() {
        return this.phaseDurationMax;
    }

    public boolean isPhaseTimedOut() {
        return this.phaseDurationMax > 0 && this.phaseElapsed >= this.phaseDurationMax;
    }

    // ——— 监视字段 ———

    public int getMoveDirForward() {
        return this.moveDir[0];
    }

    public int getMoveDirStrafe() {
        return this.moveDir[1];
    }

    public int getFwdChangeAge() {
        return this.fwdChangeAge;
    }

    public int getStrafeChangeAge() {
        return this.strafeChangeAge;
    }

    public boolean isManeuverSneaking() {
        return this.maneuverSneaking;
    }

    public int getSneakTicks() {
        return this.sneakTicks;
    }

    public boolean isManeuverCrawling() {
        return this.maneuverCrawling;
    }

    public int getCrawlTicks() {
        return this.crawlTicks;
    }

    /** 设置走位方向；轴变化时清对应变向 age。 */
    public void setMoveDir(int forward, int strafe) {
        int f = Mth.clamp(forward, -1, 1);
        int s = Mth.clamp(strafe, -1, 1);
        if (this.moveDir[0] != f) {
            this.moveDir[0] = f;
            this.fwdChangeAge = 0;
        }
        if (this.moveDir[1] != s) {
            this.moveDir[1] = s;
            this.strafeChangeAge = 0;
        }
    }

    public void setManeuverSneaking(boolean sneaking) {
        if (this.maneuverSneaking != sneaking) {
            this.maneuverSneaking = sneaking;
            this.sneakTicks = 0;
        }
    }

    public void setManeuverCrawling(boolean crawling) {
        if (this.maneuverCrawling != crawling) {
            this.maneuverCrawling = crawling;
            this.crawlTicks = 0;
        }
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_HIGH;
    }

    public void applyStrafeFromMoveDir() {
        float a = GUN_STRAFE_AXIS;
        this.getMoveControl().strafe(this.moveDir[0] * a, this.moveDir[1] * a);
    }

    public void tickManeuverTelemetry() {
        int cap = CeilingDuelManeuverSkills.AGE_CAP;
        this.phaseElapsed = Math.min(this.phaseElapsed + 1, cap);
        this.fwdChangeAge = Math.min(this.fwdChangeAge + 1, cap);
        this.strafeChangeAge = Math.min(this.strafeChangeAge + 1, cap);
        this.sneakTicks = Math.min(this.sneakTicks + 1, cap);
        this.crawlTicks = Math.min(this.crawlTicks + 1, cap);
    }

    public void resetManeuverTelemetry() {
        this.moveDir[0] = 0;
        this.moveDir[1] = 0;
        this.fwdChangeAge = 0;
        this.strafeChangeAge = 0;
        this.maneuverSneaking = false;
        this.sneakTicks = 0;
        this.maneuverCrawling = false;
        this.crawlTicks = 0;
        this.phaseElapsed = 0;
    }

    // ——— 转换通道 ———

    /**
     * BASE→技能。空技能 {@link CeilingDuelManeuverSkills#EMPTY_SKILL} / 未注册 / 准入失败 / 非 BASE → 不转，返回 false。
     */
    public boolean tryEnterSkill(int skillId) {
        if (this.maneuverPhase != CeilingDuelManeuverPhase.BASE) {
            return false;
        }
        if (CeilingDuelManeuverSkills.isEmptySkill(skillId)) {
            return false;
        }
        if (!CeilingDuelManeuverSkills.isSkillFamily(skillId)) {
            return false;
        }
        if (!CeilingDuelManeuverSkills.isRegisteredSkill(skillId)) {
            return false;
        }
        if (!CeilingDuelManeuverSkills.canEnterSkill(skillId, this)) {
            return false;
        }
        this.maneuverPhase = CeilingDuelManeuverPhase.SKILL;
        this.maneuverSkillId = skillId;
        this.phaseElapsed = 0;
        this.phaseDurationMax =
                CeilingDuelManeuverTiming.rollDurationTicks(skillId, this.random, false);
        return true;
    }

    /** 技能成功结束→衔接。非 SKILL 时返回 false。池空/准入失败则空衔接 200。 */
    public boolean enterBridge() {
        if (this.maneuverPhase != CeilingDuelManeuverPhase.SKILL) {
            return false;
        }
        int bridgeId = this.pickBridgeIdOrEmpty();
        this.maneuverPhase = CeilingDuelManeuverPhase.BRIDGE;
        this.maneuverSkillId = bridgeId;
        this.phaseElapsed = 0;
        this.phaseDurationMax =
                CeilingDuelManeuverTiming.rollDurationTicks(bridgeId, this.random, false);
        return true;
    }

    /**
     * 从已注册且可准入的实体衔接（201+）中等权随机抽取；池空则 {@link CeilingDuelManeuverSkills#BRIDGE}。
     */
    public int pickBridgeIdOrEmpty() {
        List<Integer> ok = new ArrayList<>();
        for (int id : CeilingDuelManeuverSkills.registeredBridgeIds()) {
            if (CeilingDuelManeuverSkills.canEnterBridge(id, this)) {
                ok.add(id);
            }
        }
        if (ok.isEmpty()) {
            return CeilingDuelManeuverSkills.BRIDGE;
        }
        return ok.get(this.random.nextInt(ok.size()));
    }

    /** 衔接结束→基础并 init 监视。 */
    public void enterBaseFromBridge() {
        this.enterBaseFresh();
    }

    /**
     * 特殊通道：技能失败 / 距离不够等直接回基础（跳过衔接），并 init。
     */
    public void failToBase() {
        this.enterBaseFresh();
    }

    /** 丢目标：回基础并全量复位。 */
    public void onTargetLost() {
        this.enterBaseFresh();
    }

    public void enterBaseFresh() {
        this.maneuverPhase = CeilingDuelManeuverPhase.BASE;
        this.maneuverSkillId = CeilingDuelManeuverSkills.BASE;
        this.resetManeuverTelemetry();
        this.phaseDurationMax =
                CeilingDuelManeuverTiming.rollDurationTicks(
                        CeilingDuelManeuverSkills.BASE, this.random, false);
    }

    public void refreshBasePhaseDuration() {
        this.phaseElapsed = 0;
        this.phaseDurationMax =
                CeilingDuelManeuverTiming.rollDurationTicks(
                        CeilingDuelManeuverSkills.BASE, this.random, false);
    }

    /** 当前阶段时长上限（衔接秒蹲 Goal 会写入站+蹲预算）。 */
    public void setPhaseDurationMax(int ticks) {
        this.phaseDurationMax = Mth.clamp(ticks, 1, CeilingDuelManeuverSkills.AGE_CAP);
    }

    /**
     * 从已注册且可准入的实体技能（301+）中按权重抽取；池空返回 {@code -1}。
     * 权重见 {@link CeilingDuelManeuverSkills#skillPickWeight(int, CeilingDuelEntity)}。
     */
    public int pickSkillIdOrEmpty() {
        List<Integer> ok = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int total = 0;
        for (int id : CeilingDuelManeuverSkills.registeredSkillIds()) {
            if (!CeilingDuelManeuverSkills.canEnterSkill(id, this)) {
                continue;
            }
            int w = CeilingDuelManeuverSkills.skillPickWeight(id, this);
            if (w < 1) {
                continue;
            }
            ok.add(id);
            weights.add(w);
            total += w;
        }
        if (ok.isEmpty() || total < 1) {
            return -1;
        }
        int roll = this.random.nextInt(total);
        for (int i = 0; i < ok.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return ok.get(i);
            }
        }
        return ok.get(ok.size() - 1);
    }

    public double distanceToTarget() {
        var t = this.getTarget();
        return t == null ? Double.POSITIVE_INFINITY : this.distanceTo(t);
    }

    /** 近战带（枪半径 × 近战比例）；行为用，不等同技能准入。 */
    public boolean isTargetInMeleeRange() {
        return this.distanceToTarget() <= meleeRange();
    }

    /** 304/305 准入：距目标 ≥ 16 格。 */
    public boolean isTargetInMidFarPaceRange() {
        return this.distanceToTarget() >= 16.0D;
    }

    // ——— 原有实体逻辑 ———

    @Override
    protected ResourceLocation pickHolsterGunId(RandomSource random) {
        return MAIN_GUN;
    }

    @Override
    @Nullable
    protected Map<AttachmentType, ResourceLocation> pickHolsterGunAttachments() {
        return MAIN_GUN_ATTACHMENTS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tacz_morelevel.ceiling_duel");
    }

    @Override
    public boolean appliesSpawnArmorOnFinalize() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return false;
    }

    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.SUPER_SPECIAL;
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.ceilingDuelAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "ceiling_duel";
    }

    @Override
    protected String skinTextureFolder() {
        return "assault_soldier";
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.ceilingDuelAttributes.gunAttackIntervalMin);
    }

    @Override
    protected MeleeAttackGoal meleeGoal() {
        return this.meleeGoal;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnReason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag dataTag) {
        this.setLeftHanded(false);
        spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
        if (!level.isClientSide()) {
            this.setSoldierSkinIndex(this.random.nextInt(SOLDIER_SKIN_COUNT));
            this.equipSeventhWithJokerHelmetLoadout();
            this.enterBaseFresh();
        }
        return spawnData;
    }

    /** 普通七级身甲 + 小丑七级头；全件保护 IV。 */
    private void equipSeventhWithJokerHelmetLoadout() {
        this.setItemSlot(EquipmentSlot.HEAD, enchantedPiece(ModItems.JOKER_SEVENTH_BULLETPROOF_HELMET.get()));
        this.setItemSlot(
                EquipmentSlot.CHEST, enchantedPiece(ModItems.SEVENTH_BULLETPROOF_CHESTPLATE.get()));
        this.setItemSlot(
                EquipmentSlot.LEGS, enchantedPiece(ModItems.SEVENTH_BULLETPROOF_LEGGINGS.get()));
        this.setItemSlot(
                EquipmentSlot.FEET, enchantedPiece(ModItems.SEVENTH_BULLETPROOF_BOOTS.get()));
        this.applyEquipmentDropChances();
    }

    @Override
    protected void applyEquipmentDropChances() {
        float body = Config.ceilingDuelEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, body);
        this.setDropChance(EquipmentSlot.OFFHAND, body);
        this.setDropChance(EquipmentSlot.HEAD, Config.ceilingDuelHelmetDropChance);
        this.setDropChance(EquipmentSlot.CHEST, body);
        this.setDropChance(EquipmentSlot.LEGS, body);
        this.setDropChance(EquipmentSlot.FEET, body);
    }

    private static ItemStack enchantedPiece(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        return stack;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new CeilingDuelManeuverDirectorGoal(this));
        this.targetSelector.addGoal(
                1, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(
                2,
                new MobGunNearestAttackablePassthroughTargetGoal<>(
                        this, AbstractFriendEntity.class, false));
        this.targetSelector.addGoal(
                3, new SoldierHurtByTargetGoal(this, CeilingDuelEntity.class).setAlertOthers());
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        // 常驻射击（LOOK）：不随 BASE/SKILL/BRIDGE 切换 stop
        this.goalSelector.addGoal(
                1,
                new CeilingDuelGunFireGoal(
                        this,
                        (float) Config.ceilingDuelAttributes.gunRadius,
                        (float) Config.ceilingDuelAttributes.gunMeleeFrac,
                        Config.ceilingDuelAttributes.gunAttackIntervalMin,
                        Config.ceilingDuelAttributes.seeTimeToShoot));
        this.goalSelector.addGoal(1, new CeilingDuelCatStepJumpGoal(this));
        this.goalSelector.addGoal(1, new CeilingDuelInstantCrawlGoal(this));
        // 301 同通道：蹲 + 横拉走位（registerGoals 在 super 中，须当场 new）
        this.goalSelector.addGoal(1, new CeilingDuelLateralQuickCrouchGoal(this));
        this.goalSelector.addGoal(
                2,
                new CeilingDuelLateralYankGoal(
                        this,
                        Config.ceilingDuelGunGoalSpeedModifier,
                        (float) Config.ceilingDuelAttributes.gunRadius,
                        Config.ceilingDuelAttributes.seeTimeToStopMove));
        // 302 同通道：低频秒趴 + 横拉（12–32）
        this.goalSelector.addGoal(1, new CeilingDuelLateralQuickCrawlGoal(this));
        this.goalSelector.addGoal(
                2,
                new CeilingDuelLateralYankCrawlGoal(
                        this,
                        Config.ceilingDuelGunGoalSpeedModifier,
                        (float) Config.ceilingDuelAttributes.gunRadius,
                        Config.ceilingDuelAttributes.seeTimeToStopMove));
        // 303 同通道：301 秒蹲 + 跳阀小跳 + 横拉（12–24）
        this.goalSelector.addGoal(1, new CeilingDuelLateralQuickCrouchHopGoal(this));
        this.goalSelector.addGoal(
                2,
                new CeilingDuelLateralYankHopGoal(
                        this,
                        Config.ceilingDuelGunGoalSpeedModifier,
                        (float) Config.ceilingDuelAttributes.gunRadius,
                        Config.ceilingDuelAttributes.seeTimeToStopMove));
        // 304 中距颠步
        this.goalSelector.addGoal(2, new CeilingDuelStaggerStepGoal(this));
        // 305 中远滑步
        this.goalSelector.addGoal(2, new CeilingDuelSlideStepGoal(this));
        Config.CombatGoalStats g = Config.ceilingDuelCombatGoals;
        this.goalSelector.addGoal(
                1,
                new CeilingDuelBaseManeuverGoals.LongSneak(
                        this,
                        g.longSneakTimeDelay,
                        g.longSneakTime,
                        g.longSneakRandomTime,
                        g.longSneakDistance,
                        g.longSneakEnabled));
        this.goalSelector.addGoal(
                1,
                new CeilingDuelBaseManeuverGoals.ShortSneak(
                        this,
                        g.shortSneakTimeDelay,
                        g.shortSneakTime,
                        g.shortSneakRandomTime,
                        g.shortSneakDistance,
                        g.shortSneakEnabled));
        this.goalSelector.addGoal(2, new CeilingDuelBaseManeuverGoals.NoTargetSneakStand(this));
        this.goalSelector.addGoal(
                1,
                new CeilingDuelBaseManeuverGoals.Crawl(
                        this,
                        g.crawlRandomDenominator,
                        g.crawlExitRandomDenominator,
                        g.crawlMaxHealthExclusive,
                        g.crawlMinPlanarDistance,
                        g.crawlMinHeightToDistanceRatio,
                        g.crawlEnabled));
        this.goalSelector.addGoal(
                2,
                new CeilingDuelBaseManeuverGoals.Jump(
                        this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.ceilingDuelMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.ceilingDuelAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.ceilingDuelAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.ceilingDuelAttributes.followRange);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            // 被动：再生 II / 海豚的恩惠 V；跳跃由 currentJumpTier + getJumpPower（默认 TIER_HIGH），深渊仍走 LivingJumpEvent
            if (!this.hasEffect(MobEffects.REGENERATION)) {
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 1, false, false));
            }
            if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 4, false, false));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(TAG_PHASE, this.maneuverPhase.name());
        compound.putInt(TAG_SKILL, this.maneuverSkillId);
        compound.putInt(TAG_PHASE_ELAPSED, this.phaseElapsed);
        compound.putInt(TAG_PHASE_MAX, this.phaseDurationMax);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_PHASE)) {
            try {
                this.maneuverPhase = CeilingDuelManeuverPhase.valueOf(compound.getString(TAG_PHASE));
            } catch (IllegalArgumentException ignored) {
                this.maneuverPhase = CeilingDuelManeuverPhase.BASE;
            }
        }
        if (compound.contains(TAG_SKILL)) {
            this.maneuverSkillId = compound.getInt(TAG_SKILL);
        } else if (compound.contains("CeilingDuelManeuverSkill")) {
            this.maneuverSkillId = compound.getInt("CeilingDuelManeuverSkill");
        }
        if (compound.contains(TAG_PHASE_ELAPSED)) {
            this.phaseElapsed = compound.getInt(TAG_PHASE_ELAPSED);
        }
        if (compound.contains(TAG_PHASE_MAX)) {
            this.phaseDurationMax = compound.getInt(TAG_PHASE_MAX);
        }
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        int pick = self.getRandom().nextInt(6);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_CEILING_DUEL_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_CEILING_DUEL_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_CEILING_DUEL_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_CEILING_DUEL_TARGET_4.get();
                    case 4 -> ModSounds.SOUND_CEILING_DUEL_TARGET_5.get();
                    default -> ModSounds.SOUND_CEILING_DUEL_TARGET_6.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int pick = this.random.nextInt(6);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_CEILING_DUEL_IDLE_1.get();
                case 1 -> ModSounds.SOUND_CEILING_DUEL_IDLE_2.get();
                case 2 -> ModSounds.SOUND_CEILING_DUEL_IDLE_3.get();
                case 3 -> ModSounds.SOUND_CEILING_DUEL_IDLE_4.get();
                case 4 -> ModSounds.SOUND_CEILING_DUEL_IDLE_5.get();
                default -> ModSounds.SOUND_CEILING_DUEL_IDLE_6.get();
            };
        }
        int pick = this.random.nextInt(6);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_CEILING_DUEL_COMBAT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_CEILING_DUEL_COMBAT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_CEILING_DUEL_COMBAT_IDLE_3.get();
            case 3 -> ModSounds.SOUND_CEILING_DUEL_COMBAT_IDLE_4.get();
            case 4 -> ModSounds.SOUND_CEILING_DUEL_COMBAT_IDLE_5.get();
            default -> ModSounds.SOUND_CEILING_DUEL_COMBAT_IDLE_6.get();
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(6);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_CEILING_DUEL_DEATH_1.get();
            case 1 -> ModSounds.SOUND_CEILING_DUEL_DEATH_2.get();
            case 2 -> ModSounds.SOUND_CEILING_DUEL_DEATH_3.get();
            case 3 -> ModSounds.SOUND_CEILING_DUEL_DEATH_4.get();
            case 4 -> ModSounds.SOUND_CEILING_DUEL_DEATH_5.get();
            default -> ModSounds.SOUND_CEILING_DUEL_DEATH_6.get();
        };
    }
}
