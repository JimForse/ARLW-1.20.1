package rw.modden.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.character.PlayerData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SkinCommand {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("skin")
                            .then(CommandManager.literal("set")
                                    .then(CommandManager.argument("player", net.minecraft.command.argument.EntityArgumentType.player())
                                            .executes(context -> {
                                                ServerCommandSource source = context.getSource();
                                                ServerPlayerEntity targetPlayer = net.minecraft.command.argument.EntityArgumentType.getPlayer(context, "player");
                                                String playerName = targetPlayer.getGameProfile().getName().toLowerCase();

                                                // Проверяем файл в .minecraft/skins/
                                                Path skinPath = Paths.get(".minecraft", "skins", playerName + ".png");
                                                File skinFile = skinPath.toFile();
                                                Identifier skinId;

                                                if (skinFile.exists() && skinFile.isFile()) {
                                                    skinId = new Identifier(Axorunelostworlds.MOD_ID, "skins/" + playerName + ".png");
                                                    System.out.println("SkinCommand: Found skin file at " + skinPath + ", registering as " + skinId);
                                                    registerServerSkin(skinFile, skinId);
                                                } else {
                                                    skinId = new Identifier(Axorunelostworlds.MOD_ID, "skins/" + playerName + ".png");
                                                    System.out.println("SkinCommand: No file at " + skinPath + ", using mod resource " + skinId);
                                                }

                                                // Применяем скин
                                                PlayerData playerData = PlayerData.getOrCreate(targetPlayer);
                                                playerData.setSkin(skinId, targetPlayer);
                                                source.sendFeedback(() -> Text.literal("Set skin for " + playerName + " to " + skinId), true);
                                                return 1;
                                            })
                                    )
                            )
            );
        });
    }

    private static void registerServerSkin(File skinFile, Identifier skinId) {
        // TODO: Реализовать загрузку PNG-файла как текстуры на сервере
        System.out.println("SkinCommand: Registering server-side skin " + skinId + " from " + skinFile.getPath());
    }
}