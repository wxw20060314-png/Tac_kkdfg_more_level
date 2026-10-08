package com.kkdfg.Tacz_morelevel.item.medicine;

import java.util.function.Supplier;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 用药音效：播放与打断（{@link ClientboundStopSoundPacket}，同冲击套蓄力音）。 */
public final class MedicineSoundEffects {

    private static final double STOP_SOUND_RANGE = 64.0;
    private static final float USE_SOUND_VOLUME = 0.75F;
    private static final float USE_SOUND_PITCH = 1.0F;

    private MedicineSoundEffects() {}

    public static void play(Level level, Player player, Supplier<SoundEvent> sound) {
        if (sound == null || level.isClientSide()) {
            return;
        }
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound.get(),
                SoundSource.PLAYERS,
                USE_SOUND_VOLUME,
                USE_SOUND_PITCH);
    }

    /** 松开 / 打断 / 读条结束时停止读条中的音效（开始音、channel 音）。 */
    public static void stopDuringUse(ServerPlayer player, MedicineSounds sounds) {
        stop(player, sounds.onStart());
        stop(player, sounds.onChannel());
    }

    private static void stop(ServerPlayer player, Supplier<SoundEvent> sound) {
        if (sound == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        var packet =
                new ClientboundStopSoundPacket(sound.get().getLocation(), SoundSource.PLAYERS);
        double rangeSq = STOP_SOUND_RANGE * STOP_SOUND_RANGE;
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(player) <= rangeSq) {
                viewer.connection.send(packet);
            }
        }
    }
}
