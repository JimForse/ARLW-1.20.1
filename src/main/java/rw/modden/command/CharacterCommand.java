package rw.modden.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import rw.modden.character.PlayerData;
import rw.modden.character.Character;
import rw.modden.character.characters.*;

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
                                                        Character character = null;
                                                        String modelPath = null;

                                                        switch (characterName) {
                                                            case "kllima777":
                                                                character = new Kllima777Character();
                                                                modelPath = "axorunelostworlds/models/kllima777/model.bbmodel";
                                                                break;
                                                            case "firrice":
                                                                character = new FIRrICECharacter();
                                                                modelPath = "axorunelostworlds/models/firrice/model.bbmodel";
                                                                break;
                                                            case "stalker_anomaly":
                                                                character = new Stalker_AnomalyCharacter();
                                                                modelPath = "axorunelostworlds/models/stalker_anomaly/model.bbmodel";
                                                                break;
                                                            case "spectorprofm":
                                                                character = new SpectorprofmCharacter();
                                                                modelPath = "axorunelostworlds/models/spectorprofm/model.bbmodel";
                                                                break;
                                                            default:
                                                                context.getSource().sendError(Text.literal("Unknown character: " + characterName));
                                                                return 0;
                                                        }

                                                        PlayerData data = PlayerData.getOrCreate(player);
                                                        data.setActiveCharacter(character, player);
                                                        data.setModel(modelPath, player);
                                                        context.getSource().sendMessage(Text.literal("Установлен персонаж " + characterName + " для игрока " + player.getGameProfile().getName()));
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                            .then(CommandManager.literal("get")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .executes(context -> {
                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                String characterName = PlayerData.getCharacterName();

                                                context.getSource().sendMessage(Text.literal("Игрок " + player.getGameProfile().getName() + " использует персонажа " + characterName));

                                                return 0;
                                            })
                                    )
                            )
            );
        });
    }
}