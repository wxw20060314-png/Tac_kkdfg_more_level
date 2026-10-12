package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.BlastSummonBombCartSkill;
import com.kkdfg.Tacz_morelevel.entity.ai.BombBabyGrenadeFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.BombBabyGrenadeMoveGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.BombBabyGrenadeWeaponSwapGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedBombBabyAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.integration.tacz.DualGunCatalog;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.item.custom.BlastSeventhBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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
 * 炸弹宝宝 Boss：双枪（主武器爆破 MK14 + 背枪传奇 M320）；穿爆破七级套。
 * 无目标时由 {@link AbstractDualGunSoldierEntity} 切回主武器。
 * 榴弹：{@link BombBabyGrenadeWeaponSwapGoal} + {@link BombBabyGrenadeMoveGoal} + {@link BombBabyGrenadeFireGoal}。
 * 射击视线：有 target 用 {@link com.kkdfg.Tacz_morelevel.entity.ai.BombBabySight}；选目标仍普通穿透。
 * 战斗走位抄超级特种兵；抗性 III（amplifier 3 = 80% 减伤）。
 * 皮肤 {@code textures/entity/bomb_baby/skin_0..2.png}，生成时随机一套。
 * 技能：有目标时走传说技能 {@link BlastSummonBombCartSkill} 召唤认主小车（冷却见技能）。
 * 装备掉落见 {@code bombBabyDeathDrops.equipmentDropChance}（默认 20%）。
 */
public final class BombBabyEntity extends AbstractDualGunSoldierEntity {

    /** 与 {@code textures/entity/bomb_baby/skin_*.png} 数量一致。 */
    public static final int BOMB_BABY_SKIN_COUNT = 3;

