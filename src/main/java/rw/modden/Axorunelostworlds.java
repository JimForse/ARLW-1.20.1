package rw.modden;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.impl.screenhandler.client.ClientNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.character.CharacterInitializer;
import rw.modden.combat.BattleCommand;
import rw.modden.combat.CombatMechanics;
import rw.modden.combat.IntermediateInitializer;
import rw.modden.combat.ServerCombatMechanics;

public class Axorunelostworlds implements ModInitializer {
	public static final String MOD_ID = "axorunelostworlds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello worlds and their Gods!");
		LOGGER.info("Mod initialized: {}", MOD_ID);

		CombatMechanics.initialize();
		ServerCombatMechanics.initialize();
		IntermediateInitializer.initialize();
		BattleCommand.initialize();
		// ModDimensions.register();
	}
}