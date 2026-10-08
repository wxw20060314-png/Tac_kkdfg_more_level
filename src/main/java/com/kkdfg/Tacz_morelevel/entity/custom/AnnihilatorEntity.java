package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunNearestAttackablePassthroughTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidGrenadeAndFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.annihilator.AnnihilatorBlink;
import com.kkdfg.Tacz_morelevel.entity.ai.annihilator.AnnihilatorBlinkCooldown;
import com.kkdfg.Tacz_morelevel.entity.ai.annihilator.AnnihilatorBlinkSkillGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.AssistAimHelper;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.google.common.collect.ImmutableMap;
import com.tacz.guns.api.item.attachment.AttachmentType;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * 歼灭者 Boss：固定 HK-416D 歼灭者 + 时空防弹全套；战斗属性/AI 默认与超级特种兵一致（见 {@code annihilator} 配置）。
 * 皮肤 {@code textures/entity/annihilator/skin_0..9.png}，生成时随机一套。
 * 主动传送扫描仅针对 {@link Player}（优先）与 {@link AbstractFriendEntity}（护航/护卫）；被打可还击任意生物。
 * 空间传送见 {@link AnnihilatorBlink}、{@link AnnihilatorBlinkSkillGoal}。
 * 死亡装备掉落概率见 common 配置 {@code [annihilatorDeathDrops] equipmentDropChance}（默认 20%）。
 * 额外掉落：战利品表 {@code loot_tables/entities/annihilator.json} 必定 1–3 时空之石。
 */
public final class AnnihilatorEntity extends AbstractPlayerLikeSoldierEntity {

    /** 与 {@code textures/entity/annihilator/skin_*.png} 数量一致。*/
    public static final int ANNIHILATOR_SKIN_COUNT = 10;

    public static final ResourceLocation MAIN_GUN = AssistAimHelper.GUN_ID;

    private static ResourceLocation taczAttachment(String path) {
        return ResourceLocation.fromNamespaceAndPath("tacz", path);
    }

    /** 主武器固定配件：Coyote 红点、FMJ、游骑兵握把、SBA3、三叉戟补偿器、小型激光。*/
    private static final Map<AttachmentType, ResourceLocation> MAIN_GUN_ATTACHMENTS =
            ImmutableMap.<AttachmentType, ResourceLocation>builder()
                    .put(AttachmentType.SCOPE, taczAttachment("sight_coyote"))
                    .put(AttachmentType.EXTENDED_MAG, taczAttachment("ammo_mod_fmj"))
                    .put(AttachmentType.GRIP, taczAttachment("grip_vertical_ranger"))
                    .put(AttachmentType.STOCK, taczAttachment("stock_sba3"))
                    .put(AttachmentType.MUZZLE, taczAttachment("muzzle_compensator_trident"))
                    .put(AttachmentType.LASER, taczAttachment("laser_compact"))
                    .build();

    /** 无目标时扫描间隔（tick）。*/
    public static final int TARGET_SCAN_INTERVAL_TICKS = 40;
    /** 前后、左右探测半距（格）。*/
    public static final double SCAN_HALF_EXTENT_PLANAR = 50.0D;
    /** 上下探测半距（格）。*/
    public static final double SCAN_HALF_EXTENT_VERTICAL = 50.0D;
    /** 空间传送技能冷却上限（TTL tick），见 {@link AnnihilatorBlinkCooldown#MAX}。*/
    public static final int BLINK_COOLDOWN_TTL_MAX = AnnihilatorBlinkCooldown.MAX;
    /** 传送到目标身旁的平面偏移（格）。*/
    public static final double BLINK_NEAR_TARGET_DISTANCE = 2.5D;
    /** 仇恨目标优先级：玩家。*/
    public static final int TARGET_PRIORITY_PLAYER = 1;
    /** 仇恨目标优先级：护航（低于玩家）。*/
    public static final int TARGET_PRIORITY_BODYGUARD = 2;

    /** 受伤闪避：受到伤害时按概率立即随机传送（见 {@link #hurt}）。*/
    public static final float DAMAGE_BLINK_CHANCE = 0.25F;
    /** 受伤闪避：水平半距（总宽 20 格）。*/
    public static final double DAMAGE_BLINK_HALF_PLANAR = 10.0D;
    /** 受伤闪避：垂直半距（总高 10 格）。*/
    public static final double DAMAGE_BLINK_HALF_VERTICAL = 5.0D;
    /** 受伤闪避：随机落点尝试次数。*/
    public static final int DAMAGE_BLINK_RANDOM_ATTEMPTS = 16;

