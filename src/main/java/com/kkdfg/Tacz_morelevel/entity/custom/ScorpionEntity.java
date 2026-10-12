package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthSeventhHandler;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthSeventhSkill;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.google.common.collect.ImmutableMap;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.entity.EntityKineticBullet;
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

/**
 * 毒蝎 Boss：枪套 M107 .50 卓越（遇敌掏枪，同列夫）；配件：狙击大扩容 / MK5HD 5-25x / 秃鹫消音；
 * 潜行七级全套；有目标开潜行，丢目标后 {@code STEALTH_OFF_TTL_TICKS} 内保持。
 * 战斗数值独立 {@code [scorpion]} 配置；无空间闪现。
 * 皮肤 {@code textures/entity/scorpion/skin_0..5.png}（Alex 细臂 UV）；渲染见 {@code ScorpionRenderer}。
 */
public final class ScorpionEntity extends AbstractHolsterGunSoldierEntity {

    /** 与 {@code textures/entity/scorpion/skin_*.png} 数量一致。 */
    public static final int SCORPION_SKIN_COUNT = 6;

    public static final ResourceLocation HOLSTER_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "m107_excellent");

    private static ResourceLocation taczAttachment(String path) {
        return ResourceLocation.fromNamespaceAndPath("tacz", path);
    }

    /** 狙击大扩容 III、Mark 5 HD 5-25x、秃鹫 .50 消音。 */
    private static final Map<AttachmentType, ResourceLocation> HOLSTER_GUN_ATTACHMENTS =
            ImmutableMap.<AttachmentType, ResourceLocation>builder()
                    .put(AttachmentType.EXTENDED_MAG, taczAttachment("sniper_extended_mag_3"))
                    .put(AttachmentType.SCOPE, taczAttachment("scope_mk5hd"))
                    .put(AttachmentType.MUZZLE, taczAttachment("muzzle_silencer_vulture"))
                    .build();

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.scorpion"),
                    BossEvent.BossBarColor.WHITE,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private final MobGunRangedPassthroughAttackGoal<ScorpionEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.scorpionGunGoalSpeedModifier,
                    Config.scorpionAttributes.gunAttackIntervalMin,
                    (float) Config.scorpionAttributes.gunRadius,
                    (float) Config.scorpionAttributes.gunMeleeFrac,
                    Config.scorpionAttributes.seeTimeToStopMove,
                    (float) Config.scorpionAttributes.gunStrafeOuterRingFrac,
                    (float) Config.scorpionAttributes.gunStrafeInnerRingFrac,
                    Config.scorpionAttributes.seeTimeToShoot);

    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.scorpionAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    ScorpionEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    ScorpionEntity.this.setAggressive(true);
                }
            };

    /** 丢目标后仍保持潜行的 tick 数；有目标时回满。 */
    private static final int STEALTH_OFF_TTL_TICKS = 60;
    private int stealthOffTtl;

    public ScorpionEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.reassessWeaponGoal();
    }

    @Override
    protected ResourceLocation pickHolsterGunId(RandomSource random) {
        return HOLSTER_GUN;
    }

    @Override
    @Nullable
    protected Map<AttachmentType, ResourceLocation> pickHolsterGunAttachments() {
        return HOLSTER_GUN_ATTACHMENTS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tacz_morelevel.scorpion");
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
        float p = Config.scorpionEquipmentDropChance;
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
        return Config.scorpionAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "scorpion";
    }

    @Override
    protected String skinTextureFolder() {
        return "scorpion";
    }

    @Override
    public int getSoldierSkinIndex() {
        return Mth.clamp(super.getSoldierSkinIndex(), 0, SCORPION_SKIN_COUNT - 1);
    }

    @Override
    protected void setSoldierSkinIndex(int index) {
        super.setSoldierSkinIndex(Mth.clamp(index, 0, SCORPION_SKIN_COUNT - 1));
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.scorpionAttributes.gunAttackIntervalMin);
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
            this.setPreferredMainGunDataId(HOLSTER_GUN);
            this.applyConfiguredLivingAttributes(true);
            this.setSoldierSkinIndex(this.random.nextInt(SCORPION_SKIN_COUNT));
            this.equipStealthSeventhLoadout();
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
        }
        return spawnData;
    }

    private void equipStealthSeventhLoadout() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.STEALTH_SEVENTH_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.STEALTH_SEVENTH_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.STEALTH_SEVENTH_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.STEALTH_SEVENTH_BOOTS.get()));
        this.applyEquipmentDropChances();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new SoldierHurtByTargetGoal(this, ScorpionEntity.class).setAlertOthers());
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.scorpionCombatGoals;
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
                .add(Attributes.MAX_HEALTH, Config.scorpionMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.scorpionAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.scorpionAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.scorpionAttributes.followRange);
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
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
            tickCombatStealth();
        }
    }

    /** 有存活目标 → TTL 回满并开潜行；无目标 → 倒计时，到 0 才关。 */
    private void tickCombatStealth() {
        LivingEntity target = this.getTarget();
        boolean hasTarget = target != null && target.isAlive();
        if (hasTarget) {
            this.stealthOffTtl = STEALTH_OFF_TTL_TICKS;
            if (!ArmorStealthSeventhHandler.isToggled(this)
                    && ArmorLegendarySkillRegistry.canActivate(this, StealthSeventhSkill.ID)) {
                ArmorLegendarySkillRegistry.tryActivate(this, StealthSeventhSkill.ID);
            }
            return;
        }
        if (this.stealthOffTtl > 0) {
            this.stealthOffTtl--;
        }
        if (this.stealthOffTtl > 0) {
            return;
        }
        if (ArmorStealthSeventhHandler.isToggled(this)) {
            ArmorStealthSeventhHandler.setToggled(this, false);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isOwnTaczProjectileDamage(source)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    private boolean isOwnTaczProjectileDamage(DamageSource source) {
        if (isOwnKineticBullet(source.getEntity()) || isOwnKineticBullet(source.getDirectEntity())) {
            return true;
        }
        Entity causing = source.getEntity();
        Entity direct = source.getDirectEntity();
        return causing == this || direct == this;
    }

    private boolean isOwnKineticBullet(@Nullable Entity entity) {
        return entity instanceof EntityKineticBullet bullet && bullet.getOwner() == this;
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

    /** 发现目标语音。 */
    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        SoundEvent voice =
                switch (self.getRandom().nextInt(5)) {
                    case 0 -> ModSounds.SOUND_SCORPION_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_SCORPION_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_SCORPION_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_SCORPION_TARGET_4.get();
                    default -> ModSounds.SOUND_SCORPION_TARGET_5.get();
                };
        self.playSound(voice, 1.0F, 1.0F);
    }

    /**
     * 闲置 7 条拉成 nextInt(14)（半数空）；战斗闲置 7 条同样 nextInt(14) 半数空。
     */
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (hasTarget) {
            return switch (this.random.nextInt(8)) {
                case 0 -> ModSounds.SOUND_SCORPION_COMBAT_IDLE_1.get();
                case 1 -> ModSounds.SOUND_SCORPION_COMBAT_IDLE_2.get();
                case 2 -> ModSounds.SOUND_SCORPION_COMBAT_IDLE_3.get();
                case 3 -> ModSounds.SOUND_SCORPION_COMBAT_IDLE_4.get();
                case 4 -> ModSounds.SOUND_SCORPION_COMBAT_IDLE_5.get();
                case 5 -> ModSounds.SOUND_SCORPION_COMBAT_IDLE_6.get();
                case 6 -> ModSounds.SOUND_SCORPION_COMBAT_IDLE_7.get();
                default -> null;
            };
        }
        return switch (this.random.nextInt(14)) {
            case 0 -> ModSounds.SOUND_SCORPION_IDLE_1.get();
            case 1 -> ModSounds.SOUND_SCORPION_IDLE_2.get();
            case 2 -> ModSounds.SOUND_SCORPION_IDLE_3.get();
            case 3 -> ModSounds.SOUND_SCORPION_IDLE_4.get();
            case 4 -> ModSounds.SOUND_SCORPION_IDLE_5.get();
            case 5 -> ModSounds.SOUND_SCORPION_IDLE_6.get();
            case 6 -> ModSounds.SOUND_SCORPION_IDLE_7.get();
            default -> null;
        };
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SOUND_SCORPION_HURT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return switch (this.random.nextInt(5)) {
            case 0 -> ModSounds.SOUND_SCORPION_DEATH_1.get();
            case 1 -> ModSounds.SOUND_SCORPION_DEATH_2.get();
            case 2 -> ModSounds.SOUND_SCORPION_DEATH_3.get();
            case 3 -> ModSounds.SOUND_SCORPION_DEATH_4.get();
            default -> ModSounds.SOUND_SCORPION_DEATH_5.get();
        };
    }
}
