package com.kkdfg.Tacz_morelevel.world;

import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 特种兵召唤方块与悬赏歼灭者召唤共用的粒子 / 音效。 */
public final class SummoningEffects {

    private SummoningEffects() {}

    public static void playCountdownSound(ServerLevel level, BlockPos pos) {
        level.playSound(
                null,
                pos,
                ModSounds.SOUND_SIGNAL_SUMMONING_COUNTDOWN.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F);
    }

    public static void playCountdownSound(ServerLevel level, Vec3 pos) {
        playCountdownSound(level, BlockPos.containing(pos));
    }

    public static void spawnCountdownSmoke(ServerLevel level, Vec3 center) {
        RandomSource random = level.getRandom();
        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                center.x() + (random.nextDouble() - 0.5D) * 0.35D,
                center.y() + 1.0D,
                center.z() + (random.nextDouble() - 0.5D) * 0.35D,
                1,
                0.08D,
                0.05D,
                0.08D,
                0.005D);
    }

    public static void spawnSummonBurst(ServerLevel level, Vec3 pos) {
        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                pos.x(),
                pos.y() + 0.5D,
                pos.z(),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D);
        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.x(),
                pos.y() + 0.6D,
                pos.z(),
                12,
                0.45D,
                0.25D,
                0.45D,
                0.02D);
    }

    public static void playSummonBurstSound(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 10.0F, 1.0F);
    }

    public static void playSummonBurstSound(ServerLevel level, Vec3 pos) {
        playSummonBurstSound(level, BlockPos.containing(pos));
    }

    public static void stopCountdownSound(ServerLevel level, Vec3 center) {
        ResourceLocation soundId = ModSounds.SOUND_SIGNAL_SUMMONING_COUNTDOWN.get().getLocation();
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(soundId, SoundSource.BLOCKS);
        AABB area = AABB.ofSize(center, 64.0D, 64.0D, 64.0D);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.connection.send(packet);
        }
    }
}
