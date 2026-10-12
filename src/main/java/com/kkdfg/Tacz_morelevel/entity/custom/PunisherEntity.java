package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunNearestAttackablePassthroughTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidGrenadeAndFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
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
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 判罚者 Boss：紫色，副手枪套 FN EVOLYS 传说（满配件）。
 * <p>
 * 「没 target」：不主动锁玩家 / 友军；仅保留对凋零、坚守者的索敌，以及受击反击
 * （挨打后 {@code setTarget} → 自动拔枪反击）。数值走 {@code bosses.toml} {@code [punisher]}；
 * 皮肤见 {@code textures/entity/punisher/skin_0..5.png}，生成时随机一套。
 * <p>
 * 装备 {@code ModItems.PUNISHER_BULLETPROOF_*} 全套，保护 IV + 耐久 III；
 * 头盔高级防闪、胸甲判罚者肩炮、腿高级防爆钢板、靴高级辐射抗板。
 */
public final class PunisherEntity extends AbstractHolsterGunSoldierEntity {

    /** 与 {@code textures/entity/punisher/skin_*.png} 数量一致。 */
    public static final int PUNISHER_SKIN_COUNT = 6;

    /** 副手枪套武器：FN EVOLYS 传说。 */
    public static final ResourceLocation HOLSTER_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "fn_evolys_legendary");

    private static ResourceLocation taczAttachment(String path) {
        return ResourceLocation.fromNamespaceAndPath("tacz", path);
    }

    /**
     * 满配件（同歼灭者，去掉激光：该枪 {@code allow_attachment_types} 无 laser 槽）。
     * sight_coyote / ammo_mod_fmj / grip_vertical_ranger / stock_sba3 / muzzle_compensator_trident。
     */
    private static final Map<AttachmentType, ResourceLocation> HOLSTER_GUN_ATTACHMENTS =
            ImmutableMap.<AttachmentType, ResourceLocation>builder()
                    .put(AttachmentType.SCOPE, taczAttachment("sight_coyote"))
                    .put(AttachmentType.EXTENDED_MAG, taczAttachment("ammo_mod_fmj"))
                    .put(AttachmentType.GRIP, taczAttachment("grip_vertical_ranger"))
                    .put(AttachmentType.STOCK, taczAttachment("stock_sba3"))
                    .put(AttachmentType.MUZZLE, taczAttachment("muzzle_compensator_trident"))
                    .build();

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.punisher"),
                    BossEvent.BossBarColor.PURPLE,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private final MobGunRangedPassthroughAttackGoal<PunisherEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.punisherGunGoalSpeedModifier,
                    Config.punisherAttributes.gunAttackIntervalMin,
                    (float) Config.punisherAttributes.gunRadius,
                    (float) Config.punisherAttributes.gunMeleeFrac,
                    Config.punisherAttributes.seeTimeToStopMove,
                    (float) Config.punisherAttributes.gunStrafeOuterRingFrac,
                    (float) Config.punisherAttributes.gunStrafeInnerRingFrac,
                    Config.punisherAttributes.seeTimeToShoot);
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.punisherAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    PunisherEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    PunisherEntity.this.setAggressive(true);
                }
            };

    public PunisherEntity(EntityType<? extends Monster> type, Level level) {
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
        return Config.punisherAttributes;
    }

    /** 不读 troop_armor 池，自行穿判罚者套。 */
    @Override
    public boolean appliesSpawnArmorOnFinalize() {
        return false;
    }

    @Override
    public String spawnArmorPoolId() {
        return "punisher";
    }

    @Override
    protected String skinTextureFolder() {
        return "punisher";
    }

    /** 判罚者皮肤集 6 套；取值时钳位到 {@link #PUNISHER_SKIN_COUNT}。 */
    @Override
    public int getSoldierSkinIndex() {
        return Mth.clamp(super.getSoldierSkinIndex(), 0, PUNISHER_SKIN_COUNT - 1);
    }

    @Override
    protected void setSoldierSkinIndex(int index) {
        super.setSoldierSkinIndex(Mth.clamp(index, 0, PUNISHER_SKIN_COUNT - 1));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return false;
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
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
        return Config.mapBossGunBurstInterval(Config.punisherAttributes.gunAttackIntervalMin);
    }

    @Override
    protected void applyEquipmentDropChances() {
        float chance = Config.punisherEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, chance);
        this.setDropChance(EquipmentSlot.OFFHAND, chance);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, chance);
            }
        }
    }

    /**
     * 索敌：凋零 + 坚守者 + 玩家（不挂友军 goal），外加避雷火 goal 与受击反击。
     * 不调用 {@code super.registerGoals()}，以免引入父类的友军索敌。
     */
    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(
                5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, WitherBoss.class, false));
        this.targetSelector.addGoal(
                5, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Warden.class, false));
        this.targetSelector.addGoal(
                2, new MobGunNearestAttackablePassthroughTargetGoal<>(this, Player.class, false));
        this.goalSelector.addGoal(1, new SoldierAvoidGrenadeAndFireGoal(this));
        this.targetSelector.addGoal(
                1, new SoldierHurtByTargetGoal(this, PunisherEntity.class).setAlertOthers());

        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        Config.CombatGoalStats g = Config.punisherCombatGoals;
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
                .add(Attributes.MAX_HEALTH, Config.punisherMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.punisherAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.punisherAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.punisherAttributes.followRange);
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
            this.setPreferredMainGunDataId(HOLSTER_GUN);
            // 皮肤：暂借超级特种兵集合，随机一套
            RandomSource rnd = level.getLevel().getRandom();
            this.setSoldierSkinIndex(rnd.nextInt(PUNISHER_SKIN_COUNT));
            this.equipPunisherArmor();
        }
        return spawnData;
    }

    /** 判罚者全套：保护 IV + 耐久 III；高级防闪 / 肩炮 / 腿高级防爆 / 靴高级辐射抗。 */
    private void equipPunisherArmor() {
        ItemStack helmet = enchantedPiece(ModItems.PUNISHER_BULLETPROOF_HELMET.get());
        ItemStack chest = enchantedPiece(ModItems.PUNISHER_BULLETPROOF_CHESTPLATE.get());
        ItemStack legs = enchantedPiece(ModItems.PUNISHER_BULLETPROOF_LEGGINGS.get());
        ItemStack boots = enchantedPiece(ModItems.PUNISHER_BULLETPROOF_BOOTS.get());
        BulletproofArmor.setPlugin(
                helmet, 0, new ItemStack(ModItems.FLASH_RESIST_GOGGLES_ADVANCED.get()));
        BulletproofArmor.setPlugin(
                chest, 0, new ItemStack(ModItems.PUNISHER_SHOULDER_CANNON_PLUGIN.get()));
        BulletproofArmor.setPlugin(
                legs, 0, new ItemStack(ModItems.BLAST_STEEL_ARMOR_PLATE_ADVANCED.get()));
        BulletproofArmor.setPlugin(
                boots, 0, new ItemStack(ModItems.RADIATION_RESIST_ARMOR_PLATE_ADVANCED.get()));
        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setItemSlot(EquipmentSlot.CHEST, chest);
        this.setItemSlot(EquipmentSlot.LEGS, legs);
        this.setItemSlot(EquipmentSlot.FEET, boots);
        this.applyEquipmentDropChances();
    }

    private static ItemStack enchantedPiece(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        stack.enchant(Enchantments.UNBREAKING, 3);
        return stack;
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

    // ---- 语音：源目录 D:\java\SOUND\判罚者 ----

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        int pick = self.getRandom().nextInt(6);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_PUNISHER_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_PUNISHER_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_PUNISHER_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_PUNISHER_TARGET_4.get();
                    case 4 -> ModSounds.SOUND_PUNISHER_TARGET_5.get();
                    default -> ModSounds.SOUND_PUNISHER_TARGET_6.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int pick = this.random.nextInt(6);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_PUNISHER_IDLE_1.get();
                case 1 -> ModSounds.SOUND_PUNISHER_IDLE_2.get();
                case 2 -> ModSounds.SOUND_PUNISHER_IDLE_3.get();
                case 3 -> ModSounds.SOUND_PUNISHER_IDLE_4.get();
                case 4 -> ModSounds.SOUND_PUNISHER_IDLE_5.get();
                default -> ModSounds.SOUND_PUNISHER_IDLE_6.get();
            };
        }
        int pick = this.random.nextInt(6);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_PUNISHER_COMBAT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_PUNISHER_COMBAT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_PUNISHER_COMBAT_IDLE_3.get();
            case 3 -> ModSounds.SOUND_PUNISHER_COMBAT_IDLE_4.get();
            case 4 -> ModSounds.SOUND_PUNISHER_COMBAT_IDLE_5.get();
            default -> ModSounds.SOUND_PUNISHER_COMBAT_IDLE_6.get();
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(7);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_PUNISHER_DEATH_1.get();
            case 1 -> ModSounds.SOUND_PUNISHER_DEATH_2.get();
            case 2 -> ModSounds.SOUND_PUNISHER_DEATH_3.get();
            case 3 -> ModSounds.SOUND_PUNISHER_DEATH_4.get();
            case 4 -> ModSounds.SOUND_PUNISHER_DEATH_5.get();
            case 5 -> ModSounds.SOUND_PUNISHER_DEATH_6.get();
            default -> ModSounds.SOUND_PUNISHER_DEATH_7.get();
        };
    }
}
