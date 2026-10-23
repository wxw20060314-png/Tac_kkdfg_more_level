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
import com.kkdfg.Tacz_morelevel.entity.ai.SlimeShareTargetToRootGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierUnarmedMeleeGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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
 * 史莱姆战士：不穿甲；自然生成 / 刷怪蛋时仅 gen1/gen2 随机（不刷最高级 gen0）；分裂只出战士。
 * 枪池 {@code slime_warrior}（perfect）；走位/枪 Goal 用 {@code [slimeWarrior]}（BossConfig）。
 * 若属于指挥官谱系则不远距离刷新；独立战士可被刷掉。
 */
public final class SlimeWarriorEntity extends AbstractSlimeExperimentEntity {

    private MobGunRangedPassthroughAttackGoal<SlimeWarriorEntity> gunGoal;
    private MobGunRangedFireGoal<SlimeWarriorEntity> gunFireGoal;
    private MeleeAttackGoal meleeGoal;
    private boolean combatGoalsMounted;

    public SlimeWarriorEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.initCombatGoals();
        this.mountCombatGoals();
    }

    @Override
    protected String gunPoolId() {
        return "slime_warrior";
    }

    @Override
    public int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.slimeWarriorAttributes.gunAttackIntervalMin);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tacz_morelevel.slime_warrior")
                .withStyle(ChatFormatting.DARK_GREEN);
    }

    @Override
    public Component getName() {
        return this.getDisplayName();
    }

    private void initCombatGoals() {
        Config.NpcAttributeStats a = Config.slimeWarriorAttributes;
        this.gunGoal =
                new MobGunRangedPassthroughAttackGoal<>(
                        this,
                        Config.slimeWarriorGunGoalSpeedModifier,
                        a.gunAttackIntervalMin,
                        (float) a.gunRadius,
                        (float) a.gunMeleeFrac,
                        a.seeTimeToStopMove,
                        (float) a.gunStrafeOuterRingFrac,
                        (float) a.gunStrafeInnerRingFrac,
                        a.seeTimeToShoot);
        this.gunFireGoal =
                new MobGunRangedFireGoal<>(
                        this,
                        (float) a.gunRadius,
                        (float) a.gunMeleeFrac,
                        a.gunAttackIntervalMin,
                        a.seeTimeToShoot,
                        GunCombatSight.COMBAT);
        this.meleeGoal =
                new MeleeAttackGoal(this, a.meleeSpeed, false) {
                    @Override
                    public void stop() {
                        super.stop();
                        SlimeWarriorEntity.this.setAggressive(false);
                    }

                    @Override
                    public void start() {
                        super.start();
                        SlimeWarriorEntity.this.setAggressive(true);
                    }
                };
    }

    @Override
    protected void onGunEquipped() {
        this.mountCombatGoals();
        int interval = this.mobGunBurstIntervalTicks();
        if (this.gunGoal != null) {
            this.gunGoal.setMinAttackInterval(interval);
        }
        if (this.gunFireGoal != null) {
            this.gunFireGoal.setMinAttackInterval(interval);
        }
    }

    @Override
    protected void onGenerationStatsApplied(SplitSpec spec) {
        int interval = this.mobGunBurstIntervalTicks();
        if (this.gunGoal != null) {
            this.gunGoal.setMinAttackInterval(interval);
        }
        if (this.gunFireGoal != null) {
            this.gunFireGoal.setMinAttackInterval(interval);
        }
    }

    private void mountCombatGoals() {
        if (this.combatGoalsMounted
                || this.gunGoal == null
                || this.gunFireGoal == null
                || this.meleeGoal == null
                || this.level() == null
                || this.level().isClientSide()) {
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
        // 对齐列夫护卫 ShareTargetToOwner：小弟有仇、指挥官无仇时上送
        this.targetSelector.addGoal(4, new SlimeShareTargetToRootGoal(this));
        this.targetSelector.addGoal(
                5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, WitherBoss.class, false));
        this.targetSelector.addGoal(
                5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Warden.class, false));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.slimeWarriorCombatGoals;
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
        boolean naturalRoot = this.getParentUuid() == null && !this.isRootCommanderTree();
        spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
        if (!level.isClientSide() && naturalRoot && this.getParentUuid() == null) {
            // 自然刷 / 刷怪蛋：只出 2 级(gen1) / 3 级(gen2)，不出最高级 gen0
            int gen = this.getRandom().nextBoolean() ? 1 : 2;
            this.setRootUuid(this.getUUID());
            this.setRootIsCommander(false);
            this.applyGenerationStats(gen, true, true);
        }
        this.mountCombatGoals();
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
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 4, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 3, false, false));
        }
    }

    /**
     * 独立战士可远距离刷新；指挥官谱系永不刷（基类）。
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (this.isRootCommanderTree()) {
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    protected AbstractSlimeExperimentEntity createSplitInstance(ServerLevel server) {
        return ModEntities.SLIME_WARRIOR.get().create(server);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSlimeExperimentEntity.createBaseAttributes();
    }
}
