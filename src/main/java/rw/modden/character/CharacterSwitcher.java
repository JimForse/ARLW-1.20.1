package rw.modden.character;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.characters.*;

public class CharacterSwitcher {
    public boolean switchCharacter(String characterName, ServerPlayerEntity player) {
        Character character = null;
        String modelPath = null;
        String animationPath = null;

        switch (characterName.toLowerCase()) {
            case "kllima777":
                character = new Kllima777Character();
                modelPath = "models/kllima777/model.geo.json";
                animationPath = "models/kllima777/animation.json";
                break;
            case "firrice":
                character = new FIRrICECharacter();
                modelPath = "models/firrice/model.geo.json";
                animationPath = "models/firrice/animation.json";
                break;
            case "stalker_anomaly":
                character = new Stalker_AnomalyCharacter();
                modelPath = "models/stalker_anomaly/model.geo.json";
                animationPath = "models/stalker_anomaly/animation.json";
                break;
            case "spectorprofm":
                character = new SpectorprofmCharacter();
                modelPath = "models/spectorprofm/model.geo.json";
                animationPath = "models/spectorprofm/animation.json";
                break;
            default:
                return false;
        }

        PlayerData data = PlayerData.getOrCreate(player);
        data.setActiveCharacter(character, player);
        data.setModel(modelPath, animationPath, player);
        return true;
    }
}