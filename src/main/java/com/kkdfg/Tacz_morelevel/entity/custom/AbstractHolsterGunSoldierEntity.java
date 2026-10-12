package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunCombatDrawGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunIdleHolsterGoal;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 与 {@link AbstractDualGunSoldierEntity} 同级：副手枪套。
 * 闲置/战斗武器整理分别由 {@link MobGunIdleHolsterGoal}、{@link MobGunCombatDrawGoal} 负责；
 * 本类只提供换槽 / TaCZ draw；背包见父类 {@link AbstractPlayerLikeSoldierEntity}。
 */
public abstract class AbstractHolsterGunSoldierEntity extends AbstractPlayerLikeSoldierEntity {

    /** 拔枪/收枪改槽期间为 true，阻止父类主手变更触发的 reassess。 */
    private boolean suppressWeaponReassess;

    protected AbstractHolsterGunSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /** 生成时写入枪套的枪械 ID。偏好主由子类自行 {@link #setPreferredMainGunDataId}。 */
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
        this.runWithWeaponSlotMute(
                () -> {
                    ItemStack gunStack =
                            attachments == null || attachments.isEmpty()
                                    ? TaczGunMobHelper.createGunStack(gunDataId)
                                    : TaczGunMobHelper.createGunStackWithAttachments(
                                            gunDataId, attachments);
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    this.setItemSlot(EquipmentSlot.OFFHAND, gunStack);
                });
        this.reassessWeaponGoal();
    }

    public boolean hasHolsteredGun() {
        return IGun.getIGunOrNull(this.getOffhandItem()) != null;
    }

    /** TaCZ 不在切枪/拉栓/换弹。 */
    public boolean canOperateWeaponSwap() {
        if (this.level().isClientSide()) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        return op.getSynDrawCoolDown() == 0L
                && !op.getSynIsBolting()
                && !op.getSynReloadState().getStateType().isReloading();
    }

    /** 主手 ↔ 副手交换（抑制 reassess）。 */
    public void swapMainAndOffhand() {
        if (this.level().isClientSide()) {
            return;
        }
        this.runWithWeaponSlotMute(
                () -> {
                    ItemStack main = this.getMainHandItem().copy();
                    ItemStack off = this.getOffhandItem().copy();
                    this.setItemSlot(EquipmentSlot.MAINHAND, off);
                    this.setItemSlot(EquipmentSlot.OFFHAND, main);
                });
    }

    /** 战斗：主手枪 {@code initialData}+{@code draw}，并开战意。 */
    public void applyCombatDrawEffects() {
        if (this.level().isClientSide() || !IGun.mainHandHoldGun(this)) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        op.initialData();
        op.draw(this::getMainHandItem);
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
        this.setAggressive(true);
        this.reassessWeaponGoal();
    }

    /** 闲置：关瞄准/换弹，并关战意。 */
    public void applyIdleHolsterEffects() {
        if (this.level().isClientSide()) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        op.aim(false);
        op.cancelReload();
        this.setAggressive(false);
        this.reassessWeaponGoal();
    }

    private void runWithWeaponSlotMute(Runnable action) {
        this.suppressWeaponReassess = true;
        try {
            action.run();
        } finally {
            this.suppressWeaponReassess = false;
        }
    }

    @Override
    public void reassessWeaponGoal() {
        if (this.suppressWeaponReassess || this.level() == null || this.level().isClientSide()) {
            return;
        }
        super.reassessWeaponGoal();
    }

    @Override
    protected void mountPermanentCombatGoals() {
        boolean alreadyMounted = this.areCombatGoalsMounted();
        super.mountPermanentCombatGoals();
        if (!alreadyMounted
                && this.areCombatGoalsMounted()
                && this.level() != null
                && !this.level().isClientSide()) {
            this.goalSelector.addGoal(1, new MobGunCombatDrawGoal(this));
            this.goalSelector.addGoal(1, new MobGunIdleHolsterGoal(this));
        }
    }

    @Override
    public boolean canUseUnarmedMeleeGoal() {
        return !IGun.mainHandHoldGun(this) && !this.hasHolsteredGun();
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
        // 仅姿态：主手有枪、副手空 → 收到副手；不补枪
        if (this.getTarget() == null
                && IGun.mainHandHoldGun(this)
                && this.getOffhandItem().isEmpty()) {
            this.swapMainAndOffhand();
            this.applyIdleHolsterEffects();
        }
    }
}
