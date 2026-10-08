package com.kkdfg.Tacz_morelevel.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

/**
 * Nether jigsaw that places on a ledge / cave floor found by scanning a noise column.
 *
 * <p>Adapted from TelepathicGrunt StructureTutorialMod guidance: use {@code getBaseColumn},
 * scan once per Y layer, and never project to the Nether heightmap (bedrock roof).
 *
 * <p>Placement is intentionally loose: a layer passes when the vertical window above the floor
 * is at least half open (non-solid / replaceable). Structures may overwrite the bottom.
 */
public final class NetherLedgeStructure extends Structure {

    /** Lowest Y to consider for a floor (above bottom lava sea). */
    private static final int SCAN_MIN_Y = 32;
    /** Highest Y to consider (below bedrock ceiling). */
    private static final int SCAN_MAX_Y = 100;
    /** Vertical window size checked above a candidate floor (one pass per layer). */
    private static final int CLEARANCE_WINDOW = 12;
    /** Fraction of the window that must be open (air / non-solid, no liquid). */
    private static final float MIN_OPEN_RATIO = 0.5f;

    public static final Codec<NetherLedgeStructure> CODEC =
            RecordCodecBuilder.<NetherLedgeStructure>mapCodec(
                            instance ->
                                    instance.group(
                                                    settingsCodec(instance),
                                                    StructureTemplatePool.CODEC
                                                            .fieldOf("start_pool")
                                                            .forGetter(s -> s.startPool),
                                                    ResourceLocation.CODEC
                                                            .optionalFieldOf("start_jigsaw_name")
                                                            .forGetter(s -> s.startJigsawName),
                                                    Codec.intRange(0, 30)
                                                            .fieldOf("size")
                                                            .forGetter(s -> s.size),
                                                    HeightProvider.CODEC
                                                            .fieldOf("start_height")
                                                            .forGetter(s -> s.startHeight),
                                                    Heightmap.Types.CODEC
                                                            .optionalFieldOf("project_start_to_heightmap")
                                                            .forGetter(s -> s.projectStartToHeightmap),
                                                    Codec.intRange(1, 128)
                                                            .fieldOf("max_distance_from_center")
                                                            .forGetter(s -> s.maxDistanceFromCenter))
                                            .apply(instance, NetherLedgeStructure::new))
                    .codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public NetherLedgeStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int size,
            HeightProvider startHeight,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    /**
     * Highest suitable floor Y in range, or empty if none.
     * Floor = solid motion-blocking (bedrock/lava fluid rejected); above = open ratio >= 50%.
     */
    private static Optional<Integer> findLedgeFloorY(GenerationContext context, int x, int z) {
        int minY = Math.max(SCAN_MIN_Y, context.chunkGenerator().getMinY() + 1);
        int maxY =
                Math.min(
                        SCAN_MAX_Y,
                        context.heightAccessor().getMaxBuildHeight() - CLEARANCE_WINDOW - 1);
        if (minY >= maxY) {
            return Optional.empty();
        }

        NoiseColumn column =
                context.chunkGenerator()
                        .getBaseColumn(x, z, context.heightAccessor(), context.randomState());

        for (int floorY = maxY; floorY >= minY; floorY--) {
            BlockState floor = column.getBlock(floorY);
            if (!isValidFloor(floor)) {
                continue;
            }
            if (!hasEnoughOpenSpace(column, floorY + 1, CLEARANCE_WINDOW, MIN_OPEN_RATIO)) {
                continue;
            }
            return Optional.of(floorY);
        }
        return Optional.empty();
    }

    private static boolean isValidFloor(BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.LAVA)) {
            return false;
        }
        return state.blocksMotion();
    }

    /** True when open (non-solid, non-liquid) cells in the window meet {@code minOpenRatio}. */
    private static boolean hasEnoughOpenSpace(
            NoiseColumn column, int fromY, int height, float minOpenRatio) {
        int open = 0;
        for (int y = fromY; y < fromY + height; y++) {
            if (isOpenSpace(column.getBlock(y))) {
                open++;
            }
        }
        return open >= Math.ceil(height * (double) minOpenRatio);
    }

    private static boolean isOpenSpace(BlockState state) {
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        // Air, vines, fire, fungi, etc. Structure bottom may replace solids below.
        return !state.blocksMotion();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();

        Optional<Integer> floorY = findLedgeFloorY(context, x, z);
        if (floorY.isEmpty()) {
            return Optional.empty();
        }

        // Sit on top of the floor block; never use Nether heightmap (bedrock roof).
        // start_height / project_start_to_heightmap in JSON are ignored for placement Y.
        BlockPos blockPos = new BlockPos(x, floorY.get() + 1, z);

        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.size,
                blockPos,
                false,
                Optional.empty(),
                this.maxDistanceFromCenter);
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.NETHER_LEDGE.get();
    }
}
