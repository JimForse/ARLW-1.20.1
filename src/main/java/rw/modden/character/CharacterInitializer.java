package rw.modden.character;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

import rw.modden.character.characters.*;
import rw.modden.combat.CombatState;

public class CharacterInitializer {
    private static final List<CharacterEntry> CHARACTERS = new ArrayList<>();

    public static void initialize() {
        System.out.println("CharacterInitializer: Initializing characters...");

        registerCharacter(
                "kllima777",
                new Kllima777Character()
        );
        registerCharacter(
                "firrice",
                new FIRrICECharacter()
        );
        registerCharacter(
                "stalker_anomaly",
                new Stalker_AnomalyCharacter()
        );
        registerCharacter(
                "spectorprofm",
                new SpectorprofmCharacter()
        );

        // TODO: Добавить других персонажей

        // Обработчик входа игрока
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getGameProfile().getName().toLowerCase();
            System.out.println("CharacterInitializer: Player joined: " + playerName);

            // Загрузка сохранённых данных или создание нового персонажа
            for (CharacterEntry entry : CHARACTERS) {
                if (entry.playerName.equals(playerName)) {
                    PlayerData playerData = PlayerData.getOrCreate(player);
                    if (!playerData.hasCharacter(playerName)) {
                        playerData.getInventory().addCharacter(entry.character);
                        playerData.setModel("axorunelostworlds:models/" + playerName + "/model.bbmodel", player);
                        playerData.markDirty();
//                        playerData.setCombatMode(CombatState.NONE, player);
                        System.out.println("CharacterInitializer: Added character to inventory for " + playerName + " (not applied)");
                    }
                    return;
                }
            }
            System.out.println("CharacterInitializer: No character found for " + playerName);
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