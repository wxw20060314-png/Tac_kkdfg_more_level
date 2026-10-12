package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunNearestAttackablePassthroughTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidGrenadeAndFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierUnarmedMeleeGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 史莱姆指挥官 Boss：固定最高代；不穿甲；分裂只出战士；本人及谱系不远距离刷新。
 * 枪池 {@code slime_commander}（excellent）；走位/枪 Goal 用 {@code [slimeCommander]}；点射对齐超特。
 */
public final class SlimeCommanderEntity extends AbstractSlimeExperimentEntity {

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.slime_commander")
                            .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
                    BossEvent.BossBarColor.GREEN,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private final MobGunRangedPassthroughAttackGoal<SlimeCommanderEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.slimeCommanderGunGoalSpeedModifier,
                    Config.slimeCommanderAttributes.gunAttackIntervalMin,
                    (float) Config.slimeCommanderAttributes.gunRadius,
                    (float) Config.slimeCommanderAttributes.gunMeleeFrac,
                    Config.slimeCommanderAttributes.seeTimeToStopMove,
                    (float) Config.slimeCommanderAttributes.gunStrafeOuterRingFrac,
                    (float) Config.slimeCommanderAttributes.gunStrafeInnerRingFrac,
                    Config.slimeCommanderAttributes.seeTimeToShoot);

    private final MobGunRangedFireGoal<SlimeCommanderEntity> gunFireGoal =
            new MobGunRangedFireGoal<>(
                    this,
                    (float) Config.slimeCommanderAttributes.gunRadius,
                    (float) Config.slimeCommanderAttributes.gunMeleeFrac,
                    Config.slimeCommanderAttributes.gunAttackIntervalMin,
                    Config.slimeCommanderAttributes.seeTimeToShoot,
                    GunCombatSight.COMBAT);

    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.slimeCommanderAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    SlimeCommanderEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    SlimeCommanderEntity.this.setAggressive(true);
                }
            };

    private boolean combatGoalsMounted;

    public SlimeCommanderEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setRootIsCommander(true);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.mountCombatGoals();
    }

    @Override
    protected boolean isCommanderEntity() {
        return true;
    }

    @Override
    protected String gunPoolId() {
        return "slime_commander";
    }

    @Override
    public int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.slimeCommanderAttributes.gunAttackIntervalMin);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tacz_morelevel.slime_commander")
                .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC);
    }

    @Override
    public Component getName() {
        return this.getDisplayName();
    }

    @Override
    protected void onGunEquipped() {
        this.mountCombatGoals();
        int interval = this.mobGunBurstIntervalTicks();
        this.gunGoal.setMinAttackInterval(interval);
        this.gunFireGoal.setMinAttackInterval(interval);
    }

    @Override
    protected void onGenerationStatsApplied(SplitSpec spec) {
        int interval = this.mobGunBurstIntervalTicks();
        this.gunGoal.setMinAttackInterval(interval);
        this.gunFireGoal.setMinAttackInterval(interval);
    }

    private void mountCombatGoals() {
        if (this.combatGoalsMounted || this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.combatGoalsMounted = true;
        this.goalSelector.addGoal(2, this.gunFireGoal);
        this.goalSelector.addGoal(2, this.gunGoal);
        this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, this.meleeGoal));
        int interval = this.mobGunBurstIntervalTicks();
        this.gunGoal.setMinAttackInterval(interval);
        this.gunFireGoal.setMinAttackInterval(interval);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(
                1, new SoldierHurtByTargetGoal(this, AbstractSlimeExperimentEntity.class));
        this.targetSelector.addGoal(
                2, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(
                3,
                new MobGunNearestAttackablePassthroughTargetGoal<>(
                        this, AbstractFriendEntity.class, false));
        this.targetSelector.addGoal(
                5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, WitherBoss.class, false));
        this.targetSelector.addGoal(
                5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Warden.class, false));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SoldierAvoidGrenadeAndFireGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.slimeCommanderCombatGoals;
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
                2, new MobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnReason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag dataTag) {
        this.setRootIsCommander(true);
        this.setRootUuid(this.getUUID());
        this.setParentUuid(null);
        spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
        if (!level.isClientSide()) {
            this.applyGenerationStats(0, true, true);
            this.mountCombatGoals();
        }
        return spawnData;
    }

    @Override
    @Nullable
    protected AbstractSlimeExperimentEntity createSplitInstance(ServerLevel server) {
        return ModEntities.SLIME_WARRIOR.get().create(server);
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_HIGH;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, SPLIT_TABLE[0].maxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.slimeCommanderAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.slimeCommanderAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.slimeCommanderAttributes.followRange);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 4, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 4, false, false));
        }
        if (!this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 2, false, false));
        }
        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
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
}
