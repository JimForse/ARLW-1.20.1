package rw.modden.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rw.modden.character.PlayerData;

public class NicknameCommand {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("name")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.literal("set")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .suggests(SkinCommand.getPlayerSuggestions())
                                        .then(CommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> {
                                                    ServerCommandSource source = context.getSource();
                                                    ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                                                    String customName = StringArgumentType.getString(context, "name");
                                                    PlayerData playerData = PlayerData.getOrCreate(targetPlayer);
                                                    playerData.setCustomName(customName, targetPlayer);
                                                    source.sendFeedback(
                                                            () -> Text.literal("Установлен кастомный ник " + customName + " для " + targetPlayer.getGameProfile().getName()),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                    )
                            )
            );
        });
    }
}
