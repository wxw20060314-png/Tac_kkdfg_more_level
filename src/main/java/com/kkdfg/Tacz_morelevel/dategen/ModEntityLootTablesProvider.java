package com.kkdfg.Tacz_morelevel.dategen;

import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import java.util.stream.Stream;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ModEntityLootTablesProvider extends EntityLootSubProvider {

    public ModEntityLootTablesProvider() {
        super(FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    public void generate() {
        refugeeDrops();
        militiaDrops();
        mercenaryDrops();
        assaultDrops();
        lurkerDrops();
        specialForcesDrops();
        superSpecialDrops();
        annihilatorDrops();
        scorpionDrops();
        slimeExperimentDrops();
        warMachineDrops();
        bombSightDrops();
        bombBabyDrops();
        punisherDrops();
    }

    private void refugeeDrops() {
        this.add(
                ModEntities.REFUGEE.get(),
                soldierExtraDrops(
                        ModItems.IMPROVED_GUN_UPGRADE_VOUCHER.get(),
                        0.05F,
                        ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_1.get(),
                        0.10F,
                        1,
                        2));
    }

    private void militiaDrops() {
        this.add(
                ModEntities.MILITIA.get(),
                soldierExtraDrops(
                        ModItems.IMPROVED_GUN_UPGRADE_VOUCHER.get(),
                        0.10F,
                        ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_1.get(),
                        0.20F,
                        1,
                        2));
    }

    private void mercenaryDrops() {
        this.add(
                ModEntities.MERCENARY.get(),
                soldierExtraDrops(
                        ModItems.REFINED_GUN_UPGRADE_VOUCHER.get(),
                        0.10F,
                        ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_2.get(),
                        0.20F,
                        1,
                        2));
    }

    private void assaultDrops() {
        this.add(
                ModEntities.ASSAULT_SOLDIER.get(),
                soldierExtraDrops(
                        ModItems.PERFECT_GUN_UPGRADE_VOUCHER.get(),
                        0.10F,
                        ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_3.get(),
                        0.20F,
                        1,
                        2));
    }

    private void lurkerDrops() {
        this.add(
                ModEntities.LURKER.get(),
                soldierExtraDrops(
                        ModItems.PERFECT_GUN_UPGRADE_VOUCHER.get(),
                        0.10F,
                        ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_3.get(),
                        0.20F,
                        1,
                        2));
    }

    private void specialForcesDrops() {
        this.add(
                ModEntities.SPECIAL_FORCES.get(),
                soldierExtraDrops(
                        ModItems.EXCELLENT_GUN_UPGRADE_VOUCHER.get(),
                        0.10F,
                        ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_4.get(),
                        0.20F,
                        1,
                        2));
    }

    private void superSpecialDrops() {
        this.add(
                ModEntities.SUPER_SPECIAL_SOLDIER.get(),
                soldierExtraDrops(
                        ModItems.EXCELLENT_GUN_UPGRADE_VOUCHER.get(),
                        0.20F,
                        ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_4.get(),
                        0.30F,
                        1,
                        3));
    }

    /** 歼灭者：必定掉落 1–3 个时空之石（物品上限 1，用 rolls 拆开）。 */
    private void annihilatorDrops() {
        this.add(
                ModEntities.ANNIHILATOR.get(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                                        .add(LootItem.lootTableItem(ModItems.SPACETIME_STONE.get()))));
    }

    /** 毒蝎：吸光管 3–5；装备掉落走槽位概率。 */
    private void scorpionDrops() {
        this.add(
                ModEntities.SCORPION.get(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(ModItems.LIGHT_ABSORBING_TUBE.get())
                                                        .apply(
                                                                SetItemCountFunction.setCount(
                                                                        UniformGenerator.between(
                                                                                3.0F, 5.0F))))));
    }

    /** 史莱姆战士：粘液球；指挥官：粘液球 + 史莱姆血清。 */
    private void slimeExperimentDrops() {
        this.add(
                ModEntities.SLIME_WARRIOR.get(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.SLIME_BALL)
                                                        .apply(
                                                                SetItemCountFunction.setCount(
                                                                        UniformGenerator.between(
                                                                                1.0F, 3.0F))))));
        this.add(
                ModEntities.SLIME_COMMANDER.get(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(Items.SLIME_BALL)
                                                        .apply(
                                                                SetItemCountFunction.setCount(
                                                                        UniformGenerator.between(
                                                                                2.0F, 5.0F)))))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(LootItem.lootTableItem(ModItems.SLIME_SERUM.get()).apply(
                                            SetItemCountFunction.setCount(
                                                UniformGenerator.between(
                                                    4.0F, 7.0F))))));
    }

    /** 战争机器：金色能量核心 1–2、金色动力推进器 3–5。 */
    private void warMachineDrops() {
        this.add(
                ModEntities.WAR_MACHINE.get(),
                bossCoreThrusterDrops(
                        ModItems.ENERGY_CORE_GOLD.get(),
                        1,
                        2,
                        ModItems.POWER_THRUSTER_GOLD.get(),
                        3,
                        5));
    }

    /** 炸弹视野：红色能量核心 1–2、红色动力推进器 3–5。 */
    private void bombSightDrops() {
        this.add(
                ModEntities.BOMB_SIGHT.get(),
                bossCoreThrusterDrops(
                        ModItems.ENERGY_CORE_RED.get(),
                        1,
                        2,
                        ModItems.POWER_THRUSTER_RED.get(),
                        3,
                        5));
    }

    /** 炸弹宝宝：装备掉落走 20% 槽位概率；实体表暂空。 */
    private void bombBabyDrops() {
        this.add(ModEntities.BOMB_BABY.get(), LootTable.lootTable());
    }

    /** 判罚者：必定掉落 1 个超级升级模板。 */
    private void punisherDrops() {
        this.add(
                ModEntities.PUNISHER.get(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(
                                                        ModItems.SUPER_UPGRADE_TEMPLATE.get()))));
    }

    private static LootTable.Builder bossCoreThrusterDrops(
            Item core, int coreMin, int coreMax, Item thruster, int thrusterMin, int thrusterMax) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(
                                        LootItem.lootTableItem(core)
                                                .apply(
                                                        SetItemCountFunction.setCount(
                                                                UniformGenerator.between(coreMin, coreMax)))))
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(
                                        LootItem.lootTableItem(thruster)
                                                .apply(
                                                        SetItemCountFunction.setCount(
                                                                UniformGenerator.between(
                                                                        thrusterMin, thrusterMax)))));
    }

    private static LootTable.Builder soldierExtraDrops(
            Item voucher,
            float voucherChance,
            Item money,
            float moneyChance,
            int moneyCountMin,
            int moneyCountMax) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(LootItemRandomChanceCondition.randomChance(voucherChance))
                                .add(LootItem.lootTableItem(voucher)))
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(LootItemRandomChanceCondition.randomChance(moneyChance))
                                .add(
                                        LootItem.lootTableItem(money)
                                                .apply(
                                                        SetItemCountFunction.setCount(
                                                                UniformGenerator.between(
                                                                        moneyCountMin, moneyCountMax)))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(
                ModEntities.REFUGEE.get(),
                ModEntities.MILITIA.get(),
                ModEntities.MERCENARY.get(),
                ModEntities.ASSAULT_SOLDIER.get(),
                ModEntities.LURKER.get(),
                ModEntities.SPECIAL_FORCES.get(),
                ModEntities.SUPER_SPECIAL_SOLDIER.get(),
                ModEntities.ANNIHILATOR.get(),
                ModEntities.SCORPION.get(),
                ModEntities.SLIME_COMMANDER.get(),
                ModEntities.SLIME_WARRIOR.get(),
                ModEntities.WAR_MACHINE.get(),
                ModEntities.BOMB_SIGHT.get(),
                ModEntities.BOMB_BABY.get(),
                ModEntities.PUNISHER.get());
    }
}
