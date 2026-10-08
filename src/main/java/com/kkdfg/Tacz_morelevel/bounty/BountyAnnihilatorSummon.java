package com.kkdfg.Tacz_morelevel.bounty;

import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.kkdfg.Tacz_morelevel.world.SummoningEffects;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;

/** 悬赏 ≥3 级时：6 秒召唤歼灭者（T=0 环境音，生成时不设 target）。 */
public final class BountyAnnihilatorSummon {

    public static final int SUMMON_TOTAL_TICKS = 125;
    public static final int DEBUFF_DURATION_TICKS = 120;
    public static final int FIRST_MESSAGE_TICK = 20;
    public static final int SECOND_MESSAGE_TICK = 80;
    public static final int MIN_SPAWN_DISTANCE = 32;
    public static final int MAX_SPAWN_DISTANCE = 64;
    private static final int SPAWN_ATTEMPTS = 64;

    private static final Map<UUID, PendingSummon> PENDING = new ConcurrentHashMap<>();

    private BountyAnnihilatorSummon() {}

    public static boolean hasPending(ServerPlayer player) {
        return PENDING.containsKey(player.getUUID());
    }

    public static void tickPlayer(ServerPlayer player) {
        PendingSummon pending = PENDING.get(player.getUUID());
        if (pending == null) {
            return;
        }
        if (!tickPlayerInternal(player, pending)) {
            PENDING.remove(player.getUUID());
        }
    }

    public static void begin(ServerPlayer player) {
        cancel(player);
        PendingSummon pending = new PendingSummon();
        pending.spawnFeet = resolveSpawnFeet(player.serverLevel(), player);
        PENDING.put(player.getUUID(), pending);
        player.playNotifySound(
                ModSounds.SOUND_BOUNTY_ANNIHILATOR_SUMMON.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        DEBUFF_DURATION_TICKS,
                        0,
                        false,
                        false,
                        true));
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.DARKNESS,
                        DEBUFF_DURATION_TICKS,
                        0,
                        false,
                        false,
                        true));
    }

    public static void cancel(ServerPlayer player) {
        PENDING.remove(player.getUUID());
    }

    public static void tickAll(ServerLevel level) {
        if (PENDING.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, PendingSummon>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingSummon> entry = iterator.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || player.isRemoved() || player.serverLevel() != level) {
                iterator.remove();
                continue;
            }
            if (!tickPlayerInternal(player, entry.getValue())) {
                iterator.remove();
            }
        }
    }

    private static boolean tickPlayerInternal(ServerPlayer player, PendingSummon pending) {
        pending.ticksElapsed++;
        ServerLevel level = player.serverLevel();

        if (pending.ticksElapsed == FIRST_MESSAGE_TICK) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.bounty_annihilator.uneasy"), false);
        }

        if (pending.ticksElapsed == SECOND_MESSAGE_TICK) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.bounty_annihilator.incoming"), false);
        }

        if (pending.ticksElapsed >= SUMMON_TOTAL_TICKS) {
            spawnAnnihilator(level, player, pending.spawnFeet);
            return false;
        }
        return true;
    }

    private static void spawnAnnihilator(ServerLevel level, ServerPlayer player, BlockPos spawnFeet) {
        Mob mob = ModEntities.ANNIHILATOR.get().create(level);
        if (mob == null) {
            return;
        }
        RandomSource random = level.getRandom();
        mob.moveTo(
                spawnFeet.getX() + 0.5D,
                spawnFeet.getY(),
                spawnFeet.getZ() + 0.5D,
                random.nextFloat() * 360.0F,
                0.0F);
        if (!level.noCollision(mob)) {
            mob.moveTo(player.getX(), player.getY(), player.getZ(), mob.getYRot(), mob.getXRot());
        }
        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnFeet),
                MobSpawnType.EVENT,
                null,
                null);
        level.addFreshEntityWithPassengers(mob);
        Vec3 spawnPos = mob.position();
        spawnLightning(level, spawnPos);
        SummoningEffects.spawnSummonBurst(level, spawnPos);
        SummoningEffects.playSummonBurstSound(level, spawnPos);
    }

    static BlockPos resolveSpawnFeet(ServerLevel level, ServerPlayer player) {
        BlockPos center = player.blockPosition();
        RandomSource random = level.getRandom();

        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            double distance =
                    MIN_SPAWN_DISTANCE
                            + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int x = center.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos feet = findStandNear(level, new BlockPos(x, center.getY(), z), center.getY());
            if (feet != null) {
                return feet;
            }
        }

        return center;
    }

    private static BlockPos findStandNear(ServerLevel level, BlockPos xz, int referenceY) {
        int minY = Math.max(level.getMinBuildHeight() + 1, referenceY - MAX_SPAWN_DISTANCE);
        int maxY = Math.min(level.getMaxBuildHeight() - 2, referenceY + MAX_SPAWN_DISTANCE);
        for (int y = maxY; y >= minY; y--) {
            BlockPos feet = new BlockPos(xz.getX(), y, xz.getZ());
            if (canStandAt(level, feet, null)) {
                return feet;
            }
        }
        return null;
    }

    static boolean canStandAt(ServerLevel level, BlockPos feet, Mob mob) {
        if (!hasSolidGround(level, feet)) {
            return false;
        }
        if (!level.isEmptyBlock(feet) || !level.isEmptyBlock(feet.above())) {
            return false;
        }
        if (mob != null) {
            return level.noCollision(
                    mob,
                    new AABB(
                            feet.getX(),
                            feet.getY(),
                            feet.getZ(),
                            feet.getX() + 1.0D,
                            feet.getY() + 2.0D,
                            feet.getZ() + 1.0D));
        }
        return true;
    }

    private static boolean hasSolidGround(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet.below()).blocksMotion();
    }

    private static void spawnLightning(ServerLevel level, Vec3 pos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning == null) {
            return;
        }
        lightning.moveTo(pos.x, pos.y, pos.z);
        lightning.setVisualOnly(true);
        level.addFreshEntity(lightning);
    }

    private static final class PendingSummon {
        int ticksElapsed;
        BlockPos spawnFeet;
    }
}
