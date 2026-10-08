package com.kkdfg.Tacz_morelevel.gunupgrade;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code /tacz_morelevel gun_upgrade <all|clear|test>} — 权限 0；只写盘，不 reload。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GunUpgradeCommands {

    private static final String URL_PACKET_FIXER =
            "https://www.curseforge.com/minecraft/mc-mods/packet-fixer";
    private static final String URL_XXL_PACKETS =
            "https://www.curseforge.com/minecraft/mc-mods/xxl-packets";

    private GunUpgradeCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("tacz_morelevel")
                        .then(
                                Commands.literal("gun_upgrade")
                                        .then(Commands.literal("test").executes(GunUpgradeCommands::test))
                                        .then(Commands.literal("all").executes(GunUpgradeCommands::all))
                                        .then(
                                                Commands.literal("clear")
                                                        .executes(GunUpgradeCommands::clear)));
        dispatcher.register(root);
    }

    private static int test(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();
        try {
            GunUpgradeGenerator.ProbeResult r = GunUpgradeGenerator.probe(server);
            source.sendSuccess(
                    () ->
                            Component.translatable(
                                    "commands.tacz_morelevel.gun_upgrade.test.success",
                                    r.scanned(),
                                    r.eligible(),
                                    r.withRecipe(),
                                    r.withoutRecipe(),
                                    GunUpgradePackPaths.packRoot(server).toString()),
                    false);
            return r.eligible();
        } catch (Exception e) {
            source.sendFailure(
                    Component.translatable(
                            "commands.tacz_morelevel.gun_upgrade.test.failed",
                            e.getMessage() == null ? "" : e.getMessage()));
            return 0;
        }
    }

    private static int all(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();
        try {
            GunUpgradeGenerator.GenerateResult r = GunUpgradeGenerator.generateAll(server);
            source.sendSuccess(
                    () ->
                            Component.translatable(
                                    "commands.tacz_morelevel.gun_upgrade.all.success",
                                    r.scanned(),
                                    r.eligible(),
                                    r.gunsOk(),
                                    r.gunsSkipped(),
                                    r.writtenFiles(),
                                    r.recipesWritten(),
                                    GunUpgradePackPaths.packRoot(server).toString()),
                    true);
            sendPacketSizeHint(source);
            return r.gunsOk();
        } catch (Exception e) {
            source.sendFailure(
                    Component.translatable(
                            "commands.tacz_morelevel.gun_upgrade.all.failed",
                            e.getMessage() == null ? "" : e.getMessage()));
            return 0;
        }
    }

    /** 升级包可能触发发包过大；提示安装 Packet Fixer / XXL Packets（不检测是否已装）。 */
    private static void sendPacketSizeHint(CommandSourceStack source) {
        source.sendSuccess(
                () ->
                        Component.translatable("message.tacz_morelevel.packet_size_hint")
                                .withStyle(ChatFormatting.GOLD),
                false);
        source.sendSuccess(
                () ->
                        Component.literal("  ")
                                .append(
                                        link(
                                                "message.tacz_morelevel.packet_fixer",
                                                "message.tacz_morelevel.packet_fixer.tooltip",
                                                URL_PACKET_FIXER))
                                .append(Component.literal("  ").withStyle(ChatFormatting.GRAY))
                                .append(
                                        link(
                                                "message.tacz_morelevel.xxl_packets",
                                                "message.tacz_morelevel.xxl_packets.tooltip",
                                                URL_XXL_PACKETS)),
                false);
        source.sendSuccess(
                () ->
                        Component.translatable("message.tacz_morelevel.packet_size_hint.tail")
                                .withStyle(ChatFormatting.GRAY),
                false);
    }

    private static MutableComponent link(String nameKey, String tooltipKey, String url) {
        return Component.translatable(nameKey)
                .withStyle(
                        Style.EMPTY
                                .withColor(ChatFormatting.AQUA)
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                .withHoverEvent(
                                        new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.translatable(tooltipKey))));
    }

    private static int clear(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();
        try {
            GunUpgradeGenerator.clearPack(server);
            source.sendSuccess(
                    () ->
                            Component.translatable(
                                    "commands.tacz_morelevel.gun_upgrade.clear.success",
                                    GunUpgradePackPaths.packRoot(server).toString()),
                    true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(
                    Component.translatable(
                            "commands.tacz_morelevel.gun_upgrade.clear.failed",
                            e.getMessage() == null ? "" : e.getMessage()));
            return 0;
        }
    }
}
