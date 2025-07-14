package rw.modden.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.CombatState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static rw.modden.Axorunelostworlds.LOGGER;

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

    private static String pendingModelPath = null;
    private static String pendingAnimationPath = null;
    private static CombatState combatState = CombatState.NONE;
    private static final Map<String, ModelData> playerModels = new HashMap<>();
    private static final Map<String, CustomPlayerModel> playerGeoModels = new HashMap<>();
    private static final Map<String, Integer> playerDashCoolDowns = new HashMap<>();
    private static Integer pendingDashCoolDowns = null;

    public void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_model"),
                (client, handler, buf, responseSender) -> {
                    String modelPath = buf.readString();
                    String animationPath = buf.readString();
                    String playerName = buf.readString();
                    LOGGER.info("ClientNetworking: Получен sync_model: modelPath={}, animationPath={}, playerName={}", modelPath, animationPath, playerName);
                    if (modelPath.isEmpty()) return;
                    client.execute(() -> applyModel(modelPath, animationPath, getPlayerByName(playerName)));
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
                        if (pendingModelPath != null && pendingAnimationPath != null) {
                            LOGGER.info("player_join: Applying pending model: {}, animation: {}", pendingModelPath, pendingAnimationPath);
                            setDashCoolDown(client.player, pendingDashCoolDowns);
                            applyModel(pendingModelPath, pendingAnimationPath, client.player);
                            playerModels.put(client.player.getGameProfile().getName().toLowerCase(), new ModelData(pendingModelPath, pendingAnimationPath));
                            pendingModelPath = null;
                            pendingAnimationPath = null;
                        }
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_dash_cool_down"),
                (client, handler, buf, responseSender) -> {
                    if (client.player != null)
                        playerDashCoolDowns.put(client.player.getGameProfile().getName().toLowerCase(), buf.readInt());
                    else
                        pendingDashCoolDowns = buf.readInt();
                }
        );
    }

// ----------------------------------------== Apply`s / Setters ==------------------------------------------------------

    public static void setDashCoolDown(PlayerEntity player, Integer i) {
        if (player != null && i != null) {
            playerDashCoolDowns.put(player.getGameProfile().getName().toLowerCase(), i);
        }
    }

    public static void applyModel(String modelPath, String animationPath, PlayerEntity player) {
        if (player == null) {
            LOGGER.warn("ClientNetworking: Игрок null, пропуск модели: {}", modelPath);
            return;
        }
        String playerName = player.getGameProfile().getName().toLowerCase();
        if (modelPath.startsWith("minecraft:")) {
            LOGGER.info("ClientNetworking: Стандартная модель: {}, пропуск", modelPath);
            playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "animations/player_animation.json"));
            playerGeoModels.remove(playerName); // Удаляем кастомную модель
            return;
        }
        try {
            // Проверка путей
            if (!modelPath.endsWith(".geo.json")) {
                LOGGER.error("ClientNetworking: Неверный путь модели: {}", modelPath);
                playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "animations/player_animation.json"));
                playerGeoModels.remove(playerName);
                return;
            }

            // Получаем рендерер
            EntityRenderer<?> renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
            if (!(renderer instanceof CustomPlayerRenderer customRenderer)) {
                LOGGER.warn("ClientNetworking: Рендерер для {} не является CustomPlayerRenderer, текущий тип: {}, пропуск",
                        playerName, renderer.getClass().getSimpleName());
                return;
            }

            // Создаём или обновляем GeoModel
            CustomPlayerModel geoModel = playerGeoModels.computeIfAbsent(playerName,
                    k -> new CustomPlayerModel(Axorunelostworlds.MOD_ID));
            geoModel.setModelPath(modelPath);
            geoModel.setAnimationPath(animationPath);
            playerModels.put(playerName, new ModelData(modelPath, animationPath));
            LOGGER.info("ClientNetworking: Загружаю модель {} и анимацию {} для {}", modelPath, animationPath, playerName);

            // Проверка загрузки
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                int attempts = 0;
                while (attempts < 10) {
                    if (customRenderer.getGeoModel() != null) {
                        LOGGER.info("ClientNetworking: Модель загружена: {}", modelPath);
                        return;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        LOGGER.error("ClientNetworking: Ошибка ожидания: {}", e.getMessage());
                    }
                    attempts++;
                }
                LOGGER.warn("ClientNetworking: Не удалось загрузить модель: {}", modelPath);
            });
        } catch (Exception e) {
            LOGGER.error("ClientNetworking: Ошибка загрузки модели {}: {}", modelPath, e.getMessage());
            playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "animations/player_animation.json"));
            playerGeoModels.remove(playerName);
        }
    }
// ----------------------------------------== Getters ==----------------------------------------------------------------

    public static CombatState getCombatState() {
        return combatState;
    }
    public static CustomPlayerModel getGeoModel(String playerName) {
        return playerGeoModels.get(playerName.toLowerCase());
    }
    public static ModelData getPlayerModel(String playerName) {
        return playerModels.getOrDefault(playerName.toLowerCase(), new ModelData("minecraft:entity/player/wide", "animations/player_animation.json"));
    }
    public static int getDashCoolDown(UUID playerUuid) {
        return playerDashCoolDowns.getOrDefault(playerUuid, 60);
    }
    private static PlayerEntity getPlayerByName(String playerName) {
        return MinecraftClient.getInstance().world != null
                ? MinecraftClient.getInstance().world.getPlayers()
                .stream()
                .filter(p -> p.getGameProfile().getName().toLowerCase().equals(playerName.toLowerCase()))
                .findFirst()
                .orElse(null)
                : null;
    }

// ---------------------------------------------------------------------------------------------------------------------

    public static class ModelData {
        public final String modelPath;
        public final String animationPath;

        public ModelData(String modelPath, String animationPath) {
            this.modelPath = modelPath;
            this.animationPath = animationPath;
        }

        public ModelData() {
            this.modelPath = "minecraft:entity/player/wide";
            this.animationPath = "animations/player_animation.json";
        }
    }
}