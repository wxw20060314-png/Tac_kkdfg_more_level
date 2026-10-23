package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.BodyguardProfile;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.SpawnArmorEnchantableMob;
import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler;
import com.kkdfg.Tacz_morelevel.armor.skill.officer.OfficerGuardRosterHelper;
import com.kkdfg.Tacz_morelevel.entity.ConfiguredLivingAttributes;
import com.kkdfg.Tacz_morelevel.entity.custom.MarshalFactionHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunFindCoverRequestGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunSimThreadCoverStrafeMoveGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionAttackMonsterGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidFlashbangLookGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidThrowableGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionFollowOwnerGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithTargetInLongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionOwnerHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionOwnerHurtTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczOrdinaryGunCatalog;
import com.kkdfg.Tacz_morelevel.api.SpawnArmorConfigurableMob;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.RegistryObject;

/**
 * 玩家友方「护卫」：战斗数值/枪甲气质对齐大帅护卫军；主人走 Friend。
 * <p>
 * 军官胸甲名册双向链路：自身 {@code OfficerBondId} + {@code linkTtl}；超时无掉落移除。
 * 武器整理在 {@link AbstractSoldierArrangeFriendEntity}。
 */
public final class GuardEntity extends AbstractSoldierArrangeFriendEntity
        implements
                TaczPlayerLikeMob,
                SpawnArmorConfigurableMob,
                SpawnArmorEnchantableMob {

    private static final String TAG_SKIN_INDEX = "TaczMoreLevelGuardSkinIdx";
    private static final String TAG_OFFICER_BOND = "OfficerBondId";
    private static final String TAG_LINK_TTL = "OfficerLinkTtl";
    private static final String GUN_POOL_ID = "marshal_guard";
    private static final String ARMOR_POOL_ID = "marshal_guard";
    /** 丢目标后仍保持头盔夜视的 tick 数。 */
    private static final int NIGHT_VISION_OFF_TTL_TICKS = 60;
    /** 与大帅护卫相同：超特种兵皮肤池。 */
    public static final int SKIN_COUNT = AbstractPlayerLikeSoldierEntity.SOLDIER_SKIN_COUNT;

    private static final EntityDataAccessor<Integer> DATA_SKIN_INDEX =
            SynchedEntityData.defineId(GuardEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID officerBondId;
    private int officerLinkTtl = OfficerGuardRosterHelper.LINK_TIMEOUT_TICKS;
    /** 无目标关夜视倒计时。 */
    private int nightVisionOffTtl;

    private final MobGunSimThreadCoverStrafeMoveGoal<GuardEntity> gunGoal =
            new MobGunSimThreadCoverStrafeMoveGoal<>(
                    this,
                    Config.marshalGuardGunGoalSpeedModifier,
                    Config.marshalGuardAttributes.gunAttackIntervalMin,
                    (float) Config.marshalGuardAttributes.gunRadius,
                    (float) Config.marshalGuardAttributes.gunMeleeFrac,
                    Config.marshalGuardAttributes.seeTimeToStopMove,
                    (float) Config.marshalGuardAttributes.gunStrafeOuterRingFrac,
                    (float) Config.marshalGuardAttributes.gunStrafeInnerRingFrac,
                    Config.marshalGuardAttributes.seeTimeToShoot);

    private final MeleeAttackGoal meleeGoal =
            newTameMeleeGoal(Config.marshalGuardAttributes.meleeSpeed);

    public GuardEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
    }

    @Override
    public int getSimThreadUrgency() {
        return 40;
    }

    @Override
    protected BodyguardProfile bodyguardProfile() {
        // 占位：实际属性由 marshalGuardAttributes 写入
        return BodyguardProfile.ELITE;
    }

    @Override
    protected Goal createGunFireGoal() {
        Config.NpcAttributeStats attrs = Config.marshalGuardAttributes;
        return new MobGunRangedFireGoal<>(
                this,
                (float) attrs.gunRadius,
                (float) attrs.gunMeleeFrac,
                attrs.gunAttackIntervalMin,
                attrs.seeTimeToShoot,
                GunCombatSight.COMBAT);
    }

    @Override
    protected void applyConfiguredLivingAttributes(boolean fillHealth) {
        ConfiguredLivingAttributes.apply(this, Config.marshalGuardAttributes, fillHealth);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_INDEX, 0);
    }

    public int getSoldierSkinIndex() {
        return Mth.clamp(this.entityData.get(DATA_SKIN_INDEX), 0, SKIN_COUNT - 1);
    }

    @Nullable
    public UUID getOfficerBondId() {
        return this.officerBondId;
    }

    public void setOfficerBondId(@Nullable UUID bondId) {
        this.officerBondId = bondId;
    }

    public void resetOfficerLinkTimeout() {
        this.officerLinkTtl = OfficerGuardRosterHelper.LINK_TIMEOUT_TICKS;
    }

    public boolean isOfficerLinkAlive() {
        return this.officerBondId != null && this.officerLinkTtl > 0;
    }

    @Override
    public ResourceLocation getSoldierSkinTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID,
                "textures/entity/super_special_soldier/skin_" + getSoldierSkinIndex() + ".png");
    }

    @Override
    public String spawnArmorPoolId() {
        return ARMOR_POOL_ID;
    }

    @Override
    public boolean shouldEnchantSpawnArmorPiece(EquipmentSlot slot, ItemStack stack, RandomSource random) {
        return false;
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected MeleeAttackGoal meleeGoal() {
        return this.meleeGoal;
    }

    @Override
    protected int bodyguardGunBurstIntervalTicks() {
        return Config.mapNpcGunBurstInterval(Config.marshalGuardAttributes.gunAttackIntervalMin);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SoldierAvoidThrowableGoal<>(this));
        this.goalSelector.addGoal(1, new SoldierAvoidFlashbangLookGoal<>(this));
        Config.NpcAttributeStats coverRange = Config.marshalGuardAttributes;
        this.goalSelector.addGoal(
                3,
                new MobGunFindCoverRequestGoal<>(
                        this,
                        (float) coverRange.findCoverNearDistance,
                        (float) coverRange.findCoverFarDistance));
        this.goalSelector.addGoal(4, new CompanionFollowOwnerGoal(this, 1.0D, 25.0F, 10.0F, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new CompanionOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CompanionHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new CompanionOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new CompanionAttackMonsterGoal(this));

        Config.CombatGoalStats g = Config.marshalGuardCombatGoals;
        this.goalSelector.addGoal(
                1,
                new CompanionMobTaczCombatCrawlGoal(
                        this,
                        g.crawlRandomDenominator,
                        g.crawlExitRandomDenominator,
                        g.crawlMaxHealthExclusive,
                        g.crawlMinPlanarDistance,
                        g.crawlMinHeightToDistanceRatio,
                        g.crawlEnabled));
        this.goalSelector.addGoal(3, new CompanionMobRandomSneakWithNoTargetGoal(this));
        this.goalSelector.addGoal(
                1,
                new CompanionMobRandomSneakWithTargetInLongDistanceGoal(
                        this,
                        g.longSneakTimeDelay,
                        g.longSneakTime,
                        g.longSneakRandomTime,
                        g.longSneakDistance,
                        g.longSneakEnabled));
        this.goalSelector.addGoal(
                1,
                new CompanionMobRandomSneakWithTargetInShortDistanceGoal(
                        this,
                        g.shortSneakTimeDelay,
                        g.shortSneakTime,
                        g.shortSneakRandomTime,
                        g.shortSneakDistance,
                        g.shortSneakEnabled));
        this.goalSelector.addGoal(
                1, new CompanionMobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
        this.registerSoldierWeaponArrangeGoals();
        this.registerCompanionGunReloadGoal();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Config.marshalGuardMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.marshalGuardAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.marshalGuardAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.marshalGuardAttributes.followRange);
    }

    @Override
    public boolean isTameItem(ItemStack stack) {
        return false;
    }

    /** 未认主时空手右键认主（刷怪蛋测试用）；已驯服无背包/待命交互。 */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            boolean usable = this.isOwnedBy(player) || this.isTame() || stack.isEmpty() && !this.isTame();
            return usable ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (!this.isTame() && stack.isEmpty()) {
            this.setTame(true);
            this.setOwnerUUID(player.getUUID());
            this.navigation.stop();
            this.setTarget(null);
            this.level().broadcastEntityEvent(this, (byte) 7);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
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
        if (!level.isClientSide()) {
            this.entityData.set(DATA_SKIN_INDEX, level.getRandom().nextInt(SKIN_COUNT));
            this.applySpawnArmorFromJson(level.getRandom());
            ResourceLocation gunId = TaczOrdinaryGunCatalog.pickRandomForPoolId(level.getRandom(), GUN_POOL_ID);
            if (gunId != null) {
                this.setPreferredMainGunDataId(gunId);
                this.equipTaCZGunMainHand(gunId);
            }
        }
        return spawnData;
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_AMP2;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.tickOfficerLink();
            this.tickTargetNightVision();
        }
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 0, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 2, false, false));
        }
    }

    /**
     * 护卫 ↔ 胸甲双向保活：主人满套且名册仍登记自己则刷新 TTL；否则倒计时，耗尽无掉落删除。
     */
    private void tickOfficerLink() {
        if (this.officerBondId == null) {
            return;
        }
        UUID ownerId = this.getOwnerUUID();
        if (ownerId == null) {
            return;
        }
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Entity raw = server.getEntity(ownerId);
        if (raw instanceof Player player && player.isAlive()) {
            if (OfficerGuardRosterHelper.chestAgreesLink(player, this)) {
                this.resetOfficerLinkTimeout();
                double limit =
                        (player.getLastHurtMob() != null && player.getLastHurtMob().isAlive())
                                        || (player.getLastHurtByMob() != null
                                                && player.getLastHurtByMob().isAlive())
                                ? OfficerGuardRosterHelper.RECALL_DIST_COMBAT
                                : OfficerGuardRosterHelper.RECALL_DIST_IDLE;
                if (this.distanceToSqr(player) > limit * limit) {
                    OfficerGuardRosterHelper.recallGuard(this, player);
                    this.resetOfficerLinkTimeout();
                }
                return;
            }
        }
        this.officerLinkTtl--;
        if (this.officerLinkTtl <= 0) {
            MarshalFactionHelper.discardWithoutDrops(this);
        }
    }

    /**
     * 对应头盔：有存活目标开夜视；丢目标后 {@link #NIGHT_VISION_OFF_TTL_TICKS} 内保持，再关。
     */
    private void tickTargetNightVision() {
        ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
        if (!ArmorNightVisionHandler.isEligibleHelmet(helmet)) {
            this.nightVisionOffTtl = 0;
            return;
        }
        LivingEntity target = this.getTarget();
        boolean want = target != null && target.isAlive();
        boolean now = ArmorNightVisionHandler.isEnabled(helmet);
        if (want) {
            this.nightVisionOffTtl = 0;
            if (!now) {
                ArmorNightVisionHandler.setEnabled(helmet, true);
            }
            if (this.level().getGameTime() % ArmorNightVisionHandler.REFRESH_INTERVAL_TICKS == 0 || !now) {
                ArmorNightVisionHandler.applyNightVision(this);
            }
            return;
        }
        if (!now && !this.hasEffect(MobEffects.NIGHT_VISION) && this.nightVisionOffTtl <= 0) {
            return;
        }
        if (this.nightVisionOffTtl <= 0) {
            this.nightVisionOffTtl = NIGHT_VISION_OFF_TTL_TICKS;
        }
        this.nightVisionOffTtl--;
        if (this.nightVisionOffTtl > 0) {
            if (!now) {
                ArmorNightVisionHandler.setEnabled(helmet, true);
            }
            if (this.level().getGameTime() % ArmorNightVisionHandler.REFRESH_INTERVAL_TICKS == 0 || !now) {
                ArmorNightVisionHandler.applyNightVision(this);
            }
            return;
        }
        if (now) {
            ArmorNightVisionHandler.setEnabled(helmet, false);
        }
        if (this.hasEffect(MobEffects.NIGHT_VISION)) {
            ArmorNightVisionHandler.clearNightVision(this);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_SKIN_INDEX, getSoldierSkinIndex());
        if (this.officerBondId != null) {
            compound.putUUID(TAG_OFFICER_BOND, this.officerBondId);
        }
        compound.putInt(TAG_LINK_TTL, this.officerLinkTtl);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_SKIN_INDEX, Tag.TAG_INT)) {
            this.entityData.set(
                    DATA_SKIN_INDEX, Mth.clamp(compound.getInt(TAG_SKIN_INDEX), 0, SKIN_COUNT - 1));
        }
        if (compound.hasUUID(TAG_OFFICER_BOND)) {
            this.officerBondId = compound.getUUID(TAG_OFFICER_BOND);
        }
        if (compound.contains(TAG_LINK_TTL, Tag.TAG_INT)) {
            this.officerLinkTtl = compound.getInt(TAG_LINK_TTL);
        }
    }

    @Override
    protected void dropFromLootTable(
            net.minecraft.world.damagesource.DamageSource damageSource, boolean hitByPlayer) {}

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide() || !this.isTame()) {
            return;
        }
        self.playSound(pickVoice(TARGET_VOICES, self.getRandom()), 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        return pickVoice(hasTarget ? COMBAT_IDLE_VOICES : IDLE_VOICES, this.random);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return pickVoice(DEATH_VOICES, this.random);
    }

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SoundEvent>[] IDLE_VOICES =
            new RegistryObject[] {
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_1,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_2,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_3,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_4,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_5,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_6,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_7,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_8,
                ModSounds.SOUND_MARSHAL_GUARD_IDLE_9
            };

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SoundEvent>[] COMBAT_IDLE_VOICES =
            new RegistryObject[] {
                ModSounds.SOUND_MARSHAL_GUARD_COMBAT_IDLE_1,
                ModSounds.SOUND_MARSHAL_GUARD_COMBAT_IDLE_2,
                ModSounds.SOUND_MARSHAL_GUARD_COMBAT_IDLE_3,
                ModSounds.SOUND_MARSHAL_GUARD_COMBAT_IDLE_4,
                ModSounds.SOUND_MARSHAL_GUARD_COMBAT_IDLE_5,
                ModSounds.SOUND_MARSHAL_GUARD_COMBAT_IDLE_6
            };

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SoundEvent>[] TARGET_VOICES =
            new RegistryObject[] {
                ModSounds.SOUND_MARSHAL_GUARD_TARGET_1,
                ModSounds.SOUND_MARSHAL_GUARD_TARGET_2,
                ModSounds.SOUND_MARSHAL_GUARD_TARGET_3,
                ModSounds.SOUND_MARSHAL_GUARD_TARGET_4,
                ModSounds.SOUND_MARSHAL_GUARD_TARGET_5,
                ModSounds.SOUND_MARSHAL_GUARD_TARGET_6
            };

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SoundEvent>[] DEATH_VOICES =
            new RegistryObject[] {
                ModSounds.SOUND_MARSHAL_GUARD_DEATH_1,
                ModSounds.SOUND_MARSHAL_GUARD_DEATH_2,
                ModSounds.SOUND_MARSHAL_GUARD_DEATH_3,
                ModSounds.SOUND_MARSHAL_GUARD_DEATH_4,
                ModSounds.SOUND_MARSHAL_GUARD_DEATH_5
            };

    private static SoundEvent pickVoice(RegistryObject<SoundEvent>[] pool, RandomSource random) {
        return pool[random.nextInt(pool.length)].get();
    }
}
