package com.cyao.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerating.class)
public class ChunkMixin {
    @Inject(method = "initializeLight", at = @At("HEAD"))
    private static void getInitializeLightingFuture(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk, CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
        BlockPos spawn = context.world().getLevelProperties().getSpawnPos();

        ChunkPos pos = chunk.getPos();
        if ((pos.x < 1874999 && pos.x > -1875000 && pos.z < 1874999 && pos.z > -1875000) &&
                (pos.z * 16 > spawn.getZ() || (pos.z + 1) * 16 < spawn.getZ())) {
            ChunkSection[] sections = chunk.getSectionArray();
            for (int i = 0; i < sections.length; i++) {
                ChunkSection chunkSection = sections[i];
                PalettedContainer<BlockState> blockStateContainer = new PalettedContainer<>(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
                ReadableContainer<RegistryEntry<Biome>> biomeContainer = chunkSection.getBiomeContainer();
                sections[i] = new ChunkSection(blockStateContainer, biomeContainer);
            }

            for (BlockPos bePos : chunk.getBlockEntityPositions()) {
                chunk.removeBlockEntity(bePos);
            }

            int elementBits = MathHelper.ceilLog2(chunk.getHeight() + 1);
            long[] emptyHeightmap = new PackedIntegerArray(elementBits, 256).getData();
            for (Map.Entry<Heightmap.Type, Heightmap> entry : chunk.getHeightmaps()) {
                entry.getValue().setTo(chunk, entry.getKey(), emptyHeightmap);
            }
        }
    }
}
