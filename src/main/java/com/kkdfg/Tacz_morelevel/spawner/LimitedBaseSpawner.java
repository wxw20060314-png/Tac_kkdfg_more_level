package com.kkdfg.Tacz_morelevel.spawner;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

/**
 * 基于原版 {@link BaseSpawner}：另支持
 * {@code RequiredPlayerRange < 0} 忽略玩家距离、和平模式开关、相对距离刷怪
 * （{@code SpawnOffsetMode=random|fixed}）；
 * {@code SpawnAttemptCount}：{@code < 0}（默认）为原版单次采样不回退，{@code ≥ 1} 时采多次点后回退笼子；
 * 以及成功刷怪回调。
 */
public abstract class LimitedBaseSpawner {

    public static final String SPAWN_DATA_TAG = "SpawnData";
    public static final String TAG_SPAWNS_REMAINING = "SpawnsRemaining";
    public static final String TAG_SPAWN_IN_PEACEFUL = "SpawnInPeaceful";
    public static final String TAG_SPAWN_OFFSET_MODE = "SpawnOffsetMode";
    public static final String TAG_SPAWN_MIN_RANGE = "SpawnMinRange";
    public static final String TAG_SPAWN_MAX_RANGE = "SpawnMaxRange";
    public static final String TAG_SPAWN_Y_OFFSET = "SpawnYOffset";
    public static final String TAG_SPAWN_OFFSET_X = "SpawnOffsetX";
    public static final String TAG_SPAWN_OFFSET_Y = "SpawnOffsetY";
    public static final String TAG_SPAWN_OFFSET_Z = "SpawnOffsetZ";
    /** {@code < 0}：原版单次采样；{@code ≥ 1}：最多尝试次数，耗尽后强制在笼子生成。 */
    public static final String TAG_SPAWN_ATTEMPT_COUNT = "SpawnAttemptCount";

    public static final String MODE_VANILLA = "vanilla";
    public static final String MODE_RANDOM = "random";
    public static final String MODE_FIXED = "fixed";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EVENT_SPAWN = 1;

    /** 仅用于 Forge 刷怪事件签名（需要 {@link BaseSpawner} 实例）。 */
    private final BaseSpawner forgeBridge =
            new BaseSpawner() {
                @Override
                public void broadcastEvent(Level level, BlockPos pos, int id) {
                    LimitedBaseSpawner.this.broadcastEvent(level, pos, id);
                }

                @Override
                public void setNextSpawnData(
                        @Nullable Level level, BlockPos pos, SpawnData nextSpawnData) {
                    LimitedBaseSpawner.this.setNextSpawnData(level, pos, nextSpawnData);
                }
            };

    private int spawnDelay = 20;
    private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
    @Nullable
    private SpawnData nextSpawnData;
    private double spin;
    private double oSpin;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity displayEntity;
    private int maxNearbyEntities = 6;
    /** 原版默认 16；{@code < 0} 表示不检查玩家距离。 */
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    private int spawnsRemaining = 1;
    private boolean spawnInPeaceful;

    /** {@link #MODE_VANILLA} / {@link #MODE_RANDOM} / {@link #MODE_FIXED} */
    private String spawnOffsetMode = MODE_VANILLA;
    /** random：最小水平距离（格）。 */
    private double spawnMinRange;
    /** random：最大水平距离；{@code < 0} 时用 {@link #spawnRange}。 */
    private double spawnMaxRange = -1.0D;
    /** random：相对笼子底面的 Y 偏移。 */
    private double spawnYOffset;
    /** fixed：相对笼子中心的偏移。 */
    private double spawnOffsetX;
    private double spawnOffsetY;
    private double spawnOffsetZ;
    /**
     * 找点尝试次数：{@code < 0}（默认）原版只采 1 次且不回退笼子；{@code ≥ 1} 采多次，耗尽后强制笼子。
     */
    private int spawnAttemptCount = -1;

    public void setEntityId(EntityType<?> type, @Nullable Level level, RandomSource random, BlockPos pos) {
        SpawnData data = new SpawnData();
        data.getEntityToSpawn()
                .putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        this.setNextSpawnData(level, pos, data);
        this.spawnPotentials = SimpleWeightedRandomList.single(data);
        this.displayEntity = null;
    }

