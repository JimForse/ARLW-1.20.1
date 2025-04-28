package rw.modden;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Axorunelostworlds implements DedicatedServerModInitializer {
	public static final String MOD_ID = "axorunelostworlds";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeServer() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			throw new RuntimeException("Этот мод предназначен только для сервера!");
		}
		LOGGER.info("Hello worlds and their Gods!");
	}
}