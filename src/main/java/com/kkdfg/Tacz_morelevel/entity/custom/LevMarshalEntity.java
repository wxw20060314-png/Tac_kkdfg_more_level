package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
 * 列夫大帅：枪套士兵；小弟库（满 9）回补、三槽放出、链路保活、超距末影拉回。
 * 死亡装备掉落概率见 common 配置 {@code [levMarshalDeathDrops] equipmentDropChance}（默认 20%）。
 */
public final class LevMarshalEntity extends AbstractHolsterGunSoldierEntity {

    public static final ResourceLocation LEGENDARY_REVOLVER =
            ResourceLocation.fromNamespaceAndPath("tacz", "taurus943_legendary");

    private static final String TAG_MINION_SLOTS = "MarshalMinionSlots";
    private static final String TAG_SPAWN_COOLDOWN = "MarshalSpawnCooldown";
    private static final String TAG_MINION_RESERVE = "MarshalMinionReserve";
    private static final String TAG_RESERVE_TTL = "MarshalReserveTtl";

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.translatable("entity.tacz_morelevel.lev_marshal"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_10);

    @Nullable
    private final UUID[] minionSlots = new UUID[MarshalFactionHelper.MAX_MINION_SLOTS];
    private final int[] minionLinkTtls = new int[MarshalFactionHelper.MAX_MINION_SLOTS];
    private int spawnCooldown = MarshalFactionHelper.SPAWN_INTERVAL_TICKS;
    /** 可放出的护卫库存（不含已在场占槽）。出生满库。 */
    private int minionReserve = MarshalFactionHelper.MAX_MINION_RESERVE;
    private int reserveTtl;

