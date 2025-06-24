package rw.modden.character;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.characters.*;

public class CharacterSwitcher {
    public boolean switchCharacter(String characterName, ServerPlayerEntity player) {
        Character character = null;
        String modelPath = null;

        switch (characterName) {
            case "kllima777":
                character = new Kllima777Character();
                modelPath = "axorunelostworlds/models/kllima777/model.bbmodel";
                break;
            case "firrice":
                character = new FIRrICECharacter();
                modelPath = "axorunelostworlds/models/firrice/model.bbmodel";
                break;
            case "stalker_anomaly":
                character = new Stalker_AnomalyCharacter();
                modelPath = "axorunelostworlds/models/stalker_anomaly/model.bbmodel";
                break;
            case "spectorprofm":
                character = new SpectorprofmCharacter();
                modelPath = "axorunelostworlds/models/spectorprofm/model.bbmodel";
                break;
            default:
                return false;
        }

        PlayerData data = PlayerData.getOrCreate(player);
        data.setActiveCharacter(character, player);
        data.setModel(modelPath, player);
        return true;
    }
}
