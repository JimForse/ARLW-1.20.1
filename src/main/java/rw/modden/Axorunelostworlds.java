package rw.modden;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.combat.CombatMechanics;
import rw.modden.combat.IntermediateInitializer;
import rw.modden.world.dimension.ModDimensions;

public class Axorunelostworlds implements ModInitializer {
	public static final String MOD_ID = "axorunelostworlds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello worlds and their Gods!");
		System.out.println("Mod initialized: " + MOD_ID);
		CombatMechanics.initialize();
		IntermediateInitializer.initialize();
//		ModDimensions.register();
	}
}