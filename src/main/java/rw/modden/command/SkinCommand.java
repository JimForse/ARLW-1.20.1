package rw.modden.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.character.Character;
import rw.modden.character.CharacterInitializer;
import rw.modden.character.PlayerData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class SkinCommand {
    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS = (context, builder) -> {
        for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

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
                    CommandManager.literal("skin")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.literal("set")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .suggests(PLAYER_SUGGESTIONS)
                                            .then(CommandManager.argument("characterName", StringArgumentType.word())
                                                    .suggests(CHARACTER_SUGGESTIONS)
                                                    .executes(context -> {
                                                        ServerCommandSource source = context.getSource();
                                                        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                                                        String characterName = StringArgumentType.getString(context, "characterName").toLowerCase();

                                                        Character character = CharacterInitializer.getCharacter(characterName);
                                                        if (character == null) {
                                                            source.sendError(Text.literal("Персонаж " + characterName + " не найден."));
                                                            return 0;
                                                        }

                                                        PlayerData playerData = PlayerData.getOrCreate(targetPlayer);
                                                        String modelPath = character.getModelPath();
                                                        String animationPath = character.getAnimationPath();
                                                        String texturePath = character.getTexturePath();
                                                        playerData.setModel(modelPath, texturePath, animationPath, targetPlayer);
                                                        source.sendFeedback(
                                                                () -> Text.literal("Установлен скин персонажа " + characterName + " для игрока " + targetPlayer.getGameProfile().getName()),
                                                                true
                                                        );
                                                        Axorunelostworlds.LOGGER.info("SkinCommand: Установлена модель {} для игрока {}", modelPath, targetPlayer.getGameProfile().getName());
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                            .then(CommandManager.literal("get")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .suggests(PLAYER_SUGGESTIONS)
                                            .executes(context -> {
                                                ServerCommandSource source = context.getSource();
                                                ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                                                PlayerData playerData = PlayerData.getOrCreate(targetPlayer);
                                                String modelPath = playerData.getModelPath();
                                                source.sendFeedback(
                                                        () -> Text.literal("Текущая модель игрока " + targetPlayer.getGameProfile().getName() + ": " + (modelPath != null ? modelPath : "нет модели")),
                                                        false
                                                );
                                                return 1;
                                            })
                                    )
                            )
            );
        });
    }

    public static SuggestionProvider<ServerCommandSource> getPlayerSuggestions() {
        return PLAYER_SUGGESTIONS;
    }
}