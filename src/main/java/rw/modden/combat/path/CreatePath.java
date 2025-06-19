package rw.modden.combat.path;

import rw.modden.character.PlayerData;

public class CreatePath extends Path {
    public CreatePath(PlayerData playerData) {
        super(playerData, PathType.CREATE);
    }
}
