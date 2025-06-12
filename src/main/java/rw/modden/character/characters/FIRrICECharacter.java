package rw.modden.character.characters;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.Character;
import rw.modden.weapon.TrainingBaton;
import rw.modden.weapon.Weapon;

public class FIRrICECharacter extends Character {
    public FIRrICECharacter() {
        super(CharacterType.ASSASSIN, 5, new String[] {"speed_boost", "critical_hit"}, "firrice");
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
            System.out.println("FIRrICECharacter: Critical hit buff not implemented");
        }
        System.out.println("FIRrICECharacter: Buffs applied to player: " + player.getGameProfile().getName());
    }

    @Override
    public Weapon getStartingWeapon() {
        return new TrainingBaton();
    }
}