    /** 主武器：爆破 MK14。 */
    public static final ResourceLocation MAIN_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "mk14_blast");
    /** 背枪：传奇榴弹发射器。 */
    public static final ResourceLocation BACK_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "m320_legendary");

    /** 抗性效果等级：amplifier 3 → 80% 减伤。 */
    private static final int RESISTANCE_AMPLIFIER = 2;

    /** @deprecated 冷却已迁至胸甲 NBT TTL；仅兼容旧存档读取。 */
    @Deprecated
    private static final String TAG_SUMMON_CART_CD_LEGACY = "BombBabySummonCartCd";

    /** @deprecated 曾用实体 PersistentData 倒数；仅兼容迁移。 */
    @Deprecated
    private static final String TAG_SUMMON_CART_CD_ENTITY_LEGACY = "BlastSummonBombCartCd";

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.bomb_baby"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private final MobGunRangedBombBabyAttackGoal gunGoal =
            new MobGunRangedBombBabyAttackGoal(
                    this,
                    Config.bombBabyGunGoalSpeedModifier,
                    Config.bombBabyAttributes.gunAttackIntervalMin,
                    (float) Config.bombBabyAttributes.gunRadius,
                    (float) Config.bombBabyAttributes.gunMeleeFrac,
                    Config.bombBabyAttributes.seeTimeToStopMove,
                    (float) Config.bombBabyAttributes.gunStrafeOuterRingFrac,
                    (float) Config.bombBabyAttributes.gunStrafeInnerRingFrac,
                    Config.bombBabyAttributes.seeTimeToShoot);

    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.bombBabyAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    BombBabyEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    BombBabyEntity.this.setAggressive(true);
                }
            };

    public BombBabyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.reassessWeaponGoal();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tacz_morelevel.bomb_baby");
    }

    @Override
    protected String primaryGunPoolId() {
        return DualGunCatalog.PRIMARY_POOL;
    }

    @Override
    protected String backGunPoolId() {
        return DualGunCatalog.BACK_POOL;
    }

    /** 主手是否握着传奇 M320。 */
    public boolean isHoldingGrenadeLauncher() {
        IGun gun = IGun.getIGunOrNull(this.getMainHandItem());
        return gun != null && BACK_GUN.equals(gun.getGunId(this.getMainHandItem()));
    }

    /** 切到榴弹发射器（主手 M320）；已在榴弹上则 true。 */
    public boolean ensureGrenadeLauncherDrawn() {
        if (this.isHoldingGrenadeLauncher()) {
            return true;
        }
        if (!this.isHoldingPreferredPrimaryGun()) {
            return false;
        }
        return this.swapActiveAndBackGun();
    }

    /** 切回爆破 MK14；已在主武器上则 true。 */
    public boolean ensurePrimaryGunDrawn() {
        if (this.isHoldingPreferredPrimaryGun()) {
            return true;
        }
        return this.trySwapBackToPreferredPrimary();
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
        float p = Config.bombBabyEquipmentDropChance;
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
    protected GunCombatSight gunCombatSight() {
        return GunCombatSight.BLAST;
    }

    @Override
    protected Goal createGunFireGoal() {
        Config.NpcAttributeStats attrs = this.configuredAttributeStats();
        return new MobGunRangedFireGoal<>(
                this,
                (float) attrs.gunRadius,
                (float) attrs.gunMeleeFrac,
                attrs.gunAttackIntervalMin,
                attrs.seeTimeToShoot,
                GunCombatSight.BLAST,
                false,
                BombBabyEntity::isHoldingPreferredPrimaryGun);
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.bombBabyAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "bomb_baby";
    }

    @Override
    protected String skinTextureFolder() {
        return "bomb_baby";
    }

    @Override
    public int getSoldierSkinIndex() {
        return Mth.clamp(super.getSoldierSkinIndex(), 0, BOMB_BABY_SKIN_COUNT - 1);
    }

    @Override
    protected void setSoldierSkinIndex(int index) {
        super.setSoldierSkinIndex(Mth.clamp(index, 0, BOMB_BABY_SKIN_COUNT - 1));
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.bombBabyAttributes.gunAttackIntervalMin);
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
        if (!level.isClientSide()) {
            this.applyConfiguredLivingAttributes(true);
            this.equipDualGunLoadout();
        }
        return spawnData;
    }

    /** 主手 MK14 爆破、背枪传奇榴弹；爆破七级全套。 */
    @Override
    protected void equipDualGunLoadout() {
        this.setSoldierSkinIndex(this.random.nextInt(BOMB_BABY_SKIN_COUNT));
        this.equipTaCZGunMainHand(MAIN_GUN);
        this.setPreferredPrimaryGunDataId(MAIN_GUN);
        this.equipTaCZGunBack(BACK_GUN);
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.SEVENTH_BLAST_BULLETPROOF_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SEVENTH_BLAST_BULLETPROOF_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.SEVENTH_BLAST_BULLETPROOF_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.SEVENTH_BLAST_BULLETPROOF_BOOTS.get()));
        this.applyEquipmentDropChances();
        this.reassessWeaponGoal();
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new SoldierHurtByTargetGoal(this, BombBabyEntity.class).setAlertOthers());
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(1, new BombBabyGrenadeWeaponSwapGoal(this));
        this.goalSelector.addGoal(1, new BombBabyGrenadeFireGoal(this));
        this.goalSelector.addGoal(2, new BombBabyGrenadeMoveGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.bombBabyCombatGoals;
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
                .add(Attributes.MAX_HEALTH, Config.bombBabyMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.bombBabyAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.bombBabyAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.bombBabyAttributes.followRange);
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
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 4, false, false));
        }
        // 抗性提升 3 → 80% 减伤
        if (!this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            this.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE, 999999, RESISTANCE_AMPLIFIER, false, false));
        }
        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
            this.tickSummonBombCartSkill();
        }
    }

    /** 有存活目标时走 {@link BlastSummonBombCartSkill#trySummon}（冷却由胸甲 TTL 现算）。 */
    private void tickSummonBombCartSkill() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        BlastSummonBombCartSkill.trySummon(this, null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        // 旧存档：剩余 tick 冷却迁到胸甲 until = gameTime + remaining
        int remaining = 0;
        if (compound.contains(TAG_SUMMON_CART_CD_LEGACY)) {
            remaining =
                    Math.max(
                            remaining,
                            Mth.clamp(
                                    compound.getInt(TAG_SUMMON_CART_CD_LEGACY),
                                    0,
                                    BlastSummonBombCartSkill.COOLDOWN_TICKS));
        }
        if (this.getPersistentData().contains(TAG_SUMMON_CART_CD_ENTITY_LEGACY)) {
            remaining =
                    Math.max(
                            remaining,
                            Mth.clamp(
                                    this.getPersistentData().getInt(TAG_SUMMON_CART_CD_ENTITY_LEGACY),
                                    0,
                                    BlastSummonBombCartSkill.COOLDOWN_TICKS));
            this.getPersistentData().remove(TAG_SUMMON_CART_CD_ENTITY_LEGACY);
        }
        if (remaining > 0) {
            ItemStack chest = this.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof BlastSeventhBulletproofArmorItem) {
                BlastSeventhBulletproofArmorItem.startSummonCooldown(
                        chest, this.level().getGameTime(), remaining);
            }
        }
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

    /**
     * 屏蔽自己打出的 TaCZ 子弹伤害
     * （含 M320 榴弹直击与爆炸：伤源多为子弹，射手在 {@link EntityKineticBullet#getOwner()}）。
     * 认主小车伤害由 {@link com.kkdfg.Tacz_morelevel.event.BombCartOwnerHateEvents} 统一拦截。
     */
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
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        int pick = self.getRandom().nextInt(7);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_BOMB_BABY_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_BOMB_BABY_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_BOMB_BABY_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_BOMB_BABY_TARGET_4.get();
                    case 4 -> ModSounds.SOUND_BOMB_BABY_TARGET_5.get();
                    case 5 -> ModSounds.SOUND_BOMB_BABY_TARGET_6.get();
                    default -> ModSounds.SOUND_BOMB_BABY_TARGET_7.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int pick = this.random.nextInt(7);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_BOMB_BABY_IDLE_1.get();
                case 1 -> ModSounds.SOUND_BOMB_BABY_IDLE_2.get();
                case 2 -> ModSounds.SOUND_BOMB_BABY_IDLE_3.get();
                case 3 -> ModSounds.SOUND_BOMB_BABY_IDLE_4.get();
                case 4 -> ModSounds.SOUND_BOMB_BABY_IDLE_5.get();
                case 5 -> ModSounds.SOUND_BOMB_BABY_IDLE_6.get();
                default -> ModSounds.SOUND_BOMB_BABY_IDLE_7.get();
            };
        }
        int pick = this.random.nextInt(7);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_BOMB_BABY_COMBAT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_BOMB_BABY_COMBAT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_BOMB_BABY_COMBAT_IDLE_3.get();
            case 3 -> ModSounds.SOUND_BOMB_BABY_COMBAT_IDLE_4.get();
            case 4 -> ModSounds.SOUND_BOMB_BABY_COMBAT_IDLE_5.get();
            case 5 -> ModSounds.SOUND_BOMB_BABY_COMBAT_IDLE_6.get();
            default -> ModSounds.SOUND_BOMB_BABY_COMBAT_IDLE_7.get();
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(7);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_BOMB_BABY_DEATH_1.get();
            case 1 -> ModSounds.SOUND_BOMB_BABY_DEATH_2.get();
            case 2 -> ModSounds.SOUND_BOMB_BABY_DEATH_3.get();
            case 3 -> ModSounds.SOUND_BOMB_BABY_DEATH_4.get();
            case 4 -> ModSounds.SOUND_BOMB_BABY_DEATH_5.get();
            case 5 -> ModSounds.SOUND_BOMB_BABY_DEATH_6.get();
            default -> ModSounds.SOUND_BOMB_BABY_DEATH_7.get();
        };
    }
}
