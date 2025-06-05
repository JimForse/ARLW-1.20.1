package rw.modden.character;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;

import java.util.ArrayList;
import java.util.List;

public class CharacterInitializer {
    private static final List<CharacterEntry> CHARACTERS = new ArrayList<CharacterEntry>();

    public static void initialize() {
        System.out.println("CharacterInitializer: Initializing characters...");

        registerCharacter(
                "kllima777",
                new Kllima777Character(),
                new Identifier(Axorunelostworlds.MOD_ID, "skins/kllima777")
        );

        // TODO: Добавить других персонажей

        // Обработчик входа игрока
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getGameProfile().getName().toLowerCase();
            System.out.println("CharacterInitializer: Player joined: " + playerName);

            // Загрузка сохранённых данных или создание нового персонажа
            for (CharacterEntry entry:CHARACTERS) {
                if (entry.playerName.equals(playerName)) {
                    PlayerData playerData = PlayerData.getOrCreate(player);
                    if (!playerData.hasCharacter(playerName)) {
                        playerData.getInventory().addCharacter(entry.character);
                        if (!playerData.getCombatCharacters().contains(entry.character) && playerData.getCombatCharacters().size() < 3) {
                            playerData.getCombatCharacters().add(entry.character);
                        }
                        playerData.markDirty();
                        System.out.println("CharacterInitializer: Added character for " + playerName + " (not applied yet)");
                    }
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