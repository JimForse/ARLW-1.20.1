package rw.modden;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.client.ClientClassesInitializer;
import rw.modden.combat.IntermediateInitializer;
import rw.modden.command.CommandClasesInitializer;
import rw.modden.server.ServerClassesInitializer;

public class Axorunelostworlds implements ModInitializer {
	public static final String MOD_ID = "axorunelostworlds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello worlds and their Gods!");
		LOGGER.info("Mod initialized: {}", MOD_ID);

		IntermediateInitializer.initialize();
		CommandClasesInitializer.initialize();
		ServerClassesInitializer.initialize();
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			ClientClassesInitializer.initialize();
	}
}
/*
 На дворе лето. Я в чужой стране. И тут эта тварь!
 Я просто хотел жить...
 */