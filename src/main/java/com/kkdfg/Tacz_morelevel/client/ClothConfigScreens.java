package com.kkdfg.Tacz_morelevel.client;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.config.BossConfig;
import com.kkdfg.Tacz_morelevel.config.NpcConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Cloth Config 屏（对照官方：ConfigBuilder → Category → EntryBuilder → setSavingRunnable）。
 * 全局：血量 / 移速 / moveTo / strafe（NPC+Boss）与对玩家伤害。
 */
public final class ClothConfigScreens {
    private ClothConfigScreens() {}

    /**
     * 模组列表 Config 按钮。在模组构造里用注入的 {@link FMLJavaModLoadingContext}
     * 调 {@code registerExtensionPoint}（勿用已过时的 {@code ModLoadingContext.get()}）。
     */
    public static void register(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () ->
                        new ConfigScreenHandler.ConfigScreenFactory(
                                (minecraft, parent) -> create(parent)));
    }

    /** 每次打开新建 builder（官方要求）。 */
    public static Screen create(Screen parent) {
        ConfigBuilder builder =
                ConfigBuilder.create()
                        .setParentScreen(parent)
                        .setTitle(Component.translatable("config.tacz_morelevel.title"));

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory npc = builder.getOrCreateCategory(Component.translatable("config.tacz_morelevel.category.npc"));
        addAbPair(
                npc,
                entries,
                "config.tacz_morelevel.npc.max_health",
                "config.tacz_morelevel.tooltip.max_health",
                NpcConfig.maxHealthOffsetA(),
                NpcConfig.maxHealthScaleB());
        addAbPair(
                npc,
                entries,
                "config.tacz_morelevel.npc.movement_speed",
                "config.tacz_morelevel.tooltip.movement_speed",
                NpcConfig.movementSpeedOffsetA(),
                NpcConfig.movementSpeedScaleB());
        addAbPair(
                npc,
                entries,
                "config.tacz_morelevel.npc.gun_goal_speed",
                "config.tacz_morelevel.tooltip.gun_goal_speed",
                NpcConfig.gunGoalSpeedModifierOffsetA(),
                NpcConfig.gunGoalSpeedModifierScaleB());
        addAbPair(
                npc,
                entries,
                "config.tacz_morelevel.npc.gun_strafe_speed",
                "config.tacz_morelevel.tooltip.gun_strafe_speed",
                NpcConfig.gunStrafeSpeedModifierOffsetA(),
                NpcConfig.gunStrafeSpeedModifierScaleB());

        ConfigCategory boss =
                builder.getOrCreateCategory(Component.translatable("config.tacz_morelevel.category.boss"));
        addAbPair(
                boss,
                entries,
                "config.tacz_morelevel.boss.max_health",
                "config.tacz_morelevel.tooltip.max_health",
                BossConfig.maxHealthOffsetA(),
                BossConfig.maxHealthScaleB());
        addAbPair(
                boss,
                entries,
                "config.tacz_morelevel.boss.movement_speed",
                "config.tacz_morelevel.tooltip.movement_speed",
                BossConfig.movementSpeedOffsetA(),
                BossConfig.movementSpeedScaleB());
        addAbPair(
                boss,
                entries,
                "config.tacz_morelevel.boss.gun_goal_speed",
                "config.tacz_morelevel.tooltip.gun_goal_speed",
                BossConfig.gunGoalSpeedModifierOffsetA(),
                BossConfig.gunGoalSpeedModifierScaleB());
        addAbPair(
                boss,
                entries,
                "config.tacz_morelevel.boss.gun_strafe_speed",
                "config.tacz_morelevel.tooltip.gun_strafe_speed",
                BossConfig.gunStrafeSpeedModifierOffsetA(),
                BossConfig.gunStrafeSpeedModifierScaleB());

        ConfigCategory general =
                builder.getOrCreateCategory(Component.translatable("config.tacz_morelevel.category.general"));
        ForgeConfigSpec.DoubleValue damageScale = Config.npcOutgoingDamageToPlayerScaleSpec();
        general.addEntry(
                entries.startDoubleField(
                                Component.translatable("config.tacz_morelevel.npc_outgoing_damage"),
                                damageScale.get())
                        .setDefaultValue(damageScale.getDefault())
                        .setMin(0.01D)
                        .setMax(100.0D)
                        .setTooltip(Component.translatable("config.tacz_morelevel.tooltip.npc_outgoing_damage"))
                        .setSaveConsumer(damageScale::set)
                        .build());

        // 官方：条目 set 后 saving runnable 调 ForgeConfigSpec.save()
        builder.setSavingRunnable(
                () -> {
                    NpcConfig.SPEC.save();
                    BossConfig.SPEC.save();
                    Config.SPEC.save();
                    NpcConfig.applyLoadedValues();
                    BossConfig.applyLoadedValues();
                    Config.applyLoadedValues();
                });

        return builder.build();
    }

    private static void addAbPair(
            ConfigCategory category,
            ConfigEntryBuilder entries,
            String nameKey,
            String tooltipKey,
            ForgeConfigSpec.DoubleValue offsetA,
            ForgeConfigSpec.DoubleValue scaleB) {
        category.addEntry(
                entries.startDoubleField(
                                Component.translatable(nameKey + ".offset_a"), offsetA.get())
                        .setDefaultValue(offsetA.getDefault())
                        .setMin(-1000.0D)
                        .setMax(1000.0D)
                        .setTooltip(Component.translatable(tooltipKey))
                        .setSaveConsumer(offsetA::set)
                        .build());
        category.addEntry(
                entries.startDoubleField(
                                Component.translatable(nameKey + ".scale_b"), scaleB.get())
                        .setDefaultValue(scaleB.getDefault())
                        .setMin(-1000.0D)
                        .setMax(1000.0D)
                        .setTooltip(Component.translatable(tooltipKey))
                        .setSaveConsumer(scaleB::set)
                        .build());
    }
}
