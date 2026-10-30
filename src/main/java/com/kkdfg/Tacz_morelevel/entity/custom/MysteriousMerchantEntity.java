package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.api.MilitiaAcquireTargetVoice;
import com.kkdfg.Tacz_morelevel.entity.ConfiguredLivingAttributes;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MysteriousMerchantLookAtTradingPlayerGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MysteriousMerchantTradeWithPlayerGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidPoliceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierUnarmedMeleeGoal;
import com.kkdfg.Tacz_morelevel.integration.tacz.MysteriousMerchantGunCatalog;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.mysteriousmerchant.MysteriousMerchantTradeCatalog;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/** 神秘商人：士兵式战斗 AI，副手藏卓越枪，无仇恨时可按流浪商人逻辑交易。
 * 死亡装备掉落概率见 common 配置 {@code [mysteriousMerchantDeathDrops] equipmentDropChance}（默认 20%）。
 */
public final class MysteriousMerchantEntity extends AbstractNeutralGunNpcEntity
        implements Merchant, MilitiaAcquireTargetVoice {

    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;

    @Nullable
    private MobGunRangedPassthroughAttackGoal<MysteriousMerchantEntity> gunRangedGoal;

    @Nullable
    private MobGunRangedFireGoal<MysteriousMerchantEntity> gunFireGoal;

    @Nullable
    private MeleeAttackGoal meleeAttackGoal;
    private boolean combatGoalsMounted;

    public MysteriousMerchantEntity(EntityType<? extends MysteriousMerchantEntity> type, Level level) {
        super(type, level);
        this.reassessCombatGoals();
    }

    @Override
    protected ResourceLocation pickSpawnGunId(net.minecraft.util.RandomSource random) {
        return MysteriousMerchantGunCatalog.pickRandom(random);
    }

    @Override
    protected String skinTextureFolder() {
        return "mysterious_merchant";
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.mysteriousMerchantAttributes;
    }

    @Override
    protected void applyEquipmentDropChances() {
        float p = Config.mysteriousMerchantEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, p);
        this.setDropChance(EquipmentSlot.OFFHAND, p);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, p);
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.registerNeutralGunCombatGoals();
        this.ensureCombatGoalsCreated();
        if (!this.combatGoalsMounted) {
            this.combatGoalsMounted = true;
            this.goalSelector.addGoal(2, this.gunFireGoal);
            this.goalSelector.addGoal(2, this.gunRangedGoal);
            this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, this.meleeAttackGoal));
        }
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new SoldierAvoidPoliceGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, Config.mysteriousMerchantWanderStrollSpeed));
        Config.CombatGoalStats g = Config.mysteriousMerchantCombatGoals;
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
                1, new SoldierHurtByTargetGoal(this, MysteriousMerchantEntity.class).setAlertOthers());
        this.goalSelector.addGoal(1, new MysteriousMerchantTradeWithPlayerGoal(this));
        this.goalSelector.addGoal(2, new MysteriousMerchantLookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    private void ensureCombatGoalsCreated() {
        if (this.gunRangedGoal != null && this.gunFireGoal != null && this.meleeAttackGoal != null) {
            return;
        }
        Config.NpcAttributeStats attrs = Config.mysteriousMerchantAttributes;
        this.gunRangedGoal =
                new MobGunRangedPassthroughAttackGoal<>(
                        this,
                        Config.mysteriousMerchantGunGoalSpeedModifier,
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
                        MysteriousMerchantEntity.this.setAggressive(false);
                    }

                    @Override
                    public void start() {
                        super.start();
                        MysteriousMerchantEntity.this.setAggressive(true);
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
            this.goalSelector.addGoal(2, this.gunRangedGoal);
            this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, this.meleeAttackGoal));
        }
        int interval =
                Config.mapNpcGunBurstInterval(
                        Config.mysteriousMerchantAttributes.gunAttackIntervalMin);
        if (this.gunRangedGoal != null) {
            this.gunRangedGoal.setMinAttackInterval(interval);
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
        return JumpController.TIER_HIGH_MID;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Config.mysteriousMerchantMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.mysteriousMerchantAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.mysteriousMerchantAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.mysteriousMerchantAttributes.followRange);
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
            this.applyConfiguredLivingAttributes(true);
            this.initializeNeutralGunLoadout(level.getRandom());
            this.applyMysteriousMerchantArmor();
            if (this.offers == null || this.offers.isEmpty()) {
                this.offers = MysteriousMerchantTradeCatalog.rollOffers(level.getRandom());
            }
        }
        return spawnData;
    }

    private void applyConfiguredLivingAttributes(boolean fillHealth) {
        ConfiguredLivingAttributes.apply(this, Config.mysteriousMerchantAttributes, fillHealth);
    }

    private void applyMysteriousMerchantArmor() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_BOOTS.get()));
    }

    @Override
    public void tick() {
        super.tick();
        if (Config.mysteriousMerchantPassiveRegenerationEnabled && !this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            Config.mysteriousMerchantPassiveRegenerationAmplifier,
                            false,
                            false));
        }
        if (Config.mysteriousMerchantPassiveDolphinsGraceEnabled && !this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(
                    new MobEffectInstance(
                            MobEffects.DOLPHINS_GRACE,
                            999999,
                            Config.mysteriousMerchantPassiveDolphinsGraceAmplifier,
                            false,
                            false));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide()) {
            this.stopTrading();
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.canTradePeacefully() || this.isTrading() || !this.isAlive()) {
            return super.mobInteract(player, hand);
        }
        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
        }
        if (this.getOffers().isEmpty()) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (!this.level().isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = MysteriousMerchantTradeCatalog.rollOffers(this.getRandom());
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        if (this.level().isClientSide) {
            return;
        }
        if (this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
            SoundEvent sound = stack.isEmpty() ? this.pickTradeFailSound() : this.pickTradeSuccessSound();
            this.playSound(sound, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {}

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return this.pickTradeSuccessSound();
    }

    private SoundEvent pickTradeSuccessSound() {
        int pick = this.random.nextInt(7);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_1.get();
            case 1 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_2.get();
            case 2 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_3.get();
            case 3 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_4.get();
            case 4 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_5.get();
            case 5 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_6.get();
            default -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_7.get();
        };
    }

    private SoundEvent pickTradeFailSound() {
        int pick = this.random.nextInt(6);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_1.get();
            case 1 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_2.get();
            case 2 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_3.get();
            case 3 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_4.get();
            case 4 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_5.get();
            default -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_6.get();
        };
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    protected void stopTrading() {
        this.setTradingPlayer(null);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        this.stopTrading();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        MerchantOffers merchantOffers = this.getOffers();
        if (!merchantOffers.isEmpty()) {
            compound.put("Offers", merchantOffers.createTag());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Offers", Tag.TAG_COMPOUND)) {
            this.offers = new MerchantOffers(compound.getCompound("Offers"));
        }
        if (this.level() != null && !this.level().isClientSide()) {
            this.applyConfiguredLivingAttributes(false);
            this.applyMysteriousMerchantArmor();
        }
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        int pick = self.getRandom().nextInt(3);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TARGET_2.get();
                    default -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_TARGET_3.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isTrading()) {
            // 交易中环境音：闲置 2/3/5/6
            return switch (this.random.nextInt(4)) {
                case 0 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_2.get();
                case 1 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_3.get();
                case 2 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_5.get();
                default -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_6.get();
            };
        }
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (hasTarget) {
            int pick = this.random.nextInt(5);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_1.get();
                case 1 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_2.get();
                case 2 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_3.get();
                case 3 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_4.get();
                default -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_5.get();
            };
        }
        int pick = this.random.nextInt(6);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_3.get();
            case 3 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_4.get();
            case 4 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_5.get();
            default -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_IDLE_6.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SOUND_SOLDIER_HURT_1.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(4);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_DEATH_1.get();
            case 1 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_DEATH_2.get();
            case 2 -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_DEATH_3.get();
            default -> ModSounds.SOUND_MYSTERIOUS_MERCHANT_DEATH_4.get();
        };
    }
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return false;
    }
}
