package rw.modden.character;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.weapon.Weapon;

public abstract class Character {
    public enum CharacterType {
        SUPPORT(100, 50, 0.8f),
        TANK(200, 80, 1.0f),
        ASSASSIN(80, 120, 1.2f);

        public final int maxHealth;
        public final int baseDamage;
        public final float stunModifier;

        CharacterType(int maxHealth, int baseDamage, float stunModifier) {
            this.maxHealth = maxHealth;
            this.baseDamage = baseDamage;
            this.stunModifier = stunModifier;
        }
    }

    protected final CharacterType type;
    protected int starLevel;
    protected float health;
    protected int damage;
    protected float stunModifier;
    protected final String[] buffs;

    public Character(CharacterType type, int starLevel, String[] buffs) {
        this.type = type;
        this.starLevel = clamp(starLevel, 0, 5);
        this.health = type.maxHealth * (1 + starLevel * 0.2f);
        this.damage = (int) (type.baseDamage * (1 + starLevel * 0.2f));
        this.stunModifier = type.stunModifier * (1 + starLevel * 0.1f);
        this.buffs = buffs != null ? buffs : new String[0];
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public void applyToPlayer(ServerPlayerEntity player) {
        // Применение здоровья
        EntityAttributeInstance healthAttr = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.addPersistentModifier(new EntityAttributeModifier(
                    "character_health",
                    this.health - 20.0,
                    EntityAttributeModifier.Operation.ADDITION
            ));
        }
        player.setHealth(this.health);

        // Применение урона
        EntityAttributeInstance damageAttr = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.addPersistentModifier(new EntityAttributeModifier(
                    "character_damage",
                    this.damage / 10.0 - 1.0,
                    EntityAttributeModifier.Operation.ADDITION
            ));
        }

        // Применение бафов
        applyBuffs(player);

        // Добавление стартового оружия
        PlayerData data = PlayerData.getOrCreate(player);
        Weapon startingWeapon = getStartingWeapon();
        if (startingWeapon != null && data.getInventory().getWeapons().isEmpty()) {
            data.getInventory().addWeapon(startingWeapon);
        }
    }

    protected abstract void applyBuffs(ServerPlayerEntity player);

    public abstract Weapon getStartingWeapon();

    public CharacterType getType() {
        return type;
    }

    public int getStarLevel() {
        return starLevel;
    }

    public float getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public float getStunModifier() {
        return stunModifier;
    }

    public String[] getBuffs() {
        return buffs;
    }

    protected boolean containsBuff(String buff) {
        for (String b : buffs) {
            if (b.equals(buff)) return true;
        }
        return false;
    }
}