package rw.modden.character;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

import rw.modden.character.characters.*;
import rw.modden.combat.CombatState;

import static rw.modden.Axorunelostworlds.LOGGER;

import net.minecraft.entity.player.PlayerEntity;
public class CharacterInitializer {

/*
    Класс инициализации всех персонажей
    Метод registerCharacter добавляет нового
    А ниже уже сравнивается имя игрока в нижнем регистре, с персонажем
    И при совпадении выдается модель с текстурой + персонаж и некоторые его данные
*/

    private static final List<CharacterEntry> CHARACTERS = new ArrayList<>();

    public static void initialize() {
        System.out.println("CharacterInitializer: Initializing characters...");

        registerCharacter("kllima777", new Kllima777Character());
        registerCharacter("firrice", new FIRrICECharacter());
        registerCharacter("stalker_anomaly", new Stalker_AnomalyCharacter());
        registerCharacter("spectorprofm", new SpectorprofmCharacter());
        // TODO: Добавить других персонажей

        // Обработчик входа игрока
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getGameProfile().getName().toLowerCase();
            LOGGER.info("CharacterInitializer: Игрок {} вошел", playerName);

            // Сбрасываем режим боя при входе
            PlayerData data = PlayerData.getOrCreate(player);
            data.setCombatMode(CombatState.NONE, player);

            // Загрузка сохранённых данных или создание нового персонажа
            for (CharacterEntry entry : CHARACTERS) {
                if (entry.playerName.equals(playerName)) {
                    if (!data.hasCharacter(playerName)) {
                        data.getInventory().addCharacter(entry.character);
                        data.markDirty();
                        LOGGER.info("CharacterInitializer: Добавлен персонаж для {} (не применён)", playerName);
                    }
                    return;
                }
            }
            LOGGER.info("CharacterInitializer: Персонаж для {} не найден", playerName);
        });
    }

    private static void registerCharacter(String playerName, Character character) {
        CHARACTERS.add(new CharacterEntry(playerName.toLowerCase(), character));
    }

    public static Character getCharacter(String playerName) {
        for (CharacterEntry entry : CHARACTERS) {
            if (entry.playerName.equals(playerName.toLowerCase())) {
                return entry.character;
            }
        }
        return null;
    }

    private static class CharacterEntry {
        public final String playerName;
        public final Character character;
        CharacterEntry(String playerName, Character character) {
            this.playerName = playerName;
            this.character = character;
        }
    }
}