package rw.modden.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import rw.modden.character.CharacterSwitcher;
import rw.modden.character.PlayerData;

public class CharacterCommand {
    private static final SuggestionProvider<ServerCommandSource> CHARACTER_SUGGESTIONS = (context, builder) -> {
        builder.suggest("kllima777");
        builder.suggest("firrice");
        builder.suggest("stalker_anomaly");
        builder.suggest("spectorprofm");
        return builder.buildFuture();
    };

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("character")
                            .then(CommandManager.literal("set")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .then(CommandManager.argument("character", StringArgumentType.word())
                                                    .suggests(CHARACTER_SUGGESTIONS)
                                                    .executes(context -> {
                                                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                        String characterName = StringArgumentType.getString(context, "character").toLowerCase();
                                                        boolean result = new CharacterSwitcher().switchCharacter(characterName, player);
                                                        if (!result) {
                                                            context.getSource().sendError(Text.literal("Unknown character: " + characterName));
                                                            return 0;
                                                        } else {
                                                            context.getSource().sendMessage(Text.literal("Установлен персонаж " + characterName + " для игрока " + player.getGameProfile().getName()));
                                                            return 1;
                                                        }
                                                    })
                                            )
                                    )
                            )
                            .then(CommandManager.literal("get")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .executes(context -> {
                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                PlayerData data = PlayerData.getOrCreate(player);
                                                context.getSource().sendMessage(Text.literal("Игрок " + player.getGameProfile().getName() + " использует персонажа " + data.getCharacterName(player)));
                                                return 0;
                                            })
                                    )
                            )
            );
        });
    }
}