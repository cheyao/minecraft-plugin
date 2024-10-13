package com.cyao;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatMinecraft implements ModInitializer {
	public static final String MOD_ID = "flat-minecraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing mod");

		Registry.register(Registries.CHUNK_GENERATOR,
				Identifier.of(MOD_ID, "paper_world"),
				PaperWorldGenerator.CODEC);
	}
}
