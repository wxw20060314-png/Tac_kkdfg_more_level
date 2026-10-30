package com.kkdfg.Tacz_morelevel.sound;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组音效注册。
 *
 * <p>编码约定见仓库根目录 {@code Tacz_morelevel规范手册.txt}。
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Tacz_MoreLevel.MOD_ID);

    public static final RegistryObject<SoundEvent> SOUND_HENHEN =
            SOUNDS_EVENTS.register(
                    "sound_henhen",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "henhen")));
    public static final RegistryObject<SoundEvent> SOUND_XIANGSHOU =
            SOUNDS_EVENTS.register(
                    "xiangshou",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "xiangshou")));
    public static final RegistryObject<SoundEvent> SOUND_FIRE =
            SOUNDS_EVENTS.register(
                    "fire",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "fire")));
    public static final RegistryObject<SoundEvent> SOUND_GRENADE_PIN =
            SOUNDS_EVENTS.register(
                    "grenade_pin",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "grenade_pin")));
    public static final RegistryObject<SoundEvent> SOUND_GRENADE_THROW =
            SOUNDS_EVENTS.register(
                    "grenade_throw",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "grenade_throw")));
    public static final RegistryObject<SoundEvent> SOUND_FLASHBANG =
            SOUNDS_EVENTS.register(
                    "flashbang",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "flashbang")));
    public static final RegistryObject<SoundEvent> SOUND_FIREBOTTLE_FLY =
            SOUNDS_EVENTS.register(
                    "firebottle_fly",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "firebottle")));
    public static final RegistryObject<SoundEvent> SOUND_SIGNAL_SUMMONING_COUNTDOWN =
            SOUNDS_EVENTS.register(
                    "signal_summoning_countdown",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(
                                            Tacz_MoreLevel.MOD_ID, "signal_summoning_countdown")));
    /** 绷带使用音效。 */
    public static final RegistryObject<SoundEvent> MEDICINE_BANDAGE = registerMedicine("medicine_bandage");
    /** 急救包使用音效。 */
    public static final RegistryObject<SoundEvent> MEDICINE_FIRST_AID = registerMedicine("medicine_first_aid");
    /** 盔甲维修包使用音效。 */
    public static final RegistryObject<SoundEvent> MEDICINE_ARMOR_REPAIR = registerMedicine("medicine_armor_repair");
    /** 医疗箱使用音效。 */
    public static final RegistryObject<SoundEvent> MEDICINE_MEDICAL = registerMedicine("medicine_medical");
    /** 针剂使用音效（硬化针 / 综合免疫针共用）。 */
    public static final RegistryObject<SoundEvent> MEDICINE_INJECTION = registerMedicine("medicine_injection");

    /** 悬赏召唤歼灭者：T=0 起播的环境音。 */
    public static final RegistryObject<SoundEvent> SOUND_BOUNTY_ANNIHILATOR_SUMMON =
            SOUNDS_EVENTS.register(
                    "bounty_annihilator_summon",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(
                                            Tacz_MoreLevel.MOD_ID, "bounty_annihilator_summon")));

    /** 士兵语音 / 战斗音效（源文件 soldiersound_*，注册名 soldier_*） */
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_1 = registerSoldier("soldier_1");

    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_3 = registerSoldier("soldier_3");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_4 = registerSoldier("soldier_4");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_5 = registerSoldier("soldier_5");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_6 = registerSoldier("soldier_6");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_DIED_1 = registerSoldier("soldier_died_1");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_DIED_2 = registerSoldier("soldier_died_2");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_DIED_4 = registerSoldier("soldier_died_4");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_DIED_5 = registerSoldier("soldier_died_5");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_FIGHT1_1 = registerSoldier("soldier_fight1_1");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_FIGHT_2 = registerSoldier("soldier_fight_2");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_HURT_1 = registerSoldier("soldier_hurt_1");
    public static final RegistryObject<SoundEvent> SOUND_SOLDIER_HURT_2 = registerSoldier("soldier_hurt_2");

    public static final RegistryObject<SoundEvent> SOUND_JIUSHI_NALI_LE = registerSoldier("jiushi_nali_le");
    public static final RegistryObject<SoundEvent> SOUND_KANQILAI_HEN_QIANGZHUANG = registerSoldier("kanqilai_hen_qiangzhuang");
    public static final RegistryObject<SoundEvent> SOUND_MIAOZHUN_NALI = registerSoldier("miaozhun_nali");
    public static final RegistryObject<SoundEvent> SOUND_QINGDUO_ZHIJIAO = registerSoldier("qingduo_zhijiao");
    public static final RegistryObject<SoundEvent> SOUND_QING_WENROU_YIDIAN = registerSoldier("qing_wenrou_yidian");
    public static final RegistryObject<SoundEvent> SOUND_TENG = registerSoldier("teng");
    public static final RegistryObject<SoundEvent> SOUND_ZHIYU_DE_LILIANG = registerSoldier("zhiyu_de_liliang");
    public static final RegistryObject<SoundEvent> SOUND_A = registerSoldier("a");
    public static final RegistryObject<SoundEvent> SOUND_BAIBEI = registerSoldier("baibei");
    public static final RegistryObject<SoundEvent> SOUND_BU_ZUO_HAO_XINLI_ZHUNBEI_BUXING =
            registerSoldier("bu_zuo_hao_xinli_zhunbei_buxing");
    public static final RegistryObject<SoundEvent> SOUND_HAIPA = registerSoldier("haipa");
    public static final RegistryObject<SoundEvent> SOUND_HEI = registerSoldier("hei");

    /** 男护航应答语音 */
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_I_STAND_READY =
            registerSoldier("male_bodyguard_i_stand_ready");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_READY_FOR_THE_THINGS =
            registerSoldier("male_bodyguard_ready_for_the_things");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_READY_TO_SIR =
            registerSoldier("male_bodyguard_ready_to_sir");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_WHAT_WOULD_MEDO =
            registerSoldier("male_bodyguard_what_would_medo");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_YES_SIR =
            registerSoldier("male_bodyguard_yes_sir");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_YOUR_COMMANDSIR =
            registerSoldier("male_bodyguard_your_commandsir");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_YOUR_ORDERS =
            registerSoldier("male_bodyguard_your_orders");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_YOUR_WISHSIR =
            registerSoldier("male_bodyguard_your_wishsir");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_AT_YOUR_COMMAND =
            registerSoldier("male_bodyguard_at_your_command");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_HERE_I_STAND =
            registerSoldier("male_bodyguard_here_i_stand");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_FORWARD_WE_GO =
            registerSoldier("male_bodyguard_forward_we_go");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_NO_ENEMY_SHALL_STAND_BEFORE_US =
            registerSoldier("male_bodyguard_no_enemy_shall_stand_before_us");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_DEFEND_YOURSELF =
            registerSoldier("male_bodyguard_defend_yourself");
    public static final RegistryObject<SoundEvent> SOUND_MALE_BODYGUARD_FOR_HONOR_AND_GLORY =
            registerSoldier("male_bodyguard_for_honor_and_glory");
    public static final RegistryObject<SoundEvent> SOUND_DIANBAO =
        registerSoldier("dianbao");

    /** 判罚者肩炮开火（RPG-7 发射音拷贝）。 */
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_SHOULDER_CANNON_SHOOT =
            SOUNDS_EVENTS.register(
                    "punisher_shoulder_cannon_shoot",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(
                                            Tacz_MoreLevel.MOD_ID, "punisher_shoulder_cannon_shoot")));

    /** 动力机甲飞行音效（jijiaqifei_01）。 */
    public static final RegistryObject<SoundEvent> SOUND_POWER_MECH_FLIGHT =
            SOUNDS_EVENTS.register(
                    "power_mech_flight",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(
                                            Tacz_MoreLevel.MOD_ID, "power_mech_flight")));

    /** 冲击套蓄力音效：按下播、松开/释放停。 */
    public static final RegistryObject<SoundEvent> SOUND_SHOCKWAVE_CHARGE =
            SOUNDS_EVENTS.register(
                    "shockwave_charge",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(
                                            Tacz_MoreLevel.MOD_ID, "shockwave_charge")));

    /** 冲击套释放音效：松开/耗尽发射时播。 */
    public static final RegistryObject<SoundEvent> SOUND_SHOCKWAVE_RELEASE =
            SOUNDS_EVENTS.register(
                    "shockwave_release",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(
                                            Tacz_MoreLevel.MOD_ID, "shockwave_release")));

    /** 动力机甲过载音效（gangtie_zhongjiezhe_absorbshiled）。 */
    public static final RegistryObject<SoundEvent> SOUND_POWER_MECH_OVERLOAD =
            SOUNDS_EVENTS.register(
                    "power_mech_overload",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(
                                            Tacz_MoreLevel.MOD_ID, "power_mech_overload")));

    /** 战争机器受创。 */
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_HURT =
            registerWarMachine("war_machine_hurt");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_DEATH_1 =
            registerWarMachine("war_machine_death_1");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_DEATH_2 =
            registerWarMachine("war_machine_death_2");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_DEATH_3 =
            registerWarMachine("war_machine_death_3");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_DEATH_4 =
            registerWarMachine("war_machine_death_4");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_IDLE_1 =
            registerWarMachine("war_machine_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_IDLE_2 =
            registerWarMachine("war_machine_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_IDLE_3 =
            registerWarMachine("war_machine_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_IDLE_4 =
            registerWarMachine("war_machine_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_COMBAT_IDLE_1 =
            registerWarMachine("war_machine_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_COMBAT_IDLE_2 =
            registerWarMachine("war_machine_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_COMBAT_IDLE_3 =
            registerWarMachine("war_machine_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_COMBAT_IDLE_4 =
            registerWarMachine("war_machine_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_TARGET_1 =
            registerWarMachine("war_machine_target_1");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_TARGET_2 =
            registerWarMachine("war_machine_target_2");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_TARGET_3 =
            registerWarMachine("war_machine_target_3");
    public static final RegistryObject<SoundEvent> SOUND_WAR_MACHINE_TARGET_4 =
            registerWarMachine("war_machine_target_4");

    /** 炸弹小车闲置音（随机 1–5）。 */
    public static final RegistryObject<SoundEvent> SOUND_BOMB_CART_IDLE_1 =
            registerBombCart("bomb_cart_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_CART_IDLE_2 =
            registerBombCart("bomb_cart_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_CART_IDLE_3 =
            registerBombCart("bomb_cart_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_CART_IDLE_4 =
            registerBombCart("bomb_cart_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_CART_IDLE_5 =
            registerBombCart("bomb_cart_idle_5");

    /** 炸弹宝宝配音：闲置 / 战斗闲置 / 战斗射击（发现目标）/ 死亡。 */
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_IDLE_1 =
            registerBombBaby("bomb_baby_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_IDLE_2 =
            registerBombBaby("bomb_baby_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_IDLE_3 =
            registerBombBaby("bomb_baby_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_IDLE_4 =
            registerBombBaby("bomb_baby_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_IDLE_5 =
            registerBombBaby("bomb_baby_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_IDLE_6 =
            registerBombBaby("bomb_baby_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_IDLE_7 =
            registerBombBaby("bomb_baby_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_COMBAT_IDLE_1 =
            registerBombBaby("bomb_baby_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_COMBAT_IDLE_2 =
            registerBombBaby("bomb_baby_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_COMBAT_IDLE_3 =
            registerBombBaby("bomb_baby_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_COMBAT_IDLE_4 =
            registerBombBaby("bomb_baby_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_COMBAT_IDLE_5 =
            registerBombBaby("bomb_baby_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_COMBAT_IDLE_6 =
            registerBombBaby("bomb_baby_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_COMBAT_IDLE_7 =
            registerBombBaby("bomb_baby_combat_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_TARGET_1 =
            registerBombBaby("bomb_baby_target_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_TARGET_2 =
            registerBombBaby("bomb_baby_target_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_TARGET_3 =
            registerBombBaby("bomb_baby_target_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_TARGET_4 =
            registerBombBaby("bomb_baby_target_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_TARGET_5 =
            registerBombBaby("bomb_baby_target_5");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_TARGET_6 =
            registerBombBaby("bomb_baby_target_6");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_TARGET_7 =
            registerBombBaby("bomb_baby_target_7");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_DEATH_1 =
            registerBombBaby("bomb_baby_death_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_DEATH_2 =
            registerBombBaby("bomb_baby_death_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_DEATH_3 =
            registerBombBaby("bomb_baby_death_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_DEATH_4 =
            registerBombBaby("bomb_baby_death_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_DEATH_5 =
            registerBombBaby("bomb_baby_death_5");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_DEATH_6 =
            registerBombBaby("bomb_baby_death_6");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_BABY_DEATH_7 =
            registerBombBaby("bomb_baby_death_7");

    /** 炸弹视野配音：闲置 / 战斗闲置 / 战斗开火（发现目标）/ 死亡。 */
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_1 =
            registerBombSight("bomb_sight_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_2 =
            registerBombSight("bomb_sight_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_3 =
            registerBombSight("bomb_sight_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_4 =
            registerBombSight("bomb_sight_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_5 =
            registerBombSight("bomb_sight_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_6 =
            registerBombSight("bomb_sight_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_7 =
            registerBombSight("bomb_sight_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_8 =
            registerBombSight("bomb_sight_idle_8");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_9 =
            registerBombSight("bomb_sight_idle_9");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_IDLE_10 =
            registerBombSight("bomb_sight_idle_10");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_COMBAT_IDLE_1 =
            registerBombSight("bomb_sight_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_COMBAT_IDLE_2 =
            registerBombSight("bomb_sight_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_COMBAT_IDLE_3 =
            registerBombSight("bomb_sight_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_COMBAT_IDLE_4 =
            registerBombSight("bomb_sight_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_COMBAT_IDLE_5 =
            registerBombSight("bomb_sight_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_COMBAT_IDLE_6 =
            registerBombSight("bomb_sight_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_1 =
            registerBombSight("bomb_sight_target_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_2 =
            registerBombSight("bomb_sight_target_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_3 =
            registerBombSight("bomb_sight_target_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_4 =
            registerBombSight("bomb_sight_target_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_5 =
            registerBombSight("bomb_sight_target_5");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_6 =
            registerBombSight("bomb_sight_target_6");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_7 =
            registerBombSight("bomb_sight_target_7");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_8 =
            registerBombSight("bomb_sight_target_8");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_9 =
            registerBombSight("bomb_sight_target_9");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_TARGET_10 =
            registerBombSight("bomb_sight_target_10");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_DEATH_1 =
            registerBombSight("bomb_sight_death_1");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_DEATH_2 =
            registerBombSight("bomb_sight_death_2");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_DEATH_3 =
            registerBombSight("bomb_sight_death_3");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_DEATH_4 =
            registerBombSight("bomb_sight_death_4");
    public static final RegistryObject<SoundEvent> SOUND_BOMB_SIGHT_DEATH_5 =
            registerBombSight("bomb_sight_death_5");

    /** 史莱姆实验体：闲置 / 战斗闲置共用 9 条。 */
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_1 = registerSlime("slime_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_2 = registerSlime("slime_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_3 = registerSlime("slime_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_4 = registerSlime("slime_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_5 = registerSlime("slime_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_6 = registerSlime("slime_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_7 = registerSlime("slime_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_8 = registerSlime("slime_idle_8");
    public static final RegistryObject<SoundEvent> SOUND_SLIME_IDLE_9 = registerSlime("slime_idle_9");

    /** 毒蝎配音：闲置 7 / 战斗闲置 7 / 发现目标 5 / 死亡 5。 */
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_IDLE_1 =
            registerScorpion("scorpion_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_IDLE_2 =
            registerScorpion("scorpion_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_IDLE_3 =
            registerScorpion("scorpion_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_IDLE_4 =
            registerScorpion("scorpion_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_IDLE_5 =
            registerScorpion("scorpion_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_IDLE_6 =
            registerScorpion("scorpion_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_IDLE_7 =
            registerScorpion("scorpion_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_COMBAT_IDLE_1 =
            registerScorpion("scorpion_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_COMBAT_IDLE_2 =
            registerScorpion("scorpion_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_COMBAT_IDLE_3 =
            registerScorpion("scorpion_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_COMBAT_IDLE_4 =
            registerScorpion("scorpion_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_COMBAT_IDLE_5 =
            registerScorpion("scorpion_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_COMBAT_IDLE_6 =
            registerScorpion("scorpion_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_COMBAT_IDLE_7 =
            registerScorpion("scorpion_combat_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_TARGET_1 =
            registerScorpion("scorpion_target_1");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_TARGET_2 =
            registerScorpion("scorpion_target_2");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_TARGET_3 =
            registerScorpion("scorpion_target_3");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_TARGET_4 =
            registerScorpion("scorpion_target_4");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_TARGET_5 =
            registerScorpion("scorpion_target_5");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_DEATH_1 =
            registerScorpion("scorpion_death_1");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_DEATH_2 =
            registerScorpion("scorpion_death_2");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_DEATH_3 =
            registerScorpion("scorpion_death_3");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_DEATH_4 =
            registerScorpion("scorpion_death_4");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_DEATH_5 =
            registerScorpion("scorpion_death_5");
    public static final RegistryObject<SoundEvent> SOUND_SCORPION_HURT =
            registerScorpion("scorpion_hurt");

    /** 列夫大帅配音：闲置 / 战斗闲置 / 发现目标（射击）。 */
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_IDLE_1 =
            registerLevMarshal("lev_marshal_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_IDLE_2 =
            registerLevMarshal("lev_marshal_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_IDLE_3 =
            registerLevMarshal("lev_marshal_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_IDLE_4 =
            registerLevMarshal("lev_marshal_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_IDLE_5 =
            registerLevMarshal("lev_marshal_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_IDLE_6 =
            registerLevMarshal("lev_marshal_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_IDLE_7 =
            registerLevMarshal("lev_marshal_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_COMBAT_IDLE_1 =
            registerLevMarshal("lev_marshal_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_COMBAT_IDLE_2 =
            registerLevMarshal("lev_marshal_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_COMBAT_IDLE_3 =
            registerLevMarshal("lev_marshal_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_COMBAT_IDLE_4 =
            registerLevMarshal("lev_marshal_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_COMBAT_IDLE_5 =
            registerLevMarshal("lev_marshal_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_COMBAT_IDLE_6 =
            registerLevMarshal("lev_marshal_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_COMBAT_IDLE_7 =
            registerLevMarshal("lev_marshal_combat_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_TARGET_1 =
            registerLevMarshal("lev_marshal_target_1");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_TARGET_2 =
            registerLevMarshal("lev_marshal_target_2");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_TARGET_3 =
            registerLevMarshal("lev_marshal_target_3");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_TARGET_4 =
            registerLevMarshal("lev_marshal_target_4");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_TARGET_5 =
            registerLevMarshal("lev_marshal_target_5");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_TARGET_6 =
            registerLevMarshal("lev_marshal_target_6");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_DEATH_1 =
            registerLevMarshal("lev_marshal_death_1");
    public static final RegistryObject<SoundEvent> SOUND_LEV_MARSHAL_DEATH_2 =
            registerLevMarshal("lev_marshal_death_2");

    /** 对掏天花板配音：闲置 / 战斗闲置 / 看见开火战斗 / 死亡。 */
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_IDLE_1 =
            registerCeilingDuel("ceiling_duel_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_IDLE_2 =
            registerCeilingDuel("ceiling_duel_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_IDLE_3 =
            registerCeilingDuel("ceiling_duel_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_IDLE_4 =
            registerCeilingDuel("ceiling_duel_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_IDLE_5 =
            registerCeilingDuel("ceiling_duel_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_IDLE_6 =
            registerCeilingDuel("ceiling_duel_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_COMBAT_IDLE_1 =
            registerCeilingDuel("ceiling_duel_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_COMBAT_IDLE_2 =
            registerCeilingDuel("ceiling_duel_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_COMBAT_IDLE_3 =
            registerCeilingDuel("ceiling_duel_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_COMBAT_IDLE_4 =
            registerCeilingDuel("ceiling_duel_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_COMBAT_IDLE_5 =
            registerCeilingDuel("ceiling_duel_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_COMBAT_IDLE_6 =
            registerCeilingDuel("ceiling_duel_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_TARGET_1 =
            registerCeilingDuel("ceiling_duel_target_1");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_TARGET_2 =
            registerCeilingDuel("ceiling_duel_target_2");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_TARGET_3 =
            registerCeilingDuel("ceiling_duel_target_3");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_TARGET_4 =
            registerCeilingDuel("ceiling_duel_target_4");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_TARGET_5 =
            registerCeilingDuel("ceiling_duel_target_5");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_TARGET_6 =
            registerCeilingDuel("ceiling_duel_target_6");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_DEATH_1 =
            registerCeilingDuel("ceiling_duel_death_1");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_DEATH_2 =
            registerCeilingDuel("ceiling_duel_death_2");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_DEATH_3 =
            registerCeilingDuel("ceiling_duel_death_3");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_DEATH_4 =
            registerCeilingDuel("ceiling_duel_death_4");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_DEATH_5 =
            registerCeilingDuel("ceiling_duel_death_5");
    public static final RegistryObject<SoundEvent> SOUND_CEILING_DUEL_DEATH_6 =
            registerCeilingDuel("ceiling_duel_death_6");

    /** 大帅护卫军配音：闲置 / 战斗闲置 / 发现目标（射击） / 死亡。 */
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_1 =
            registerMarshalGuard("marshal_guard_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_2 =
            registerMarshalGuard("marshal_guard_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_3 =
            registerMarshalGuard("marshal_guard_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_4 =
            registerMarshalGuard("marshal_guard_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_5 =
            registerMarshalGuard("marshal_guard_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_6 =
            registerMarshalGuard("marshal_guard_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_7 =
            registerMarshalGuard("marshal_guard_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_8 =
            registerMarshalGuard("marshal_guard_idle_8");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_IDLE_9 =
            registerMarshalGuard("marshal_guard_idle_9");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_COMBAT_IDLE_1 =
            registerMarshalGuard("marshal_guard_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_COMBAT_IDLE_2 =
            registerMarshalGuard("marshal_guard_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_COMBAT_IDLE_3 =
            registerMarshalGuard("marshal_guard_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_COMBAT_IDLE_4 =
            registerMarshalGuard("marshal_guard_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_COMBAT_IDLE_5 =
            registerMarshalGuard("marshal_guard_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_COMBAT_IDLE_6 =
            registerMarshalGuard("marshal_guard_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_TARGET_1 =
            registerMarshalGuard("marshal_guard_target_1");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_TARGET_2 =
            registerMarshalGuard("marshal_guard_target_2");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_TARGET_3 =
            registerMarshalGuard("marshal_guard_target_3");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_TARGET_4 =
            registerMarshalGuard("marshal_guard_target_4");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_TARGET_5 =
            registerMarshalGuard("marshal_guard_target_5");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_TARGET_6 =
            registerMarshalGuard("marshal_guard_target_6");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_DEATH_1 =
            registerMarshalGuard("marshal_guard_death_1");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_DEATH_2 =
            registerMarshalGuard("marshal_guard_death_2");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_DEATH_3 =
            registerMarshalGuard("marshal_guard_death_3");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_DEATH_4 =
            registerMarshalGuard("marshal_guard_death_4");
    public static final RegistryObject<SoundEvent> SOUND_MARSHAL_GUARD_DEATH_5 =
            registerMarshalGuard("marshal_guard_death_5");

    /** 歼灭者语音（源目录 jianmie：闲置 / 战斗闲置 / 发现射击 / 死亡） */
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_1 =
            registerAnnihilator("annihilator_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_2 =
            registerAnnihilator("annihilator_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_3 =
            registerAnnihilator("annihilator_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_4 =
            registerAnnihilator("annihilator_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_5 =
            registerAnnihilator("annihilator_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_6 =
            registerAnnihilator("annihilator_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_7 =
            registerAnnihilator("annihilator_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_IDLE_8 =
            registerAnnihilator("annihilator_idle_8");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_COMBAT_IDLE_1 =
            registerAnnihilator("annihilator_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_COMBAT_IDLE_2 =
            registerAnnihilator("annihilator_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_COMBAT_IDLE_3 =
            registerAnnihilator("annihilator_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_COMBAT_IDLE_4 =
            registerAnnihilator("annihilator_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_COMBAT_IDLE_5 =
            registerAnnihilator("annihilator_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_COMBAT_IDLE_6 =
            registerAnnihilator("annihilator_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_COMBAT_IDLE_7 =
            registerAnnihilator("annihilator_combat_idle_7");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_1 =
            registerAnnihilator("annihilator_target_1");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_2 =
            registerAnnihilator("annihilator_target_2");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_3 =
            registerAnnihilator("annihilator_target_3");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_4 =
            registerAnnihilator("annihilator_target_4");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_5 =
            registerAnnihilator("annihilator_target_5");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_6 =
            registerAnnihilator("annihilator_target_6");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_7 =
            registerAnnihilator("annihilator_target_7");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_TARGET_8 =
            registerAnnihilator("annihilator_target_8");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_DEATH_1 =
            registerAnnihilator("annihilator_death_1");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_DEATH_2 =
            registerAnnihilator("annihilator_death_2");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_DEATH_3 =
            registerAnnihilator("annihilator_death_3");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_DEATH_4 =
            registerAnnihilator("annihilator_death_4");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_DEATH_5 =
            registerAnnihilator("annihilator_death_5");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_DEATH_6 =
            registerAnnihilator("annihilator_death_6");
    public static final RegistryObject<SoundEvent> SOUND_ANNIHILATOR_DEATH_7 =
            registerAnnihilator("annihilator_death_7");

    /** 判罚者语音（源目录 判罚者：闲置 / 战斗闲置 / 发现目标 / 死亡） */
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_IDLE_1 =
            registerPunisher("punisher_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_IDLE_2 =
            registerPunisher("punisher_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_IDLE_3 =
            registerPunisher("punisher_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_IDLE_4 =
            registerPunisher("punisher_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_IDLE_5 =
            registerPunisher("punisher_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_IDLE_6 =
            registerPunisher("punisher_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_COMBAT_IDLE_1 =
            registerPunisher("punisher_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_COMBAT_IDLE_2 =
            registerPunisher("punisher_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_COMBAT_IDLE_3 =
            registerPunisher("punisher_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_COMBAT_IDLE_4 =
            registerPunisher("punisher_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_COMBAT_IDLE_5 =
            registerPunisher("punisher_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_COMBAT_IDLE_6 =
            registerPunisher("punisher_combat_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_TARGET_1 =
            registerPunisher("punisher_target_1");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_TARGET_2 =
            registerPunisher("punisher_target_2");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_TARGET_3 =
            registerPunisher("punisher_target_3");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_TARGET_4 =
            registerPunisher("punisher_target_4");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_TARGET_5 =
            registerPunisher("punisher_target_5");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_TARGET_6 =
            registerPunisher("punisher_target_6");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_DEATH_1 =
            registerPunisher("punisher_death_1");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_DEATH_2 =
            registerPunisher("punisher_death_2");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_DEATH_3 =
            registerPunisher("punisher_death_3");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_DEATH_4 =
            registerPunisher("punisher_death_4");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_DEATH_5 =
            registerPunisher("punisher_death_5");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_DEATH_6 =
            registerPunisher("punisher_death_6");
    public static final RegistryObject<SoundEvent> SOUND_PUNISHER_DEATH_7 =
            registerPunisher("punisher_death_7");

    /** 神秘商人闲置语音 */
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_IDLE_1 =
            registerMysteriousMerchant("mysterious_merchant_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_IDLE_2 =
            registerMysteriousMerchant("mysterious_merchant_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_IDLE_3 =
            registerMysteriousMerchant("mysterious_merchant_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_IDLE_4 =
            registerMysteriousMerchant("mysterious_merchant_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_IDLE_5 =
            registerMysteriousMerchant("mysterious_merchant_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_IDLE_6 =
            registerMysteriousMerchant("mysterious_merchant_idle_6");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TARGET_1 =
            registerMysteriousMerchant("mysterious_merchant_target_1");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TARGET_2 =
            registerMysteriousMerchant("mysterious_merchant_target_2");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TARGET_3 =
            registerMysteriousMerchant("mysterious_merchant_target_3");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_1 =
            registerMysteriousMerchant("mysterious_merchant_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_2 =
            registerMysteriousMerchant("mysterious_merchant_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_3 =
            registerMysteriousMerchant("mysterious_merchant_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_4 =
            registerMysteriousMerchant("mysterious_merchant_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_COMBAT_IDLE_5 =
            registerMysteriousMerchant("mysterious_merchant_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_DEATH_1 =
            registerMysteriousMerchant("mysterious_merchant_death_1");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_DEATH_2 =
            registerMysteriousMerchant("mysterious_merchant_death_2");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_DEATH_3 =
            registerMysteriousMerchant("mysterious_merchant_death_3");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_DEATH_4 =
            registerMysteriousMerchant("mysterious_merchant_death_4");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_1 =
            registerMysteriousMerchant("mysterious_merchant_trade_success_1");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_2 =
            registerMysteriousMerchant("mysterious_merchant_trade_success_2");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_3 =
            registerMysteriousMerchant("mysterious_merchant_trade_success_3");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_4 =
            registerMysteriousMerchant("mysterious_merchant_trade_success_4");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_5 =
            registerMysteriousMerchant("mysterious_merchant_trade_success_5");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_6 =
            registerMysteriousMerchant("mysterious_merchant_trade_success_6");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_SUCCESS_7 =
            registerMysteriousMerchant("mysterious_merchant_trade_success_7");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_1 =
            registerMysteriousMerchant("mysterious_merchant_trade_fail_1");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_2 =
            registerMysteriousMerchant("mysterious_merchant_trade_fail_2");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_3 =
            registerMysteriousMerchant("mysterious_merchant_trade_fail_3");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_4 =
            registerMysteriousMerchant("mysterious_merchant_trade_fail_4");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_5 =
            registerMysteriousMerchant("mysterious_merchant_trade_fail_5");
    public static final RegistryObject<SoundEvent> SOUND_MYSTERIOUS_MERCHANT_TRADE_FAIL_6 =
            registerMysteriousMerchant("mysterious_merchant_trade_fail_6");

    /** 警察闲置 / 战斗闲置 / 发现目标 / 死亡 */
    public static final RegistryObject<SoundEvent> SOUND_POLICE_IDLE_1 = registerPolice("police_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_IDLE_2 = registerPolice("police_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_IDLE_3 = registerPolice("police_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_IDLE_4 = registerPolice("police_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_IDLE_5 = registerPolice("police_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_COMBAT_IDLE_1 =
            registerPolice("police_combat_idle_1");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_COMBAT_IDLE_2 =
            registerPolice("police_combat_idle_2");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_COMBAT_IDLE_3 =
            registerPolice("police_combat_idle_3");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_COMBAT_IDLE_4 =
            registerPolice("police_combat_idle_4");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_COMBAT_IDLE_5 =
            registerPolice("police_combat_idle_5");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_TARGET_1 = registerPolice("police_target_1");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_TARGET_2 = registerPolice("police_target_2");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_TARGET_3 = registerPolice("police_target_3");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_TARGET_4 = registerPolice("police_target_4");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_TARGET_5 = registerPolice("police_target_5");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_DEATH_1 = registerPolice("police_death_1");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_DEATH_2 = registerPolice("police_death_2");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_DEATH_3 = registerPolice("police_death_3");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_DEATH_4 = registerPolice("police_death_4");
    public static final RegistryObject<SoundEvent> SOUND_POLICE_DEATH_5 = registerPolice("police_death_5");

    /** 护航存储器：存入 / 放出 */
    public static final RegistryObject<SoundEvent> SOUND_BODYGUARD_STORAGE_XIU =
            registerSoldier("bodyguard_storage_xiu");
    public static final RegistryObject<SoundEvent> SOUND_BODYGUARD_STORAGE_BIU =
            registerSoldier("bodyguard_storage_biu");

    /** 士兵战术语音（oggnpc） */
    public static final RegistryObject<SoundEvent> SOUND_FIRE_AT_WILL = registerSoldier("fire_at_will");
    public static final RegistryObject<SoundEvent> SOUND_GOOD_WORK_SOLDIER =
            registerSoldier("good_work_soldier");
    public static final RegistryObject<SoundEvent> SOUND_HOLD_YOUR_POSITION =
            registerSoldier("hold_your_position");
    public static final RegistryObject<SoundEvent> SOUND_HOSTILES_INBOUND =
            registerSoldier("hostiles_inbound");
    public static final RegistryObject<SoundEvent> SOUND_OBJECTIVE_SECURED =
            registerSoldier("objective_secured");
    public static final RegistryObject<SoundEvent> SOUND_SECURE_THE_PERIMETER =
            registerSoldier("secure_the_perimeter");
    public static final RegistryObject<SoundEvent> SOUND_WATCH_YOUR_AMMO = registerSoldier("watch_your_ammo");
    public static final RegistryObject<SoundEvent> SOUND_WATCH_YOUR_SIX = registerSoldier("watch_your_six");
    public static final RegistryObject<SoundEvent> SOUND_WERE_UNDER_ATTACK =
            registerSoldier("were_under_attack");
    public static final RegistryObject<SoundEvent> SOUND_AREA_CLEAR = registerSoldier("area_clear");
    public static final RegistryObject<SoundEvent> SOUND_ENEMY_ON_THE_MOVE =
            registerSoldier("enemy_on_the_move");
    public static final RegistryObject<SoundEvent> SOUND_ENTERING_HOSTILE_TERRITORY =
            registerSoldier("entering_hostile_territory");

    private static RegistryObject<SoundEvent> registerWarMachine(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerBombCart(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerBombBaby(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerBombSight(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerSlime(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerScorpion(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerLevMarshal(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerCeilingDuel(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerMarshalGuard(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerAnnihilator(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerPunisher(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerMysteriousMerchant(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerPolice(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerSoldier(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    private static RegistryObject<SoundEvent> registerMedicine(String id) {
        return SOUNDS_EVENTS.register(
                id,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, id)));
    }

    public static void register(IEventBus pEventBus) {
        SOUNDS_EVENTS.register(pEventBus);
    }
}