    /**
     * 解析刷怪坐标。若实体带绝对 {@code Pos} 则优先；否则按 {@link #spawnOffsetMode}。
     *
     * @return {@code [x, y, z]}
     */
    private double[] resolveSpawnPosition(BlockPos pos, CompoundTag entityTag, RandomSource random) {
        ListTag posList = entityTag.getList("Pos", 6);
        if (posList.size() >= 3) {
            return new double[] {posList.getDouble(0), posList.getDouble(1), posList.getDouble(2)};
        }

        if (MODE_FIXED.equals(this.spawnOffsetMode)) {
            return new double[] {
                (double) pos.getX() + 0.5D + this.spawnOffsetX,
                (double) pos.getY() + this.spawnOffsetY,
                (double) pos.getZ() + 0.5D + this.spawnOffsetZ
            };
        }

        if (MODE_RANDOM.equals(this.spawnOffsetMode)) {
            double min = Math.max(0.0D, this.spawnMinRange);
            double max = this.spawnMaxRange >= 0.0D ? this.spawnMaxRange : (double) this.spawnRange;
            if (max < min) {
                double swap = min;
                min = max;
                max = swap;
            }
            double radius =
                    min >= max ? min : min + random.nextDouble() * (max - min);
            double angle = random.nextDouble() * (Math.PI * 2.0D);
            return new double[] {
                (double) pos.getX() + 0.5D + Math.cos(angle) * radius,
                (double) pos.getY() + this.spawnYOffset,
                (double) pos.getZ() + 0.5D + Math.sin(angle) * radius
            };
        }

        // vanilla
        return new double[] {
            (double) pos.getX()
                    + (random.nextDouble() - random.nextDouble()) * (double) this.spawnRange
                    + 0.5D,
            (double) (pos.getY() + random.nextInt(3) - 1),
            (double) pos.getZ()
                    + (random.nextDouble() - random.nextDouble()) * (double) this.spawnRange
                    + 0.5D
        };
    }

    /** 附近同类计数用的水平膨胀半径。 */
    private double getHorizontalScanRange() {
        if (MODE_FIXED.equals(this.spawnOffsetMode)) {
            return Math.max(
                    (double) this.spawnRange,
                    Math.hypot(this.spawnOffsetX, this.spawnOffsetZ) + 1.0D);
        }
        if (MODE_RANDOM.equals(this.spawnOffsetMode)) {
            double max = this.spawnMaxRange >= 0.0D ? this.spawnMaxRange : (double) this.spawnRange;
            return Math.max((double) this.spawnRange, Math.max(max, this.spawnMinRange));
        }
        return (double) this.spawnRange;
    }

    private boolean isNearPlayer(Level level, BlockPos pos) {
        if (this.requiredPlayerRange < 0) {
            return true;
        }
        return level.hasNearbyAlivePlayer(
                (double) pos.getX() + 0.5D,
                (double) pos.getY() + 0.5D,
                (double) pos.getZ() + 0.5D,
                (double) this.requiredPlayerRange);
    }

    public void clientTick(Level level, BlockPos pos) {
        if (!this.isNearPlayer(level, pos)) {
            this.oSpin = this.spin;
        } else if (this.displayEntity != null) {
            RandomSource random = level.getRandom();
            double x = (double) pos.getX() + random.nextDouble();
            double y = (double) pos.getY() + random.nextDouble();
            double z = (double) pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.oSpin = this.spin;
            this.spin = (this.spin + (double) (1000.0F / ((float) this.spawnDelay + 200.0F))) % 360.0D;
        }
    }

