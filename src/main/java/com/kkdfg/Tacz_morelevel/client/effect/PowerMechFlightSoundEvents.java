package com.kkdfg.Tacz_morelevel.client.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.kkdfg.Tacz_morelevel.power_mech.PowerMechFlightSoundConstants;
import com.kkdfg.Tacz_morelevel.power_mech.PowerMechFlightSync;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechFlightState;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 动力机甲持续飞行循环音：由 {@link com.kkdfg.Tacz_morelevel.network.packet.PowerMechFlightSyncPacket}
 * 同步状态驱动；起飞/落地短音由服务端 {@code playSound} / {@code ClientboundStopSoundPacket} 广播。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PowerMechFlightSoundEvents {

    private static final Map<UUID, Boolean> WAS_FLYING = new ConcurrentHashMap<>();
    private static final Map<UUID, EntityBoundSoundInstance> ACTIVE = new ConcurrentHashMap<>();

    private PowerMechFlightSoundEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        for (Player player : mc.level.players()) {
            tickEntityFlightSound(mc, player);
        }
        if (mc.player != null) {
            AABB box = mc.player.getBoundingBox().inflate(128.0D);
            for (WarMachineEntity warMachine : mc.level.getEntitiesOfClass(WarMachineEntity.class, box)) {
                tickEntityFlightSound(mc, warMachine);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        PowerMechFlightSync.clear(id);
        stopFlightSound(Minecraft.getInstance(), id);
        WAS_FLYING.remove(id);
    }

    private static void tickEntityFlightSound(Minecraft mc, LivingEntity entity) {
        UUID id = entity.getUUID();

        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                stopFlightSound(mc, id);
                WAS_FLYING.remove(id);
                return;
            }
        }

        if (!PowerMechFlightState.hasChestplate(entity)) {
            stopFlightSound(mc, id);
            WAS_FLYING.remove(id);
            return;
        }

        boolean flying = PowerMechFlightState.isPowerMechFlying(entity);
        boolean wasFlying = WAS_FLYING.getOrDefault(id, false);

        if (flying && !wasFlying) {
            startFlightSound(mc, entity);
            WAS_FLYING.put(id, true);
        } else if (!flying && wasFlying) {
            stopFlightSound(mc, id);
            WAS_FLYING.remove(id);
        }
    }

    private static void startFlightSound(Minecraft mc, LivingEntity entity) {
        stopFlightSound(mc, entity.getUUID());
        EntityBoundSoundInstance instance =
                new EntityBoundSoundInstance(
                        ModSounds.SOUND_POWER_MECH_FLIGHT.get(),
                        SoundSource.PLAYERS,
                        PowerMechFlightSoundConstants.volume,
                        PowerMechFlightSoundConstants.pitch,
                        entity,
                        entity.getRandom().nextLong());
        ACTIVE.put(entity.getUUID(), instance);
        mc.getSoundManager().play(instance);
    }

    private static void stopFlightSound(Minecraft mc, UUID entityId) {
        EntityBoundSoundInstance instance = ACTIVE.remove(entityId);
        if (instance != null) {
            mc.getSoundManager().stop(instance);
        }
    }
}
