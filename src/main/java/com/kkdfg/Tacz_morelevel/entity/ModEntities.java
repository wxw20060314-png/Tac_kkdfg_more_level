package com.kkdfg.Tacz_morelevel.entity;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.AnnihilatorEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.ScorpionEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SlimeCommanderEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SlimeWarriorEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.PunisherShoulderCannonEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.BombSightEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AssaultSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.LurkerEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.DetectionGrenadeEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.FlashbangEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MolotovEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MercenarySoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MilitiaEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MysteriousMerchantEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.PoliceEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.PunisherEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.RefugeeEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SpecialForcesSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SuperSpecialSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.LevMarshalEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MarshalGuardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.DemonLordBodyguardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.EliteBodyguardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.BeautyBodyguardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.GuardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.StandardBodyguardEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组实体类型注册。
 *
 * <p>编码约定见仓库根目录 {@code Tacz_morelevel规范手册.txt}。
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Tacz_MoreLevel.MOD_ID);

    public static final RegistryObject<EntityType<MilitiaEntity>> MILITIA =
            ENTITY_TYPES.register(
                    "militia",
                    () ->
                            EntityType.Builder.of(MilitiaEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":militia"));

    public static final RegistryObject<EntityType<RefugeeEntity>> REFUGEE =
            ENTITY_TYPES.register(
                    "refugee",
                    () ->
                            EntityType.Builder.of(RefugeeEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":refugee"));

    public static final RegistryObject<EntityType<MercenarySoldierEntity>> MERCENARY =
            ENTITY_TYPES.register(
                    "mercenary",
                    () ->
                            EntityType.Builder.of(MercenarySoldierEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":mercenary"));

    public static final RegistryObject<EntityType<AssaultSoldierEntity>> ASSAULT_SOLDIER =
            ENTITY_TYPES.register(
                    "assault_soldier",
                    () ->
                            EntityType.Builder.of(AssaultSoldierEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":assault_soldier"));

    /** 潜伏者：独立 [lurker] 配置；固定隐身套；默认不远距刷没。 */
    public static final RegistryObject<EntityType<LurkerEntity>> LURKER =
            ENTITY_TYPES.register(
                    "lurker",
                    () ->
                            EntityType.Builder.of(LurkerEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":lurker"));

    public static final RegistryObject<EntityType<SpecialForcesSoldierEntity>> SPECIAL_FORCES =
            ENTITY_TYPES.register(
                    "special_forces",
                    () ->
                            EntityType.Builder.of(SpecialForcesSoldierEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":special_forces"));

    public static final RegistryObject<EntityType<SuperSpecialSoldierEntity>> SUPER_SPECIAL_SOLDIER =
            ENTITY_TYPES.register(
                    "super_special_soldier",
                    () ->
                            EntityType.Builder.of(SuperSpecialSoldierEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":super_special_soldier"));

    public static final RegistryObject<EntityType<LevMarshalEntity>> LEV_MARSHAL =
            ENTITY_TYPES.register(
                    "lev_marshal",
                    () ->
                            EntityType.Builder.of(LevMarshalEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":lev_marshal"));

    public static final RegistryObject<EntityType<MarshalGuardEntity>> MARSHAL_GUARD =
            ENTITY_TYPES.register(
                    "marshal_guard",
                    () ->
                            EntityType.Builder.of(MarshalGuardEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":marshal_guard"));

    public static final RegistryObject<EntityType<WarMachineEntity>> WAR_MACHINE =
            ENTITY_TYPES.register(
                    "war_machine",
                    () ->
                            EntityType.Builder.of(WarMachineEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":war_machine"));

    public static final RegistryObject<EntityType<AnnihilatorEntity>> ANNIHILATOR =
            ENTITY_TYPES.register(
                    "annihilator",
                    () ->
                            EntityType.Builder.of(AnnihilatorEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":annihilator"));

    /** 毒蝎：双枪 M95+Vector；潜行七级；有目标开潜行；数值默认同歼灭者。 */
    public static final RegistryObject<EntityType<ScorpionEntity>> SCORPION =
            ENTITY_TYPES.register(
                    "scorpion",
                    () ->
                            EntityType.Builder.of(ScorpionEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":scorpion"));

    /** 史莱姆指挥官 Boss：固定 gen0；分裂只出战士。 */
    public static final RegistryObject<EntityType<SlimeCommanderEntity>> SLIME_COMMANDER =
            ENTITY_TYPES.register(
                    "slime_commander",
                    () ->
                            EntityType.Builder.of(SlimeCommanderEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":slime_commander"));

    /** 史莱姆战士：自然刷 gen0/1 随机；指挥官谱系分裂产物。 */
    public static final RegistryObject<EntityType<SlimeWarriorEntity>> SLIME_WARRIOR =
            ENTITY_TYPES.register(
                    "slime_warrior",
                    () ->
                            EntityType.Builder.of(SlimeWarriorEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":slime_warrior"));

    /** 炸弹宝宝：主手爆破 MK14 + 副手榴弹；穿爆破七级；抗性 III。 */
    public static final RegistryObject<EntityType<BombBabyEntity>> BOMB_BABY =
            ENTITY_TYPES.register(
                    "bomb_baby",
                    () ->
                            EntityType.Builder.of(BombBabyEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":bomb_baby"));

    public static final RegistryObject<EntityType<CeilingDuelEntity>> CEILING_DUEL =
            ENTITY_TYPES.register(
                    "ceiling_duel",
                    () ->
                            EntityType.Builder.of(CeilingDuelEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":ceiling_duel"));

    /** 炸弹视野：枪套 AA12 冲击波 + 冲击波防弹；抗性见 bosses.toml [bombSight]。 */
    public static final RegistryObject<EntityType<BombSightEntity>> BOMB_SIGHT =
            ENTITY_TYPES.register(
                    "bomb_sight",
                    () ->
                            EntityType.Builder.of(BombSightEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":bomb_sight"));

    /** 判罚者：被动（不主动锁玩家/友军），副手 FN EVOLYS 传说 + 判罚者套；数值硬编码同超特。 */
    public static final RegistryObject<EntityType<PunisherEntity>> PUNISHER =
            ENTITY_TYPES.register(
                    "punisher",
                    () ->
                            EntityType.Builder.of(PunisherEntity::new, MobCategory.MONSTER)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":punisher"));

    public static final RegistryObject<EntityType<MysteriousMerchantEntity>> MYSTERIOUS_MERCHANT =
            ENTITY_TYPES.register(
                    "mysterious_merchant",
                    () ->
                            EntityType.Builder.of(MysteriousMerchantEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":mysterious_merchant"));

    /** 警察：中立；独立 [police] / troop_guns/police；警官套；集体仇恨；趴寻路；CoverStrafe 交火。 */
    public static final RegistryObject<EntityType<PoliceEntity>> POLICE =
            ENTITY_TYPES.register(
                    "police",
                    () ->
                            EntityType.Builder.of(PoliceEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":police"));

    /** 中立炸弹小车：有主追仇恨目标自爆；无主待机。碰撞约车体大小。 */
    public static final RegistryObject<EntityType<BombCartEntity>> BOMB_CART =
            ENTITY_TYPES.register(
                    "bomb_cart",
                    () ->
                            EntityType.Builder.<BombCartEntity>of(BombCartEntity::new, MobCategory.CREATURE)
                                    .sized(0.9F, 0.6F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":bomb_cart"));

    public static final RegistryObject<EntityType<PunisherShoulderCannonEntity>> PUNISHER_SHOULDER_CANNON =
            ENTITY_TYPES.register(
                    "punisher_shoulder_cannon",
                    () ->
                            EntityType.Builder.<PunisherShoulderCannonEntity>of(
                                            PunisherShoulderCannonEntity::new, MobCategory.MISC)
                                    .sized(0.4F, 0.4F)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(Tacz_MoreLevel.MOD_ID + ":punisher_shoulder_cannon"));

    public static final RegistryObject<EntityType<GrenadeEntity>> GRENADE =
            ENTITY_TYPES.register(
                    "grenade",
                    () ->
                            EntityType.Builder.<GrenadeEntity>of(GrenadeEntity::new, MobCategory.MISC)
                                    .sized(0.25F, 0.25F)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(Tacz_MoreLevel.MOD_ID + ":grenade"));

    public static final RegistryObject<EntityType<FlashbangEntity>> FLASHBANG =
            ENTITY_TYPES.register(
                    "flashbang",
                    () ->
                            EntityType.Builder.<FlashbangEntity>of(
                                            FlashbangEntity::new, MobCategory.MISC)
                                    .sized(0.25F, 0.25F)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(Tacz_MoreLevel.MOD_ID + ":flashbang"));

    public static final RegistryObject<EntityType<MolotovEntity>> MOLOTOV =
            ENTITY_TYPES.register(
                    "molotov",
                    () ->
                            EntityType.Builder.<MolotovEntity>of(MolotovEntity::new, MobCategory.MISC)
                                    .sized(0.25F, 0.25F)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(Tacz_MoreLevel.MOD_ID + ":molotov"));

    public static final RegistryObject<EntityType<DetectionGrenadeEntity>> DETECTION_GRENADE =
            ENTITY_TYPES.register(
                    "detection_grenade",
                    () ->
                            EntityType.Builder.<DetectionGrenadeEntity>of(
                                            DetectionGrenadeEntity::new, MobCategory.MISC)
                                    .sized(0.25F, 0.25F)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(Tacz_MoreLevel.MOD_ID + ":detection_grenade"));

    public static final RegistryObject<EntityType<BeautyBodyguardEntity>> BEAUTY_BODYGUARD =
            registerBodyguard("beauty_bodyguard", BeautyBodyguardEntity::new);

    public static final RegistryObject<EntityType<StandardBodyguardEntity>> STANDARD_BODYGUARD =
            registerBodyguard("standard_bodyguard", StandardBodyguardEntity::new);

    public static final RegistryObject<EntityType<EliteBodyguardEntity>> ELITE_BODYGUARD =
            registerBodyguard("elite_bodyguard", EliteBodyguardEntity::new);

    public static final RegistryObject<EntityType<DemonLordBodyguardEntity>> DEMON_LORD_BODYGUARD =
            registerBodyguard("demon_lord_bodyguard", DemonLordBodyguardEntity::new);

    /** 玩家友方护卫（CREATURE）；军官胸甲名册双向链路。 */
    public static final RegistryObject<EntityType<GuardEntity>> GUARD =
            ENTITY_TYPES.register(
                    "guard",
                    () ->
                            EntityType.Builder.of(GuardEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(10)
                                    .build(Tacz_MoreLevel.MOD_ID + ":guard"));

    private static <T extends AbstractFriendlyCompanionEntity> RegistryObject<EntityType<T>> registerBodyguard(
            String id, EntityType.EntityFactory<T> factory) {
        return ENTITY_TYPES.register(
                id,
                () ->
                        EntityType.Builder.of(factory, MobCategory.CREATURE)
                                .sized(0.6F, 1.8F)
                                .clientTrackingRange(10)
                                .build(Tacz_MoreLevel.MOD_ID + ":" + id));
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    private ModEntities() {}
}
