package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.item.custom.SpacetimeBulletproofArmorItem;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 时空套传说技能：站立+传说键按视线记点；蹲下+传说键回传。
 * 冷却仅作用于回传（胸甲 TTL，默认 5s）。旧视线/随机闪现见 {@link com.kkdfg.Tacz_morelevel.item.custom.SpacetimeStoneItem}。
 */
public final class SpacetimeBlinkSkill implements ArmorLegendarySkill {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "spacetime_blink");

    private static final SpacetimeBlinkSkill INSTANCE = new SpacetimeBlinkSkill();

    private SpacetimeBlinkSkill() {}

    public static void register() {
        ArmorLegendarySkillRegistry.register(INSTANCE);
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return SpacetimeBulletproofArmorItem.countEquippedPieces(entity)
                >= SpacetimeBulletproofArmorItem.FULL_SET_PIECES;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean canActivate(LivingEntity entity) {
        if (!(entity instanceof Player player) || player.isSpectator()) {
            return false;
        }
        if (!hasFullSet(player)) {
            return false;
        }
        // 站立记点：无冷却限制；蹲下回传：冷却中不可激活
        if (player.isShiftKeyDown()) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            return !SpacetimeBulletproofArmorItem.isBlinkOnCooldown(chest);
        }
        return true;
    }

    @Override
    public boolean activate(LivingEntity entity) {
        if (!(entity instanceof Player player) || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!hasFullSet(player)) {
            return false;
        }
        if (player.isShiftKeyDown()) {
            return tryTeleportToWaypoint(player);
        }
        return tryRecordLookWaypoint(player, level);
    }

    /** 站立：视线落点记点（原闪现射线规则）；不合规不覆盖旧存档。 */
    private static boolean tryRecordLookWaypoint(Player player, ServerLevel level) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof SpacetimeBulletproofArmorItem)) {
            return false;
        }
        Optional<Vec3> dest = SpacetimeBlinkHelper.resolvePlayerLookBlink(level, player);
        if (dest.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.spacetime.record_invalid")
                            .withStyle(ChatFormatting.RED),
                    true);
            return false;
        }
        Vec3 pos = dest.get();
        ResourceLocation dim = level.dimension().location();
        SpacetimeBulletproofArmorItem.writeWaypoint(chest, dim, pos.x, pos.y, pos.z);
        player.displayClientMessage(
                Component.translatable(
                                "message.tacz_morelevel.spacetime.record_success",
                                formatCoord(pos.x),
                                formatCoord(pos.y),
                                formatCoord(pos.z))
                        .withStyle(ChatFormatting.AQUA),
                true);
        return true;
    }

    /** 蹲下：回传到胸甲存档点。 */
    private static boolean tryTeleportToWaypoint(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (SpacetimeBulletproofArmorItem.isBlinkOnCooldown(chest)) {
            return false;
        }
        Optional<SpacetimeBulletproofArmorItem.Waypoint> waypoint =
                SpacetimeBulletproofArmorItem.readWaypoint(chest);
        if (waypoint.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.spacetime.no_waypoint")
                            .withStyle(ChatFormatting.RED),
                    true);
            return false;
        }
        SpacetimeBulletproofArmorItem.Waypoint wp = waypoint.get();
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, wp.dimension());
        ServerLevel targetLevel = player.getServer() != null ? player.getServer().getLevel(dimKey) : null;
        if (targetLevel == null) {
            player.displayClientMessage(
                    Component.translatable(
                                    "message.tacz_morelevel.spacetime.invalid_dimension",
                                    wp.dimension().toString())
                            .withStyle(ChatFormatting.RED),
                    true);
            return false;
        }
        if (!SpacetimeBlinkHelper.canStandAtSaved(targetLevel, player, wp.x(), wp.y(), wp.z())) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.spacetime.unsafe_waypoint")
                            .withStyle(ChatFormatting.RED),
                    true);
            return false;
        }
        Vec3 dest = new Vec3(wp.x(), wp.y(), wp.z());
        if (!SpacetimeBlinkHelper.teleportPlayer(player, targetLevel, dest)) {
            return false;
        }
        SpacetimeBulletproofArmorItem.setBlinkCooldownTtl(
                chest, SpacetimeBulletproofArmorItem.BLINK_COOLDOWN_TTL_MAX);
        player.displayClientMessage(
                Component.translatable(
                                "message.tacz_morelevel.spacetime.teleport_success",
                                formatCoord(wp.x()),
                                formatCoord(wp.y()),
                                formatCoord(wp.z()))
                        .withStyle(ChatFormatting.AQUA),
                true);
        return true;
    }

    private static String formatCoord(double v) {
        return String.format("%.1f", v);
    }
}
