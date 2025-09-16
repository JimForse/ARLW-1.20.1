package rw.modden.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.client.particle.PrimordialMatterParticle;
import rw.modden.combat.CombatState;
import rw.modden.world.dimension.ModDimensions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.random.Random;
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
    private static String pendingTexturePath = null;
    private static final Map<String, ModelData> playerModels = new HashMap<>();
    private static final Map<String, CustomPlayerModel> playerGeoModels = new HashMap<>();
    private static final Map<String, String> playerCustomNames = new HashMap<>();
    private static final Map<String, Integer> playerDashCoolDowns = new HashMap<>();
    private static Integer pendingDashCoolDowns = null;

    public void initialize() {
        LOGGER.info("ClientNetworking.initialize called!");
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_model"),
                (client, handler, buf, responseSender) -> {
                    String modelPath = buf.readString();
                    String texturePath = buf.readString();
                    String animationPath = buf.readString();
                    String playerName = buf.readString();
                    LOGGER.info("ClientNetworking: Получен sync_model: modelPath={}, texturePath={}, animationPath={}, playerName={}",
                            modelPath, texturePath, animationPath, playerName);
                    if (modelPath.isEmpty()) return;
                    client.execute(() -> applyModel(modelPath, texturePath, animationPath, getPlayerByName(playerName)));
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
                        if (pendingModelPath != null && pendingTexturePath != null && pendingAnimationPath != null) {
                            LOGGER.info("player_join: Applying pending model: {}, texture: {}, animation: {}",
                                    pendingModelPath, pendingTexturePath, pendingAnimationPath);
                            setDashCoolDown(client.player, pendingDashCoolDowns);
                            applyModel(pendingModelPath, pendingTexturePath, pendingAnimationPath, client.player);
                            playerModels.put(client.player.getGameProfile().getName().toLowerCase(),
                                    new ModelData(pendingModelPath, pendingAnimationPath));
                            pendingModelPath = null;
                            pendingTexturePath = null;
                            pendingAnimationPath = null;
                        }
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_custom_name"),
                (client, handler, buf, responseSender) -> {
                    String customName = buf.readString();
                    String playerName = buf.readString();
                    client.execute(() -> {
                        if (!customName.isEmpty()) {
                            playerCustomNames.put(playerName.toLowerCase(), customName);
                            LOGGER.info("ClientNetworking: Установлен кастомный ник {} для {}", customName, playerName);
                        } else {
                            playerCustomNames.remove(playerName.toLowerCase());
                        }
                    });
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

        // Регистрация фабрики частицы
        ParticleFactoryRegistry.getInstance().register(ModDimensions.PRIMORDIAL_MATTER, PrimordialMatterParticle.Factory::new);
        LOGGER.info("Registered particle factory for primordialmatter");

        // Спавн частиц в измерении create
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.world.getRegistryKey() == ModDimensions.CREATE_WORLD && client.player != null) {
                Random random = client.world.random;
                if (random.nextFloat() < 0.03f) {
                    double x = client.player.getX() + (random.nextDouble() * 32 - 16);
                    double y = client.player.getY() + (random.nextDouble() * 16 - 8);
                    double z = client.player.getZ() + (random.nextDouble() * 32 - 16);
                    client.world.addParticle(ModDimensions.PRIMORDIAL_MATTER, x, y, z, 0.0, 0.01, 0.0);
                }}
        });
    }

// ----------------------------------------== Apply`s / Setters ==------------------------------------------------------

    public static void setDashCoolDown(PlayerEntity player, Integer i) {
        if (player != null && i != null) {
            playerDashCoolDowns.put(player.getGameProfile().getName().toLowerCase(), i);
        }
    }

    public static void applyModel(String modelPath, String texturePath, String animationPath, PlayerEntity player) {
        if (player == null) {
            LOGGER.warn("ClientNetworking: Игрок null, пропуск модели: {}", modelPath);
            return;
        }
        String playerName = player.getGameProfile().getName().toLowerCase();
        String entityName = getCustomName(playerName).toLowerCase();
        if (modelPath.startsWith("minecraft:")) {
            LOGGER.info("ClientNetworking: Стандартная модель: {}, пропуск", modelPath);
            playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "minecraft:animations/player_animation.json"));
            playerGeoModels.remove(playerName);
            return;
        }
        try {
            // Проверка путей
            if (!modelPath.endsWith(".geo.json")) {
                LOGGER.error("ClientNetworking: Неверный путь модели: {}", modelPath);
                playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "minecraft:animations/player_animation.json"));
                playerGeoModels.remove(playerName);
                return;
            }
            if (!texturePath.endsWith(".png")) {
                LOGGER.error("ClientNetworking: Неверный путь текстуры: {}", texturePath);
                playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "minecraft:animations/player_animation.json"));
                playerGeoModels.remove(playerName);
                return;
            }
            if (!animationPath.endsWith(".anim.json")) {
                LOGGER.error("ClientNetworking: Неверный путь анимации: {}", animationPath);
                playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "minecraft:animations/player_animation.json"));
                playerGeoModels.remove(playerName);
                return;
            }

            // Проверка наличия файлов
            Path runeModelsDir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "RuneModels");
            File modelFile = runeModelsDir.resolve(modelPath).toFile();
            File textureFile = runeModelsDir.resolve(texturePath).toFile();
            File animationFile = runeModelsDir.resolve(animationPath).toFile();
            if (!modelFile.exists() || !textureFile.exists()) {
                LOGGER.error("ClientNetworking: Файлы не найдены: model={}, texture={}", modelPath, texturePath);
                playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "minecraft:animations/player_animation.json"));
                playerGeoModels.remove(playerName);
                return;
            }
            if (!animationFile.exists()) {
                LOGGER.warn("ClientNetworking: Файл анимации не найден: {}, продолжаем без анимации", animationPath);
            }

            // Получаем рендерер
            EntityRenderer<?> renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
            if (!(renderer instanceof CustomPlayerRenderer)) {
                LOGGER.warn("ClientNetworking: Рендерер для {} не является CustomPlayerRenderer, текущий тип: {}, пропуск",
                        playerName, renderer.getClass().getSimpleName());
                return;
            }

            // Создаём или обновляем GeoModel
            CustomPlayerModel geoModel = playerGeoModels.computeIfAbsent(playerName,
                    k -> new CustomPlayerModel(Axorunelostworlds.MOD_ID));
            geoModel.setModelPath(modelPath);
            geoModel.setTexturePath(texturePath);
            geoModel.setAnimationPath(animationPath);
            playerModels.put(playerName, new ModelData(modelPath, animationPath));

            // Логируем успешное применение
            LOGGER.info("ClientNetworking: Применена модель {}, текстура {} и анимация {} для {}",
                    modelPath, texturePath, animationPath, playerName);
        } catch (Exception e) {
            LOGGER.error("ClientNetworking: Ошибка загрузки модели {}, текстуры {} или анимации {}: {}",
                    modelPath, texturePath, animationPath, e.getMessage());
            playerModels.put(playerName, new ModelData("minecraft:entity/player/wide", "minecraft:animations/player_animation.json"));
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
    public static String getCustomName(String playerName) {
        return playerCustomNames.getOrDefault(playerName.toLowerCase(), playerName);
    }
    public static ModelData getPlayerModel(String playerName) {
        return playerModels.getOrDefault(playerName.toLowerCase(), new ModelData("minecraft:entity/player/wide", "animations/player_animation.json"));
    }
    public static int getDashCoolDown(String playerName) {
        return playerDashCoolDowns.getOrDefault(playerName.toLowerCase(), 60);
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