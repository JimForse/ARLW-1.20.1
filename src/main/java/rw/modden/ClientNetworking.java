package rw.modden;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.CombatState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientNetworking implements ClientModInitializer {
    private static final Map<UUID, Identifier> playerSkins = new HashMap<>();
    private static CombatState combatState = CombatState.NONE;
    private static Identifier pendingSkinId = null;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"),
                (client, handler, buf, responseSender) -> {
                    int stateOrdinal = buf.readInt();
                    CombatState state = CombatState.values()[stateOrdinal];
                    System.out.println("ClientNetworking: Updated combatState to: " + state);
                    client.execute(() -> combatState = state);
                });

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"),
                (client, handler, buf, responseSender) -> {
                    Identifier skinId = buf.readIdentifier();
                    System.out.println("ClientNetworking: Received sync_skin packet for skin: " + skinId);
                    client.execute(() -> {
                        if (client.player != null) {
                            UUID playerUuid = client.player.getUuid();
                            playerSkins.put(playerUuid, skinId);
                            applySkin(client.player, skinId);
                        } else {
                            System.out.println("ClientNetworking: Player is null, caching skin: " + skinId);
                            pendingSkinId = skinId;
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "player_join"),
                (client, handler, buf, responseSender) -> {
                    client.execute(() -> {
                        if (client.player != null && pendingSkinId != null) {
                            UUID playerUuid = client.player.getUuid();
                            playerSkins.put(playerUuid, pendingSkinId);
                            applySkin(client.player, pendingSkinId);
                            System.out.println("ClientNetworking: Applied cached skin: " + pendingSkinId);
                            pendingSkinId = null;
                        }
                    });
                });
    }

    private static void applySkin(AbstractClientPlayerEntity player, Identifier skinId) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        // Извлекаем имя скина
        String skinName = skinId.getPath().replace("skins/", "").replace(".png", "");
        Identifier textureId = new Identifier(skinId.getNamespace(), "skins/" + skinName);

        // Регистрируем текстуру из ресурсов мода
        ResourceTexture skinTexture = new ResourceTexture(textureId);
        textureManager.registerTexture(textureId, skinTexture);

        System.out.println("ClientNetworking: Applied skin " + textureId + " for player " + player.getName().getString());
    }

    public static Identifier getPlayerSkin(UUID uuid) {
        return playerSkins.getOrDefault(uuid, new Identifier("minecraft", "textures/entity/steve.png"));
    }

    public static CombatState getCombatState() {
        return combatState;
    }
}