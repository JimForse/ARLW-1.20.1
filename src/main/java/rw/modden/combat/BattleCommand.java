package rw.modden.combat;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.Character;
import rw.modden.character.CharacterInitializer;
import rw.modden.character.PlayerData;

import static net.minecraft.server.command.CommandManager.literal;

public class BattleCommand {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            register(dispatcher);
        });
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("battle")
                .then(literal("start").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    PlayerData data = PlayerData.getOrCreate(player);
                    Character character = CharacterInitializer.getCharacter(player.getGameProfile().getName().toLowerCase());
                    if (character != null && data.getCombatCharacters().isEmpty()) {
                        data.getCombatCharacters().add(character);
                        data.setCombatMode(CombatState.NORMAL, player);
                        System.out.println("BattleCommand: Set active character for " + player.getGameProfile().getName() + " to " + character.getType().name());
                    } else {
                        System.out.println("BattleCommand: Failed to start battle for " + player.getGameProfile().getName() + ", character: " + (character == null ? "null" : character.getType().name()));
                    }
                    return 1;
                }))
                .then(literal("stop").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    PlayerData data = PlayerData.getOrCreate(player);
                    data.setCombatMode(CombatState.NONE, player);
                    data.getCombatCharacters().clear();
                    System.out.println("BattleCommand: Stopped battle for " + player.getGameProfile().getName());
                    return 1;
                })));
    }
}