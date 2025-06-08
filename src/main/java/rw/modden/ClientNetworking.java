package rw.modden;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.PlayerSkinTexture;
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
    private static Identifier pendingSkinId = null; // Кэш для скина, если игрок ещё не инициализирован

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
                            pendingSkinId = skinId; // Кэшируем скин
                        }
                    });
                });

        // Проверка отложенного скина при входе игрока
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "player_join"),
                (client, handler, buf, responseSender) -> {
                    client.execute(() -> {
                        if (client.player != null && pendingSkinId != null) {
                            UUID playerUuid = client.player.getUuid();
                            playerSkins.put(playerUuid, pendingSkinId);
                            applySkin(client.player, pendingSkinId);
                            System.out.println("ClientNetworking: Applied cached skin: " + pendingSkinId);
                            pendingSkinId = null; // Очищаем кэш
                        }
                    });
                });
    }

    private static void applySkin(AbstractClientPlayerEntity player, Identifier skinId) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();
        // Регистрация текстуры
        textureManager.registerTexture(skinId, new PlayerSkinTexture(null, skinId.toString(), null, false, null));
        // Форсирование перерисовки мира для "резкого" эффекта
        client.worldRenderer.reload();
        System.out.println("ClientNetworking: Applied skin " + skinId + " for player " + player.getName().getString());
    }

    public static Identifier getPlayerSkin(UUID uuid) {
        return playerSkins.getOrDefault(uuid, new Identifier("minecraft", "textures/entity/player/steve.png"));
    }

    public static CombatState getCombatState() {
        return combatState;
    }
}