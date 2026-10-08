//package com.kkdfg.Tacz_morelevel.dategen;
//
//import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
//import com.kkdfg.Tacz_morelevel.entity.ModEntities;
//import com.kkdfg.Tacz_morelevel.item.ModItems;
//import net.minecraft.data.PackOutput;
//import net.minecraftforge.common.data.LanguageProvider;
//
///** 仅生成本模组 {@code tacz_morelevel} 的语言；覆盖 TaCZ 枪械译名见 {@code assets/tacz_morelevel/lang/zh_cn.json}（{@code tacz.gun.*}）。 */
//public class ModZhCNLangProvider extends LanguageProvider {
//
//    public ModZhCNLangProvider(PackOutput output) {
//        super(output, Tacz_MoreLevel.MOD_ID, "zh_cn");
//    }
//
//    @Override
//    protected void addTranslations() {
//        add(ModEntities.MILITIA.get(), "民兵");
//        add(ModEntities.MERCENARY.get(), "雇佣兵");
//        add(ModEntities.ASSAULT_SOLDIER.get(), "突击兵");
//        add(ModEntities.SPECIAL_FORCES.get(), "特种兵");
//        add(ModEntities.SUPER_SPECIAL_SOLDIER.get(), "超级特种兵");
//        add(ModItems.MILITIA_SPAWN_EGG.get(), "民兵刷怪蛋");
//        add(ModItems.MERCENARY_SPAWN_EGG.get(), "雇佣兵刷怪蛋");
//        add(ModItems.ASSAULT_SOLDIER_SPAWN_EGG.get(), "突击兵刷怪蛋");
//        add(ModItems.SPECIAL_FORCES_SPAWN_EGG.get(), "特种兵刷怪蛋");
//        add(ModItems.SUPER_SPECIAL_SOLDIER_SPAWN_EGG.get(), "超级特种兵刷怪蛋");
//        add("itemGroup.tacz_morelevel.main", "TaCZ 更多等级");
//        add(ModItems.FIRST_BULLETPROOF_HELMET.get(), "I 级防弹头盔");
//        add(ModItems.FIRST_BULLETPROOF_CHESTPLATE.get(), "I 级防弹胸甲");
//        add(ModItems.FIRST_BULLETPROOF_LEGGINGS.get(), "I 级防弹护腿");
//        add(ModItems.FIRST_BULLETPROOF_BOOTS.get(), "I 级防弹靴");
//        add(ModItems.SECOND_BULLETPROOF_HELMET.get(), "II 级防弹头盔");
//        add(ModItems.SECOND_BULLETPROOF_CHESTPLATE.get(), "II 级防弹胸甲");
//        add(ModItems.SECOND_BULLETPROOF_LEGGINGS.get(), "II 级防弹护腿");
//        add(ModItems.SECOND_BULLETPROOF_BOOTS.get(), "II 级防弹靴");
//        add(ModItems.THIRD_BULLETPROOF_HELMET.get(), "III 级防弹头盔");
//        add(ModItems.THIRD_BULLETPROOF_CHESTPLATE.get(), "III 级防弹胸甲");
//        add(ModItems.THIRD_BULLETPROOF_LEGGINGS.get(), "III 级防弹护腿");
//        add(ModItems.THIRD_BULLETPROOF_BOOTS.get(), "III 级防弹靴");
//        add(ModItems.FOURTH_BULLETPROOF_HELMET.get(), "IV 级防弹头盔");
//        add(ModItems.FOURTH_BULLETPROOF_CHESTPLATE.get(), "IV 级防弹胸甲");
//        add(ModItems.FOURTH_BULLETPROOF_LEGGINGS.get(), "IV 级防弹护腿");
//        add(ModItems.FOURTH_BULLETPROOF_BOOTS.get(), "IV 级防弹靴");
//        add(ModItems.FIFTH_BULLETPROOF_HELMET.get(), "V 级防弹头盔");
//        add(ModItems.FIFTH_BULLETPROOF_CHESTPLATE.get(), "V 级防弹胸甲");
//        add(ModItems.FIFTH_BULLETPROOF_LEGGINGS.get(), "V 级防弹护腿");
//        add(ModItems.FIFTH_BULLETPROOF_BOOTS.get(), "V 级防弹靴");
//        add(ModItems.SIXTH_BULLETPROOF_HELMET.get(), "VI 级防弹头盔");
//        add(ModItems.SIXTH_BULLETPROOF_CHESTPLATE.get(), "VI 级防弹胸甲");
//        add(ModItems.SIXTH_BULLETPROOF_LEGGINGS.get(), "VI 级防弹护腿");
//        add(ModItems.SIXTH_BULLETPROOF_BOOTS.get(), "VI 级防弹靴");
//        add(ModItems.SEVENTH_BULLETPROOF_HELMET.get(), "VII 级防弹头盔");
//        add(ModItems.SEVENTH_BULLETPROOF_CHESTPLATE.get(), "VII 级防弹胸甲");
//        add(ModItems.SEVENTH_BULLETPROOF_LEGGINGS.get(), "VII 级防弹护腿");
//        add(ModItems.SEVENTH_BULLETPROOF_BOOTS.get(), "VII 级防弹靴");
//    }
//}
