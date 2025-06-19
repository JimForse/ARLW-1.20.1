package rw.modden;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.figuramc.figura.avatar.*;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.FiguraMod;
import rw.modden.combat.CombatState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)

/*
        Слышали про круги ада?
        Это десятый.
        Сколько кружек чая было выпито,
        Пока я не понял, как его настроить...
 */

public class ClientNetworking implements ClientModInitializer {
//    private static Identifier pendingSkinId = null;
    private static Identifier pendingModelId = null;
    private static CombatState combatState = CombatState.NONE;
    private static final Map<UUID, Identifier> playerSkins = new HashMap<>();
    private static final Map<UUID, Identifier> playerModels = new HashMap<>();

    @Override
    public void onInitializeClient() {
//        ClientPlayNetworking.registerGlobalReceiver(
//                new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"),
//                (client, handler, buf, responseSender) -> {
//                    Identifier skinId = buf.readIdentifier();
//                    client.execute(() -> {
//                        if (client.player != null) {
//                            applySkin(skinId, client.player);
//                            playerSkins.put(client.player.getUuid(), skinId);
//                        } else {
//                            pendingSkinId = skinId;
//                        }
//                    });
//                }
//        );

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_model"),
                (client, handler, buf, responseSender) -> {
                    System.out.println("ClientNetworking block sync_model buf status is: "+buf);
                    String bf = buf.readString();
                    System.out.println("ClientNetworking block sync_model bf status is: "+bf);
                    Identifier modelId = new Identifier(bf);
                    System.out.println("ClientNetworking block sync_model modelId status is: "+modelId);
                    client.execute(() -> {
                        if (client.player != null) {
                            applyModel(modelId, client.player);
                            playerModels.put(client.player.getUuid(), modelId);
                        } else {
                            pendingModelId = modelId;
                        }
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"),
                (client, handler, buf, responseSender) -> {
                    int stateOrdinal = buf.readInt();
                    client.execute(() -> {
                        combatState = CombatState.values()[stateOrdinal];
                        System.out.println("ClientNetworking: Updated combatState to: " + combatState);
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "player_join"),
                (client, handler, buf, responseSender) -> {
                    if (client.player != null) {
//                        if (pendingSkinId != null) {
//                            applySkin(pendingSkinId, client.player);
//                            playerSkins.put(client.player.getUuid(), pendingSkinId);
//                            pendingSkinId = null;
//                        }
                        if (pendingModelId != null) {
                            System.out.println("player_join: Applying pendingModelId: " + pendingModelId);
                            applyModel(pendingModelId, client.player);
                            playerModels.put(client.player.getUuid(), pendingModelId);
                            pendingModelId = null;
                        }
                    }
                }
        );
    }

    public static void applySkin(Identifier skinId, PlayerEntity player) {
//        System.out.println("Attempting to load skin: " + skinId);
//        try {
//            ResourceTexture texture = new ResourceTexture(skinId);
//            MinecraftClient.getInstance().getTextureManager().registerTexture(skinId, texture);
//            System.out.println("ClientNetworking: Successfully applied skin " + skinId + " for player " + player.getGameProfile().getName());
//            System.out.println("Skin applied, model assumed: default (Steve)");
//        } catch (Exception e) {
//            System.err.println("Failed to apply skin " + skinId + ": " + e.getMessage());
//            e.printStackTrace();
//        }
        System.out.println("Skin loading via Figura not implemented yet");
    }

    public static void applyModel(Identifier modelId, PlayerEntity player) {
        System.out.println("applyModel: Received modelId: " + modelId);
        if (modelId.getNamespace().equals("minecraft")) {
            System.out.println("applyModel: Skipping minecraft namespace");
            return; // Пропустить для minecraft:entity/player/wide
        }
        System.out.println("Attempting to apply model: " + modelId);
        String rawPath = modelId.getNamespace() + "/" + modelId.getPath().replace("/model.bbmodel", "");
        Path path = Paths.get("assets/" + rawPath);
        System.out.println("ClientNetworking.applyModel object rawPath: "+rawPath);
        System.out.println("ClientNetworking.applyModel object path: "+path);
        UUID uuid = FiguraMod.getLocalPlayerUUID();
        UserData user = new UserData(uuid);
        LocalAvatarLoader.loadAvatar(path, user);
        // Модель обрабатывается через Mixin
    }

    public static CombatState getCombatState() {
        return combatState;
    }

    public static Identifier getPlayerSkin(UUID playerUuid) {
        return playerSkins.getOrDefault(playerUuid, null);
    }

    public static Identifier getPlayerModel(UUID playerUuid) {
        return playerModels.getOrDefault(playerUuid, new Identifier("minecraft", "entity/player/wide"));
    }
}