package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.integration.tacz.DualGunCatalog;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
 * 双枪士兵基类：主手为当前枪械，副手为背上枪械（由 TaCZ {@code HumanoidOffhandRender} 渲染）。
 * 默认 {@link #equipDualGunLoadout()} 从 {@link DualGunCatalog} JSON 池装配；子类可 override 写死装配。
 * 无存活目标连续 {@link #NO_TARGET_SWAP_PRIMARY_TTL} tick 后，若主手不是偏好主武器则换回（对标枪套收枪）。
 * <p>
 * 射击 Goal 常驻（父类 {@link #mountPermanentCombatGoals}）；MK14↔背枪换枪抑制 reassess，不改 Goal 列表，
 * 主/副武器靠各自 Goal 的 {@code canUse} 互斥。
 * 死亡装备掉落与 {@link AbstractPlayerLikeSoldierEntity} 相同（含主手/副手）；不参与
 * {@link com.kkdfg.Tacz_morelevel.integration.tacz.TaczCompatForgeEvents} 的弹匣子弹额外掉落。
 */
public abstract class AbstractDualGunSoldierEntity extends AbstractPlayerLikeSoldierEntity {

    private static final String TAG_BACK_GUN_DATA_ID = "TaczMoreLevelBackGunDataId";
    private static final String TAG_PREFERRED_PRIMARY_GUN_DATA_ID = "TaczMoreLevelPreferredPrimaryGunDataId";

    /** 无存活目标达到该 tick 数后切回偏好主武器。 */
    protected static final int NO_TARGET_SWAP_PRIMARY_TTL = 20;

    @Nullable
    private ResourceLocation persistedBackGunDataId;

    /** 装配时记下的主武器 id；换枪不改，用于无目标时切回。 */
    @Nullable
    private ResourceLocation preferredPrimaryGunDataId;

    private int noTargetTicks;

    /** 换枪改槽期间抑制 reassess。 */
    private boolean suppressWeaponReassess;

    protected AbstractDualGunSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /** 主手枪械池 id，见 {@link DualGunCatalog} 与 {@code data/tacz_morelevel/troop_guns/}。 */
    protected abstract String primaryGunPoolId();

    /** 背枪枪械池 id，见 {@link DualGunCatalog}。 */
    protected abstract String backGunPoolId();

    /** 服务端：装配主手 + 背枪；默认从 JSON 池随机，子类可 override 固定装配。 */
    protected void equipDualGunLoadout() {
        RandomSource rnd = this.getRandom();
        ResourceLocation primary = DualGunCatalog.pickRandom(this.primaryGunPoolId(), rnd);
        this.equipTaCZGunMainHand(primary);
        this.setPreferredPrimaryGunDataId(primary);
        this.equipTaCZGunBack(DualGunCatalog.pickRandom(this.backGunPoolId(), rnd));
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
    }

    /** 子类写死装配时调用，标记无目标时应回到的主武器。 */
    protected void setPreferredPrimaryGunDataId(@Nullable ResourceLocation gunDataId) {
        this.preferredPrimaryGunDataId = gunDataId;
    }

    @Nullable
    public ResourceLocation getPreferredPrimaryGunDataId() {
        return this.preferredPrimaryGunDataId;
    }

    public boolean hasBackGun() {
        return IGun.getIGunOrNull(this.getOffhandItem()) != null;
    }

    @Nullable
    public ResourceLocation getPersistedBackGunDataId() {
        return this.persistedBackGunDataId;
    }

    public boolean canSwapGun() {
        if (this.level().isClientSide() || !IGun.mainHandHoldGun(this) || !this.hasBackGun()) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        return op.getSynDrawCoolDown() == 0L
                && !op.getSynIsBolting()
                && !op.getSynReloadState().getStateType().isReloading();
    }

    /** 主手是否已是偏好主武器（未标记偏好时视为已在主武器态）。 */
    public boolean isHoldingPreferredPrimaryGun() {
        if (this.preferredPrimaryGunDataId == null) {
            return true;
        }
        IGun gun = IGun.getIGunOrNull(this.getMainHandItem());
        return gun != null && this.preferredPrimaryGunDataId.equals(gun.getGunId(this.getMainHandItem()));
    }

    /** 服务端：副手（背上）装备 TaCZ 枪。 */
    public void equipTaCZGunBack(ResourceLocation gunDataId) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack gunStack = TaczGunMobHelper.createGunStack(gunDataId);
        this.setItemSlot(EquipmentSlot.OFFHAND, gunStack);
        if (gunStack.isEmpty()) {
            this.persistedBackGunDataId = null;
        } else {
            this.persistedBackGunDataId = gunDataId;
        }
    }

    /** 主手 ↔ 副手换枪，走 TaCZ {@link IGunOperator#draw}。换枪不改射击 Goal 列表。 */
    public boolean swapActiveAndBackGun() {
        if (!this.canSwapGun()) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        op.aim(false);
        op.cancelReload();
        this.suppressWeaponReassess = true;
        try {
            ItemStack main = this.getMainHandItem().copy();
            ItemStack back = this.getOffhandItem().copy();
            this.setItemSlot(EquipmentSlot.MAINHAND, back);
            this.setItemSlot(EquipmentSlot.OFFHAND, main);
            this.syncPersistedGunIdsFromEquipment();
            op.draw(this::getMainHandItem);
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
        } finally {
            this.suppressWeaponReassess = false;
        }
        return true;
    }

    /** 若偏好主武器在背上，则换回主手。 */
    public boolean trySwapBackToPreferredPrimary() {
        if (this.preferredPrimaryGunDataId == null || this.isHoldingPreferredPrimaryGun()) {
            return false;
        }
        IGun backGun = IGun.getIGunOrNull(this.getOffhandItem());
        if (backGun == null
                || !this.preferredPrimaryGunDataId.equals(backGun.getGunId(this.getOffhandItem()))) {
            return false;
        }
        return this.swapActiveAndBackGun();
    }

    private void syncPersistedGunIdsFromEquipment() {
        IGun mainGun = IGun.getIGunOrNull(this.getMainHandItem());
        IGun backGun = IGun.getIGunOrNull(this.getOffhandItem());
        this.persistedGunDataId = mainGun == null ? null : mainGun.getGunId(this.getMainHandItem());
        this.persistedBackGunDataId = backGun == null ? null : backGun.getGunId(this.getOffhandItem());
    }

    @Override
    public void reassessWeaponGoal() {
        if (this.suppressWeaponReassess) {
            return;
        }
        super.reassessWeaponGoal();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.noTargetTicks = 0;
            return;
        }
        this.noTargetTicks++;
        if (this.noTargetTicks >= NO_TARGET_SWAP_PRIMARY_TTL && !this.isHoldingPreferredPrimaryGun()) {
            this.trySwapBackToPreferredPrimary();
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
            this.equipDualGunLoadout();
        }
        return spawnData;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_PREFERRED_PRIMARY_GUN_DATA_ID, Tag.TAG_STRING)) {
            this.preferredPrimaryGunDataId =
                    ResourceLocation.tryParse(compound.getString(TAG_PREFERRED_PRIMARY_GUN_DATA_ID));
        }
        if (compound.contains(TAG_BACK_GUN_DATA_ID, Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(compound.getString(TAG_BACK_GUN_DATA_ID));
            Level level = this.level();
            if (id != null && level != null && !level.isClientSide()) {
                this.equipTaCZGunBack(id);
            }
        }
        if (this.preferredPrimaryGunDataId == null && this.persistedGunDataId != null) {
            this.preferredPrimaryGunDataId = this.persistedGunDataId;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.persistedBackGunDataId != null) {
            compound.putString(TAG_BACK_GUN_DATA_ID, this.persistedBackGunDataId.toString());
        }
        if (this.preferredPrimaryGunDataId != null) {
            compound.putString(
                    TAG_PREFERRED_PRIMARY_GUN_DATA_ID, this.preferredPrimaryGunDataId.toString());
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (this.level() != null && !this.level().isClientSide()) {
            if (slot == EquipmentSlot.OFFHAND) {
                IGun gun = IGun.getIGunOrNull(stack);
                this.persistedBackGunDataId = gun == null ? null : gun.getGunId(stack);
            }
        }
    }
}
