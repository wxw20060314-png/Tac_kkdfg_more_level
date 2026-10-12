package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedMoveGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierAvoidGrenadeAndFireGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierUnarmedMeleeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.war_machine.WarMachineFlyingGunMoveGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.war_machine.WarMachinePowerMechSkillGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.war_machine.WarMachineTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.war_machine.WarMachineWeaponSwapGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.DualGunCatalog;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.network.packet.PowerMechFlightSyncPacket;
import com.kkdfg.Tacz_morelevel.power_mech.PowerMechFlightEffects;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.tacz.guns.entity.EntityKineticBullet;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 战争机器：传说加特林主武器 + 卓越 RPG 背枪，固定动力机甲四件。
 * 贴图 {@code textures/entity/war_machine/skin_0.png}；Boss 血条；override {@link #equipDualGunLoadout()} 固定装配。
 * 死亡装备掉落概率见 common 配置 {@code [warMachineDeathDrops] equipmentDropChance}（默认 20%）。
 * 额外掉落：战利品表 {@code loot_tables/entities/war_machine.json}
 * 金色能量核心 1–2、金色动力推进器 3–5。
 * 免疫自身发射的 TaCZ 子弹/火箭爆炸伤害（见 {@link #hurt}）。
 */
public final class WarMachineEntity extends AbstractDualGunSoldierEntity {

    private static final EntityDataAccessor<Boolean> DATA_POWER_MECH_FLYING =
            SynchedEntityData.defineId(WarMachineEntity.class, EntityDataSerializers.BOOLEAN);

    /** 主手：传说 M134 转管机枪 */
    public static final ResourceLocation MAIN_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "minigun_legendary");
    /** 副手（背上）：卓越 RPG-7 */
    public static final ResourceLocation BACK_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "rpg7_legendary");

    public static final double FLY_GUN_MOVE_SPEED = 2D;

    /** 进入/维持飞行所需的最低胸甲能量比例。*/
    public static final float FLIGHT_ENERGY_THRESHOLD = 0.7f;
    /** 满能量且当前血量低于该值时触发过载，否则进入飞行。*/
    public static final float OVERLOAD_HEALTH_THRESHOLD = 5.0f;
    /** 战斗中有足够能量时，两次飞行判定之间的间隔（tick）。*/
    public static final int FLIGHT_ROLL_INTERVAL_TICKS = 20;
    /** 每次判定进入飞行的概率（0–1）。*/
    public static final double FLIGHT_ROLL_CHANCE = 0.3;
    /** 空中移动 {@link Attributes#FLYING_SPEED}。*/
    public static final double FLYING_SPEED = 0.9;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.war_machine"),
                    BossEvent.BossBarColor.YELLOW,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    private MobGunRangedMoveGoal<WarMachineEntity> gunGoal;
    private WarMachineFlyingGunMoveGoal flyingGunGoal;
    private MobGunRangedFireGoal<WarMachineEntity> minigunFireGoal;
    private MobGunRangedFireGoal<WarMachineEntity> rpgFireGoal;
    private MeleeAttackGoal meleeGoal;

    @Nullable
    private MoveControl groundMoveControl;
    private boolean serverWasFlying;

    public WarMachineEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.reassessWeaponGoal();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_POWER_MECH_FLYING, false);
    }

    public boolean isPowerMechFlying() {
        return this.entityData.get(DATA_POWER_MECH_FLYING);
    }

    public void setPowerMechFlying(boolean flying) {
        if (this.level().isClientSide() || this.isPowerMechFlying() == flying) {
            return;
        }
        this.entityData.set(DATA_POWER_MECH_FLYING, flying);
        if (flying) {
            this.groundMoveControl = this.moveControl;
            this.moveControl = new FlyingMoveControl(this, 10, true);
            this.setNoGravity(true);
        } else {
            if (this.groundMoveControl != null) {
                this.moveControl = this.groundMoveControl;
                this.groundMoveControl = null;
            }
            this.setNoGravity(false);
            // 关飞时砍掉向上冲量，避免落地瞬间「窜高」
            var velocity = this.getDeltaMovement();
            if (velocity.y > 0.0D) {
                this.setDeltaMovement(velocity.x, 0.0D, velocity.z);
            }
        }
        this.getNavigation().stop();
        this.syncFlightVisuals(flying);
    }

    private void syncFlightVisuals(boolean flying) {
        if (this.serverWasFlying == flying) {
            return;
        }
        this.serverWasFlying = flying;
        PowerMechFlightSyncPacket.sendToTracking(this, flying);
        if (flying) {
            PowerMechFlightEffects.startFlightSound(this);
        } else {
            PowerMechFlightEffects.stopFlightSound(this);
        }
    }

    /** 与炸弹宝宝同思路：射击/近战常驻，仅刷新点射间隔。 */
    @Override
    public void reassessWeaponGoal() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.mountPermanentCombatGoals();
        int interval = this.mobGunBurstIntervalTicks();
        if (this.gunGoal != null) {
            this.gunGoal.setMinAttackInterval(interval);
        }
        if (this.flyingGunGoal != null) {
            this.flyingGunGoal.setMinAttackInterval(interval);
        }
        if (this.minigunFireGoal != null) {
            this.minigunFireGoal.setMinAttackInterval(interval);
        }
        if (this.rpgFireGoal != null) {
            this.rpgFireGoal.setMinAttackInterval(interval);
        }
    }

    /** 战争机器在 {@link #registerGoals} 内挂载 Fire×2 + Move×2 + 近战，勿再走父类单枪挂载。 */
    @Override
    protected void mountPermanentCombatGoals() {
        if (this.areCombatGoalsMounted()) {
            return;
        }
        if (this.gunGoal != null
                && this.flyingGunGoal != null
                && this.minigunFireGoal != null
                && this.rpgFireGoal != null
                && this.meleeGoal != null) {
            this.markCombatGoalsMounted();
        }
    }

    @Override
    protected Goal createGunFireGoal() {
        return null;
    }

    @Override
    public boolean requiresPreferredMainGunForCombat() {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tacz_morelevel.war_machine");
    }

    @Override
    protected String primaryGunPoolId() {
        return DualGunCatalog.PRIMARY_POOL;
    }

    @Override
    protected String backGunPoolId() {
        return DualGunCatalog.BACK_POOL;
    }

    @Override
    protected void equipDualGunLoadout() {
        this.setSoldierSkinIndex(0);
        this.equipTaCZGunMainHand(MAIN_GUN);
        this.setPreferredMainGunDataId(MAIN_GUN);
        this.equipTaCZGunBack(BACK_GUN);
        this.setPreferredOffhandGunDataId(BACK_GUN);
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.POWER_MECH_BULLETPROOF_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.POWER_MECH_BULLETPROOF_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.POWER_MECH_BULLETPROOF_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.POWER_MECH_BULLETPROOF_BOOTS.get()));
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
    }

    @Override
    public boolean appliesSpawnArmorOnFinalize() {
        return false;
    }
    /** 基类抽象要求；战争机器不读 Config / 兵种 JSON，见 {@link #finalizeSpawn} 与 {@link #removeWhenFarAway(double)}。*/
    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.SUPER_SPECIAL;
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.warMachineAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "war_machine";
    }

    @Override
    protected String skinTextureFolder() {
        return "war_machine";
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return false;
    }

    @Override
    protected void applyEquipmentDropChances() {
        float p = Config.warMachineEquipmentDropChance;
        this.setDropChance(EquipmentSlot.MAINHAND, p);
        this.setDropChance(EquipmentSlot.OFFHAND, p);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, p);
            }
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
        this.setLeftHanded(false);
        if (!level.isClientSide()) {
            this.applyConfiguredLivingAttributes(true);
            this.setSoldierSkinIndex(0);
            this.equipDualGunLoadout();
        }
        return spawnData;
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.warMachineAttributes.gunAttackIntervalMin);
    }

    @Override
    protected MeleeAttackGoal meleeGoal() {
        return this.meleeGoal;
    }

    @Override
    protected void registerGoals() {

        this.meleeGoal =
                new MeleeAttackGoal(this, Config.warMachineAttributes.meleeSpeed, false) {
                    @Override
                    public void stop() {
                        super.stop();
                        WarMachineEntity.this.setAggressive(false);
                    }

                    @Override
                    public void start() {
                        super.start();
                        WarMachineEntity.this.setAggressive(true);
                    }
                };

        this.targetSelector.addGoal(5, new WarMachineTargetGoal<>(this, WitherBoss.class));
        this.targetSelector.addGoal(5, new WarMachineTargetGoal<>(this, Warden.class));
        this.targetSelector.addGoal(2, new WarMachineTargetGoal<>(this, Player.class));
        this.targetSelector.addGoal(3, new WarMachineTargetGoal<>(this, AbstractFriendEntity.class));
        this.targetSelector.addGoal(
                1, new WarMachineTargetGoal.HurtBy(this, WarMachineEntity.class).setAlertOthers());
        this.goalSelector.addGoal(1, new SoldierAvoidGrenadeAndFireGoal(this));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        Config.NpcAttributeStats attrs = Config.warMachineAttributes;
        this.minigunFireGoal =
                new MobGunRangedFireGoal<>(
                        this,
                        (float) attrs.gunRadius,
                        (float) attrs.gunMeleeFrac,
                        attrs.gunAttackIntervalMin,
                        attrs.seeTimeToShoot,
                        GunCombatSight.WAR_STRICT,
                        false,
                        this.requiresPreferredMainGunForCombat()
                                ? AbstractDualGunSoldierEntity::isHoldingPreferredMainGun
                                : null);
        this.rpgFireGoal =
                new MobGunRangedFireGoal<>(
                        this,
                        (float) attrs.gunRadius,
                        (float) attrs.gunMeleeFrac,
                        attrs.gunAttackIntervalMin,
                        WarMachineWeaponSwapGoal.RPG_SEE_TICKS_TO_SHOOT,
                        GunCombatSight.WAR_RPG,
                        true,
                        AbstractDualGunSoldierEntity::isHoldingPreferredOffhandGun);
        this.flyingGunGoal =
                new WarMachineFlyingGunMoveGoal(
                        this,
                        FLY_GUN_MOVE_SPEED,
                        attrs.gunAttackIntervalMin,
                        (float) attrs.gunRadius,
                        attrs.seeTimeToStopMove,
                        (float) attrs.gunStrafeOuterRingFrac,
                        (float) attrs.gunStrafeInnerRingFrac);
        this.gunGoal =
                new MobGunRangedMoveGoal<>(
                        this,
                        Config.warMachineGunGoalSpeedModifier,
                        attrs.gunAttackIntervalMin,
                        (float) attrs.gunRadius,
                        attrs.seeTimeToStopMove,
                        (float) attrs.gunStrafeOuterRingFrac,
                        (float) attrs.gunStrafeInnerRingFrac,
                        GunCombatSight.WAR_MOVE,
                        m -> !m.isPowerMechFlying());
        this.goalSelector.addGoal(1, new WarMachineWeaponSwapGoal(this));
        this.goalSelector.addGoal(2, this.minigunFireGoal);
        this.goalSelector.addGoal(2, this.rpgFireGoal);
        this.goalSelector.addGoal(2, this.flyingGunGoal);
        this.goalSelector.addGoal(3, this.gunGoal);
        this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, this.meleeGoal));
        this.markCombatGoalsMounted();
        this.goalSelector.addGoal(4, new WarMachinePowerMechSkillGoal(this));
        this.goalSelector.addGoal(5, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
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
        if (!this.level().isClientSide()) {
            LivingEntity combatTarget = this.getTarget();
            if (combatTarget != null && !combatTarget.isAlive()) {
                this.setTarget(null);
            }
        }
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 1, false, false));
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
        PowerMechFlightSyncPacket.sendToPlayer(player, this, this.isPowerMechFlying());
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
        this.bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.warMachineMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.warMachineAttributes.movementSpeed)
                .add(Attributes.FLYING_SPEED, FLYING_SPEED)
                .add(Attributes.ATTACK_DAMAGE, Config.warMachineAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.warMachineAttributes.followRange);
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        int pick = self.getRandom().nextInt(4);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_WAR_MACHINE_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_WAR_MACHINE_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_WAR_MACHINE_TARGET_3.get();
                    default -> ModSounds.SOUND_WAR_MACHINE_TARGET_4.get();
                };
        self.playSound(sound, 1.0F, 1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int pick = this.random.nextInt(4);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_WAR_MACHINE_IDLE_1.get();
                case 1 -> ModSounds.SOUND_WAR_MACHINE_IDLE_2.get();
                case 2 -> ModSounds.SOUND_WAR_MACHINE_IDLE_3.get();
                default -> ModSounds.SOUND_WAR_MACHINE_IDLE_4.get();
            };
        }
        int pick = this.random.nextInt(4);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_WAR_MACHINE_COMBAT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_WAR_MACHINE_COMBAT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_WAR_MACHINE_COMBAT_IDLE_3.get();
            default -> ModSounds.SOUND_WAR_MACHINE_COMBAT_IDLE_4.get();
        };
    }

    /**
     * 屏蔽自己打出的 TaCZ 子弹伤害（含 RPG 直击与爆炸：伤源多为子弹，射手在 {@link EntityKineticBullet#getOwner()}）。
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
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SOUND_WAR_MACHINE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        int pick = this.random.nextInt(4);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_WAR_MACHINE_DEATH_1.get();
            case 1 -> ModSounds.SOUND_WAR_MACHINE_DEATH_2.get();
            case 2 -> ModSounds.SOUND_WAR_MACHINE_DEATH_3.get();
            default -> ModSounds.SOUND_WAR_MACHINE_DEATH_4.get();
        };
    }
}
