package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.api.SpawnArmorEnchantableMob;
import com.kkdfg.Tacz_morelevel.entity.ai.MarshalGuardHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MarshalGuardOwnerHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MarshalGuardOwnerHurtTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MarshalGuardShareTargetToOwnerGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraftforge.registries.RegistryObject;

/**
 * 大帅护卫军：敌对士兵；皮肤复用超特；枪池 {@code troop_guns/marshal_guard}（完美），
 * 甲池 {@code troop_armor/marshal_guard}（特种作战套、不附魔）；主人 UUID 供召唤绑定。
 */
public final class MarshalGuardEntity extends AbstractPlayerLikeSoldierEntity
        implements SpawnArmorEnchantableMob {

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(MarshalGuardEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final String TAG_OWNER = "MarshalOwner";
    private static final String TAG_LINK_TTL = "MarshalLinkTtl";

    /** 丢目标后仍保持头盔夜视的 tick 数。 */
    private static final int NIGHT_VISION_OFF_TTL_TICKS = 60;

    private final MobGunRangedPassthroughAttackGoal<MarshalGuardEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
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
            new MeleeAttackGoal(this, Config.marshalGuardAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    MarshalGuardEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    MarshalGuardEntity.this.setAggressive(true);
                }
            };

    /** 与主人的链路保活剩余 tick；有主时生效。*/
    private int linkTtl = MarshalFactionHelper.LINK_TIMEOUT_TICKS;
    /** 无目标关夜视倒计时。 */
    private int nightVisionOffTtl;

    public MarshalGuardEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.applyEquipmentDropChances();
        this.reassessWeaponGoal();
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
    }

    public void setMarshalOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getMarshalOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setMarshalOwner(@Nullable LivingEntity owner) {
        this.setMarshalOwnerUUID(owner == null ? null : owner.getUUID());
        if (owner != null) {
            this.resetLinkTimeout();
        }
    }

    public void resetLinkTimeout() {
        this.linkTtl = MarshalFactionHelper.LINK_TIMEOUT_TICKS;
    }

    /** 护卫侧会话是否仍存活（TTL &gt; 0）；主人未加载时会递减至断连。 */
    public boolean isOwnerLinkAlive() {
        return this.linkTtl > 0;
    }

    /**
     * 解析主人实体（不论是否存活）；未加载返回 {@code null}。     */
    @Nullable
    public Entity findOwnerEntityRaw() {
        UUID uuid = this.getMarshalOwnerUUID();
        if (uuid == null || !(this.level() instanceof ServerLevel server)) {
            return null;
        }
        return server.getEntity(uuid);
    }

    @Nullable
    public LivingEntity getMarshalOwner() {
        Entity entity = this.findOwnerEntityRaw();
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    /** 服务端：链路保活 / 主人死亡或超时无掉落删除 / 超距末影拉回。*/
    private void tickOwnerLink() {
        UUID ownerId = this.getMarshalOwnerUUID();
        if (ownerId == null) {
            return;
        }
        Entity raw = this.findOwnerEntityRaw();
        if (raw instanceof LivingEntity living) {
            if (!living.isAlive()) {
                MarshalFactionHelper.discardWithoutDrops(this);
                return;
            }
            this.resetLinkTimeout();
            double limit = MarshalFactionHelper.recallDistanceLimit(
                    living instanceof Mob mob ? mob : null);
            if (this.distanceToSqr(living) > limit * limit) {
                MarshalFactionHelper.recallToMarshal(this, living);
                this.resetLinkTimeout();
            }
            return;
        }
        // 主人未加载：倒计时，耗尽断连
        this.linkTtl--;
        if (this.linkTtl <= 0) {
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

    /** 不攻击主人、列夫大帅、同主护卫；无主时仍可正常仇敌。*/
    public boolean wantsToAttack(@Nullable LivingEntity target, @Nullable LivingEntity owner) {
        if (target == null || !target.isAlive() || target == this) {
            return false;
        }
        if (owner != null && (target == owner || target.isAlliedTo(owner))) {
            return false;
        }
        if (target instanceof LevMarshalEntity) {
            return false;
        }
        if (target instanceof MarshalGuardEntity other) {
            UUID selfOwner = this.getMarshalOwnerUUID();
            UUID otherOwner = other.getMarshalOwnerUUID();
            if (selfOwner != null && selfOwner.equals(otherOwner)) {
                return false;
            }
        }
        return true;
    }
    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.MERCENARY;
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.marshalGuardAttributes;
    }

    @Override
    protected String troopGunPoolId() {
        return "marshal_guard";
    }

    @Override
    public String spawnArmorPoolId() {
        return "marshal_guard";
    }

    @Override
    public boolean shouldEnchantSpawnArmorPiece(EquipmentSlot slot, ItemStack stack, RandomSource random) {
        return false;
    }

    @Override
    public boolean appliesSpawnArmorOnFinalize() {
        return true;
    }

    /** 小弟死亡不掉落任何装备。*/
    @Override
    protected void applyEquipmentDropChances() {
        float p = Config.marshalGuardEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, p);
        this.setDropChance(EquipmentSlot.OFFHAND, p);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, p);
            }
        }
    }

    @Override
    protected void dropFromLootTable(net.minecraft.world.damagesource.DamageSource damageSource, boolean hitByPlayer) {
        // 拒绝战利品表掉落
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return false;
    }

    @Override
    protected String skinTextureFolder() {
        return "super_special_soldier";
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapNpcGunBurstInterval(Config.marshalGuardAttributes.gunAttackIntervalMin);
    }

    @Override
    protected MeleeAttackGoal meleeGoal() {
        return this.meleeGoal;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new MarshalGuardOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new MarshalGuardHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new MarshalGuardOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new MarshalGuardShareTargetToOwnerGoal(this));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.marshalGuardCombatGoals;
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
                .add(Attributes.MAX_HEALTH, Config.marshalGuardMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.marshalGuardAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.marshalGuardAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.marshalGuardAttributes.followRange);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (MarshalFactionHelper.shouldBlockFactionDamage(this, source)) {
            return false;
        }
        return super.hurt(source, amount);
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
            this.tickOwnerLink();
            this.tickTargetNightVision();
        }
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 0, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 2, false, false));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        UUID owner = this.getMarshalOwnerUUID();
        if (owner != null) {
            compound.putUUID(TAG_OWNER, owner);
        }
        compound.putInt(TAG_LINK_TTL, this.linkTtl);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID(TAG_OWNER)) {
            this.setMarshalOwnerUUID(compound.getUUID(TAG_OWNER));
        }
        if (compound.contains(TAG_LINK_TTL)) {
            this.linkTtl = compound.getInt(TAG_LINK_TTL);
        } else if (this.getMarshalOwnerUUID() != null) {
            this.resetLinkTimeout();
        }
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
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
