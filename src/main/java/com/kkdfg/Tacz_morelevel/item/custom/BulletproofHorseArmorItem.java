package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * 防弹马甲。注册时传入防弹等级（1–8）。马只有这一件，数值用该档 Config 的整套总和，不再除以 4。
 * 原版护甲点数仍由 {@link HorseArmorItem#getProtection()} 提供。
 */
public class BulletproofHorseArmorItem extends HorseArmorItem {

    private final int level;
    private final ResourceLocation texture;

    /**
     * @param bulletproofLevel 1–8，与人型防弹甲同一套 {@link Config#bulletproofTierForLevel}
     * @param protection 原版马甲护甲点数（铁 5、金 7、钻 11）
     * @param texture 马上的盔甲层贴图
     */
    public BulletproofHorseArmorItem(
            int bulletproofLevel, int protection, ResourceLocation texture, Properties properties) {
        super(protection, "bulletproof", properties.stacksTo(1));
        this.level = Mth.clamp(bulletproofLevel, 1, 8);
        this.texture = texture;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public ResourceLocation getTexture() {
        return this.texture;
    }

    /** 当前 Config 下整套子弹减伤比例。 */
    public float bulletproofFromConfig() {
        return Config.bulletproofTierForLevel(this.level).totalBulletproof;
    }

    /** 当前 Config 下整套穿甲减免。 */
    public float penetrationReductionFromConfig() {
        return Config.bulletproofTierForLevel(this.level).totalPenReduction;
    }

    /** 是马且胸甲槽为本类时返回减伤；否则 0。 */
    public static float sumBulletproof(LivingEntity entity) {
        BulletproofHorseArmorItem armor = equipped(entity);
        return armor == null ? 0f : armor.bulletproofFromConfig();
    }

    /** 是马且胸甲槽为本类时返回穿甲减免；否则 0。 */
    public static float sumPenetrationReduction(LivingEntity entity) {
        BulletproofHorseArmorItem armor = equipped(entity);
        return armor == null ? 0f : armor.penetrationReductionFromConfig();
    }

    private static BulletproofHorseArmorItem equipped(LivingEntity entity) {
        if (!(entity instanceof Horse)) {
            return null;
        }
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof BulletproofHorseArmorItem armor) {
            return armor;
        }
        return null;
    }
}
