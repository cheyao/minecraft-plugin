package com.cyao.mixin;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldPresets.Registrar.class)
public abstract class WorldPresetMixin {
    @Unique
    private static final RegistryKey<WorldPreset> PAPER_WORLD = RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.of("flat-minecraft:2D-world"));
    @Shadow
    protected abstract void register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);
    @Shadow
    protected abstract DimensionOptions createOverworldOptions(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> chunkGeneratorSettings);

    @Shadow @Final private RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> multiNoisePresetLookup;
    @Shadow @Final private RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup;

    @Inject(method = "bootstrap()V", at = @At("RETURN"))
    private void addPresets(CallbackInfo ci) {
        RegistryEntry.Reference<MultiNoiseBiomeSourceParameterList> reference = this.multiNoisePresetLookup
                .getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
        MultiNoiseBiomeSource.create(reference);
        RegistryEntry<ChunkGeneratorSettings> registryEntry = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
        this.register(PAPER_WORLD, this.createOverworldOptions(MultiNoiseBiomeSource.create(reference), registryEntry));
    }
}