    private final MobGunRangedPassthroughAttackGoal<LevMarshalEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.levMarshalGunGoalSpeedModifier,
                    Config.levMarshalAttributes.gunAttackIntervalMin,
                    (float) Config.levMarshalAttributes.gunRadius,
                    (float) Config.levMarshalAttributes.gunMeleeFrac,
                    Config.levMarshalAttributes.seeTimeToStopMove,
                    (float) Config.levMarshalAttributes.gunStrafeOuterRingFrac,
                    (float) Config.levMarshalAttributes.gunStrafeInnerRingFrac,
                    Config.levMarshalAttributes.seeTimeToShoot);
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.levMarshalAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    LevMarshalEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    LevMarshalEntity.this.setAggressive(true);
                }
            };

    public LevMarshalEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.reassessWeaponGoal();
    }

    @Override
    protected ResourceLocation pickHolsterGunId(RandomSource random) {
        return LEGENDARY_REVOLVER;
    }
    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.MERCENARY;
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.levMarshalAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "lev_marshal";
    }

    @Override
    public boolean appliesSpawnArmorOnFinalize() {
        return false;
    }

    @Override
    protected void applyEquipmentDropChances() {
        float p = Config.levMarshalEquipmentDropChance;
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
        return "lev_marshal";
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapBossGunBurstInterval(Config.levMarshalAttributes.gunAttackIntervalMin);
    }

    @Override
    protected MeleeAttackGoal meleeGoal() {
        return this.meleeGoal;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new SoldierHurtByTargetGoal(this, LevMarshalEntity.class));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        Config.CombatGoalStats jump = Config.levMarshalCombatGoals;
        this.goalSelector.addGoal(
                3, new MobRandomJumpGoal(this, jump.jumpChance, jump.jumpEnabled, jump.jumpMaxDistance));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.92D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 9.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.levMarshalMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.levMarshalAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.levMarshalAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.levMarshalAttributes.followRange);
    }

    private int firstEmptySlot() {
        for (int i = 0; i < this.minionSlots.length; i++) {
            if (this.minionSlots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private void clearSlot(int index) {
        this.minionSlots[index] = null;
        this.minionLinkTtls[index] = 0;
    }

    /** 名册槽内当前已加载且存活的己方护卫数（用于军官套传说技能）。*/
    public int countOnlineMinions() {
        if (!(this.level() instanceof ServerLevel server)) {
            return 0;
        }
        int n = 0;
        for (UUID id : this.minionSlots) {
            if (id == null) {
                continue;
            }
            Entity entity = server.getEntity(id);
            if (entity instanceof MarshalGuardEntity guard
                    && guard.isAlive()
                    && this.getUUID().equals(guard.getMarshalOwnerUUID())) {
                n++;
            }
        }
        return n;
    }

    /**
     * 双向会话是否仍连通：护卫认主为本大帅、护卫已加载解析到本实体，且名册有槽且槽 TTL &gt; 0。
     */
    public boolean isLinkedGuard(MarshalGuardEntity guard) {
        if (guard == null || !guard.isAlive() || this.level().isClientSide()) {
            return false;
        }
        if (!this.getUUID().equals(guard.getMarshalOwnerUUID())) {
            return false;
        }
        if (guard.getMarshalOwner() != this) {
            return false;
        }
        UUID id = guard.getUUID();
        for (int i = 0; i < this.minionSlots.length; i++) {
            if (id.equals(this.minionSlots[i]) && this.minionLinkTtls[i] > 0) {
                return true;
            }
        }
        return false;
    }

    /** 服务端：库回补、三槽链路、超距拉回、空槽从库放出。 */
    private void tickMarshalMinions(ServerLevel level) {
        double recallLimit = MarshalFactionHelper.recallDistanceLimit(this);
        double recallLimitSqr = recallLimit * recallLimit;

        for (int i = 0; i < this.minionSlots.length; i++) {
            UUID id = this.minionSlots[i];
            if (id == null) {
                continue;
            }
            Entity entity = level.getEntity(id);
            if (entity instanceof MarshalGuardEntity guard
                    && guard.isAlive()
                    && this.getUUID().equals(guard.getMarshalOwnerUUID())) {
                this.minionLinkTtls[i] = MarshalFactionHelper.LINK_TIMEOUT_TICKS;
                if (this.distanceToSqr(guard) > recallLimitSqr) {
                    MarshalFactionHelper.recallToMarshal(guard, this);
                    guard.resetLinkTimeout();
                }
            } else if (entity != null) {
                // 解析到但非存活己方护卫 → 立刻腾槽
                this.clearSlot(i);
            } else {
                this.minionLinkTtls[i]--;
                if (this.minionLinkTtls[i] <= 0) {
                    this.clearSlot(i);
                }
            }
        }

        this.tickMinionReserveRefill();

        if (this.spawnCooldown > 0) {
            this.spawnCooldown--;
        }
        if (this.spawnCooldown <= 0) {
            this.spawnCooldown = MarshalFactionHelper.SPAWN_INTERVAL_TICKS;
            int slot = this.firstEmptySlot();
            if (slot >= 0 && this.minionReserve > 0) {
                MarshalGuardEntity spawned = MarshalFactionHelper.spawnGuardNear(this);
                if (spawned != null) {
                    this.minionReserve--;
                    this.minionSlots[slot] = spawned.getUUID();
                    this.minionLinkTtls[slot] = MarshalFactionHelper.LINK_TIMEOUT_TICKS;
                    if (this.minionReserve < MarshalFactionHelper.MAX_MINION_RESERVE
                            && this.reserveTtl <= 0) {
                        this.reserveTtl = MarshalFactionHelper.RESERVE_REFILL_TTL_TICKS;
                    }
                }
            }
        }
    }

    /** 库未满时倒计时；耗尽则 +1 并重置 TTL。满库不倒计时。 */
    private void tickMinionReserveRefill() {
        if (this.minionReserve >= MarshalFactionHelper.MAX_MINION_RESERVE) {
            this.reserveTtl = 0;
            return;
        }
        if (this.reserveTtl <= 0) {
            this.reserveTtl = MarshalFactionHelper.RESERVE_REFILL_TTL_TICKS;
        }
        this.reserveTtl--;
        if (this.reserveTtl <= 0) {
            this.minionReserve++;
            if (this.minionReserve < MarshalFactionHelper.MAX_MINION_RESERVE) {
                this.reserveTtl = MarshalFactionHelper.RESERVE_REFILL_TTL_TICKS;
            } else {
                this.reserveTtl = 0;
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
        spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
        if (!level.isClientSide()) {
            this.setSoldierSkinIndex(0);
            this.applyOfficerUniform();
        }
        return spawnData;
    }

    private void applyOfficerUniform() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.OFFICER_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.OFFICER_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.OFFICER_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.OFFICER_BOOTS.get()));
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_HIGH_MID;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel server) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.tickMarshalMinions(server);
        }
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 0, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 1, false, false));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ListTag slots = new ListTag();
        for (int i = 0; i < this.minionSlots.length; i++) {
            CompoundTag entry = new CompoundTag();
            if (this.minionSlots[i] != null) {
                entry.putUUID("Id", this.minionSlots[i]);
                entry.putInt("Ttl", this.minionLinkTtls[i]);
            }
            slots.add(entry);
        }
        compound.put(TAG_MINION_SLOTS, slots);
        compound.putInt(TAG_SPAWN_COOLDOWN, this.spawnCooldown);
        compound.putInt(TAG_MINION_RESERVE, this.minionReserve);
        compound.putInt(TAG_RESERVE_TTL, this.reserveTtl);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        for (int i = 0; i < this.minionSlots.length; i++) {
            this.clearSlot(i);
        }
        if (compound.contains(TAG_MINION_SLOTS, Tag.TAG_LIST)) {
            ListTag slots = compound.getList(TAG_MINION_SLOTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(slots.size(), this.minionSlots.length); i++) {
                CompoundTag entry = slots.getCompound(i);
                if (entry.hasUUID("Id")) {
                    this.minionSlots[i] = entry.getUUID("Id");
                    this.minionLinkTtls[i] =
                            entry.contains("Ttl")
                                    ? entry.getInt("Ttl")
                                    : MarshalFactionHelper.LINK_TIMEOUT_TICKS;
                }
            }
        }
        if (compound.contains(TAG_SPAWN_COOLDOWN)) {
            this.spawnCooldown = compound.getInt(TAG_SPAWN_COOLDOWN);
        }
        if (compound.contains(TAG_MINION_RESERVE)) {
            this.minionReserve =
                    Mth.clamp(
                            compound.getInt(TAG_MINION_RESERVE),
                            0,
                            MarshalFactionHelper.MAX_MINION_RESERVE);
        }
        if (compound.contains(TAG_RESERVE_TTL)) {
            this.reserveTtl = Math.max(0, compound.getInt(TAG_RESERVE_TTL));
        } else if (this.minionReserve < MarshalFactionHelper.MAX_MINION_RESERVE) {
            this.reserveTtl = MarshalFactionHelper.RESERVE_REFILL_TTL_TICKS;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (MarshalFactionHelper.shouldBlockFactionDamage(this, source)) {
            return false;
        }
        return super.hurt(source, amount);
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
    public void remove(RemovalReason reason) {
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
                    case 0 -> ModSounds.SOUND_LEV_MARSHAL_TARGET_1.get();
                    case 1 -> ModSounds.SOUND_LEV_MARSHAL_TARGET_2.get();
                    case 2 -> ModSounds.SOUND_LEV_MARSHAL_TARGET_3.get();
                    case 3 -> ModSounds.SOUND_LEV_MARSHAL_TARGET_4.get();
                    case 4 -> ModSounds.SOUND_LEV_MARSHAL_TARGET_5.get();
                    default -> ModSounds.SOUND_LEV_MARSHAL_TARGET_6.get();
                };
        self.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int pick = this.random.nextInt(7);
            return switch (pick) {
                case 0 -> ModSounds.SOUND_LEV_MARSHAL_IDLE_1.get();
                case 1 -> ModSounds.SOUND_LEV_MARSHAL_IDLE_2.get();
                case 2 -> ModSounds.SOUND_LEV_MARSHAL_IDLE_3.get();
                case 3 -> ModSounds.SOUND_LEV_MARSHAL_IDLE_4.get();
                case 4 -> ModSounds.SOUND_LEV_MARSHAL_IDLE_5.get();
                case 5 -> ModSounds.SOUND_LEV_MARSHAL_IDLE_6.get();
                default -> ModSounds.SOUND_LEV_MARSHAL_IDLE_7.get();
            };
        }
        int pick = this.random.nextInt(7);
        return switch (pick) {
            case 0 -> ModSounds.SOUND_LEV_MARSHAL_COMBAT_IDLE_1.get();
            case 1 -> ModSounds.SOUND_LEV_MARSHAL_COMBAT_IDLE_2.get();
            case 2 -> ModSounds.SOUND_LEV_MARSHAL_COMBAT_IDLE_3.get();
            case 3 -> ModSounds.SOUND_LEV_MARSHAL_COMBAT_IDLE_4.get();
            case 4 -> ModSounds.SOUND_LEV_MARSHAL_COMBAT_IDLE_5.get();
            case 5 -> ModSounds.SOUND_LEV_MARSHAL_COMBAT_IDLE_6.get();
            default -> ModSounds.SOUND_LEV_MARSHAL_COMBAT_IDLE_7.get();
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.random.nextBoolean()
                ? ModSounds.SOUND_LEV_MARSHAL_DEATH_1.get()
                : ModSounds.SOUND_LEV_MARSHAL_DEATH_2.get();
    }
}