    private final AnnihilatorBlinkCooldown blinkCooldown = new AnnihilatorBlinkCooldown();

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.annihilator"),
                    BossEvent.BossBarColor.WHITE,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private final MobGunRangedPassthroughAttackGoal<AnnihilatorEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.annihilatorGunGoalSpeedModifier,
                    Config.annihilatorAttributes.gunAttackIntervalMin,
                    (float) Config.annihilatorAttributes.gunRadius,
                    (float) Config.annihilatorAttributes.gunMeleeFrac,
                    Config.annihilatorAttributes.seeTimeToStopMove,
                    (float) Config.annihilatorAttributes.gunStrafeOuterRingFrac,
                    (float) Config.annihilatorAttributes.gunStrafeInnerRingFrac,
                    Config.annihilatorAttributes.seeTimeToShoot);

    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.annihilatorAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    AnnihilatorEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    AnnihilatorEntity.this.setAggressive(true);
                }
            };

    public AnnihilatorEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.reassessWeaponGoal();
    }

    public AnnihilatorBlinkCooldown getBlinkCooldown() {
        return this.blinkCooldown;
    }

    public boolean tryBlinkTo(Vec3 dest) {
        return AnnihilatorBlink.tryBlinkTo(this, dest, this.blinkCooldown);
    }

    public boolean tryBlinkNearTarget(LivingEntity target) {
        return AnnihilatorBlink.tryBlinkNearTarget(this, target, this.blinkCooldown);
    }

    public boolean tryRandomNearbyBlink() {
        return AnnihilatorBlink.tryRandomNearby(this, this.blinkCooldown);
    }

    public Optional<Vec3> resolveBlinkNearTarget(LivingEntity target) {
        return AnnihilatorBlink.resolveNearTarget(this, target);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tacz_morelevel.annihilator");
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
    protected void applyEquipmentDropChances() {
        float p = Config.annihilatorEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, p);
        this.setDropChance(EquipmentSlot.OFFHAND, p);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, p);
            }
        }
    }

    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.SUPER_SPECIAL;
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.annihilatorAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "annihilator";
    }

    @Override
    protected String skinTextureFolder() {
        return "annihilator";
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.annihilatorAttributes.gunAttackIntervalMin);
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
            @Nullable net.minecraft.nbt.CompoundTag dataTag) {
        this.setLeftHanded(false);
        if (!level.isClientSide()) {
            this.applyConfiguredLivingAttributes(true);
            this.equipAnnihilatorLoadout();
        }
        return spawnData;
    }

    private void equipAnnihilatorLoadout() {
        this.setSoldierSkinIndex(this.random.nextInt(ANNIHILATOR_SKIN_COUNT));
        if (TaczGunMobHelper.equipMainHandWithAttachments(this, MAIN_GUN, MAIN_GUN_ATTACHMENTS)) {
            this.persistedGunDataId = MAIN_GUN;
        } else {
            this.persistedGunDataId = null;
        }
        this.reassessWeaponGoal();
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.SPACETIME_BULLETPROOF_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SPACETIME_BULLETPROOF_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.SPACETIME_BULLETPROOF_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.SPACETIME_BULLETPROOF_BOOTS.get()));
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SoldierAvoidGrenadeAndFireGoal(this));
        this.targetSelector.addGoal(
                TARGET_PRIORITY_PLAYER,
                new MobGunNearestAttackablePassthroughTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(
                TARGET_PRIORITY_BODYGUARD,
                new MobGunNearestAttackablePassthroughTargetGoal<>(
                        this, AbstractFriendEntity.class, false));
        this.targetSelector.addGoal(
                3, new SoldierHurtByTargetGoal(this, AnnihilatorEntity.class).setAlertOthers());
        this.goalSelector.addGoal(1, new AnnihilatorBlinkSkillGoal(this));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.annihilatorCombatGoals;
        this.goalSelector.addGoal(
                1,
                new MobRandomSneakWithTargetInlongDistanceGoal(
                        this,
                        g.longSneakTimeDelay,
                        g.longSneakTime,
                        g.longSneakRandomTime,
                        g.longSneakDistance,
                        g.longSneakEnabled));
        this.goalSelector.addGoal(
                1,
                new MobRandomSneakWithTargetInShortDistanceGoal(
                        this,
                        g.shortSneakTimeDelay,
                        g.shortSneakTime,
                        g.shortSneakRandomTime,
                        g.shortSneakDistance,
                        g.shortSneakEnabled));
        this.goalSelector.addGoal(2, new MobRandomSneakWithNoTargetGoal(this));
        this.goalSelector.addGoal(
                1,
                new MobTaczCombatCrawlGoal(
                        this,
                        g.crawlRandomDenominator,
                        g.crawlExitRandomDenominator,
                        g.crawlMaxHealthExclusive,
                        g.crawlMinPlanarDistance,
                        g.crawlMinHeightToDistanceRatio,
                        g.crawlEnabled));
        this.goalSelector.addGoal(2, new MobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.annihilatorMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.annihilatorAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.annihilatorAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.annihilatorAttributes.followRange);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!hurt || this.level().isClientSide() || amount <= 0.0F) {
            return hurt;
        }
        if (this.random.nextFloat() < DAMAGE_BLINK_CHANCE) {
            this.tryRandomNearbyBlink();
        }
        return hurt;
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_HIGH;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 1, false, false));
        }
        if (!this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 2, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 4, false, false));
        }
        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.blinkCooldown.tick();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.blinkCooldown.write(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.blinkCooldown.read(compound);
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
    public void remove(Entity.RemovalReason reason) {
        this.bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        int pick = self.getRandom().nextInt(8);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_ANNIHILATOR_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_ANNIHILATOR_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_ANNIHILATOR_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_ANNIHILATOR_TARGET_4.get();
                    case 4 -> ModSounds.SOUND_ANNIHILATOR_TARGET_5.get();
                    case 5 -> ModSounds.SOUND_ANNIHILATOR_TARGET_6.get();
                    case 6 -> ModSounds.SOUND_ANNIHILATOR_TARGET_7.get();
                    default -> ModSounds.SOUND_ANNIHILATOR_TARGET_8.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int pick = this.random.nextInt(8);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_ANNIHILATOR_IDLE_1.get();
                case 1 -> ModSounds.SOUND_ANNIHILATOR_IDLE_2.get();
                case 2 -> ModSounds.SOUND_ANNIHILATOR_IDLE_3.get();
                case 3 -> ModSounds.SOUND_ANNIHILATOR_IDLE_4.get();
                case 4 -> ModSounds.SOUND_ANNIHILATOR_IDLE_5.get();
                case 5 -> ModSounds.SOUND_ANNIHILATOR_IDLE_6.get();
                case 6 -> ModSounds.SOUND_ANNIHILATOR_IDLE_7.get();
                default -> ModSounds.SOUND_ANNIHILATOR_IDLE_8.get();
            };
        }
        int pick = this.random.nextInt(7);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_ANNIHILATOR_COMBAT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_ANNIHILATOR_COMBAT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_ANNIHILATOR_COMBAT_IDLE_3.get();
            case 3 -> ModSounds.SOUND_ANNIHILATOR_COMBAT_IDLE_4.get();
            case 4 -> ModSounds.SOUND_ANNIHILATOR_COMBAT_IDLE_5.get();
            case 5 -> ModSounds.SOUND_ANNIHILATOR_COMBAT_IDLE_6.get();
            default -> ModSounds.SOUND_ANNIHILATOR_COMBAT_IDLE_7.get();
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(7);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_ANNIHILATOR_DEATH_1.get();
            case 1 -> ModSounds.SOUND_ANNIHILATOR_DEATH_2.get();
            case 2 -> ModSounds.SOUND_ANNIHILATOR_DEATH_3.get();
            case 3 -> ModSounds.SOUND_ANNIHILATOR_DEATH_4.get();
            case 4 -> ModSounds.SOUND_ANNIHILATOR_DEATH_5.get();
            case 5 -> ModSounds.SOUND_ANNIHILATOR_DEATH_6.get();
            default -> ModSounds.SOUND_ANNIHILATOR_DEATH_7.get();
        };
    }
}
