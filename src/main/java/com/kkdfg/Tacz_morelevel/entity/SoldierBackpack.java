package com.kkdfg.Tacz_morelevel.entity;

import com.tacz.guns.api.item.IGun;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 士兵 / 史莱姆共用的固定格背包操作（存取、与装备槽交换、NBT）。
 */
public final class SoldierBackpack {

    public static final int SLOTS = 6;
    public static final String TAG = "TaczMoreLevelSoldierBackpack";

    private SoldierBackpack() {}

    public static NonNullList<ItemStack> createSlots() {
        return NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    }

    /** 放入空格（copy）；满则 false。调用方自行清空来源槽。 */
    public static boolean tryPut(NonNullList<ItemStack> slots, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).isEmpty()) {
                slots.set(i, stack.copy());
                return true;
            }
        }
        return false;
    }

    /**
     * 从背包取一把枪到 {@code dest}：优先匹配 {@code preferredOrNull}，为 null 则任意枪。
     * 与 {@code dest} 当前物品交换（空则仅取出）。
     */
    public static boolean tryTakeGun(
            NonNullList<ItemStack> slots,
            LivingEntity mob,
            @Nullable ResourceLocation preferredOrNull,
            EquipmentSlot dest) {
        if (mob.level().isClientSide()) {
            return false;
        }
        int idx = findGunIndex(slots, preferredOrNull);
        if (idx < 0) {
            return false;
        }
        ItemStack fromPack = slots.get(idx);
        ItemStack destStack = mob.getItemBySlot(dest);
        slots.set(idx, destStack.isEmpty() ? ItemStack.EMPTY : destStack.copy());
        mob.setItemSlot(dest, fromPack);
        return true;
    }

    public static void save(CompoundTag compound, NonNullList<ItemStack> slots) {
        CompoundTag bag = new CompoundTag();
        ContainerHelper.saveAllItems(bag, slots, true);
        compound.put(TAG, bag);
    }

    public static void load(CompoundTag compound, NonNullList<ItemStack> slots) {
        for (int i = 0; i < slots.size(); i++) {
            slots.set(i, ItemStack.EMPTY);
        }
        if (!compound.contains(TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        ContainerHelper.loadAllItems(compound.getCompound(TAG), slots);
    }

    private static int findGunIndex(
            NonNullList<ItemStack> slots, @Nullable ResourceLocation preferredOrNull) {
        int anyGun = -1;
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i);
            IGun gun = IGun.getIGunOrNull(stack);
            if (gun == null) {
                continue;
            }
            if (preferredOrNull != null && preferredOrNull.equals(gun.getGunId(stack))) {
                return i;
            }
            if (anyGun < 0) {
                anyGun = i;
            }
        }
        return preferredOrNull == null ? anyGun : -1;
    }
}
