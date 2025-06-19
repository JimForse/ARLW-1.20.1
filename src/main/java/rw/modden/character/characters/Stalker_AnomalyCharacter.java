package rw.modden.character.characters;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.Character;
import rw.modden.combat.path.PathType;
import rw.modden.weapon.TrainingBaton;
import rw.modden.weapon.Weapon;

public class Stalker_AnomalyCharacter extends Character {
    public Stalker_AnomalyCharacter() {
        super(CharacterType.SUPPORT, 0, new String[]{"speed_boost","critical_hit"}, "stalker_anomaly", PathType.HUNTING);
    }

    @Override
    protected void applyBuffs(ServerPlayerEntity player) {
        if (containsBuff("speed_boost")) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    Integer.MAX_VALUE,
                    1,
                    false,
                    false
            ));
        }
        if (containsBuff("critical_hit")) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH,
                    Integer.MAX_VALUE,
                    1,
                    false,
                    false
            ));
            System.out.println("Stalker_AnomalyCharacter: Critical hit buff not implemented");
        }
        System.out.println("Stalker_AnomalyCharacter: Buffs applied to player: " + player.getGameProfile().getName());
    }

    @Override
    public Weapon getStartingWeapon() {
        return new TrainingBaton();
    }
}