    public void serverTick(ServerLevel level, BlockPos pos) {
        if (this.spawnsRemaining <= 0) {
            return;
        }
        if (!this.spawnInPeaceful && level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (!this.isNearPlayer(level, pos)) {
            return;
        }

        if (this.spawnDelay == -1) {
            this.delay(level, pos);
        }

        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }

        boolean spawnedAny = false;
        int spawnedThisBatch = 0;
        RandomSource random = level.getRandom();
        SpawnData spawnData = this.getOrCreateNextSpawnData(level, random, pos);
        double scanRange = this.getHorizontalScanRange();
        AABB nearbyBox =
                new AABB(
                                (double) pos.getX(),
                                (double) pos.getY(),
                                (double) pos.getZ(),
                                (double) (pos.getX() + 1),
                                (double) (pos.getY() + 1),
                                (double) (pos.getZ() + 1))
                        .inflate(scanRange);

        for (int i = 0; i < this.spawnCount; ++i) {
            CompoundTag entityTag = spawnData.getEntityToSpawn();
            Optional<EntityType<?>> typeOpt = EntityType.by(entityTag);
            if (typeOpt.isEmpty()) {
                this.finishBatchAttempt(level, pos, spawnedAny);
                return;
            }

            int result =
                    this.trySpawnWithAttempts(
                            level,
                            pos,
                            spawnData,
                            entityTag,
                            typeOpt.get(),
                            nearbyBox,
                            spawnedThisBatch,
                            random);
            if (result < 0) {
                // 本批已刷出至少一只时仍须扣次，否则 MaxNearby 等中止会导致限次永不消耗、笼子不拆。
                this.finishBatchAttempt(level, pos, spawnedAny);
                return;
            }
            if (result > 0) {
                spawnedAny = true;
                ++spawnedThisBatch;
            }
        }

        this.finishBatchAttempt(level, pos, spawnedAny);
    }

    /**
     * 一批尝试结束：重置 Delay；若本批刷出过实体则扣 {@link #spawnsRemaining}。
     * 全失败也 Delay，避免 spawnDelay 停在 0 导致每 tick 空转。
     */
    private void finishBatchAttempt(ServerLevel level, BlockPos pos, boolean spawnedAny) {
        this.delay(level, pos);
        if (spawnedAny) {
            this.onSpawnBatchSuccess();
        }
    }

    /**
     * 按 {@link #spawnAttemptCount} 找点并刷一只。
     *
     * <ul>
     *   <li>{@code spawnAttemptCount < 0}：原版，只采 1 点，失败不回退笼子</li>
     *   <li>{@code ≥ 1}：采最多 N 个点入栈逐个试，耗尽后强制笼子</li>
     * </ul>
     *
     * @return {@code 1} 成功，{@code 0} 本只未刷出，{@code -1} 中止本批
     */
    private int trySpawnWithAttempts(
            ServerLevel level,
            BlockPos cagePos,
            SpawnData spawnData,
            CompoundTag entityTag,
            EntityType<?> type,
            AABB nearbyBox,
            int spawnedThisBatch,
            RandomSource random) {
        // 原版：单次采样，失败即跳过
        if (this.spawnAttemptCount < 0) {
            double[] xyz = this.resolveSpawnPosition(cagePos, entityTag, random);
            return this.trySpawnOne(
                    level,
                    cagePos,
                    spawnData,
                    entityTag,
                    type,
                    xyz[0],
                    xyz[1],
                    xyz[2],
                    nearbyBox,
                    spawnedThisBatch,
                    random,
                    false);
        }

        ArrayDeque<double[]> candidates = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        int maxAttempts = this.spawnAttemptCount;
        for (int t = 0; t < maxAttempts; ++t) {
            double[] xyz = this.resolveSpawnPosition(cagePos, entityTag, random);
            BlockPos key = BlockPos.containing(xyz[0], xyz[1], xyz[2]);
            if (seen.add(key)) {
                candidates.addLast(xyz);
            }
        }

        while (!candidates.isEmpty()) {
            double[] xyz = candidates.removeFirst();
            int result =
                    this.trySpawnOne(
                            level,
                            cagePos,
                            spawnData,
                            entityTag,
                            type,
                            xyz[0],
                            xyz[1],
                            xyz[2],
                            nearbyBox,
                            spawnedThisBatch,
                            random,
                            false);
            if (result != 0) {
                return result;
            }
        }

        // 栈空：强制在刷怪笼原点
        return this.trySpawnOne(
                level,
                cagePos,
                spawnData,
                entityTag,
                type,
                (double) cagePos.getX() + 0.5D,
                (double) cagePos.getY(),
                (double) cagePos.getZ() + 0.5D,
                nearbyBox,
                spawnedThisBatch,
                random,
                true);
    }

