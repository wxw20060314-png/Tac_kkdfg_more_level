package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.api.MilitiaAcquireTargetVoice;
import com.kkdfg.Tacz_morelevel.entity.ConfiguredLivingAttributes;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunFindCoverRequestGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunSimThreadCoverStrafeMoveGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierUnarmedMeleeGoal;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczOrdinaryGunCatalog;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 警察：中立持枪 NPC。独立 {@code [police]} / {@code textures/entity/police}；
 * 枪池复用 {@code special_forces}；固定警官套；开趴寻路；被打集体仇恨。
 * 战斗数值默认对齐突击档（可配置差异），但不依赖突击兵类型或 profile。
 */
public final class PoliceEntity extends AbstractNeutralGunNpcEntity implements MilitiaAcquireTargetVoice {

    /** 警察皮肤集：{@code textures/entity/police/skin_0..5.png}。 */
    public static final int POLICE_SKIN_COUNT = 6;

    @Nullable
    private MobGunSimThreadCoverStrafeMoveGoal<PoliceEntity> gunCoverStrafeGoal;

    @Nullable
    private MobGunRangedFireGoal<PoliceEntity> gunFireGoal;

    @Nullable
    private MeleeAttackGoal meleeAttackGoal;

    private boolean combatGoalsMounted;

    public PoliceEntity(EntityType<? extends PoliceEntity> type, Level level) {
        super(type, level);
        this.reassessCombatGoals();
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
    }

    @Override
    protected int skinVariantCount() {
        return POLICE_SKIN_COUNT;
    }

    @Override
    protected ResourceLocation pickSpawnGunId(RandomSource random) {
        return TaczOrdinaryGunCatalog.pickRandomForPoolId(random, "special_forces");
    }

    @Override
    protected String skinTextureFolder() {
        return "police";
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.policeAttributes;
    }

