package rw.modden.character;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;

import java.util.ArrayList;
import java.util.List;

public class CharacterInitializer {
    private static final List<CharacterEntry> CHARACTERS = new ArrayList<>();

    public static void initialize() {
        System.out.println("CharacterInitializer: Initializing characters...");

        // Регистрация персонажа Kllima777
        registerCharacter(
                "Kllima777",
                new Kllima777Character(),
                new Identifier(Axorunelostworlds.MOD_ID, "skins/kllima777")
        );

        // TODO: Добавить других персонажей

        // Обработчик входа игрока
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getGameProfile().getName().toLowerCase();
            System.out.println("CharacterInitializer: Player joined: " + playerName);

            for (CharacterEntry entry : CHARACTERS) {
                if (entry.playerName.equals(playerName)) {
                    System.out.println("CharacterInitializer: Assigning character for " + playerName);
                    // Загрузка сохранённых данных или создание нового персонажа
                    PlayerData playerData = PlayerData.getOrCreate(player);
                    playerData.setCharacter(entry.character);
                    playerData.applyToPlayer(player);
                    // TODO: Применить скин (entry.skinId)
                    return;
                }
            }
            System.out.println("CharacterInitializer: No character found for " + playerName);
        });
    }

    private static void registerCharacter(String playerName, Character character, Identifier skinId) {
        CHARACTERS.add(new CharacterEntry(playerName.toLowerCase(), character, skinId));
        System.out.println("CharacterInitializer: Registered character for player: " + playerName +
                ", skin: " + skinId);
    }

    public static Character getCharacter(String playerName) {
        for (CharacterEntry entry : CHARACTERS) {
            if (entry.playerName.equals(playerName.toLowerCase())) {
                return entry.character;
            }
        }
        return null;
    }

    public static Identifier getSkin(String playerName) {
        for (CharacterEntry entry : CHARACTERS) {
            if (entry.playerName.equals(playerName.toLowerCase())) {
                return entry.skinId;
            }
        }
        return null;
    }

    private static class CharacterEntry {
        final String playerName;
        final Character character;
        final Identifier skinId;

        CharacterEntry(String playerName, Character character, Identifier skinId) {
            this.playerName = playerName;
            this.character = character;
            this.skinId = skinId;
        }
    }
}