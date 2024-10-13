package com.cyao.mixin;

import com.cyao.FlatMinecraft;
import com.cyao.PaperWorldGenerator;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldPresets.Registrar.class)
public abstract class WordPresetMixin {
    @Unique
    private static final RegistryKey<WorldPreset> PAPER_WORLD = RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.of(FlatMinecraft.MOD_ID, "paper_world"));
    @Shadow protected abstract void register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);
    @Shadow protected abstract DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator);

    @Shadow @Final private RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup;

    @Shadow @Final private RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> multiNoisePresetLookup;

    @Inject(method = "bootstrap()V", at = @At("RETURN"), remap = false)
    private void addPresets(CallbackInfo ci) {
        this.register(PAPER_WORLD, this.createOverworldOptions(
                new PaperWorldGenerator(
                        MultiNoiseBiomeSource.create(this.multiNoisePresetLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)),
                        this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD))
        ));
    }
}
