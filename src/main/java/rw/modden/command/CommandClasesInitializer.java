package rw.modden.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandClasesInitializer {
    public static void initialize() {
        BattleCommand.initialize();
        SkinCommand.initialize();
        CharacterCommand.initialize();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DimensionCommands.register(dispatcher));
    }
}
