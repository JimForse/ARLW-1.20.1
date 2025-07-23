package rw.modden.character;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.characters.*;

public class CharacterSwitcher {
    public boolean switchCharacter(String characterName, ServerPlayerEntity player) {
        Character character = null;
        String modelPath = null;
        String animationPath = null;
        String texturePath = null;

        switch (characterName.toLowerCase()) {
            case "kllima777":
                character = new Kllima777Character();
                modelPath = "kllima777/model.geo.json";
                animationPath = "kllima777/anim.json";
                texturePath = "kllima777/kllima777.png";
                break;
            case "firrice":
                character = new FIRrICECharacter();
                modelPath = "firrice/model.geo.json";
                animationPath = "firrice/anim.json";
                texturePath = "firrice/firrice.png";
                break;
            case "stalker_anomaly":
                character = new Stalker_AnomalyCharacter();
                modelPath = "stalker_anomaly/model.geo.json";
                animationPath = "stalker_anomaly/anim.json";
                texturePath = "stalker_anomaly/stalker_anomaly.png";
                break;
            case "spectorprofm":
                character = new SpectorprofmCharacter();
                modelPath = "spectorprofm/model.geo.json";
                animationPath = "spectorprofm/anim.json";
                texturePath = "spectorprofm/spectorprofm.png";
                break;
            default:
                return false;
        }

        PlayerData data = PlayerData.getOrCreate(player);
        data.setActiveCharacter(character, player);
        data.setModel(modelPath, texturePath, animationPath, player);
        return true;
    }
}