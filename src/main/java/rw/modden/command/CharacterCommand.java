package rw.modden.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rw.modden.character.PlayerData;
import rw.modden.character.Character;
import rw.modden.character.characters.FIRrICECharacter;
import rw.modden.character.characters.Kllima777Character;

public class CharacterCommand {
    private static final SuggestionProvider<ServerCommandSource> CHARACTER_SUGGESTIONS = (context, builder) -> {
        builder.suggest("kllima777");
        builder.suggest("firrice");
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
                                                        Identifier skinId = null;

                                                        switch (characterName) {
                                                            case "kllima777":
                                                                character = new Kllima777Character();
                                                                skinId = new Identifier("axorunelostworlds", "skins/kllima777.png");
                                                                break;
                                                            case "firrice":
                                                                character = new FIRrICECharacter();
                                                                skinId = new Identifier("axorunelostworlds", "skins/firrice.png");
                                                                break;
                                                            default:
                                                                context.getSource().sendError(Text.literal("Unknown character: " + characterName));
                                                                return 0;
                                                        }

                                                        PlayerData data = PlayerData.getOrCreate(player);
                                                        data.setActiveCharacter(character, player);
                                                        data.setSkin(skinId, player);
                                                        context.getSource().sendMessage(Text.literal("Установлен персонаж " + characterName + " для игрока " + player.getGameProfile().getName()));
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
            );
        });
    }
}