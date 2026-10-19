package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 护航主手装枪。换弹不再直接写满弹匣。 */
public final class CompanionBodyguardAmmoHelper {

    public static final ResourceLocation TACZ_AMMO_BOX_ITEM =
            ResourceLocation.fromNamespaceAndPath("tacz", "ammo_box");

    private CompanionBodyguardAmmoHelper() {}

    public static boolean equipCompanionMainHand(AbstractFriendEntity friend, ResourceLocation gunDataId) {
        if (friend.level().isClientSide()) {
            return false;
        }
        ItemStack gunStack = TaczGunMobHelper.createPlayerGunStack(gunDataId);
        if (gunStack.isEmpty()) {
            friend.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            return false;
        }
        friend.setItemSlot(EquipmentSlot.MAINHAND, gunStack);
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
