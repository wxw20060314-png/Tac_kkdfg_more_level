package com.kkdfg.Tacz_morelevel.item;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.item.custom.*;
import com.kkdfg.Tacz_morelevel.armor.plugin.BallisticSteelArmorPlateItem;
import com.kkdfg.Tacz_morelevel.armor.plugin.BlastSteelArmorPlateItem;
import com.kkdfg.Tacz_morelevel.armor.plugin.CarbonFiberArmorPlateItem;
import com.kkdfg.Tacz_morelevel.armor.plugin.FlashResistGogglesItem;
import com.kkdfg.Tacz_morelevel.armor.plugin.RadiationResistArmorPlateItem;
import com.kkdfg.Tacz_morelevel.armor.plugin.shoulder_cannon.PunisherShoulderCannonPluginItem;
import com.kkdfg.Tacz_morelevel.item.medicine.ChannelMedicineItem;
import com.kkdfg.Tacz_morelevel.item.medicine.HealSpec;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineConsumeMode;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineEffectSpec;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineSounds;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineSpec;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import java.util.List;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.world.item.Items.APPLE;
import static net.minecraft.world.item.Items.GLASS_BOTTLE;

/**
 * 模组物品注册。
 *
 * <p>编码约定见仓库根目录 {@code Tacz_morelevel规范手册.txt}。
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Tacz_MoreLevel.MOD_ID);

    /** 刷怪蛋底色均为红色系，斑点略深以便辨认；等级越高底色越深（民兵→超级特种兵）。 */
    public static final RegistryObject<Item> MILITIA_SPAWN_EGG =
            ITEMS.register(
                    "militia_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.MILITIA,
                                    0xFF6B6B,
                                    0xCC3333,
                                    new Item.Properties()));

    public static final RegistryObject<Item> REFUGEE_SPAWN_EGG =
            ITEMS.register(
                    "refugee_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.REFUGEE,
                                    0xE8C4A8,
                                    0xA67C52,
                                    new Item.Properties()));

    public static final RegistryObject<Item> MERCENARY_SPAWN_EGG =
            ITEMS.register(
                    "mercenary_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.MERCENARY, 0xE85555, 0xB82222, new Item.Properties()));

    public static final RegistryObject<Item> ASSAULT_SOLDIER_SPAWN_EGG =
            ITEMS.register(
                    "assault_soldier_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.ASSAULT_SOLDIER, 0xCC4444, 0x991111, new Item.Properties()));

    /** 潜伏者刷怪蛋：青灰底 / 深斑。 */
    public static final RegistryObject<Item> LURKER_SPAWN_EGG =
            ITEMS.register(
                    "lurker_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.LURKER,
                                    0x5A7A8A,
                                    0x1E2A30,
                                    new Item.Properties()));

    public static final RegistryObject<Item> SPECIAL_FORCES_SPAWN_EGG =
            ITEMS.register(
                    "special_forces_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.SPECIAL_FORCES, 0xAA2222, 0x770808, new Item.Properties()));

    public static final RegistryObject<Item> SUPER_SPECIAL_SOLDIER_SPAWN_EGG =
            ITEMS.register(
                    "super_special_soldier_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.SUPER_SPECIAL_SOLDIER, 0x771111, 0x440404, new Item.Properties()));

    /** 列夫大帅：深红刷怪蛋。 */
    public static final RegistryObject<Item> LEV_MARSHAL_SPAWN_EGG =
            ITEMS.register(
                    "lev_marshal_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.LEV_MARSHAL, 0x8B0000, 0xFFD700, new Item.Properties()));

    public static final RegistryObject<Item> MYSTERIOUS_MERCHANT_SPAWN_EGG =
            ITEMS.register(
                    "mysterious_merchant_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.MYSTERIOUS_MERCHANT,
                                    0x2B2B3A,
                                    0x6B5B95,
                                    new Item.Properties()));

    /** 战争机器：金色刷怪蛋。 */
    public static final RegistryObject<Item> WAR_MACHINE_SPAWN_EGG =
            ITEMS.register(
                    "war_machine_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.WAR_MACHINE, 0xFFAA00, 0x884400, new Item.Properties()));

    /** 歼灭者：深灰刷怪蛋（与 HK-416D 歼灭者 §8 配色一致）。 */
    public static final RegistryObject<Item> ANNIHILATOR_SPAWN_EGG =
            ITEMS.register(
                    "annihilator_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.ANNIHILATOR, 0x555555, 0x333333, new Item.Properties()));

    /** 毒蝎：灰刷怪蛋（与潜行七级 §7 配色一致）。 */
    public static final RegistryObject<Item> SCORPION_SPAWN_EGG =
            ITEMS.register(
                    "scorpion_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.SCORPION, 0x7A7A7A, 0x2E2E2E, new Item.Properties()));

    /** 史莱姆指挥官：深绿刷怪蛋。 */
    public static final RegistryObject<Item> SLIME_COMMANDER_SPAWN_EGG =
            ITEMS.register(
                    "slime_commander_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.SLIME_COMMANDER,
                                    0x006400,
                                    0x003300,
                                    new Item.Properties()));

    /** 史莱姆战士：亮绿刷怪蛋。 */
    public static final RegistryObject<Item> SLIME_WARRIOR_SPAWN_EGG =
            ITEMS.register(
                    "slime_warrior_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.SLIME_WARRIOR, 0x32CD32, 0x006400, new Item.Properties()));

    /** 史莱姆指挥官成就图标（不进创造栏）。 */
    public static final RegistryObject<Item> SLIME_COMMANDER_FACE =
            ITEMS.register("slime_commander_face", () -> new Item(new Item.Properties()));

    /** 史莱姆血清：普通材料物品。 */
    public static final RegistryObject<Item> SLIME_SERUM =
            ITEMS.register("slime_serum", () -> new Item(new Item.Properties()));

    /** 炸弹宝宝：暗红刷怪蛋。 */
    public static final RegistryObject<Item> BOMB_BABY_SPAWN_EGG =
            ITEMS.register(
                    "bomb_baby_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.BOMB_BABY, 0x8B0000, 0x2A0000, new Item.Properties()));

    /** 对掏天花板：红色刷怪蛋。 */
    public static final RegistryObject<Item> CEILING_DUEL_SPAWN_EGG =
            ITEMS.register(
                    "ceiling_duel_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.CEILING_DUEL, 0xC41E3A, 0x8B0000, new Item.Properties()));

    /** 炸弹视野：天蓝刷怪蛋。 */
    public static final RegistryObject<Item> BOMB_SIGHT_SPAWN_EGG =
            ITEMS.register(
                    "bomb_sight_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.BOMB_SIGHT, 0x1E90FF, 0x0A3D62, new Item.Properties()));

    /** 判罚者：紫色刷怪蛋（底色 / 斑点，与判罚者 §d 配色一致）。 */
    public static final RegistryObject<Item> PUNISHER_SPAWN_EGG =
            ITEMS.register(
                    "punisher_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.PUNISHER, 0xB24BF3, 0x3A1B52, new Item.Properties()));

    /** 美女护航：粉色刷怪蛋（底色 / 斑点）。 */
    public static final RegistryObject<Item> BEAUTY_BODYGUARD_SPAWN_EGG =
            ITEMS.register(
                    "beauty_bodyguard_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.BEAUTY_BODYGUARD, 0xFFB6D9, 0xE75480, new Item.Properties()));

    /** 标准护航：浅蓝。 */
    public static final RegistryObject<Item> STANDARD_BODYGUARD_SPAWN_EGG =
            ITEMS.register(
                    "standard_bodyguard_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.STANDARD_BODYGUARD, 0x7EC8FF, 0x3D85C6, new Item.Properties()));

    /** 高级护航：中蓝（比标准更深）。 */
    public static final RegistryObject<Item> ELITE_BODYGUARD_SPAWN_EGG =
            ITEMS.register(
                    "elite_bodyguard_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.ELITE_BODYGUARD, 0x4A9FE8, 0x1E5FA8, new Item.Properties()));

    /** 魔王护航：深蓝。 */
    public static final RegistryObject<Item> DEMON_LORD_BODYGUARD_SPAWN_EGG =
            ITEMS.register(
                    "demon_lord_bodyguard_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.DEMON_LORD_BODYGUARD, 0x1E4A7A, 0x0B1F3D, new Item.Properties()));

    /** 护卫：军绿刷怪蛋。 */
    public static final RegistryObject<Item> GUARD_SPAWN_EGG =
            ITEMS.register(
                    "guard_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.GUARD, 0x4A5D23, 0x2F3A18, new Item.Properties()));

    public static final RegistryObject<Item> GRENADE =
            ITEMS.register("grenade", () -> new GrenadeItem(new Item.Properties()));
    public static final RegistryObject<Item> FLASHBANG =
            ITEMS.register("flashbang", () -> new FlashbangItem(new Item.Properties()));
    public static final RegistryObject<Item> MOLOTOV =
            ITEMS.register("molotov", () -> new MolotovItem(new Item.Properties()));
    public static final RegistryObject<Item> DETECTION_GRENADE =
            ITEMS.register(
                    "detection_grenade",
                    () -> new DetectionGrenadeItem(new Item.Properties()));

    public static final RegistryObject<Item> LANDMINE =
            ITEMS.register("landmine", () -> new LandmineBlockItem(new Item.Properties()));

    public static final RegistryObject<Item> FROZEN_LANDMINE =
            ITEMS.register("frozen_landmine", () -> new FrozenLandmineBlockItem(new Item.Properties()));

    public static final RegistryObject<Item> SIGNAL_SUMMONING_BLOCK =
            ITEMS.register(
                    "signal_summoning_block",
                    () -> new SignalSummoningBlockItem(new Item.Properties()));

    public static final RegistryObject<Item> BLACK_MARKET_CONTACT =
            ITEMS.register(
                    "black_market_contact",
                    () -> new BlackMarketContactBlockItem(new Item.Properties()));

    public static final RegistryObject<Item> LIMITED_SPAWNER =
            ITEMS.register(
                    "limited_spawner",
                    () -> new BlockItem(ModBlocks.LIMITED_SPAWNER.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXPLOSION_PROOF_STEEL_BLOCK =
            ITEMS.register(
                    "explosion_proof_steel_block",
                    () ->
                            new BlockItem(
                                    ModBlocks.EXPLOSION_PROOF_STEEL_BLOCK.get(),
                                    new Item.Properties()));

    public static final RegistryObject<Item> PLUGIN_WORKBENCH =
            ITEMS.register(
                    "plugin_workbench",
                    () -> new PluginWorkbenchBlockItem(new Item.Properties()));

    public static final RegistryObject<Item> BODYGUARD_STORAGE =
            ITEMS.register("bodyguard_storage", () -> new BodyguardStorageItem(new Item.Properties()));

    public static final RegistryObject<Item> INFINITE_NETWORK_RECEIVER =
            ITEMS.register(
                    "infinite_network_receiver",
                    () -> new InfiniteNetworkReceiverItem(new Item.Properties()));

    public static final RegistryObject<Item> MONITOR =
            ITEMS.register("monitor", () -> new MonitorItem(new Item.Properties()));

    public static final RegistryObject<Item> COMPUTER_CASE =
            ITEMS.register("computer_case", () -> new ComputerCaseItem(new Item.Properties()));

    /** 枪械升级凭证：improved 绿 / refined 蓝 / perfect 紫 / excellent 红 */
    public static final RegistryObject<Item> IMPROVED_GUN_UPGRADE_VOUCHER =
            ITEMS.register(
                    "improved_gun_upgrade_voucher", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REFINED_GUN_UPGRADE_VOUCHER =
            ITEMS.register(
                    "refined_gun_upgrade_voucher", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PERFECT_GUN_UPGRADE_VOUCHER =
            ITEMS.register("perfect_gun_upgrade_voucher", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EXCELLENT_GUN_UPGRADE_VOUCHER =
            ITEMS.register(
                    "excellent_gun_upgrade_voucher", () -> new Item(new Item.Properties()));

    public static final List<RegistryObject<Item>> ALL_GUN_UPGRADE_VOUCHERS =
            List.of(
                    IMPROVED_GUN_UPGRADE_VOUCHER,
                    REFINED_GUN_UPGRADE_VOUCHER,
                    PERFECT_GUN_UPGRADE_VOUCHER,
                    EXCELLENT_GUN_UPGRADE_VOUCHER);

    /** 黑色通用货币（八进制位）：1 位→2 位→3 位→4 位，8 张低一级合成 1 张高一级。 */
    public static final RegistryObject<Item> BLACK_UNIVERSAL_CURRENCY_DIGIT_1 =
            ITEMS.register(
                    "black_universal_currency_digit_1", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLACK_UNIVERSAL_CURRENCY_DIGIT_2 =
            ITEMS.register(
                    "black_universal_currency_digit_2", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLACK_UNIVERSAL_CURRENCY_DIGIT_3 =
            ITEMS.register(
                    "black_universal_currency_digit_3", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLACK_UNIVERSAL_CURRENCY_DIGIT_4 =
            ITEMS.register(
                    "black_universal_currency_digit_4", () -> new Item(new Item.Properties()));

    public static final List<RegistryObject<Item>> ALL_BLACK_UNIVERSAL_CURRENCIES =
            List.of(
                    BLACK_UNIVERSAL_CURRENCY_DIGIT_1,
                    BLACK_UNIVERSAL_CURRENCY_DIGIT_2,
                    BLACK_UNIVERSAL_CURRENCY_DIGIT_3,
                    BLACK_UNIVERSAL_CURRENCY_DIGIT_4);

    /** 六级钢锭（创造栏展示名见语言文件 §5 紫色） */
    public static final RegistryObject<Item> SIXTH_STEEL_INGOT =
            ITEMS.register("sixth_steel_ingot", () -> new Item(new Item.Properties()));

    /** 七级钢锭（创造栏展示名见语言文件 §c 红色） */
    public static final RegistryObject<Item> SEVENTH_STEEL_INGOT =
            ITEMS.register("seventh_steel_ingot", () -> new Item(new Item.Properties()));

    /** 防爆钢锭 */
    public static final RegistryObject<Item> EXPLOSION_PROOF_STEEL_INGOT =
            ITEMS.register("explosion_proof_steel_ingot", () -> new Item(new Item.Properties()));

    /** 碳钎维护板（深渊轴）：初 / 中 / 高 */
    public static final RegistryObject<Item> CARBON_FIBER_ARMOR_PLATE_JUNIOR =
            ITEMS.register(
                    "carbon_fiber_armor_plate_junior",
                    () ->
                            new CarbonFiberArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.1f, 0.032f, 0.1f));
    public static final RegistryObject<Item> CARBON_FIBER_ARMOR_PLATE_MEDIUM =
            ITEMS.register(
                    "carbon_fiber_armor_plate_medium",
                    () ->
                            new CarbonFiberArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.175f, 0.056f, 0.175f));
    public static final RegistryObject<Item> CARBON_FIBER_ARMOR_PLATE_ADVANCED =
            ITEMS.register(
                    "carbon_fiber_armor_plate_advanced",
                    () ->
                            new CarbonFiberArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.25f, 0.08f, 0.25f));

    /** 防弹钢板（幽匿差）：初 / 中 / 高 */
    public static final RegistryObject<Item> BALLISTIC_STEEL_ARMOR_PLATE_JUNIOR =
            ITEMS.register(
                    "ballistic_steel_armor_plate_junior",
                    () ->
                            new BallisticSteelArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.003f, 0.008f));
    public static final RegistryObject<Item> BALLISTIC_STEEL_ARMOR_PLATE_MEDIUM =
            ITEMS.register(
                    "ballistic_steel_armor_plate_medium",
                    () ->
                            new BallisticSteelArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.00525f, 0.014f));
    public static final RegistryObject<Item> BALLISTIC_STEEL_ARMOR_PLATE_ADVANCED =
            ITEMS.register(
                    "ballistic_steel_armor_plate_advanced",
                    () ->
                            new BallisticSteelArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.0075f, 0.02f));

    /** 防爆钢板（爆炎轴）：初 / 中 / 高 */
    public static final RegistryObject<Item> BLAST_STEEL_ARMOR_PLATE_JUNIOR =
            ITEMS.register(
                    "blast_steel_armor_plate_junior",
                    () -> new BlastSteelArmorPlateItem(new Item.Properties().stacksTo(16), 0.08f));
    public static final RegistryObject<Item> BLAST_STEEL_ARMOR_PLATE_MEDIUM =
            ITEMS.register(
                    "blast_steel_armor_plate_medium",
                    () -> new BlastSteelArmorPlateItem(new Item.Properties().stacksTo(16), 0.14f));
    public static final RegistryObject<Item> BLAST_STEEL_ARMOR_PLATE_ADVANCED =
            ITEMS.register(
                    "blast_steel_armor_plate_advanced",
                    () -> new BlastSteelArmorPlateItem(new Item.Properties().stacksTo(16), 0.20f));

    /** 辐射抗板：初 / 中 / 高（抗性 0.32 / 0.56 / 0.80） */
    public static final RegistryObject<Item> RADIATION_RESIST_ARMOR_PLATE_JUNIOR =
            ITEMS.register(
                    "radiation_resist_armor_plate_junior",
                    () ->
                            new RadiationResistArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.32f));
    public static final RegistryObject<Item> RADIATION_RESIST_ARMOR_PLATE_MEDIUM =
            ITEMS.register(
                    "radiation_resist_armor_plate_medium",
                    () ->
                            new RadiationResistArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.56f));
    public static final RegistryObject<Item> RADIATION_RESIST_ARMOR_PLATE_ADVANCED =
            ITEMS.register(
                    "radiation_resist_armor_plate_advanced",
                    () ->
                            new RadiationResistArmorPlateItem(
                                    new Item.Properties().stacksTo(16), 0.80f));

    /** 防闪光护目镜（仅头盔）：初 / 中 / 高 每 10 tick 削减 20 / 40 / 60 tick */
    public static final RegistryObject<Item> FLASH_RESIST_GOGGLES_JUNIOR =
            ITEMS.register(
                    "flash_resist_goggles_junior",
                    () -> new FlashResistGogglesItem(new Item.Properties().stacksTo(16), 20));
    public static final RegistryObject<Item> FLASH_RESIST_GOGGLES_MEDIUM =
            ITEMS.register(
                    "flash_resist_goggles_medium",
                    () -> new FlashResistGogglesItem(new Item.Properties().stacksTo(16), 40));
    public static final RegistryObject<Item> FLASH_RESIST_GOGGLES_ADVANCED =
            ITEMS.register(
                    "flash_resist_goggles_advanced",
                    () -> new FlashResistGogglesItem(new Item.Properties().stacksTo(16), 60));

    public static final RegistryObject<Item> PUNISHER_SHOULDER_CANNON_PLUGIN =
            ITEMS.register(
                    "punisher_shoulder_cannon_plugin",
                    () -> new PunisherShoulderCannonPluginItem(new Item.Properties().stacksTo(1)));

    public static final List<RegistryObject<Item>> ALL_ARMOR_PLATE_ITEMS =
            List.of(
                    CARBON_FIBER_ARMOR_PLATE_JUNIOR,
                    CARBON_FIBER_ARMOR_PLATE_MEDIUM,
                    CARBON_FIBER_ARMOR_PLATE_ADVANCED,
                    BALLISTIC_STEEL_ARMOR_PLATE_JUNIOR,
                    BALLISTIC_STEEL_ARMOR_PLATE_MEDIUM,
                    BALLISTIC_STEEL_ARMOR_PLATE_ADVANCED,
                    BLAST_STEEL_ARMOR_PLATE_JUNIOR,
                    BLAST_STEEL_ARMOR_PLATE_MEDIUM,
                    BLAST_STEEL_ARMOR_PLATE_ADVANCED,
                    RADIATION_RESIST_ARMOR_PLATE_JUNIOR,
                    RADIATION_RESIST_ARMOR_PLATE_MEDIUM,
                    RADIATION_RESIST_ARMOR_PLATE_ADVANCED,
                    FLASH_RESIST_GOGGLES_JUNIOR,
                    FLASH_RESIST_GOGGLES_MEDIUM,
                    FLASH_RESIST_GOGGLES_ADVANCED,
                    PUNISHER_SHOULDER_CANNON_PLUGIN);

    /** 超级升级模板（仅注册占位，无锻造逻辑；展示名见语言文件 §6 金色）。 */
    public static final RegistryObject<Item> SUPER_UPGRADE_TEMPLATE =
            ITEMS.register("super_upgrade_template", () -> new Item(new Item.Properties()));

    /** 士兵刷怪蛋（创造栏 / datagen）；顺序：难民→民兵→… */
    public static final List<RegistryObject<Item>> ALL_SOLDIER_SPAWN_EGGS =
            List.of(
                    REFUGEE_SPAWN_EGG,
                    MILITIA_SPAWN_EGG,
                    MERCENARY_SPAWN_EGG,
                    ASSAULT_SOLDIER_SPAWN_EGG,
                    LURKER_SPAWN_EGG,
                    SPECIAL_FORCES_SPAWN_EGG,
                    SUPER_SPECIAL_SOLDIER_SPAWN_EGG,
                    LEV_MARSHAL_SPAWN_EGG,
                    WAR_MACHINE_SPAWN_EGG,
                    ANNIHILATOR_SPAWN_EGG,
                    SCORPION_SPAWN_EGG,
                    SLIME_COMMANDER_SPAWN_EGG,
                    SLIME_WARRIOR_SPAWN_EGG,
                    BOMB_BABY_SPAWN_EGG,
                    CEILING_DUEL_SPAWN_EGG,
                    BOMB_SIGHT_SPAWN_EGG,
                    PUNISHER_SPAWN_EGG);

    /** 护航刷怪蛋（创造栏顺序：美女→标准→高级→魔王）。 */
    public static final List<RegistryObject<Item>> ALL_BODYGUARD_SPAWN_EGGS =
            List.of(
                    BEAUTY_BODYGUARD_SPAWN_EGG,
                    STANDARD_BODYGUARD_SPAWN_EGG,
                    ELITE_BODYGUARD_SPAWN_EGG,
                    DEMON_LORD_BODYGUARD_SPAWN_EGG);

    /** 全部刷怪蛋（创造栏连续排列：士兵→护航→神秘商人）。 */
    public static final List<RegistryObject<Item>> ALL_SPAWN_EGGS =
            List.of(
                    REFUGEE_SPAWN_EGG,
                    MILITIA_SPAWN_EGG,
                    MERCENARY_SPAWN_EGG,
                    ASSAULT_SOLDIER_SPAWN_EGG,
                    LURKER_SPAWN_EGG,
                    SPECIAL_FORCES_SPAWN_EGG,
                    SUPER_SPECIAL_SOLDIER_SPAWN_EGG,
                    LEV_MARSHAL_SPAWN_EGG,
                    WAR_MACHINE_SPAWN_EGG,
                    ANNIHILATOR_SPAWN_EGG,
                    SCORPION_SPAWN_EGG,
                    SLIME_COMMANDER_SPAWN_EGG,
                    SLIME_WARRIOR_SPAWN_EGG,
                    BOMB_BABY_SPAWN_EGG,
                    CEILING_DUEL_SPAWN_EGG,
                    BOMB_SIGHT_SPAWN_EGG,
                    PUNISHER_SPAWN_EGG,
                    BEAUTY_BODYGUARD_SPAWN_EGG,
                    STANDARD_BODYGUARD_SPAWN_EGG,
                    ELITE_BODYGUARD_SPAWN_EGG,
                    DEMON_LORD_BODYGUARD_SPAWN_EGG,
                    MYSTERIOUS_MERCHANT_SPAWN_EGG);

    /** 美女护航联络器（粉色，k=0）。 */
    public static final RegistryObject<Item> BEAUTY_BODYGUARD_CONTACT =
            ITEMS.register(
                    "beauty_bodyguard_contact",
                    () -> new BodyguardContactItem(new Item.Properties(), 0));

    /** 标准护航联络器（绿色，k=1）。 */
    public static final RegistryObject<Item> STANDARD_BODYGUARD_CONTACT =
            ITEMS.register(
                    "standard_bodyguard_contact",
                    () -> new BodyguardContactItem(new Item.Properties(), 1));

    /** 高级护航联络器（紫色，k=2）。 */
    public static final RegistryObject<Item> ELITE_BODYGUARD_CONTACT =
            ITEMS.register(
                    "elite_bodyguard_contact",
                    () -> new BodyguardContactItem(new Item.Properties(), 2));

    /** 魔王护航联络器（红色，k=3）。 */
    public static final RegistryObject<Item> DEMON_LORD_BODYGUARD_CONTACT =
            ITEMS.register(
                    "demon_lord_bodyguard_contact",
                    () -> new BodyguardContactItem(new Item.Properties(), 3));

    /** 护航联络器（创造栏顺序：美女→标准→高级→魔王）。 */
    public static final List<RegistryObject<Item>> ALL_BODYGUARD_CONTACT_ITEMS =
            List.of(
                    BEAUTY_BODYGUARD_CONTACT,
                    STANDARD_BODYGUARD_CONTACT,
                    ELITE_BODYGUARD_CONTACT,
                    DEMON_LORD_BODYGUARD_CONTACT);

    public static final RegistryObject<Item> FIRST_BULLETPROOF_HELMET =
            registerTierArmor("first_bulletproof_helmet", BulletproofArmorTier.FIRST, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FIRST_BULLETPROOF_CHESTPLATE =
            registerTierArmor("first_bulletproof_chestplate", BulletproofArmorTier.FIRST, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> FIRST_BULLETPROOF_LEGGINGS =
            registerTierArmor("first_bulletproof_leggings", BulletproofArmorTier.FIRST, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FIRST_BULLETPROOF_BOOTS =
            registerTierArmor("first_bulletproof_boots", BulletproofArmorTier.FIRST, ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> SECOND_BULLETPROOF_HELMET =
            registerTierArmor("second_bulletproof_helmet", BulletproofArmorTier.SECOND, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SECOND_BULLETPROOF_CHESTPLATE =
            registerTierArmor("second_bulletproof_chestplate", BulletproofArmorTier.SECOND, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SECOND_BULLETPROOF_LEGGINGS =
            registerTierArmor("second_bulletproof_leggings", BulletproofArmorTier.SECOND, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SECOND_BULLETPROOF_BOOTS =
            registerTierArmor("second_bulletproof_boots", BulletproofArmorTier.SECOND, ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> THIRD_BULLETPROOF_HELMET =
            registerTierArmor("third_bulletproof_helmet", BulletproofArmorTier.THIRD, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> THIRD_BULLETPROOF_CHESTPLATE =
            registerTierArmor("third_bulletproof_chestplate", BulletproofArmorTier.THIRD, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> THIRD_BULLETPROOF_LEGGINGS =
            registerTierArmor("third_bulletproof_leggings", BulletproofArmorTier.THIRD, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> THIRD_BULLETPROOF_BOOTS =
            registerTierArmor("third_bulletproof_boots", BulletproofArmorTier.THIRD, ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> FOURTH_BULLETPROOF_HELMET =
            registerTierArmor("fourth_bulletproof_helmet", BulletproofArmorTier.FOURTH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FOURTH_BULLETPROOF_CHESTPLATE =
            registerTierArmor("fourth_bulletproof_chestplate", BulletproofArmorTier.FOURTH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> FOURTH_BULLETPROOF_LEGGINGS =
            registerTierArmor("fourth_bulletproof_leggings", BulletproofArmorTier.FOURTH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FOURTH_BULLETPROOF_BOOTS =
            registerTierArmor("fourth_bulletproof_boots", BulletproofArmorTier.FOURTH, ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> FIFTH_BULLETPROOF_HELMET =
            registerTierArmor("fifth_bulletproof_helmet", BulletproofArmorTier.FIFTH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FIFTH_BULLETPROOF_CHESTPLATE =
            registerTierArmor("fifth_bulletproof_chestplate", BulletproofArmorTier.FIFTH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> FIFTH_BULLETPROOF_LEGGINGS =
            registerTierArmor("fifth_bulletproof_leggings", BulletproofArmorTier.FIFTH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FIFTH_BULLETPROOF_BOOTS =
            registerTierArmor("fifth_bulletproof_boots", BulletproofArmorTier.FIFTH, ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> SIXTH_BULLETPROOF_HELMET =
            registerTierArmor("sixth_bulletproof_helmet", BulletproofArmorTier.SIXTH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SIXTH_BULLETPROOF_CHESTPLATE =
            registerTierArmor("sixth_bulletproof_chestplate", BulletproofArmorTier.SIXTH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SIXTH_BULLETPROOF_LEGGINGS =
            registerTierArmor("sixth_bulletproof_leggings", BulletproofArmorTier.SIXTH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SIXTH_BULLETPROOF_BOOTS =
            registerTierArmor("sixth_bulletproof_boots", BulletproofArmorTier.SIXTH, ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> SEVENTH_BULLETPROOF_HELMET =
            registerTierArmor("seventh_bulletproof_helmet", BulletproofArmorTier.SEVENTH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SEVENTH_BULLETPROOF_CHESTPLATE =
            registerTierArmor("seventh_bulletproof_chestplate", BulletproofArmorTier.SEVENTH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SEVENTH_BULLETPROOF_LEGGINGS =
            registerTierArmor("seventh_bulletproof_leggings", BulletproofArmorTier.SEVENTH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SEVENTH_BULLETPROOF_BOOTS =
            registerTierArmor("seventh_bulletproof_boots", BulletproofArmorTier.SEVENTH, ArmorItem.Type.BOOTS);

    /**
     * 爆破七级套：数值同七级防弹；独立 Gecko {@code geo/armor/seventh_blast_bulletproof.geo.json}；
     * 穿戴贴图 {@code textures/item/armor/seventh_blast_bulletproof.png}，发光层 {@code seventh_blast_bulletproof_glowmask.png}。
     */
    public static final RegistryObject<Item> SEVENTH_BLAST_BULLETPROOF_HELMET =
            ITEMS.register(
                    "seventh_blast_bulletproof_helmet",
                    () -> new BlastSeventhBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_BLAST_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "seventh_blast_bulletproof_chestplate",
                    () -> new BlastSeventhBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_BLAST_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "seventh_blast_bulletproof_leggings",
                    () -> new BlastSeventhBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_BLAST_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "seventh_blast_bulletproof_boots",
                    () -> new BlastSeventhBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 冲击波套：数值同七级防弹；Gecko {@code geo/armor/shockwave_bulletproof.geo.json}。 */
    public static final RegistryObject<Item> SHOCKWAVE_BULLETPROOF_HELMET =
            ITEMS.register(
                    "shockwave_bulletproof_helmet",
                    () -> new ShockwaveBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SHOCKWAVE_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "shockwave_bulletproof_chestplate",
                    () -> new ShockwaveBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SHOCKWAVE_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "shockwave_bulletproof_leggings",
                    () -> new ShockwaveBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SHOCKWAVE_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "shockwave_bulletproof_boots",
                    () -> new ShockwaveBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 爆炎七级套：数值同七级防弹，额外防爆伤减免（见 {@link com.kkdfg.Tacz_morelevel.event.ExplosionArmorEvents}）。 */
    public static final RegistryObject<Item> SEVENTH_BLAZEFLAME_BULLETPROOF_HELMET =
            ITEMS.register(
                    "seventh_blazeflame_bulletproof_helmet",
                    () -> new BlazeflameSeventhBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_BLAZEFLAME_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "seventh_blazeflame_bulletproof_chestplate",
                    () -> new BlazeflameSeventhBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_BLAZEFLAME_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "seventh_blazeflame_bulletproof_leggings",
                    () -> new BlazeflameSeventhBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_BLAZEFLAME_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "seventh_blazeflame_bulletproof_boots",
                    () -> new BlazeflameSeventhBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 深渊七级套：数值同七级防弹；移速与跳跃见 {@link com.kkdfg.Tacz_morelevel.event.AbyssSeventhArmorAttributeEvents}（无合成配方）。 */
    public static final RegistryObject<Item> SEVENTH_ABYSS_BULLETPROOF_HELMET =
            ITEMS.register(
                    "seventh_abyss_bulletproof_helmet",
                    () -> new AbyssSeventhBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_ABYSS_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "seventh_abyss_bulletproof_chestplate",
                    () -> new AbyssSeventhBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_ABYSS_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "seventh_abyss_bulletproof_leggings",
                    () -> new AbyssSeventhBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_ABYSS_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "seventh_abyss_bulletproof_boots",
                    () -> new AbyssSeventhBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 幽匿七级套：防弹档位 8；Gecko 与七级同模，贴图见 {@link com.kkdfg.Tacz_morelevel.item.client.SculkSeventhBulletproofArmorGeoModel}。 */
    public static final RegistryObject<Item> SEVENTH_SCULK_BULLETPROOF_HELMET =
            ITEMS.register(
                    "seventh_sculk_bulletproof_helmet",
                    () -> new SculkSeventhBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_SCULK_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "seventh_sculk_bulletproof_chestplate",
                    () -> new SculkSeventhBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_SCULK_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "seventh_sculk_bulletproof_leggings",
                    () -> new SculkSeventhBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_SCULK_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "seventh_sculk_bulletproof_boots",
                    () -> new SculkSeventhBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 判罚者套：数值同七级；传说技能：限制伤害。见 {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorPunisherDamageCapHandler}。 */
    public static final RegistryObject<Item> PUNISHER_BULLETPROOF_HELMET =
            ITEMS.register(
                    "punisher_bulletproof_helmet",
                    () -> new PunisherBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> PUNISHER_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "punisher_bulletproof_chestplate",
                    () -> new PunisherBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> PUNISHER_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "punisher_bulletproof_leggings",
                    () -> new PunisherBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> PUNISHER_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "punisher_bulletproof_boots",
                    () -> new PunisherBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 辐抗七级套：数值同七级防弹；每件辐射抗性 0.8，见 {@link com.kkdfg.Tacz_morelevel.event.RadiationResistArmorPlateEvents}。 */
    public static final RegistryObject<Item> SEVENTH_RADIATION_BULLETPROOF_HELMET =
            ITEMS.register(
                    "seventh_radiation_bulletproof_helmet",
                    () -> new RadiationSeventhBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_RADIATION_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "seventh_radiation_bulletproof_chestplate",
                    () ->
                            new RadiationSeventhBulletproofArmorItem(
                                    ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_RADIATION_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "seventh_radiation_bulletproof_leggings",
                    () ->
                            new RadiationSeventhBulletproofArmorItem(
                                    ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SEVENTH_RADIATION_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "seventh_radiation_bulletproof_boots",
                    () -> new RadiationSeventhBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 动力机甲防弹套：档位 7；Gecko 见 {@link com.kkdfg.Tacz_morelevel.item.client.PowerMechBulletproofArmorGeoModel}。 */
    public static final RegistryObject<Item> POWER_MECH_BULLETPROOF_HELMET =
            ITEMS.register(
                    "power_mech_bulletproof_helmet",
                    () -> new PowerMechBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> POWER_MECH_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "power_mech_bulletproof_chestplate",
                    () -> new PowerMechBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> POWER_MECH_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "power_mech_bulletproof_leggings",
                    () -> new PowerMechBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> POWER_MECH_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "power_mech_bulletproof_boots",
                    () -> new PowerMechBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 神秘商人防弹套：数值同四级甲；穿戴 Gecko 见 {@code geo/armor/mysterious_merchant_bulletproof.geo.json}。 */
    public static final RegistryObject<Item> MYSTERIOUS_MERCHANT_BULLETPROOF_HELMET =
            ITEMS.register(
                    "mysterious_merchant_bulletproof_helmet",
                    () -> new MysteriousMerchantBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> MYSTERIOUS_MERCHANT_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "mysterious_merchant_bulletproof_chestplate",
                    () -> new MysteriousMerchantBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> MYSTERIOUS_MERCHANT_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "mysterious_merchant_bulletproof_leggings",
                    () -> new MysteriousMerchantBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> MYSTERIOUS_MERCHANT_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "mysterious_merchant_bulletproof_boots",
                    () -> new MysteriousMerchantBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 特种作战盔甲：数值同六级；Gecko {@code geo/armor/special_ops_bulletproof.geo.json}；背包贴图 {@code textures/item/special_ops_bulletproof_<部位>.png}。 */
    public static final RegistryObject<Item> SPECIAL_OPS_BULLETPROOF_HELMET =
            ITEMS.register(
                    "special_ops_bulletproof_helmet",
                    () -> new SpecialOpsBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SPECIAL_OPS_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "special_ops_bulletproof_chestplate",
                    () -> new SpecialOpsBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SPECIAL_OPS_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "special_ops_bulletproof_leggings",
                    () -> new SpecialOpsBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SPECIAL_OPS_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "special_ops_bulletproof_boots",
                    () -> new SpecialOpsBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 军官制服套：帽/军衣/裤/鞋；数值暂同四级甲；Gecko {@code geo/armor/officer_uniform.geo.json}。 */
    public static final RegistryObject<Item> OFFICER_HELMET =
            ITEMS.register(
                    "officer_helmet",
                    () -> new OfficerUniformArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> OFFICER_CHESTPLATE =
            ITEMS.register(
                    "officer_chestplate",
                    () -> new OfficerUniformArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> OFFICER_LEGGINGS =
            ITEMS.register(
                    "officer_leggings",
                    () -> new OfficerUniformArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> OFFICER_BOOTS =
            ITEMS.register(
                    "officer_boots",
                    () -> new OfficerUniformArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /**
     * 隐身斗篷套：帽/斗篷/裤/靴；{@link StealthCloakArmorItem} 继承防弹、档位 3。
     * Gecko {@code geo/armor/stealth_cloak.geo.json}，贴图 64×64。
     */
    public static final RegistryObject<Item> STEALTH_HELMET =
            ITEMS.register(
                    "stealth_helmet",
                    () -> new StealthCloakArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> STEALTH_CLOAK =
            ITEMS.register(
                    "stealth_cloak",
                    () -> new StealthCloakArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> STEALTH_LEGGINGS =
            ITEMS.register(
                    "stealth_leggings",
                    () -> new StealthCloakArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> STEALTH_BOOTS =
            ITEMS.register(
                    "stealth_boots",
                    () -> new StealthCloakArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /**
     * 潜行七级套：独立隐身系统；档位 7；Gecko {@code geo/armor/stealth_seventh.geo.json}。
     * 技能见 {@link com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthSeventhSkill}。
     */
    public static final RegistryObject<Item> STEALTH_SEVENTH_HELMET =
            ITEMS.register(
                    "stealth_seventh_helmet",
                    () -> new StealthSeventhArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> STEALTH_SEVENTH_CHESTPLATE =
            ITEMS.register(
                    "stealth_seventh_chestplate",
                    () ->
                            new StealthSeventhArmorItem(
                                    ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> STEALTH_SEVENTH_LEGGINGS =
            ITEMS.register(
                    "stealth_seventh_leggings",
                    () ->
                            new StealthSeventhArmorItem(
                                    ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> STEALTH_SEVENTH_BOOTS =
            ITEMS.register(
                    "stealth_seventh_boots",
                    () -> new StealthSeventhArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /**
     * 小丑七级防弹头盔：档位 7；Gecko {@code geo/armor/xiaocou_armor.geo.json}。
     * 物品注释「（天花板的认证）」；传说技能见 {@link JokerSeventhBulletproofHelmetItem}。
     */
    public static final RegistryObject<Item> JOKER_SEVENTH_BULLETPROOF_HELMET =
            ITEMS.register(
                    "joker_seventh_bulletproof_helmet",
                    () -> new JokerSeventhBulletproofHelmetItem(new Item.Properties()));

    /** 时空防弹套：档位 7；Gecko 见 {@link com.kkdfg.Tacz_morelevel.item.client.SpacetimeBulletproofArmorGeoModel}。 */
    public static final RegistryObject<Item> SPACETIME_BULLETPROOF_HELMET =
            ITEMS.register(
                    "spacetime_bulletproof_helmet",
                    () -> new SpacetimeBulletproofArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SPACETIME_BULLETPROOF_CHESTPLATE =
            ITEMS.register(
                    "spacetime_bulletproof_chestplate",
                    () -> new SpacetimeBulletproofArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SPACETIME_BULLETPROOF_LEGGINGS =
            ITEMS.register(
                    "spacetime_bulletproof_leggings",
                    () -> new SpacetimeBulletproofArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SPACETIME_BULLETPROOF_BOOTS =
            ITEMS.register(
                    "spacetime_bulletproof_boots",
                    () -> new SpacetimeBulletproofArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    /** 时空之石：右键视线/随机闪现；冷却 20s；模型后补。 */
    public static final RegistryObject<Item> SPACETIME_STONE =
            ITEMS.register("spacetime_stone", () -> new SpacetimeStoneItem(new Item.Properties()));

    /** 能量核心（金）：Gecko 3D + glowmask；资源 {@code energy_core_gold*}；可叠 64。 */
    public static final RegistryObject<Item> ENERGY_CORE_GOLD =
            ITEMS.register(
                    "energy_core_gold",
                    () -> new EnergyCoreItem(new Item.Properties().stacksTo(64), "energy_core_gold"));

    /** 能量核心（红）：Gecko 3D + glowmask；资源 {@code energy_core_red*}；可叠 64。 */
    public static final RegistryObject<Item> ENERGY_CORE_RED =
            ITEMS.register(
                    "energy_core_red",
                    () -> new EnergyCoreItem(new Item.Properties().stacksTo(64), "energy_core_red"));

    /** 动力推进器（金）：Gecko 3D + glowmask；资源 {@code power_thruster_gold*}；可叠 64。 */
    public static final RegistryObject<Item> POWER_THRUSTER_GOLD =
            ITEMS.register(
                    "power_thruster_gold",
                    () ->
                            new PowerThrusterItem(
                                    new Item.Properties().stacksTo(64), "power_thruster_gold"));

    /** 动力推进器（红）：Gecko 3D + glowmask；资源 {@code power_thruster_red*}；可叠 64。 */
    public static final RegistryObject<Item> POWER_THRUSTER_RED =
            ITEMS.register(
                    "power_thruster_red",
                    () ->
                            new PowerThrusterItem(
                                    new Item.Properties().stacksTo(64), "power_thruster_red"));

    /** 吸光管：Gecko 3D + glowmask；资源 {@code light_absorbing_tube*}；可叠 64。 */
    public static final RegistryObject<Item> LIGHT_ABSORBING_TUBE =
            ITEMS.register(
                    "light_absorbing_tube",
                    () -> new LightAbsorbingTubeItem(new Item.Properties().stacksTo(64)));

    /** 注射器：普通材料物品（硬化针模型）。 */
    public static final RegistryObject<Item> SYRINGE =
            ITEMS.register("syringe", () -> new Item(new Item.Properties()));

    /** 绷带：恢复少量生命值。 */
    public static final RegistryObject<Item> BANDAGE =
            ITEMS.register(
                    "bandage",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(40)
                                            .heal(HealSpec.onFinish(0.10F, 2.0F))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_BANDAGE::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 急救包：恢复中量生命值，生命回复。 */
    public static final RegistryObject<Item> FIRST_AID_KIT =
            ITEMS.register(
                    "first_aid_kit",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(60)
                                            .heal(HealSpec.onFinish(0.50F, 4.0F))
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            () -> MobEffects.REGENERATION,
                                                            3 * 20,
                                                            0,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_FIRST_AID::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 医疗箱：恢复大量生命值，生命回复。 */
    public static final RegistryObject<Item> MEDICAL_BOX =
            ITEMS.register(
                    "medical_box",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(80)
                                            .heal(HealSpec.onFinish(0.75F, 5.0F))
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            () -> MobEffects.REGENERATION,
                                                            5 * 20,
                                                            0,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_MEDICAL::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 初级硬化针：使用后获得硬化 I。 */
    public static final RegistryObject<Item> HARDENING_NEEDLE_JUNIOR =
            ITEMS.register(
                    "hardening_needle_junior",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(60)
                                            .heal(HealSpec.none())
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            ModMobEffects.HARDENING::get,
                                                            40 * 20,
                                                            0,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH,
                                                            false))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_INJECTION::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 中级硬化针：使用后获得硬化 II。 */
    public static final RegistryObject<Item> HARDENING_NEEDLE_MEDIUM =
            ITEMS.register(
                    "hardening_needle_medium",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(60)
                                            .heal(HealSpec.none())
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            ModMobEffects.HARDENING::get,
                                                            60 * 20,
                                                            1,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH,
                                                            false))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_INJECTION::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 高级硬化针：使用后获得硬化 III。 */
    public static final RegistryObject<Item> HARDENING_NEEDLE_ADVANCED =
            ITEMS.register(
                    "hardening_needle_advanced",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(60)
                                            .heal(HealSpec.none())
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            ModMobEffects.HARDENING::get,
                                                            80 * 20,
                                                            2,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH,
                                                            false))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_INJECTION::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 初级综合免疫针：使用后获得综合免疫 I。 */
    public static final RegistryObject<Item> COMPREHENSIVE_IMMUNITY_NEEDLE_JUNIOR =
            ITEMS.register(
                    "comprehensive_immunity_needle_junior",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(60)
                                            .heal(HealSpec.none())
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            ModMobEffects.COMPREHENSIVE_IMMUNITY::get,
                                                            40 * 20,
                                                            0,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH,
                                                            false))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_INJECTION::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 中级综合免疫针：使用后获得综合免疫 II。 */
    public static final RegistryObject<Item> COMPREHENSIVE_IMMUNITY_NEEDLE_MEDIUM =
            ITEMS.register(
                    "comprehensive_immunity_needle_medium",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(60)
                                            .heal(HealSpec.none())
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            ModMobEffects.COMPREHENSIVE_IMMUNITY::get,
                                                            60 * 20,
                                                            1,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH,
                                                            false))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_INJECTION::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    /** 高级综合免疫针：使用后获得综合免疫 III。 */
    public static final RegistryObject<Item> COMPREHENSIVE_IMMUNITY_NEEDLE_ADVANCED =
            ITEMS.register(
                    "comprehensive_immunity_needle_advanced",
                    () ->
                            new ChannelMedicineItem(
                                    new Item.Properties().stacksTo(16),
                                    MedicineSpec.builder()
                                            .useTicks(60)
                                            .heal(HealSpec.none())
                                            .addEffect(
                                                    MedicineEffectSpec.of(
                                                            ModMobEffects.COMPREHENSIVE_IMMUNITY::get,
                                                            80 * 20,
                                                            2,
                                                            MedicineEffectSpec.EffectPhase.ON_FINISH,
                                                            false))
                                            .sounds(MedicineSounds.once(ModSounds.MEDICINE_INJECTION::get))
                                            .consumeMode(MedicineConsumeMode.SHRINK_ONE)
                                            .build()));

    public  static  final  RegistryObject<Item> OBSIDIAN_ITEM=ITEMS.register("obsidian_item",()->new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> OBSIDIAN_SWORD=ITEMS.register("obsidian_sword",()->new SwordItem(ModToolTies.Obsidian,4,-2,new Item.Properties().fireResistant()));
    public static  final RegistryObject<Item> OBSIDIAN_PICKAXE=ITEMS.register("obsidian_pickaxe",()->new PickaxeItem(ModToolTies.Obsidian,-1,-2.8f,(new Item.Properties().fireResistant())));
    public static  final RegistryObject<Item> OBSIDIAN_AXE=ITEMS.register("obsidian_axe",()->new AxeItem(ModToolTies.Obsidian,10,-3.2f,(new Item.Properties().fireResistant())));
    public static  final RegistryObject<Item> OBSIDIAN_SHOVEL=ITEMS.register("obsidian_shovel",()->new ShovelItem(ModToolTies.Obsidian,-1,-2.8f,(new Item.Properties().fireResistant())));
    public static  final RegistryObject<Item> OBSIDIAN_HOE=ITEMS.register("obsidian_hoe",()->new HoeItem(ModToolTies.Obsidian,-1,-2.8f,(new Item.Properties().fireResistant())));
    public static final RegistryObject<Item> OBSIDIAN_HELMET =
            ITEMS.register(
                    "obsidian_helmet",
                    () ->
                            new ObsidianArmorItem(
                                    ModArmorMaterials.Obsidian,
                                    ArmorItem.Type.HELMET,
                                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> OBSIDIAN_LEGGINGS =
            ITEMS.register(
                    "obsidian_leggings",
                    () ->
                            new ObsidianArmorItem(
                                    ModArmorMaterials.Obsidian,
                                    ArmorItem.Type.LEGGINGS,
                                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> OBSIDIAN_CHESTPLATE =
            ITEMS.register(
                    "obsidian_chestplate",
                    () ->
                            new ObsidianArmorItem(
                                    ModArmorMaterials.Obsidian,
                                    ArmorItem.Type.CHESTPLATE,
                                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> OBSIDIAN_BOOTS =
            ITEMS.register(
                    "obsidian_boots",
                    () ->
                            new ObsidianArmorItem(
                                    ModArmorMaterials.Obsidian,
                                    ArmorItem.Type.BOOTS,
                                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    /** 全部 GeckoLib 防弹甲注册项（28 件常规分级），用于创造栏同步、{@code GeoItem.registerSyncedAnimatable}、datagen。
     * 爆破七级见 {@link #SEVENTH_BLAST_BULLETPROOF_ITEMS}；爆炎七级见 {@link #SEVENTH_BLAZEFLAME_BULLETPROOF_ITEMS}；深渊七级见 {@link #SEVENTH_ABYSS_BULLETPROOF_ITEMS}；幽匿七级见 {@link #SEVENTH_SCULK_BULLETPROOF_ITEMS}；动力机甲见 {@link #POWER_MECH_BULLETPROOF_ITEMS}。 */
    public static final RegistryObject<Item> SUPERPOTIONBOTTLE=ITEMS.register("super_potion_bottle",()->new SuperPotionBottleItem(new Item.Properties().food(Foods.ROTTEN_FLESH).stacksTo(1).craftRemainder(GLASS_BOTTLE)));
    public static final List<RegistryObject<Item>> ALL_BULLETPROOF_ITEMS =
            List.of(
                    FIRST_BULLETPROOF_HELMET,
                    FIRST_BULLETPROOF_CHESTPLATE,
                    FIRST_BULLETPROOF_LEGGINGS,
                    FIRST_BULLETPROOF_BOOTS,
                    SECOND_BULLETPROOF_HELMET,
                    SECOND_BULLETPROOF_CHESTPLATE,
                    SECOND_BULLETPROOF_LEGGINGS,
                    SECOND_BULLETPROOF_BOOTS,
                    THIRD_BULLETPROOF_HELMET,
                    THIRD_BULLETPROOF_CHESTPLATE,
                    THIRD_BULLETPROOF_LEGGINGS,
                    THIRD_BULLETPROOF_BOOTS,
                    FOURTH_BULLETPROOF_HELMET,
                    FOURTH_BULLETPROOF_CHESTPLATE,
                    FOURTH_BULLETPROOF_LEGGINGS,
                    FOURTH_BULLETPROOF_BOOTS,
                    FIFTH_BULLETPROOF_HELMET,
                    FIFTH_BULLETPROOF_CHESTPLATE,
                    FIFTH_BULLETPROOF_LEGGINGS,
                    FIFTH_BULLETPROOF_BOOTS,
                    SIXTH_BULLETPROOF_HELMET,
                    SIXTH_BULLETPROOF_CHESTPLATE,
                    SIXTH_BULLETPROOF_LEGGINGS,
                    SIXTH_BULLETPROOF_BOOTS,
                    SEVENTH_BULLETPROOF_HELMET,
                    SEVENTH_BULLETPROOF_CHESTPLATE,
                    SEVENTH_BULLETPROOF_LEGGINGS,
                    SEVENTH_BULLETPROOF_BOOTS);

    /** 爆破七级四件：背包图标暂用穿戴贴图占位 {@code textures/item/seventh_blast_bulletproof_<部位>.png}。 */
    public static final List<RegistryObject<Item>> SEVENTH_BLAST_BULLETPROOF_ITEMS =
            List.of(
                    SEVENTH_BLAST_BULLETPROOF_HELMET,
                    SEVENTH_BLAST_BULLETPROOF_CHESTPLATE,
                    SEVENTH_BLAST_BULLETPROOF_LEGGINGS,
                    SEVENTH_BLAST_BULLETPROOF_BOOTS);

    /** 冲击波四件：背包图标 {@code textures/item/shockwave_bulletproof_<部位>.png}。 */
    public static final List<RegistryObject<Item>> SHOCKWAVE_BULLETPROOF_ITEMS =
            List.of(
                    SHOCKWAVE_BULLETPROOF_HELMET,
                    SHOCKWAVE_BULLETPROOF_CHESTPLATE,
                    SHOCKWAVE_BULLETPROOF_LEGGINGS,
                    SHOCKWAVE_BULLETPROOF_BOOTS);

    /** 爆炎七级四件：单独列出；物品栏贴图为 {@code textures/item/seventh_blazeflame_bulletproof_<部位>.png}，datagen 中单独 {@code basicItem}。 */
    public static final List<RegistryObject<Item>> SEVENTH_BLAZEFLAME_BULLETPROOF_ITEMS =
            List.of(
                    SEVENTH_BLAZEFLAME_BULLETPROOF_HELMET,
                    SEVENTH_BLAZEFLAME_BULLETPROOF_CHESTPLATE,
                    SEVENTH_BLAZEFLAME_BULLETPROOF_LEGGINGS,
                    SEVENTH_BLAZEFLAME_BULLETPROOF_BOOTS);

    /** 深渊七级四件：物品栏贴图 {@code textures/item/seventh_abyss_bulletproof_<部位>.png}；无配方。 */
    public static final List<RegistryObject<Item>> SEVENTH_ABYSS_BULLETPROOF_ITEMS =
            List.of(
                    SEVENTH_ABYSS_BULLETPROOF_HELMET,
                    SEVENTH_ABYSS_BULLETPROOF_CHESTPLATE,
                    SEVENTH_ABYSS_BULLETPROOF_LEGGINGS,
                    SEVENTH_ABYSS_BULLETPROOF_BOOTS);

    /** 幽匿七级四件：物品栏贴图 {@code textures/item/seventh_sculk_bulletproof_<部位>.png}；锻造台升级。 */
    public static final List<RegistryObject<Item>> SEVENTH_SCULK_BULLETPROOF_ITEMS =
            List.of(
                    SEVENTH_SCULK_BULLETPROOF_HELMET,
                    SEVENTH_SCULK_BULLETPROOF_CHESTPLATE,
                    SEVENTH_SCULK_BULLETPROOF_LEGGINGS,
                    SEVENTH_SCULK_BULLETPROOF_BOOTS);

    /** 判罚者四件：{@code punisher_bulletproof*}；无配方。 */
    public static final List<RegistryObject<Item>> PUNISHER_BULLETPROOF_ITEMS =
            List.of(
                    PUNISHER_BULLETPROOF_HELMET,
                    PUNISHER_BULLETPROOF_CHESTPLATE,
                    PUNISHER_BULLETPROOF_LEGGINGS,
                    PUNISHER_BULLETPROOF_BOOTS);

    /** 辐抗七级四件：物品栏贴图 {@code textures/item/seventh_radiation_bulletproof_<部位>.png}。 */
    public static final List<RegistryObject<Item>> SEVENTH_RADIATION_BULLETPROOF_ITEMS =
            List.of(
                    SEVENTH_RADIATION_BULLETPROOF_HELMET,
                    SEVENTH_RADIATION_BULLETPROOF_CHESTPLATE,
                    SEVENTH_RADIATION_BULLETPROOF_LEGGINGS,
                    SEVENTH_RADIATION_BULLETPROOF_BOOTS);

    /** 动力机甲四件：背包图标 {@code textures/item/power_mech_bulletproof_<部位>.png}；穿戴贴图 {@code textures/item/armor/power_mech_bulletproof.png}。 */
    public static final List<RegistryObject<Item>> POWER_MECH_BULLETPROOF_ITEMS =
            List.of(
                    POWER_MECH_BULLETPROOF_HELMET,
                    POWER_MECH_BULLETPROOF_CHESTPLATE,
                    POWER_MECH_BULLETPROOF_LEGGINGS,
                    POWER_MECH_BULLETPROOF_BOOTS);

    /** 神秘商人四件：背包 item 模型暂未配置；穿戴贴图 {@code textures/item/armor/mysterious_merchant_bulletproof.png}。 */
    public static final List<RegistryObject<Item>> MYSTERIOUS_MERCHANT_BULLETPROOF_ITEMS =
            List.of(
                    MYSTERIOUS_MERCHANT_BULLETPROOF_HELMET,
                    MYSTERIOUS_MERCHANT_BULLETPROOF_CHESTPLATE,
                    MYSTERIOUS_MERCHANT_BULLETPROOF_LEGGINGS,
                    MYSTERIOUS_MERCHANT_BULLETPROOF_BOOTS);

    /** 特种作战四件：背包图标 {@code textures/item/special_ops_bulletproof_<部位>.png}；穿戴贴图 {@code textures/item/armor/special_ops_bulletproof.png}。 */
    public static final List<RegistryObject<Item>> SPECIAL_OPS_BULLETPROOF_ITEMS =
            List.of(
                    SPECIAL_OPS_BULLETPROOF_HELMET,
                    SPECIAL_OPS_BULLETPROOF_CHESTPLATE,
                    SPECIAL_OPS_BULLETPROOF_LEGGINGS,
                    SPECIAL_OPS_BULLETPROOF_BOOTS);

    /** 军官制服四件：背包图标 {@code textures/item/officer_<部位>.png}；穿戴贴图 {@code textures/item/armor/officer_uniform.png}。 */
    public static final List<RegistryObject<Item>> OFFICER_UNIFORM_ITEMS =
            List.of(OFFICER_HELMET, OFFICER_CHESTPLATE, OFFICER_LEGGINGS, OFFICER_BOOTS);

    /** 隐身斗篷四件：背包图标 {@code textures/item/stealth_*.png}；穿戴贴图 {@code textures/item/armor/stealth_cloak.png}。 */
    public static final List<RegistryObject<Item>> STEALTH_CLOAK_ITEMS =
            List.of(STEALTH_HELMET, STEALTH_CLOAK, STEALTH_LEGGINGS, STEALTH_BOOTS);

    /** 潜行七级四件：穿戴贴图 {@code stealth_seventh.png}，发光层 {@code stealth_seventh_glowmask.png}。 */
    public static final List<RegistryObject<Item>> STEALTH_SEVENTH_ITEMS =
            List.of(
                    STEALTH_SEVENTH_HELMET,
                    STEALTH_SEVENTH_CHESTPLATE,
                    STEALTH_SEVENTH_LEGGINGS,
                    STEALTH_SEVENTH_BOOTS);

    /** 小丑七级头盔（单件）。 */
    public static final List<RegistryObject<Item>> JOKER_SEVENTH_HELMET_ITEMS =
            List.of(JOKER_SEVENTH_BULLETPROOF_HELMET);

    /** 时空防弹四件：穿戴贴图 {@code textures/item/armor/spacetime_bulletproof.png}，发光层 {@code spacetime_bulletproof_glomask.png}。 */
    public static final List<RegistryObject<Item>> SPACETIME_BULLETPROOF_ITEMS =
            List.of(
                    SPACETIME_BULLETPROOF_HELMET,
                    SPACETIME_BULLETPROOF_CHESTPLATE,
                    SPACETIME_BULLETPROOF_LEGGINGS,
                    SPACETIME_BULLETPROOF_BOOTS);

    private static RegistryObject<Item> registerTierArmor(
            String name, BulletproofArmorTier tier, ArmorItem.Type slot) {
        return ITEMS.register(
                name, () -> new TieredBulletproofGeoArmorItem(tier, slot, new Item.Properties()));
    }

    public static void register(IEventBus modEventBus) {
        ModCreativeModeTabs.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    private ModItems() {}
}
