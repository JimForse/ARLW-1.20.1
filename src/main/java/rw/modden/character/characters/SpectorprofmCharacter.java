package rw.modden.character.characters;

import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.Character;
import rw.modden.combat.path.PathType;
import rw.modden.weapon.Weapon;

public class SpectorprofmCharacter extends Character {
    public SpectorprofmCharacter() {
        super(CharacterType.TANK, 0, null, "spectorprofm", PathType.CHAOS);
    }

    @Override
    protected void applyBuffs(ServerPlayerEntity player) {

    }

    @Override
    public Weapon getStartingWeapon() {
        return null;
    }
}
