package com.cyao;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.registry.Registries.CHUNK_GENERATOR;

public class FlatMinecraft implements ModInitializer {
	public static final String MOD_ID = "flat-minecraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing mod");
		Registry.register(CHUNK_GENERATOR,
                Identifier.of("flat-minecraft:2D-world"),
				PaperChunkGenerator.CODEC);
	}
}