    /**
     * 尝试在指定坐标刷一只。
     *
     * @param forceAtCage {@code true} 时跳过碰撞、光照与放置规则（笼子回退）
     * @return {@code 1} 成功，{@code 0} 本点不可刷可改试它处，{@code -1} 应中止本批（上限/加载失败等）
     */
    private int trySpawnOne(
            ServerLevel level,
            BlockPos cagePos,
            SpawnData spawnData,
            CompoundTag entityTag,
            EntityType<?> type,
            double x,
            double y,
            double z,
            AABB nearbyBox,
            int spawnedThisBatch,
            RandomSource random,
            boolean forceAtCage) {
        if (!forceAtCage && !level.noCollision(type.getAABB(x, y, z))) {
            return 0;
        }

        BlockPos spawnPos = BlockPos.containing(x, y, z);
        if (!forceAtCage) {
            if (spawnData.getCustomSpawnRules().isPresent()) {
                if (!this.spawnInPeaceful
                        && !type.getCategory().isFriendly()
                        && level.getDifficulty() == Difficulty.PEACEFUL) {
                    return 0;
                }
                SpawnData.CustomSpawnRules rules = spawnData.getCustomSpawnRules().get();
                if (!rules.blockLightLimit()
                                .isValueInRange(level.getBrightness(LightLayer.BLOCK, spawnPos))
                        || !rules.skyLightLimit()
                                .isValueInRange(level.getBrightness(LightLayer.SKY, spawnPos))) {
                    return 0;
                }
            } else if (!(this.spawnInPeaceful && level.getDifficulty() == Difficulty.PEACEFUL)
                    && !SpawnPlacements.checkSpawnRules(
                            type, level, MobSpawnType.SPAWNER, spawnPos, level.getRandom())) {
                return 0;
            }
        }

        Entity entity =
                EntityType.loadEntityRecursive(
                        entityTag,
                        level,
                        loaded -> {
                            loaded.moveTo(x, y, z, loaded.getYRot(), loaded.getXRot());
                            return loaded;
                        });
        if (entity == null) {
            return -1;
        }

        int nearby = level.getEntitiesOfClass(entity.getClass(), nearbyBox).size();
        if (nearby + spawnedThisBatch >= this.maxNearbyEntities) {
            return -1;
        }

        entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), random.nextFloat() * 360.0F, 0.0F);
        if (entity instanceof Mob mob) {
            if (!forceAtCage
                    && !net.minecraftforge.event.ForgeEventFactory.checkSpawnPositionSpawner(
                            mob, level, MobSpawnType.SPAWNER, spawnData, this.forgeBridge)) {
                return 0;
            }
            var event =
                    net.minecraftforge.event.ForgeEventFactory.onFinalizeSpawnSpawner(
                            mob,
                            level,
                            level.getCurrentDifficultyAt(entity.blockPosition()),
                            null,
                            entityTag,
                            this.forgeBridge);
            if (event != null
                    && spawnData.getEntityToSpawn().size() == 1
                    && spawnData.getEntityToSpawn().contains("id", 8)) {
                mob.finalizeSpawn(
                        level,
                        event.getDifficulty(),
                        event.getSpawnType(),
                        event.getSpawnData(),
                        event.getSpawnTag());
            }
        }

        if (!level.tryAddFreshEntityWithPassengers(entity)) {
            return -1;
        }

        level.levelEvent(2004, cagePos, 0);
        level.gameEvent(entity, GameEvent.ENTITY_PLACE, spawnPos);
        if (entity instanceof Mob mob) {
            mob.spawnAnim();
        }
        return 1;
    }

    /** 本批至少刷出一只后调用：默认扣 1 次剩余次数。 */
    protected void onSpawnBatchSuccess() {
        if (this.spawnsRemaining > 0) {
            --this.spawnsRemaining;
        }
    }

    private void delay(Level level, BlockPos pos) {
        RandomSource random = level.random;
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            this.spawnDelay =
                    this.minSpawnDelay + random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }
        this.spawnPotentials
                .getRandom(random)
                .ifPresent(wrapper -> this.setNextSpawnData(level, pos, wrapper.getData()));
        this.broadcastEvent(level, pos, EVENT_SPAWN);
    }

    public void load(@Nullable Level level, BlockPos pos, CompoundTag tag) {
        this.spawnDelay = tag.getShort("Delay");
        if (tag.contains(SPAWN_DATA_TAG, 10)) {
            SpawnData data =
                    SpawnData.CODEC
                            .parse(NbtOps.INSTANCE, tag.getCompound(SPAWN_DATA_TAG))
                            .resultOrPartial(msg -> LOGGER.warn("Invalid SpawnData: {}", msg))
                            .orElseGet(SpawnData::new);
            this.setNextSpawnData(level, pos, data);
        }

        if (tag.contains("SpawnPotentials", 9)) {
            ListTag list = tag.getList("SpawnPotentials", 10);
            this.spawnPotentials =
                    SpawnData.LIST_CODEC
                            .parse(NbtOps.INSTANCE, list)
                            .resultOrPartial(
                                    msg -> LOGGER.warn("Invalid SpawnPotentials list: {}", msg))
                            .orElseGet(SimpleWeightedRandomList::empty);
        } else {
            this.spawnPotentials =
                    SimpleWeightedRandomList.single(
                            this.nextSpawnData != null ? this.nextSpawnData : new SpawnData());
        }

        if (tag.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = tag.getShort("MinSpawnDelay");
            this.maxSpawnDelay = tag.getShort("MaxSpawnDelay");
            this.spawnCount = tag.getShort("SpawnCount");
        }

        if (tag.contains("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = tag.getShort("MaxNearbyEntities");
        }
        // 单独读取，允许负数表示忽略距离（不必依赖 MaxNearbyEntities 存在）
        if (tag.contains("RequiredPlayerRange", 99)) {
            this.requiredPlayerRange = tag.getShort("RequiredPlayerRange");
        }
        if (tag.contains("SpawnRange", 99)) {
            this.spawnRange = tag.getShort("SpawnRange");
        }

        if (tag.contains(TAG_SPAWN_OFFSET_MODE, 8)) {
            String mode = tag.getString(TAG_SPAWN_OFFSET_MODE).trim().toLowerCase();
            if (MODE_RANDOM.equals(mode) || MODE_FIXED.equals(mode) || MODE_VANILLA.equals(mode)) {
                this.spawnOffsetMode = mode;
            } else {
                this.spawnOffsetMode = MODE_VANILLA;
            }
        } else {
            this.spawnOffsetMode = MODE_VANILLA;
        }
        if (tag.contains(TAG_SPAWN_MIN_RANGE, 99)) {
            this.spawnMinRange = tag.getDouble(TAG_SPAWN_MIN_RANGE);
        }
        if (tag.contains(TAG_SPAWN_MAX_RANGE, 99)) {
            this.spawnMaxRange = tag.getDouble(TAG_SPAWN_MAX_RANGE);
        }
        if (tag.contains(TAG_SPAWN_Y_OFFSET, 99)) {
            this.spawnYOffset = tag.getDouble(TAG_SPAWN_Y_OFFSET);
        }
        if (tag.contains(TAG_SPAWN_OFFSET_X, 99)) {
            this.spawnOffsetX = tag.getDouble(TAG_SPAWN_OFFSET_X);
        }
        if (tag.contains(TAG_SPAWN_OFFSET_Y, 99)) {
            this.spawnOffsetY = tag.getDouble(TAG_SPAWN_OFFSET_Y);
        }
        if (tag.contains(TAG_SPAWN_OFFSET_Z, 99)) {
            this.spawnOffsetZ = tag.getDouble(TAG_SPAWN_OFFSET_Z);
        }
        if (tag.contains(TAG_SPAWN_ATTEMPT_COUNT, 99)) {
            this.spawnAttemptCount = tag.getInt(TAG_SPAWN_ATTEMPT_COUNT);
        } else {
            this.spawnAttemptCount = -1;
        }

        if (tag.contains(TAG_SPAWNS_REMAINING, 99)) {
            this.spawnsRemaining = Math.max(0, tag.getInt(TAG_SPAWNS_REMAINING));
        }
        this.spawnInPeaceful = tag.getBoolean(TAG_SPAWN_IN_PEACEFUL);

        this.displayEntity = null;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putShort("Delay", (short) this.spawnDelay);
        tag.putShort("MinSpawnDelay", (short) this.minSpawnDelay);
        tag.putShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
        tag.putShort("SpawnCount", (short) this.spawnCount);
        tag.putShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
        tag.putShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
        tag.putShort("SpawnRange", (short) this.spawnRange);
        tag.putInt(TAG_SPAWNS_REMAINING, this.spawnsRemaining);
        tag.putBoolean(TAG_SPAWN_IN_PEACEFUL, this.spawnInPeaceful);
        tag.putString(TAG_SPAWN_OFFSET_MODE, this.spawnOffsetMode);
        tag.putDouble(TAG_SPAWN_MIN_RANGE, this.spawnMinRange);
        tag.putDouble(TAG_SPAWN_MAX_RANGE, this.spawnMaxRange);
        tag.putDouble(TAG_SPAWN_Y_OFFSET, this.spawnYOffset);
        tag.putDouble(TAG_SPAWN_OFFSET_X, this.spawnOffsetX);
        tag.putDouble(TAG_SPAWN_OFFSET_Y, this.spawnOffsetY);
        tag.putDouble(TAG_SPAWN_OFFSET_Z, this.spawnOffsetZ);
        tag.putInt(TAG_SPAWN_ATTEMPT_COUNT, this.spawnAttemptCount);
        if (this.nextSpawnData != null) {
            tag.put(
                    SPAWN_DATA_TAG,
                    SpawnData.CODEC
                            .encodeStart(NbtOps.INSTANCE, this.nextSpawnData)
                            .result()
                            .orElseThrow(() -> new IllegalStateException("Invalid SpawnData")));
        }
        tag.put(
                "SpawnPotentials",
                SpawnData.LIST_CODEC
                        .encodeStart(NbtOps.INSTANCE, this.spawnPotentials)
                        .result()
                        .orElseThrow());
        return tag;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level level, RandomSource random, BlockPos pos) {
        if (this.displayEntity == null) {
            CompoundTag entityTag =
                    this.getOrCreateNextSpawnData(level, random, pos).getEntityToSpawn();
            if (!entityTag.contains("id", 8)) {
                return null;
            }
            this.displayEntity = EntityType.loadEntityRecursive(entityTag, level, Function.identity());
        }
        return this.displayEntity;
    }

    public boolean onEventTriggered(Level level, int id) {
        if (id == EVENT_SPAWN) {
            if (level.isClientSide) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    protected void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData next) {
        this.nextSpawnData = next;
    }

    private SpawnData getOrCreateNextSpawnData(
            @Nullable Level level, RandomSource random, BlockPos pos) {
        if (this.nextSpawnData != null) {
            return this.nextSpawnData;
        }
        this.setNextSpawnData(
                level,
                pos,
                this.spawnPotentials
                        .getRandom(random)
                        .map(WeightedEntry.Wrapper::getData)
                        .orElseGet(SpawnData::new));
        return this.nextSpawnData;
    }

    public abstract void broadcastEvent(Level level, BlockPos pos, int eventId);

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }

    public int getSpawnsRemaining() {
        return this.spawnsRemaining;
    }

    public void setSpawnsRemaining(int spawnsRemaining) {
        this.spawnsRemaining = Math.max(0, spawnsRemaining);
    }

    public boolean isSpawnInPeaceful() {
        return this.spawnInPeaceful;
    }

    public void setSpawnInPeaceful(boolean spawnInPeaceful) {
        this.spawnInPeaceful = spawnInPeaceful;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }
}
