package com.kkdfg.Tacz_morelevel.blackmarket;

import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class BlackMarketInventoryHelper {

    private BlackMarketInventoryHelper() {}

    public static boolean hasItems(Inventory inventory, ItemStack required) {
        return countItems(inventory, required) >= required.getCount();
    }

    public static int countItems(Inventory inventory, ItemStack required) {
        if (required.isEmpty()) {
            return 0;
        }
        ResourceLocation requiredGunId = getGunId(required);
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (requiredGunId != null) {
                ResourceLocation gunId = getGunId(stack);
                if (requiredGunId.equals(gunId)) {
                    total += stack.getCount();
                }
                continue;
            }
            if (ItemStack.isSameItemSameTags(stack, required)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static boolean consumeItems(Inventory inventory, ItemStack required) {
        if (!hasItems(inventory, required)) {
            return false;
        }
        ResourceLocation requiredGunId = getGunId(required);
        int remaining = required.getCount();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            boolean matches;
            if (requiredGunId != null) {
                matches = requiredGunId.equals(getGunId(stack));
            } else {
                matches = ItemStack.isSameItemSameTags(stack, required);
            }
            if (!matches) {
                continue;
            }
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        return remaining <= 0;
    }

    private static ResourceLocation getGunId(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return null;
        }
        return gun.getGunId(stack);
    }
}
