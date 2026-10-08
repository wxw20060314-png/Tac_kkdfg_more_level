package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 与 {@link AbstractDualGunSoldierEntity} 同级：副手枪套，有攻击目标时拔至主手。
 * <p>
 * 无存活目标连续 {@link #NO_TARGET_HOLSTER_TTL} tick 后收枪；拔/收枪时抑制主手 {@code setItemSlot}
 * 触发的反复 {@link #reassessWeaponGoal()}，并在拔枪时 {@code initialData}+{@code draw}，避免 Goal
 * 反复 stop/start 导致只跟随不射击。
 */
public abstract class AbstractHolsterGunSoldierEntity extends AbstractPlayerLikeSoldierEntity {

    /** 无存活目标达到该 tick 数后收枪。 */
    protected static final int NO_TARGET_HOLSTER_TTL = 20;

    /** 连续无存活目标的 tick 计数。 */
    private int noTargetTicks;
    /** 拔枪/收枪改槽期间为 true，阻止父类主手变更触发的 reassess。 */
    private boolean suppressWeaponReassess;

    protected AbstractHolsterGunSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /** 生成时写入枪套的枪械 ID（不读 troop_guns 池，由子类决定）。 */
    protected abstract ResourceLocation pickHolsterGunId(RandomSource random);

    /** 枪套配件；默认无。子类可覆盖（如对掏挂扩容弹匣）。 */
    @Nullable
    protected Map<AttachmentType, ResourceLocation> pickHolsterGunAttachments() {
        return null;
    }

    /** 服务端：主手清空，枪放入副手（枪套）；配件见 {@link #pickHolsterGunAttachments()}。 */
    public void equipHolsteredGun(ResourceLocation gunDataId) {
        this.equipHolsteredGun(gunDataId, this.pickHolsterGunAttachments());
    }

    /** 服务端：主手清空，枪（可带配件）放入副手（枪套）。 */
    public void equipHolsteredGun(
            ResourceLocation gunDataId, @Nullable Map<AttachmentType, ResourceLocation> attachments) {
        if (this.level().isClientSide()) {
            return;
        }
        this.suppressWeaponReassess = true;
        try {
            ItemStack gunStack =
                    attachments == null || attachments.isEmpty()
                            ? TaczGunMobHelper.createGunStack(gunDataId)
                            : TaczGunMobHelper.createGunStackWithAttachments(gunDataId, attachments);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setItemSlot(EquipmentSlot.OFFHAND, gunStack);
            this.persistedGunDataId = gunStack.isEmpty() ? null : gunDataId;
            this.noTargetTicks = 0;
        } finally {
            this.suppressWeaponReassess = false;
        }
        this.reassessWeaponGoal();
    }

    public boolean hasHolsteredGun() {
        return IGun.getIGunOrNull(this.getOffhandItem()) != null;
    }

    public boolean canDrawHolsteredGun() {
        if (this.level().isClientSide() || !this.hasHolsteredGun() || IGun.mainHandHoldGun(this)) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        return op.getSynDrawCoolDown() == 0L
                && !op.getSynIsBolting()
                && !op.getSynReloadState().getStateType().isReloading();
    }

    /** 副手 → 主手，对齐 {@link TaczGunMobHelper}：{@code initialData} + {@code draw}。 */
    public boolean drawHolsteredGun() {
        if (!this.canDrawHolsteredGun()) {
            return false;
        }
        this.suppressWeaponReassess = true;
        try {
            ItemStack holstered = this.getOffhandItem().copy();
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            this.setItemSlot(EquipmentSlot.MAINHAND, holstered);
            this.syncPersistedGunIdFromHands();
            IGunOperator op = IGunOperator.fromLivingEntity(this);
            op.initialData();
            op.draw(this::getMainHandItem);
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
            this.setAggressive(true);
            this.noTargetTicks = 0;
        } finally {
            this.suppressWeaponReassess = false;
        }
        this.reassessWeaponGoal();
        return true;
    }

    /** 主手 → 副手（枪套）。 */
    public boolean holsterGun() {
        if (this.level().isClientSide() || !IGun.mainHandHoldGun(this)) {
            return false;
        }
        this.suppressWeaponReassess = true;
        try {
            IGunOperator op = IGunOperator.fromLivingEntity(this);
            op.aim(false);
            op.cancelReload();
            ItemStack main = this.getMainHandItem().copy();
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (this.getOffhandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.OFFHAND, main);
            }
            this.syncPersistedGunIdFromHands();
            this.setAggressive(false);
            this.noTargetTicks = 0;
        } finally {
            this.suppressWeaponReassess = false;
        }
        this.reassessWeaponGoal();
        return true;
    }

    private void syncPersistedGunIdFromHands() {
        IGun main = IGun.getIGunOrNull(this.getMainHandItem());
        if (main != null) {
            this.persistedGunDataId = main.getGunId(this.getMainHandItem());
            return;
        }
        IGun off = IGun.getIGunOrNull(this.getOffhandItem());
        this.persistedGunDataId = off == null ? null : off.getGunId(this.getOffhandItem());
    }

    /**
     * 枪套：射击/近战由父类常驻挂载；此处仅在非抑制时刷新点射间隔。
     */
    @Override
    public void reassessWeaponGoal() {
        if (this.suppressWeaponReassess || this.level() == null || this.level().isClientSide()) {
            return;
        }
        super.reassessWeaponGoal();
    }

    @Override
    public boolean canUseUnarmedMeleeGoal() {
        return !IGun.mainHandHoldGun(this) && !this.hasHolsteredGun();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = this.getTarget();
        super.setTarget(target);
        if (this.level().isClientSide()) {
            return;
        }
        if (target != null && target.isAlive()) {
            this.noTargetTicks = 0;
            if (!IGun.mainHandHoldGun(this)) {
                if (!this.drawHolsteredGun()) {
                    this.reassessWeaponGoal();
                }
            } else {
                this.setAggressive(true);
                this.reassessWeaponGoal();
            }
        } else if (target == null && previous != null) {
            this.setAggressive(false);
            this.reassessWeaponGoal();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        LivingEntity target = this.getTarget();
        boolean hasAliveTarget = target != null && target.isAlive();
        if (hasAliveTarget) {
            this.noTargetTicks = 0;
            if (!IGun.mainHandHoldGun(this) && this.hasHolsteredGun()) {
                this.drawHolsteredGun();
            }
            return;
        }
        this.noTargetTicks++;
        if (this.noTargetTicks >= NO_TARGET_HOLSTER_TTL && IGun.mainHandHoldGun(this)) {
            this.holsterGun();
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
            this.equipHolsteredGun(this.pickHolsterGunId(level.getRandom()));
        }
        return spawnData;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        if (this.getTarget() == null && IGun.mainHandHoldGun(this) && this.getOffhandItem().isEmpty()) {
            this.holsterGun();
        } else if (this.getTarget() == null
                && !IGun.mainHandHoldGun(this)
                && this.persistedGunDataId != null
                && !this.hasHolsteredGun()) {
            this.equipHolsteredGun(this.persistedGunDataId);
        }
    }
}
