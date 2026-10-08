package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechFlightState;
import com.kkdfg.Tacz_morelevel.network.packet.PowerMechFlightSyncPacket;
import com.kkdfg.Tacz_morelevel.power_mech.PowerMechFlightEffects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 动力机甲飞行：胸甲 + 能量触发（非全套）。
 * <ul>
 *   <li>A：本 tick 是否应机甲飞；B：上一 tick 的 A</li>
 *   <li>A ≠ B（否定边沿）：同 tick 先清空复原（飞速默认、关 mayfly/flying、停视觉）</li>
 *   <li>A == 1（肯定）：才执行机甲飞行一切（开 mayfly、按能量写飞速、粒子/音效）</li>
 *   <li>末尾 B = A；稳定 A=B=0 不碰他人飞行</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PowerMechFlightEvents {

    private static final float DEFAULT_FLYING_SPEED = 0.05f;

    private static final Map<UUID, Boolean> SERVER_WAS_FLYING = new ConcurrentHashMap<>();

    /** B：上一 tick 是否应拥有机甲飞行。 */
    private static final Map<UUID, Boolean> PREV_SHOULD_FLY = new ConcurrentHashMap<>();

    private PowerMechFlightEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }
        tickFlight(player);
    }

    @SubscribeEvent
    public static void onArmorChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        if (event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }
        tickFlight(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        tickFlight(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.level().getServer() == null) {
            return;
        }
        PREV_SHOULD_FLY.remove(player.getUUID());
        player.level().getServer().execute(() -> tickFlight(player));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getTarget() instanceof Player target)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer tracker)) {
            return;
        }
        boolean flying = PowerMechFlightState.isPowerMechFlying(target);
        PowerMechFlightSyncPacket.sendToPlayer(tracker, target, flying);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        SERVER_WAS_FLYING.remove(id);
        PREV_SHOULD_FLY.remove(id);
    }

    static void tickFlight(Player player) {
        UUID id = player.getUUID();
        boolean prevShouldFly = PREV_SHOULD_FLY.getOrDefault(id, false);

        boolean shouldFly = false;
        int energy = 0;

        if (!player.isCreative()
                && !player.isSpectator()
                && PowerMechFlightState.hasChestplate(player)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            energy = PowerMechBulletproofArmorItem.getFlightEnergy(chest);
            int pieces = PowerMechBulletproofArmorItem.countEquippedPieces(player);

            if (pieces > 0) {
                energy += PowerMechBulletproofArmorItem.FLIGHT_REGEN_PER_PIECE * pieces;
            }
            if (PowerMechFlightState.isPowerMechFlying(player)) {
                float multiplier = PowerMechBulletproofArmorItem.getFlightSpeedMultiplier(energy);
                energy -=
                        Mth.ceil(
                                PowerMechBulletproofArmorItem.FLIGHT_DRAIN_PER_TICK * multiplier);
            }
            energy =
                    Mth.clamp(
                            energy,
                            0,
                            PowerMechBulletproofArmorItem.FLIGHT_ENERGY_MAX);
            PowerMechBulletproofArmorItem.setFlightEnergy(chest, energy);
            shouldFly = energy > 0;
        }

        // 否定边沿：先清空复原（同 tick 内再肯定，消掉中间 1 tick 脏状态）
        if (shouldFly != prevShouldFly) {
            clearPowerMechFlight(player);
        }

        // 肯定：仅 A==1 执行机甲飞行一切
        if (shouldFly) {
            applyPowerMechFlight(player, energy);
        }

        PREV_SHOULD_FLY.put(id, shouldFly);
    }

    /** A==1：开 mayfly、按能量写飞速、同步视觉/音效。 */
    private static void applyPowerMechFlight(Player player, int energy) {
        float speed = PowerMechBulletproofArmorItem.getFlightSpeed(energy);
        boolean changed = false;
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            changed = true;
        }
        if (player.getAbilities().getFlyingSpeed() != speed) {
            player.getAbilities().setFlyingSpeed(speed);
            changed = true;
        }
        if (changed) {
            player.onUpdateAbilities();
        }
        syncFlightVisuals(player, PowerMechFlightState.isPowerMechFlying(player));
        PowerMechFlightEffects.tickEmit(player);
    }

    /** A≠B：飞速默认、关 mayfly/flying、停视觉。创造/旁观不关 mayfly。 */
    private static void clearPowerMechFlight(Player player) {
        boolean changed = false;
        if (player.getAbilities().getFlyingSpeed() != DEFAULT_FLYING_SPEED) {
            player.getAbilities().setFlyingSpeed(DEFAULT_FLYING_SPEED);
            changed = true;
        }
        if (!player.isCreative() && !player.isSpectator()) {
            if (player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                changed = true;
            }
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
                changed = true;
            }
        }
        if (changed) {
            player.onUpdateAbilities();
        }
        syncFlightVisuals(player, false);
    }

    private static void syncFlightVisuals(Player player, boolean flying) {
        UUID id = player.getUUID();
        boolean wasFlying = SERVER_WAS_FLYING.getOrDefault(id, false);
        if (wasFlying == flying) {
            return;
        }
        SERVER_WAS_FLYING.put(id, flying);
        PowerMechFlightSyncPacket.sendToTracking(player, flying);
        if (flying) {
            PowerMechFlightEffects.startFlightSound(player);
        } else {
            PowerMechFlightEffects.stopFlightSound(player);
        }
    }
}
