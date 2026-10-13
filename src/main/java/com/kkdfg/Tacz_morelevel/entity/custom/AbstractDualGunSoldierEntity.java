package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunCombatDualArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunIdleDualArrangeGoal;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 双枪士兵基类：主手为当前枪械，副手为背上枪械（由 TaCZ {@code HumanoidOffhandRender} 渲染）。
 * 默认 {@link #equipDualGunLoadout()} 从 {@link DualGunCatalog} JSON 池装配（不写偏好标记）；
 * Boss 子类 override 装配并 {@link #setPreferredMainGunDataId}/{@link #setPreferredOffhandGunDataId}。
 * <p>
 * 合规：{@link #hasPreferredPairInHands()}（顺序不论）/ {@link #isPreferredDualOrderReady()}（顺序也对）。
 * 闲置/战斗整理：{@link MobGunIdleDualArrangeGoal} / {@link MobGunCombatDualArrangeGoal}。
 * 射击 Goal 常驻；主/副武器靠各自 Goal 的 {@code canUse} 互斥。偏好标记与槽位无关；枪动态只在装备槽。
 */
public abstract class AbstractDualGunSoldierEntity extends AbstractPlayerLikeSoldierEntity {

    private static final String TAG_PREFERRED_OFFHAND_GUN_DATA_ID =
            "TaczMoreLevelPreferredOffhandGunDataId";

    /** 偏好副武器（背枪）标记；换枪不改。默认 null。 */
    @Nullable
    private ResourceLocation preferredOffhandGunDataId;

    /** 换枪改槽期间抑制 reassess。 */
    private boolean suppressWeaponReassess;

    private boolean dualArrangeGoalsMounted;

    protected AbstractDualGunSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /** 主手枪械池 id，见 {@link DualGunCatalog} 与 {@code data/tacz_morelevel/troop_guns/}。 */
    protected abstract String primaryGunPoolId();

    /** 背枪枪械池 id，见 {@link DualGunCatalog}。 */
    protected abstract String backGunPoolId();

    /** 服务端：装配主手 + 背枪；默认从 JSON 池随机，不写偏好标记。子类可 override 固定装配。 */
    protected void equipDualGunLoadout() {
        RandomSource rnd = this.getRandom();
        ResourceLocation primary = DualGunCatalog.pickRandom(this.primaryGunPoolId(), rnd);
        this.equipTaCZGunMainHand(primary);
        this.equipTaCZGunBack(DualGunCatalog.pickRandom(this.backGunPoolId(), rnd));
        TaczGunMobHelper.tickInfiniteMobAmmo(this);
    }

    public void setPreferredOffhandGunDataId(@Nullable ResourceLocation gunDataId) {
        this.preferredOffhandGunDataId = gunDataId;
    }

    @Nullable
    public ResourceLocation getPreferredOffhandGunDataId() {
        return this.preferredOffhandGunDataId;
    }

    public boolean hasBackGun() {
        return IGun.getIGunOrNull(this.getOffhandItem()) != null;
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

    /** 主手是否为偏好副武器（未设置偏好副时 false）。 */
    public boolean isHoldingPreferredOffhandGun() {
        if (this.preferredOffhandGunDataId == null) {
            return false;
        }
        IGun gun = IGun.getIGunOrNull(this.getMainHandItem());
        return gun != null
                && this.preferredOffhandGunDataId.equals(gun.getGunId(this.getMainHandItem()));
    }

    /**
     * 主副手正好是偏好主 + 偏好副（顺序不论）。偏好未设齐或任一手非对应枪 → false。
     * 战术切枪启用用此档。
     */
    public boolean hasPreferredPairInHands() {
        ResourceLocation preferredMain = this.getPreferredMainGunDataId();
        ResourceLocation preferredOff = this.preferredOffhandGunDataId;
        if (preferredMain == null || preferredOff == null) {
            return false;
        }
        ResourceLocation mainId = gunDataId(this.getMainHandItem());
        ResourceLocation offId = gunDataId(this.getOffhandItem());
        if (mainId == null || offId == null) {
            return false;
        }
        return (preferredMain.equals(mainId) && preferredOff.equals(offId))
                || (preferredOff.equals(mainId) && preferredMain.equals(offId));
    }

    /**
     * 主手=偏好主 且 副手=偏好副。偏好未设齐 → false。
     * 闲置顺序合规用此档。
     */
    public boolean isPreferredDualOrderReady() {
        ResourceLocation preferredMain = this.getPreferredMainGunDataId();
        ResourceLocation preferredOff = this.preferredOffhandGunDataId;
        if (preferredMain == null || preferredOff == null) {
            return false;
        }
        ResourceLocation mainId = gunDataId(this.getMainHandItem());
        ResourceLocation offId = gunDataId(this.getOffhandItem());
        return preferredMain.equals(mainId) && preferredOff.equals(offId);
    }

    @Nullable
    private static ResourceLocation gunDataId(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun == null ? null : gun.getGunId(stack);
    }

    /** 服务端：副手（背上）装备 TaCZ 枪。 */
    public void equipTaCZGunBack(ResourceLocation gunDataId) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack gunStack = TaczGunMobHelper.createGunStack(gunDataId);
        this.setItemSlot(EquipmentSlot.OFFHAND, gunStack);
    }

    /** 主手 ↔ 副手换枪，走 TaCZ {@link IGunOperator#draw}。不改偏好标记。 */
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
            op.draw(this::getMainHandItem);
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
        } finally {
            this.suppressWeaponReassess = false;
        }
        return true;
    }

    /** 若偏好主武器在背上，则换回主手。 */
    public boolean trySwapBackToPreferredMain() {
        ResourceLocation preferredMain = this.getPreferredMainGunDataId();
        if (preferredMain == null || this.isHoldingPreferredMainGun()) {
            return false;
        }
        IGun backGun = IGun.getIGunOrNull(this.getOffhandItem());
        if (backGun == null || !preferredMain.equals(backGun.getGunId(this.getOffhandItem()))) {
            return false;
        }
        return this.swapActiveAndBackGun();
    }

    @Override
    protected boolean mountsMainhandWeaponArrangeGoals() {
        return false;
    }

    /**
     * 挂载双枪闲置/战斗整理 Goal（priority 1）。战争机器等自行 {@link #markCombatGoalsMounted} 时也须调用。
     */
    protected final void mountDualWeaponArrangeGoals() {
        if (this.dualArrangeGoalsMounted
                || this.level() == null
                || this.level().isClientSide()) {
            return;
        }
        this.dualArrangeGoalsMounted = true;
        this.goalSelector.addGoal(1, new MobGunCombatDualArrangeGoal(this));
        this.goalSelector.addGoal(1, new MobGunIdleDualArrangeGoal(this));
    }

    @Override
    protected void mountPermanentCombatGoals() {
        boolean alreadyMounted = this.areCombatGoalsMounted();
        super.mountPermanentCombatGoals();
        if (!alreadyMounted && this.areCombatGoalsMounted()) {
            this.mountDualWeaponArrangeGoals();
        }
    }

    @Override
    public void reassessWeaponGoal() {
        if (this.suppressWeaponReassess) {
            return;
        }
        super.reassessWeaponGoal();
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
        if (compound.contains(TAG_PREFERRED_OFFHAND_GUN_DATA_ID, Tag.TAG_STRING)) {
            this.preferredOffhandGunDataId =
                    ResourceLocation.tryParse(compound.getString(TAG_PREFERRED_OFFHAND_GUN_DATA_ID));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.preferredOffhandGunDataId != null) {
            compound.putString(
                    TAG_PREFERRED_OFFHAND_GUN_DATA_ID, this.preferredOffhandGunDataId.toString());
        }
    }
}
