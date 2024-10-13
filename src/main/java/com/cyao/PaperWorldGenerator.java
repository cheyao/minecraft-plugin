package com.cyao;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

public class PaperWorldGenerator extends NoiseChunkGenerator {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();

	public PaperWorldGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings);
        System.out.println("Hello Paper Chunk Generator!");
    }
}
