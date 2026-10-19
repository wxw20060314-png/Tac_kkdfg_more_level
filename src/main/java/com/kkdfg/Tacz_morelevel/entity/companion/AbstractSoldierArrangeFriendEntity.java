package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.entity.SoldierBackpack;
import com.kkdfg.Tacz_morelevel.entity.SoldierWeaponArrangeHost;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunCombatMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunIdleMainhandArrangeGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBox;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxHolder;
import com.kkdfg.Tacz_morelevel.integration.tacz.MobCreativeAmmoBoxReloadAccess;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 友方持枪整理：6 格士兵背包 + 主手 Idle/Combat Arrange。
 * 护航不走这条线。
 */
public abstract class AbstractSoldierArrangeFriendEntity extends AbstractFriendEntity
        implements SoldierWeaponArrangeHost, MobCreativeAmmoBoxHolder {

    private static final String TAG_PREFERRED_MAIN_GUN_DATA_ID = "TaczMoreLevelPreferredMainGunDataId";

    @Nullable
    private ResourceLocation preferredMainGunDataId;
    private final NonNullList<ItemStack> soldierBackpack = SoldierBackpack.createSlots();

    /** 一格全类型创造弹药箱，供 TaCZ {@code reload()} 扫描。 */
    private final NonNullList<ItemStack> creativeAmmoBox = MobCreativeAmmoBox.createSlot();

    private final MobCreativeAmmoBoxReloadAccess creativeAmmoReload =
            new MobCreativeAmmoBoxReloadAccess(this.creativeAmmoBox);

    protected AbstractSoldierArrangeFriendEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    protected void registerSoldierWeaponArrangeGoals() {
        this.goalSelector.addGoal(1, new MobGunCombatMainhandArrangeGoal(this));
        this.goalSelector.addGoal(1, new MobGunIdleMainhandArrangeGoal(this));
    }

    @Override
    protected void registerCompanionGunReloadGoal() {
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
    }

    @Override
    public NonNullList<ItemStack> mobCreativeAmmoBox() {
        return this.creativeAmmoBox;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> ammo = this.creativeAmmoReload.getCapability(cap, side, this.isAlive());
        return ammo != null ? ammo : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.creativeAmmoReload.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.creativeAmmoReload.revive();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            MobCreativeAmmoBox.ensure(this.creativeAmmoBox, this.tickCount);
        }
        super.tick();
    }

    public void setPreferredMainGunDataId(@Nullable ResourceLocation gunDataId) {
        this.preferredMainGunDataId = gunDataId;
    }

    @Override
    @Nullable
    public ResourceLocation getPreferredMainGunDataId() {
        return this.preferredMainGunDataId;
    }

    @Override
    public Mob asArrangeLiving() {
        return this;
    }

    @Override
    public NonNullList<ItemStack> soldierBackpackSlots() {
        return this.soldierBackpack;
    }

    @Override
    public boolean tryPutIntoSoldierBackpack(ItemStack stack) {
        if (this.level().isClientSide()) {
            return false;
        }
        return SoldierBackpack.tryPut(this.soldierBackpack, stack);
    }

    @Override
    public void swapHandWithSoldierBackpackSlot(EquipmentSlot hand, int index) {
        if (this.level().isClientSide()
                || index < 0
                || index >= this.soldierBackpack.size()) {
            return;
        }
        ItemStack bag = this.soldierBackpack.get(index);
        ItemStack held = this.getItemBySlot(hand);
        this.soldierBackpack.set(index, held.isEmpty() ? ItemStack.EMPTY : held.copy());
        this.setItemSlot(hand, bag);
    }

    @Override
    public void swapMainAndOffhand() {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack main = this.getMainHandItem().copy();
        ItemStack off = this.getOffhandItem().copy();
        this.setItemSlot(EquipmentSlot.MAINHAND, off);
        this.setItemSlot(EquipmentSlot.OFFHAND, main);
    }

    @Override
    public boolean canOperateWeaponSwap() {
        if (this.level().isClientSide()) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        return op.getSynDrawCoolDown() == 0L
                && !op.getSynIsBolting()
                && !op.getSynReloadState().getStateType().isReloading();
    }

    @Override
    public void afterCombatMainhandArrange(boolean hadMainGunBefore) {
        if (IGun.mainHandHoldGun(this)) {
            IGunOperator op = IGunOperator.fromLivingEntity(this);
            op.initialData();
            op.draw(this::getMainHandItem);
            TaczGunMobHelper.tickInfiniteMobAmmo(this);
            this.setAggressive(true);
            this.reassessWeaponGoal();
        } else if (hadMainGunBefore) {
            this.reassessWeaponGoal();
        }
    }

    @Override
    public void afterIdleMainhandArrange() {
        if (this.level().isClientSide()) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        op.aim(false);
        op.cancelReload();
        this.setAggressive(false);
        this.reassessWeaponGoal();
    }

    @Override
    public int noTargetWeaponArrangeTtl() {
        return AbstractPlayerLikeSoldierEntity.NO_TARGET_WEAPON_ARRANGE_TTL;
    }

    @Override
    public void compactSoldierInventory() {
        if (this.level().isClientSide()) {
            return;
        }
        SoldierBackpack.compact(this.soldierBackpack);
        this.mergeHandIntoBackpack(EquipmentSlot.MAINHAND);
        this.mergeHandIntoBackpack(EquipmentSlot.OFFHAND);
        this.mergeHandsTogether();
        SoldierBackpack.compact(this.soldierBackpack);
    }

    private void mergeHandIntoBackpack(EquipmentSlot hand) {
        ItemStack stack = this.getItemBySlot(hand);
        if (stack.isEmpty()) {
            return;
        }
        if (SoldierBackpack.tryMergeIntoExisting(this.soldierBackpack, stack) && stack.isEmpty()) {
            this.setItemSlot(hand, ItemStack.EMPTY);
        }
    }

    private void mergeHandsTogether() {
        ItemStack main = this.getMainHandItem();
        ItemStack off = this.getOffhandItem();
        if (main.isEmpty() || off.isEmpty() || !main.isStackable() || !ItemStack.isSameItemSameTags(main, off)) {
            return;
        }
        int move = Math.min(main.getCount(), off.getMaxStackSize() - off.getCount());
        if (move <= 0) {
            return;
        }
        off.grow(move);
        main.shrink(move);
        if (main.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.preferredMainGunDataId != null) {
            compound.putString(TAG_PREFERRED_MAIN_GUN_DATA_ID, this.preferredMainGunDataId.toString());
        }
        SoldierBackpack.save(compound, this.soldierBackpack);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_PREFERRED_MAIN_GUN_DATA_ID, Tag.TAG_STRING)) {
            this.preferredMainGunDataId =
                    ResourceLocation.tryParse(compound.getString(TAG_PREFERRED_MAIN_GUN_DATA_ID));
        }
        SoldierBackpack.load(compound, this.soldierBackpack);
    }
}
