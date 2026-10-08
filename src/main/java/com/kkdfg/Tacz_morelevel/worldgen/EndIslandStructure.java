package com.kkdfg.Tacz_morelevel.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

/**
 * Jigsaw structure that only places where End islands have solid ground (not void columns).
 *
 * <p>Adapted from TelepathicGrunt StructureTutorialMod {@code EndIslandStructures}:
 * reject candidates whose first occupied height is below {@link #MIN_PLACE_Y}.
 * Start Y is that occupied height (plus JSON {@code start_height}); jigsaw is
 * <strong>not</strong> projected to a heightmap, so void at the piece center cannot snap to Y=0.
 */
public final class EndIslandStructure extends Structure {

    /** 地表占用高度低于此值则不生成（贴岛底 / 虚空附近）。 */
    private static final int MIN_PLACE_Y = 10;

    public static final Codec<EndIslandStructure> CODEC =
            RecordCodecBuilder.<EndIslandStructure>mapCodec(
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
                                            .apply(instance, EndIslandStructure::new))
                    .codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public EndIslandStructure(
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

    private static int occupiedHeightAt(GenerationContext context, int x, int z) {
        return context.chunkGenerator()
                .getFirstOccupiedHeight(
                        x,
                        z,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        context.heightAccessor(),
                        context.randomState());
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int occupiedY = occupiedHeightAt(context, x, z);
        if (occupiedY < MIN_PLACE_Y) {
            return Optional.empty();
        }

        int startOffset =
                this.startHeight.sample(
                        context.random(),
                        new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockPos = new BlockPos(x, occupiedY + startOffset, z);

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
        return ModStructureTypes.END_ISLAND.get();
    }
}
