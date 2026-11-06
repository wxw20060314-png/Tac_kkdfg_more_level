package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunFindCoverRequestGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunSimThreadCoverStrafeMoveGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.google.common.collect.ImmutableMap;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.tacz.guns.api.item.attachment.AttachmentType;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
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

/**
 * 机械牛仔：枪套龙息喷，牛仔套，数值走 {@code bosses.toml} {@code [mechanicalCowboy]}（默认同超特）。
 * 趴下寻路、掩体请求、蹲跳与超特相同。常态防御显示 VII。语音见 {@code sounds/cowboy}。
 */
public final class MechanicalCowboyEntity extends AbstractHolsterGunSoldierEntity {

    public static final ResourceLocation HOLSTER_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "m1014_dragonbreath");

    /** 瞄具、三级霰弹扩容、收束器。扩容槽与弹药改装互斥。 */
    private static final Map<AttachmentType, ResourceLocation> HOLSTER_GUN_ATTACHMENTS =
            ImmutableMap.<AttachmentType, ResourceLocation>builder()
                    .put(AttachmentType.SCOPE, ResourceLocation.fromNamespaceAndPath("tacz", "sight_coyote"))
                    .put(
                            AttachmentType.EXTENDED_MAG,
                            ResourceLocation.fromNamespaceAndPath("tacz", "shotgun_extended_mag_3"))
                    .put(
                            AttachmentType.MUZZLE,
                            ResourceLocation.fromNamespaceAndPath("tacz", "muzzle_choke_sg"))
                    .build();

    /** 防御显示等级。{@code amplifier + 1}，伤害先除以 {@code 等级 + 1}。 */
    private static final int DEFENCE_LEVEL = 10;

    private final MobGunSimThreadCoverStrafeMoveGoal<MechanicalCowboyEntity> gunGoal =
            new MobGunSimThreadCoverStrafeMoveGoal<>(
                    this,
                    Config.mechanicalCowboyGunGoalSpeedModifier,
                    Config.mechanicalCowboyAttributes.gunAttackIntervalMin,
                    (float) Config.mechanicalCowboyAttributes.gunRadius,
                    (float) Config.mechanicalCowboyAttributes.gunMeleeFrac,
                    Config.mechanicalCowboyAttributes.seeTimeToStopMove,
                    (float) Config.mechanicalCowboyAttributes.gunStrafeOuterRingFrac,
                    (float) Config.mechanicalCowboyAttributes.gunStrafeInnerRingFrac,
                    Config.mechanicalCowboyAttributes.seeTimeToShoot);
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.mechanicalCowboyAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    MechanicalCowboyEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    MechanicalCowboyEntity.this.setAggressive(true);
                }
            };

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.mechanical_cowboy"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    public MechanicalCowboyEntity(EntityType<? extends Monster> type, Level level) {
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
    public boolean appliesSpawnArmorOnFinalize() {
        return false;
    }

    @Override
    public String spawnArmorPoolId() {
        return "mechanical_cowboy";
    }

    @Override
    protected String skinTextureFolder() {
        return "mechanical_cowboy";
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.mechanicalCowboyAttributes;
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
    }

    @Override
    public int getSimThreadUrgency() {
        return 60;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
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
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.mechanicalCowboyAttributes.gunAttackIntervalMin);
    }

    @Override
    protected void applyEquipmentDropChances() {
        float chance = Config.mechanicalCowboyEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, chance);
        this.setDropChance(EquipmentSlot.OFFHAND, chance);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, chance);
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(
                1, new SoldierHurtByTargetGoal(this, MechanicalCowboyEntity.class).setAlertOthers());
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
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.mechanicalCowboyCombatGoals;
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
        this.goalSelector.addGoal(
                2, new MobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.mechanicalCowboyMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.mechanicalCowboyAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.mechanicalCowboyAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.mechanicalCowboyAttributes.followRange);
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
        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        MobEffectInstance defence = this.getEffect(ModMobEffects.MOD_DEFFENCE.get());
        if (defence == null || defence.getAmplifier() != DEFENCE_LEVEL - 1) {
            this.addEffect(
                    new MobEffectInstance(
                            ModMobEffects.MOD_DEFFENCE.get(),
                            999999,
                            DEFENCE_LEVEL - 1,
                            false,
                            false,
                            true));
        }
    }

    @Override
    protected void initializeSpawnEquipment(RandomSource random) {
        super.initializeSpawnEquipment(random);
        this.setPreferredMainGunDataId(HOLSTER_GUN);
        this.setSoldierSkinIndex(0);
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DARK_COWBOY_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DARK_COWBOY_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DARK_COWBOY_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.DARK_COWBOY_BOOTS.get()));
        this.applyEquipmentDropChances();
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
        int pick = self.getRandom().nextInt(6);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_MECHANICAL_COWBOY_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_MECHANICAL_COWBOY_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_MECHANICAL_COWBOY_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_MECHANICAL_COWBOY_TARGET_4.get();
                    case 4 -> ModSounds.SOUND_MECHANICAL_COWBOY_TARGET_5.get();
                    default -> ModSounds.SOUND_MECHANICAL_COWBOY_TARGET_6.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int pick = this.random.nextInt(6);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_MECHANICAL_COWBOY_IDLE_1.get();
                case 1 -> ModSounds.SOUND_MECHANICAL_COWBOY_IDLE_2.get();
                case 2 -> ModSounds.SOUND_MECHANICAL_COWBOY_IDLE_3.get();
                case 3 -> ModSounds.SOUND_MECHANICAL_COWBOY_IDLE_4.get();
                case 4 -> ModSounds.SOUND_MECHANICAL_COWBOY_IDLE_5.get();
                default -> ModSounds.SOUND_MECHANICAL_COWBOY_IDLE_6.get();
            };
        }
        int pick = this.random.nextInt(6);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_MECHANICAL_COWBOY_COMBAT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_MECHANICAL_COWBOY_COMBAT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_MECHANICAL_COWBOY_COMBAT_IDLE_3.get();
            case 3 -> ModSounds.SOUND_MECHANICAL_COWBOY_COMBAT_IDLE_4.get();
            case 4 -> ModSounds.SOUND_MECHANICAL_COWBOY_COMBAT_IDLE_5.get();
            default -> ModSounds.SOUND_MECHANICAL_COWBOY_COMBAT_IDLE_6.get();
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(6);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_MECHANICAL_COWBOY_DEATH_1.get();
            case 1 -> ModSounds.SOUND_MECHANICAL_COWBOY_DEATH_2.get();
            case 2 -> ModSounds.SOUND_MECHANICAL_COWBOY_DEATH_3.get();
            case 3 -> ModSounds.SOUND_MECHANICAL_COWBOY_DEATH_4.get();
            case 4 -> ModSounds.SOUND_MECHANICAL_COWBOY_DEATH_5.get();
            default -> ModSounds.SOUND_MECHANICAL_COWBOY_DEATH_6.get();
        };
    }
}