    @Override
    protected void registerGoals() {
        this.registerNeutralGunCombatGoals();
        this.ensureCombatGoalsCreated();
        if (!this.combatGoalsMounted) {
            this.combatGoalsMounted = true;
            this.goalSelector.addGoal(2, this.gunFireGoal);
            this.goalSelector.addGoal(2, this.gunCoverStrafeGoal);
            this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, this.meleeAttackGoal));
        }
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        Config.NpcAttributeStats coverRange = this.configuredAttributeStats();
        this.goalSelector.addGoal(
                3,
                new MobGunFindCoverRequestGoal<>(
                        this,
                        (float) coverRange.findCoverNearDistance,
                        (float) coverRange.findCoverFarDistance));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.95D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 9.5F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.policeCombatGoals;
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
        this.targetSelector.addGoal(
                1,
                new SoldierHurtByTargetGoal(this, PoliceEntity.class, PunisherEntity.class)
                        .setAlertOthers()
                        .setAlertAllyTypes(PunisherEntity.class));
    }

    private void ensureCombatGoalsCreated() {
        if (this.gunCoverStrafeGoal != null && this.gunFireGoal != null && this.meleeAttackGoal != null) {
            return;
        }
        Config.NpcAttributeStats attrs = Config.policeAttributes;
        this.gunCoverStrafeGoal =
                new MobGunSimThreadCoverStrafeMoveGoal<>(
                        this,
                        Config.policeGunGoalSpeedModifier,
                        attrs.gunAttackIntervalMin,
                        (float) attrs.gunRadius,
                        (float) attrs.gunMeleeFrac,
                        attrs.seeTimeToStopMove,
                        (float) attrs.gunStrafeOuterRingFrac,
                        (float) attrs.gunStrafeInnerRingFrac,
                        attrs.seeTimeToShoot);
        this.gunFireGoal =
                new MobGunRangedFireGoal<>(
                        this,
                        (float) attrs.gunRadius,
                        (float) attrs.gunMeleeFrac,
                        attrs.gunAttackIntervalMin,
                        attrs.seeTimeToShoot,
                        GunCombatSight.COMBAT);
        this.meleeAttackGoal =
                new MeleeAttackGoal(this, attrs.meleeSpeed, false) {
                    @Override
                    public void stop() {
                        super.stop();
                        PoliceEntity.this.setAggressive(false);
                    }

                    @Override
                    public void start() {
                        super.start();
                        PoliceEntity.this.setAggressive(true);
                    }
                };
    }

    @Override
    protected void reassessCombatGoals() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.ensureCombatGoalsCreated();
        if (!this.combatGoalsMounted) {
            this.combatGoalsMounted = true;
            this.goalSelector.addGoal(2, this.gunFireGoal);
            this.goalSelector.addGoal(2, this.gunCoverStrafeGoal);
            this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, this.meleeAttackGoal));
        }
        int interval = Config.mapNpcGunBurstInterval(Config.policeAttributes.gunAttackIntervalMin);
        if (this.gunCoverStrafeGoal != null) {
            this.gunCoverStrafeGoal.setMinAttackInterval(interval);
        }
        if (this.gunFireGoal != null) {
            this.gunFireGoal.setMinAttackInterval(interval);
        }
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
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Config.policeMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.policeAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.policeAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.policeAttributes.followRange);
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
            ConfiguredLivingAttributes.apply(this, Config.policeAttributes, true);
            this.initializeNeutralGunLoadout(level.getRandom());
            this.applyPoliceArmor();
        }
        return spawnData;
    }

    /** 警官全套：保护 IV；头盔中级防闪；胸/腿/靴各一块中级防爆板。 */
    private void applyPoliceArmor() {
        ItemStack helmet = protectionIv(ModItems.POLICE_HELMET.get());
        ItemStack chest = protectionIv(ModItems.POLICE_CHESTPLATE.get());
        ItemStack legs = protectionIv(ModItems.POLICE_LEGGINGS.get());
        ItemStack boots = protectionIv(ModItems.POLICE_BOOTS.get());
        BulletproofArmor.setPlugin(
                helmet, 0, new ItemStack(ModItems.FLASH_RESIST_GOGGLES_MEDIUM.get()));
        ItemStack blastPlate = new ItemStack(ModItems.BLAST_STEEL_ARMOR_PLATE_MEDIUM.get());
        BulletproofArmor.setPlugin(chest, 0, blastPlate.copy());
        BulletproofArmor.setPlugin(legs, 0, blastPlate.copy());
        BulletproofArmor.setPlugin(boots, 0, blastPlate.copy());
        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setItemSlot(EquipmentSlot.CHEST, chest);
        this.setItemSlot(EquipmentSlot.LEGS, legs);
        this.setItemSlot(EquipmentSlot.FEET, boots);
    }

    private static ItemStack protectionIv(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        return stack;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 0, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 2, false, false));
        }
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        int pick = self.getRandom().nextInt(5);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_POLICE_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_POLICE_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_POLICE_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_POLICE_TARGET_4.get();
                    default -> ModSounds.SOUND_POLICE_TARGET_5.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (hasTarget) {
            int pick = this.random.nextInt(5);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_POLICE_COMBAT_IDLE_1.get();
                case 1 -> ModSounds.SOUND_POLICE_COMBAT_IDLE_2.get();
                case 2 -> ModSounds.SOUND_POLICE_COMBAT_IDLE_3.get();
                case 3 -> ModSounds.SOUND_POLICE_COMBAT_IDLE_4.get();
                default -> ModSounds.SOUND_POLICE_COMBAT_IDLE_5.get();
            };
        }
        int pick = this.random.nextInt(5);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_POLICE_IDLE_1.get();
            case 1 -> ModSounds.SOUND_POLICE_IDLE_2.get();
            case 2 -> ModSounds.SOUND_POLICE_IDLE_3.get();
            case 3 -> ModSounds.SOUND_POLICE_IDLE_4.get();
            default -> ModSounds.SOUND_POLICE_IDLE_5.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SOUND_SOLDIER_HURT_1.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(5);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_POLICE_DEATH_1.get();
            case 1 -> ModSounds.SOUND_POLICE_DEATH_2.get();
            case 2 -> ModSounds.SOUND_POLICE_DEATH_3.get();
            case 3 -> ModSounds.SOUND_POLICE_DEATH_4.get();
            default -> ModSounds.SOUND_POLICE_DEATH_5.get();
        };
    }
}
