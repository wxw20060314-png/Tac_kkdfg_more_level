package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.armor.plugin.BlastSteelArmorPlateItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 各防弹套每件爆炸减伤来自 {@code armor.toml} {@code explosionReductionPerPiece}，按槽位相加，见 {@link
 * com.kkdfg.Tacz_morelevel.event.ExplosionArmorEvents}。已安装防爆钢板一并计入。
 */
public final class ExplosionArmorReduction {

    private ExplosionArmorReduction() {}

    public static float perPieceReduction(ItemStack stack) {
        float fromSet = 0f;
        if (stack.getItem() instanceof BlastSeventhBulletproofArmorItem) {
            fromSet = Config.blastExplosionReductionPerPiece;
        } else if (stack.getItem() instanceof BlazeflameSeventhBulletproofArmorItem) {
            fromSet = Config.blazeflameExplosionReductionPerPiece;
        } else if (stack.getItem() instanceof PowerMechBulletproofArmorItem) {
            fromSet = Config.powerMechExplosionReductionPerPiece;
        } else if (stack.getItem() instanceof SpecialOpsBulletproofArmorItem) {
            fromSet = Config.specialOpsExplosionReductionPerPiece;
        } else if (stack.getItem() instanceof ShockwaveBulletproofArmorItem) {
            fromSet = Config.shockwaveExplosionReductionPerPiece;
        }
        float fromPlate = BlastSteelArmorPlateItem.sumExplosionReduction(stack);
        return fromSet + fromPlate;
    }

    /** 四盔甲槽防爆减免之和，上限 1。 */
    public static float total(LivingEntity entity) {
        float sum = 0f;
        for (ItemStack stack : entity.getArmorSlots()) {
            sum += perPieceReduction(stack);
        }
        return Math.min(sum, 1f);
    }
}
