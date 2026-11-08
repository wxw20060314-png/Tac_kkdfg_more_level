package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** 变种七级相同且最高，然后普通七级、六级，原版马甲按护甲点数。 */
final class HorseArmorRank {

    static final int VARIANT_SEVENTH = 400;

    private HorseArmorRank() {}

    static int of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        if (isVariantSeventh(item)) {
            return VARIANT_SEVENTH;
        }
        if (item instanceof BulletproofHorseArmorItem armor) {
            return 200 + armor.getLevel();
        }
        if (item instanceof HorseArmorItem vanilla) {
            return vanilla.getProtection();
        }
        return 0;
    }

    static boolean isVariantSeventh(Item item) {
        return item == ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_HORSE_ARMOR.get()
                || item == ModItems.SEVENTH_ABYSS_BULLETPROOF_HORSE_ARMOR.get()
                || item == ModItems.SEVENTH_SCULK_BULLETPROOF_HORSE_ARMOR.get()
                || item == ModItems.SEVENTH_RADIATION_BULLETPROOF_HORSE_ARMOR.get();
    }
}
