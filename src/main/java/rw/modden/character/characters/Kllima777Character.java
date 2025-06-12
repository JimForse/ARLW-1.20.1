package rw.modden.character.characters;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.weapon.TrainingBaton;
import rw.modden.weapon.Weapon;
import rw.modden.character.Character;

public class Kllima777Character extends Character {
    public Kllima777Character() {
        super(CharacterType.SUPPORT, 0, new String[]{"speed_boost", "critical_hit"}, "kllima777");
        this.modelId = new Identifier("minecraft", "entity/player/wide");
        // TODO: Доработать бафы
        // TODO: Добавить пассивные способности
        // TODO: Уточнить статы
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
            System.out.println("Kllima777Character: Critical hit buff not implemented");
        }
        System.out.println("Kllima777Character: Buffs applied to player: " + player.getGameProfile().getName());
    }

    @Override
    public Weapon getStartingWeapon() {
        return new TrainingBaton();
    }
}