package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill;
import com.kkdfg.Tacz_morelevel.effect.ShockwaveImpactEffect;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.bomb_sight.BombSightDashGoal;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.google.common.collect.ImmutableMap;
import com.tacz.guns.api.item.attachment.AttachmentType;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
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
import net.minecraftforge.registries.RegistryObject;

/**
 * 炸弹视野 Boss：枪套 {@code tacz:aa12_shockwave} + 冲击波防弹全套；战斗 AI 同超级特种兵。
 * 贴图 {@code textures/entity/bomb_sight/skin_0.png}；血量 / 抗性等级 / 装备掉落见 {@code bosses.toml} {@code [bombSight]}。
 * 额外掉落：战利品表 {@code loot_tables/entities/bomb_sight.json}
 * 红色能量核心 1–2、红色动力推进器 3–5。
 */
public final class BombSightEntity extends AbstractHolsterGunSoldierEntity {

    public static final ResourceLocation HOLSTER_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "aa12_shockwave");

    private static ResourceLocation taczAttachment(String path) {
        return ResourceLocation.fromNamespaceAndPath("tacz", path);
    }

    /** 固定配件：Coyote 红点、全金属 FMJ、三叉戟补偿器（同歼灭者枪口）。 */
    private static final Map<AttachmentType, ResourceLocation> HOLSTER_GUN_ATTACHMENTS =
            ImmutableMap.<AttachmentType, ResourceLocation>builder()
                    .put(AttachmentType.SCOPE, taczAttachment("sight_coyote"))
                    .put(AttachmentType.EXTENDED_MAG, taczAttachment("ammo_mod_fmj"))
                    .put(AttachmentType.MUZZLE, taczAttachment("muzzle_compensator_trident"))
                    .build();

    /** 丢目标后仍保持头盔夜视的 tick 数。 */
    private static final int NIGHT_VISION_OFF_TTL_TICKS = 60;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.bomb_sight"),
                    BossEvent.BossBarColor.BLUE,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private final MobGunRangedPassthroughAttackGoal<BombSightEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.bombSightGunGoalSpeedModifier,
                    Config.bombSightAttributes.gunAttackIntervalMin,
                    (float) Config.bombSightAttributes.gunRadius,
                    (float) Config.bombSightAttributes.gunMeleeFrac,
                    Config.bombSightAttributes.seeTimeToStopMove,
                    (float) Config.bombSightAttributes.gunStrafeOuterRingFrac,
                    (float) Config.bombSightAttributes.gunStrafeInnerRingFrac,
                    Config.bombSightAttributes.seeTimeToShoot);
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.bombSightAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    BombSightEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    BombSightEntity.this.setAggressive(true);
                }
            };

    /** 无目标关夜视倒计时。 */
    private int nightVisionOffTtl;

    public BombSightEntity(EntityType<? extends Monster> type, Level level) {
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
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.SUPER_SPECIAL;
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.bombSightAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "bomb_sight";
    }

    @Override
    public boolean appliesSpawnArmorOnFinalize() {
        return false;
    }

    @Override
    protected void applyEquipmentDropChances() {
        float p = Config.bombSightEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, p);
        this.setDropChance(EquipmentSlot.OFFHAND, p);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, p);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return false;
    }

    @Override
    protected String skinTextureFolder() {
        return "bomb_sight";
    }

    @Override
    public int getSoldierSkinIndex() {
        return 0;
    }

    @Override
    protected void setSoldierSkinIndex(int index) {
        super.setSoldierSkinIndex(0);
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.bombSightAttributes.gunAttackIntervalMin);
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
        spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
        if (!level.isClientSide()) {
            this.setSoldierSkinIndex(0);
            this.equipShockwaveLoadout();
        }
        return spawnData;
    }

    private void equipShockwaveLoadout() {
        ItemStack helmet = new ItemStack(ModItems.SHOCKWAVE_BULLETPROOF_HELMET.get());
        // 夜视由 tickTargetNightVision：有目标才开，丢目标后 TTL 再关
        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SHOCKWAVE_BULLETPROOF_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.SHOCKWAVE_BULLETPROOF_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.SHOCKWAVE_BULLETPROOF_BOOTS.get()));
        this.applyEquipmentDropChances();
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new SoldierHurtByTargetGoal(this, BombSightEntity.class).setAlertOthers());
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BombSightDashGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.bombSightCombatGoals;
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
        this.goalSelector.addGoal(2, new MobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.bombSightMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.bombSightAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.bombSightAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.bombSightAttributes.followRange);
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
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 1, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 4, false, false));
        }
        int resistanceAmp = Config.bombSightResistanceAmplifier;
        MobEffectInstance resistance = this.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance == null || resistance.getAmplifier() != resistanceAmp) {
            this.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE, 999999, resistanceAmp, false, false));
        }
        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
            this.tickTargetNightVision();
            // NPC：回能与冲击判定（玩家走 ShockwaveEnergyEvents，勿混用 tickCharge 自动释放）
            if (ShockwaveDashSkill.hasFullSet(this)) {
                ShockwaveDashSkill.tickEnergyRegen(this);
                ShockwaveImpactEffect.tickImpact(this);
            }
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
                ModSounds.SOUND_BOMB_SIGHT_IDLE_1,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_2,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_3,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_4,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_5,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_6,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_7,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_8,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_9,
                ModSounds.SOUND_BOMB_SIGHT_IDLE_10
            };

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SoundEvent>[] COMBAT_IDLE_VOICES =
            new RegistryObject[] {
                ModSounds.SOUND_BOMB_SIGHT_COMBAT_IDLE_1,
                ModSounds.SOUND_BOMB_SIGHT_COMBAT_IDLE_2,
                ModSounds.SOUND_BOMB_SIGHT_COMBAT_IDLE_3,
                ModSounds.SOUND_BOMB_SIGHT_COMBAT_IDLE_4,
                ModSounds.SOUND_BOMB_SIGHT_COMBAT_IDLE_5,
                ModSounds.SOUND_BOMB_SIGHT_COMBAT_IDLE_6
            };

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SoundEvent>[] TARGET_VOICES =
            new RegistryObject[] {
                ModSounds.SOUND_BOMB_SIGHT_TARGET_1,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_2,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_3,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_4,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_5,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_6,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_7,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_8,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_9,
                ModSounds.SOUND_BOMB_SIGHT_TARGET_10
            };

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SoundEvent>[] DEATH_VOICES =
            new RegistryObject[] {
                ModSounds.SOUND_BOMB_SIGHT_DEATH_1,
                ModSounds.SOUND_BOMB_SIGHT_DEATH_2,
                ModSounds.SOUND_BOMB_SIGHT_DEATH_3,
                ModSounds.SOUND_BOMB_SIGHT_DEATH_4,
                ModSounds.SOUND_BOMB_SIGHT_DEATH_5
            };

    private static SoundEvent pickVoice(
            RegistryObject<SoundEvent>[] voices, RandomSource random) {
        return voices[random.nextInt(voices.length)].get();
    }
}
