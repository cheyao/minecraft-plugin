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

            boolean var19 = false;

            Chunk var21;
            try {
                var19 = true;
                var21 = this.populateNoise(blender, structureAccessor, noiseConfig, chunk, j, k);
                var19 = false;
            } finally {
                if (var19) {
                    for (ChunkSection chunkSection3 : set) {
                        chunkSection3.unlock();
                    }
                }
            }

            for (ChunkSection chunkSection2 : set) {
                chunkSection2.unlock();
            }

            return var21;
        }, Util.getMainWorkerExecutor().named("wgen_fill_noise"));
    }

    private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(
                chunkx -> this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig)
        );
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getStartX();
        int j = chunkPos.getStartZ();
        AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
        chunkNoiseSampler.sampleStartDensity();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int k = chunkNoiseSampler.getHorizontalCellBlockCount();
        int l = chunkNoiseSampler.getVerticalCellBlockCount();
        int m = 16 / k;
        int n = 16 / k;

        for (int o = 0; o < m; o++) {
            chunkNoiseSampler.sampleEndDensity(o);

            for (int p = 0; p < n; p++) {
                int q = chunk.countVerticalSections() - 1;
                ChunkSection chunkSection = chunk.getSection(q);

                for (int r = cellHeight - 1; r >= 0; r--) {
                    chunkNoiseSampler.onSampledCellCorners(r, p);

                    for (int s = l - 1; s >= 0; s--) {
                        int t = (minimumCellY + r) * l + s;
                        int u = t & 15;
                        int v = chunk.getSectionIndex(t);
                        if (q != v) {
                            q = v;
                            chunkSection = chunk.getSection(v);
                        }

                        double d = (double)s / (double)l;
                        chunkNoiseSampler.interpolateY(t, d);

                        for (int w = 0; w < k; w++) {
                            int x = i + o * k + w;
                            int y = x & 15;
                            double e = (double)w / (double)k;
                            chunkNoiseSampler.interpolateX(x, e);

                            for (int z = 0; z < k; z++) {
                                int aa = j + p * k + z;
                                int ab = aa & 15;
                                double f = (double)z / (double)k;
                                chunkNoiseSampler.interpolateZ(aa, f);
                                BlockState blockState = chunkNoiseSampler.sampleBlockState();
                                if (blockState == null) {
                                    blockState = this.getSettings().value().defaultBlock();
                                }

                                if (aa != -1 && aa != 0 && aa != 1) {
                                    blockState = AIR;
                                    continue;
                                }

                                blockState = super.getBlockState(chunkNoiseSampler, x, t, aa, blockState);
                                if (blockState != AIR && !SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
                                    chunkSection.setBlockState(y, u, ab, blockState, false);
                                    heightmap.trackUpdate(y, t, ab, blockState);
                                    heightmap2.trackUpdate(y, t, ab, blockState);
                                    if (aquiferSampler.needsFluidTick() && !blockState.getFluidState().isEmpty()) {
                                        mutable.set(x, t, aa);
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