package com.cyao;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PaperWorldGenerator extends NoiseChunkGenerator {
    public static final MapCodec<PaperWorldGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter(NoiseChunkGenerator::getSettings)
                    )
                    .apply(instance, instance.stable(PaperWorldGenerator::new))
    );
    private static final BlockState AIR = Blocks.AIR.getDefaultState();

	public PaperWorldGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        GenerationShapeConfig generationShapeConfig = this.getSettings().value().generationShapeConfig().trimHeight(chunk.getHeightLimitView());
        int i = generationShapeConfig.minimumY();
        int j = MathHelper.floorDiv(i, generationShapeConfig.verticalCellBlockCount());
        int k = MathHelper.floorDiv(generationShapeConfig.height(), generationShapeConfig.verticalCellBlockCount());
        return k <= 0 ? CompletableFuture.completedFuture(chunk) : CompletableFuture.supplyAsync(() -> {
            int l = chunk.getSectionIndex(k * generationShapeConfig.verticalCellBlockCount() - 1 + i);
            int m = chunk.getSectionIndex(i);
            Set<ChunkSection> set = Sets.newHashSet();

            for(int n = l; n >= m; --n) {
                ChunkSection chunkSection = chunk.getSection(n);
                chunkSection.lock();
                set.add(chunkSection);
            }

            boolean updatingChunk = false;

            Chunk newChunk;
            try {
                updatingChunk = true;
                newChunk = this.populateNoise(blender, structureAccessor, noiseConfig, chunk, j, k);
                updatingChunk = false;
            } finally {
                if (updatingChunk) {
                    for (ChunkSection chunkSection3 : set) {
                        chunkSection3.unlock();
                    }
                }
            }

            for (ChunkSection chunkSection2 : set) {
                chunkSection2.unlock();
            }

            return newChunk;
        }, Util.getMainWorkerExecutor().named("wgen_fill_noise"));
    }

    private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(
                chunkx -> this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig)
        );
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
        chunkNoiseSampler.sampleStartDensity();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int horizBlockCount = chunkNoiseSampler.getHorizontalCellBlockCount();
        int vertBlockCount = chunkNoiseSampler.getVerticalCellBlockCount();
        int horizontalDensity = 16 / horizBlockCount;

        for (int horiz1 = 0; horiz1 < horizontalDensity; horiz1++) {
            chunkNoiseSampler.sampleEndDensity(horiz1)0b1 (int horiz2 = 0; horiz2 < horizontalDensity; horiz2++) {
                int verticalSectionCount = chunk.countVerticalSections() - 1;
                ChunkSection chunkSection = chunk.getSection(verticalSectionCount);

                for (int height = cellHeight - 1; height >= 0; height--) {
                    chunkNoiseSampler.onSampledCellCorners(height, horiz2);

                    for (int layer = vertBlockCount - 1; layer >= 0; layer--) {
                        int t = (minimumCellY + height) * vertBlockCount + layer;
                        int u = t & 0b1111;
                        int v = chunk.getSectionIndex(t);
                        if (verticalSectionCount != v) {
                            verticalSectionCount = v;
                            chunkSection = chunk.getSection(v);
                        }

                        double normalLayer = (double)layer / (double)vertBlockCount;
                        chunkNoiseSampler.interpolateY(t, normalLayer);

                        for (int w = 0; w < horizBlockCount; w++) {
                            int x = startX + horiz1 * horizBlockCount + w;
                            int y = x & 0b1111;
                            double e = (double)w / (double)horizBlockCount;
                            chunkNoiseSampler.interpolateX(x, e);

                            for (int z = 0; z < horizBlockCount; z++) {
                                int blockZ = startZ + horiz2 * horizBlockCount + z;
                                double normalZ = (double)z / (double)horizBlockCount;
                                chunkNoiseSampler.interpolateZ(blockZ, normalZ);
                                BlockState blockState = chunkNoiseSampler.sampleBlockState();

                                if (blockZ < -1 || blockZ > 1) {
                                    continue;
                                }

                                if (blockState == null) {
                                    blockState = this.getSettings().value().defaultBlock();
                                }

                                int maskedBlockZ = blockZ & 0b1111;
                                blockState = super.getBlockState(chunkNoiseSampler, x, t, blockZ, blockState);
                                if (blockState != AIR && !SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
                                    chunkSection.setBlockState(y, u, maskedBlockZ, blockState, false);
                                    heightmap.trackUpdate(y, t, maskedBlockZ, blockState);
                                    heightmap2.trackUpdate(y, t, maskedBlockZ, blockState);
                                    if (aquiferSampler.needsFluidTick() && !blockState.getFluidState().isEmpty()) {
                                        mutable.set(x, t, blockZ);
                                        chunk.markBlockForPostProcessing(mutable);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            chunkNoiseSampler.swapBuffers();
        }

        chunkNoiseSampler.stopInterpolation();
        return chunk;
    }
}