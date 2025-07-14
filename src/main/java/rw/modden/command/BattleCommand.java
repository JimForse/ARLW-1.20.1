package rw.modden.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rw.modden.character.Character;
import rw.modden.character.CharacterInitializer;
import rw.modden.character.PlayerData;
import rw.modden.combat.CombatState;

import net.minecraft.command.argument.EntityArgumentType;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BattleCommand {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            register(dispatcher);
        });
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("battle")
                .requires(source -> source.hasPermissionLevel(2)) // Только для операторов
                .then(literal("start")
                        .then(literal("Standart")
                                .then(argument("players", EntityArgumentType.players())
                                        .executes(context -> {
                                            List<ServerPlayerEntity> players = new ArrayList<>(EntityArgumentType.getPlayers(context, "players"));
                                            for (ServerPlayerEntity player : players) {
                                                PlayerData data = PlayerData.getOrCreate(player);
                                                Character character = CharacterInitializer.getCharacter(player.getGameProfile().getName().toLowerCase());
                                                if (character != null && data.getCombatCharacters().isEmpty()) {
                                                    data.getCombatCharacters().add(character);
                                                    data.setCombatMode(CombatState.NORMAL, player);
                                                    System.out.println("BattleCommand: Started Standart battle for " + player.getGameProfile().getName());
                                                }
                                            }
                                            return 1;
                                        })))
                        .then(literal("Event")
                                .then(argument("players", EntityArgumentType.players())
                                        .executes(context -> {
                                            List<ServerPlayerEntity> players = new ArrayList<>(EntityArgumentType.getPlayers(context, "players"));
                                            for (ServerPlayerEntity player : players) {
                                                PlayerData data = PlayerData.getOrCreate(player);
                                                Character character = CharacterInitializer.getCharacter(player.getGameProfile().getName().toLowerCase());
                                                if (character != null && data.getCombatCharacters().isEmpty()) {
                                                    data.getCombatCharacters().add(character);
                                                    data.setCombatMode(CombatState.EVENT, player);
                                                    System.out.println("BattleCommand: Started Event battle for " + player.getGameProfile().getName());
                                                }
                                            }
                                            return 1;
                                        }))))
                .then(literal("stop")
                        .then(argument("players", EntityArgumentType.players())
                                .executes(context -> {
                                    List<ServerPlayerEntity> players = new ArrayList<>(EntityArgumentType.getPlayers(context, "players"));
                                    for (ServerPlayerEntity player : players) {
                                        PlayerData data = PlayerData.getOrCreate(player);
                                        data.setCombatMode(CombatState.NONE, player);
                                        data.getCombatCharacters().clear();
                                        System.out.println("BattleCommand: Stopped battle for " + player.getGameProfile().getName());
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("state")
                        .then(argument("players", EntityArgumentType.player())
                                .executes(context -> {
                                    List<ServerPlayerEntity> players = new ArrayList<>(EntityArgumentType.getPlayers(context, "players"));
                                    for (ServerPlayerEntity player: players) {
                                        PlayerData data = PlayerData.getOrCreate(player);
                                        System.out.println("BattleCommand: Battle state is: " + data.getCombatState());
                                        context.getSource().sendMessage(Text.of("Battle state at "+ player.getGameProfile().getName() +" is: " + data.getCombatState()));
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}