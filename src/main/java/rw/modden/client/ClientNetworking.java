package rw.modden.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.figuramc.figura.avatar.*;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.FiguraMod;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.CombatState;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
        Слышали про круги ада?
        Это десятый.
        Сколько кружек чая было выпито,
        Пока я не понял, как его настроить...
 */

public class ClientNetworking {

/*
    Класс для хранения и отправки клиентских данных на сервер
    Предварительно синхронизировав их с серверными
    Благодаря методам в PlayerData и этом классах
*/

    private static Identifier pendingModelId = null;
    private static CombatState combatState = CombatState.NONE;
    private static final Map<UUID, Identifier> playerModels = new HashMap<>();
    private static Map<UUID, Integer> playerDashCoolDowns = new HashMap<>();
    private static Integer pendingDashCoolDowns = null;

    public void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_model"),
                (client, handler, buf, responseSender) -> {
                    String bf = buf.readString();
                    Identifier modelId = new Identifier(bf);
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
                        if (pendingModelId != null) {
                            System.out.println("player_join: Applying pendingModelId: " + pendingModelId);
                            setDashCoolDown(client.player, pendingDashCoolDowns);
                            applyModel(pendingModelId, client.player);
                            playerModels.put(client.player.getUuid(), pendingModelId);
                            pendingModelId = null;
                        }
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_dash_cool_down"),
                (client, handler, buf, responseSender) -> {
                    if (client.player != null)
                        playerDashCoolDowns.put(client.player.getUuid(), buf.readInt());
                    else
                        pendingDashCoolDowns = buf.readInt();
                }
        );
    }

// ----------------------------------------== Apply`s / Setters ==------------------------------------------------------

    public static void setDashCoolDown(PlayerEntity player, Integer i) {
        if (player != null && i != null) {
            playerDashCoolDowns.put(player.getUuid(), i);
        }
    }

    public static void applyModel(Identifier modelId, PlayerEntity player) {
        Axorunelostworlds.LOGGER.info("applyModel: Получен modelId: {} для игрока {}", modelId, player.getGameProfile().getName());
        if (modelId.getNamespace().equals("minecraft")) {
            Axorunelostworlds.LOGGER.info("applyModel: Пропускаем модель из пространства minecraft для игрока {}", player.getGameProfile().getName());
            return;
        }
        try {
            String modelPath = modelId.getPath();
            Path path = Paths.get("figura/avatars", player.getEntityName().toLowerCase());
            AvatarManager.loadLocalAvatar(path);
            Avatar avatar = AvatarManager.getAvatar(player);
            if (avatar == null) {
                UUID playerUuid = player.getUuid();
                UserData user = new UserData(playerUuid);
                LocalAvatarLoader.loadAvatar(path, user);
            }
        } catch (Exception e) {
            Axorunelostworlds.LOGGER.error("Не удалось загрузить модель Figura для игрока {}: {}", player.getGameProfile().getName(), e.getMessage());
            e.printStackTrace();
        }
    }

// ----------------------------------------== Getters ==----------------------------------------------------------------

    public static CombatState getCombatState() {
        return combatState;
    }
    public static Identifier getPlayerModel(UUID playerUuid) {
        return playerModels.getOrDefault(playerUuid, new Identifier("minecraft", "entity/player/wide"));
    }
    public static int getDashCoolDown(UUID playerUuid) {
        return playerDashCoolDowns.getOrDefault(playerUuid, 60);
    }
}