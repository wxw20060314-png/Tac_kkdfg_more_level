package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionInventory;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** {@link CompanionInventory} {@link net.minecraftforge.items.IItemHandler} */
public final class CompanionBodyguardAmmoHelper {

    private static final String TACZ_DUMMY_AMMO_TAG = "DummyAmmo";

    
    public static final ResourceLocation TACZ_AMMO_BOX_ITEM =
            ResourceLocation.fromNamespaceAndPath("tacz", "ammo_box");

    private CompanionBodyguardAmmoHelper() {}

    
    public static boolean hasReloadAmmoSupply(AbstractFriendlyCompanionEntity companion, ItemStack gunStack) {
        CompanionInventory inventory = companion.getCompanionInventory();
        return hasCreativeAmmoSupply(inventory, gunStack) || countAmmoInInventory(inventory, gunStack) > 0;
    }

    /** {@link IGun#useDummyAmmo} */
    public static void stripDummyAmmoTag(ItemStack gunStack) {
        if (gunStack == null || gunStack.isEmpty()) {
            return;
        }
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null || !gun.useDummyAmmo(gunStack)) {
            return;
        }
        if (gunStack.hasTag()) {
            gunStack.getTag().remove(TACZ_DUMMY_AMMO_TAG);
            if (gunStack.getTag().isEmpty()) {
                gunStack.setTag(null);
            }
        }
    }

    
    public static void applyCreativeMagazineTopUp(AbstractFriendlyCompanionEntity companion) {
        if (companion.level().isClientSide() || !IGun.mainHandHoldGun(companion)) {
            return;
        }
        ItemStack gunStack = companion.getMainHandItem();
        if (!hasCreativeAmmoSupply(companion.getCompanionInventory(), gunStack)) {
            return;
        }
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) {
            return;
        }
        var op = IGunOperator.fromLivingEntity(companion);
        if (op.getSynReloadState().getStateType().isReloading()) {
            return;
        }
        var indexOpt = TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack));
        if (indexOpt.isEmpty()) {
            return;
        }
        GunData gunData = indexOpt.get().getGunData();
        if (gunData.getReloadData().isInfinite()) {
            return;
        }
        int maxMag = Math.max(1, AttachmentDataUtils.getAmmoCountWithAttachment(gunStack, gunData));
        if (gun.getCurrentAmmoCount(gunStack) < maxMag) {
            gun.setCurrentAmmoCount(gunStack, maxMag);
        }
    }

    
    public static boolean hasCreativeAmmoSupply(CompanionInventory inventory, ItemStack gunStack) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof IAmmoBox box)) {
                continue;
            }
            if (!box.isCreative(stack) && !box.isAllTypeCreative(stack)) {
                continue;
            }
            if (box.isAllTypeCreative(stack) || box.isAmmoBoxOfGun(gunStack, stack)) {
                return true;
            }
        }
        return false;
    }

    /** {@link #hasCreativeAmmoSupply} */
    public static int countAmmoInInventory(CompanionInventory inventory, ItemStack gunStack) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            total += countAmmoInStack(inventory.getItem(slot), gunStack);
        }
        return total;
    }

    private static int countAmmoInStack(ItemStack stack, ItemStack gunStack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gunStack, stack)) {
            return stack.getCount();
        }
        if (stack.getItem() instanceof IAmmoBox box && box.isAmmoBoxOfGun(gunStack, stack)) {
            if (box.isCreative(stack) || box.isAllTypeCreative(stack)) {
                return 0;
            }
            return Math.max(0, box.getAmmoCount(stack));
        }
        return 0;
    }

    
    public static boolean equipCompanionMainHand(AbstractFriendEntity friend, ResourceLocation gunDataId) {
        if (friend.level().isClientSide()) {
            return false;
        }
        ItemStack gunStack = TaczGunMobHelper.createPlayerGunStack(gunDataId);
        if (gunStack.isEmpty()) {
            friend.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            return false;
        }
        stripDummyAmmoTag(gunStack);
        friend.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, gunStack);
        var op = com.tacz.guns.api.entity.IGunOperator.fromLivingEntity(friend);
        op.initialData();
        op.draw(friend::getMainHandItem);
        return true;
    }

    
    public static boolean isAmmoBoxItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof IAmmoBox) {
            return true;
        }
        Item item = BuiltInRegistries.ITEM.get(TACZ_AMMO_BOX_ITEM);
        return item != Items.AIR && stack.getItem() == item;
    }
}
