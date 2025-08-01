package rw.modden.character;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.path.PathType;
import rw.modden.weapon.Weapon;

import java.util.UUID;

public abstract class Character {

/*
    Абстрактный класс для всех персонажей
    Часть реализации делегируется в него
*/

// ----------------------------------------== Private ==----------------------------------------------------------------

    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

// ----------------------------------------== Protected ==--------------------------------------------------------------

    protected final CharacterType type;
    protected int starLevel;
    protected float health;
    protected int damage;
    protected float stunModifier;
    protected final String[] buffs;
    protected final String characterName;
    protected String modelPath;
    protected final String texturePath;
    protected String animationPath;
    protected final PathType pathType;

// ---------------------------------------------------------------------------------------------------------------------

    public Character(CharacterType type, int starLevel, String[] buffs, String characterName, PathType pathType) {
        this.type = type;
        this.starLevel = clamp(starLevel, 0, 5);
        this.health = type.maxHealth * (1 + starLevel * 0.2f);
        this.damage = (int) (type.baseDamage * (1 + starLevel * 0.2f));
        this.stunModifier = type.stunModifier * (1 + starLevel * 0.1f);
        this.buffs = buffs != null ? buffs : new String[0];
        this.characterName = characterName;
        this.modelPath = String.format("%s/%s.geo.json",characterName.toLowerCase(),characterName.toLowerCase());
        this.animationPath = String.format("%s/%s.anim.json",characterName.toLowerCase(),characterName.toLowerCase());
        this.texturePath = String.format("%s/%s.png",characterName.toLowerCase(),characterName.toLowerCase());
        this.pathType = pathType;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    protected boolean containsBuff(String buff) {
        for (String b: buffs) {
            if (b.equals(buff)) return true;
        }
        return false;
    }

// ----------------------------------------== Apply`s / Setters ==------------------------------------------------------

    public void applyToPlayer(ServerPlayerEntity player) {
        PlayerData data = PlayerData.getOrCreate(player);
        if (!data.isCombatMode()) {
            System.out.println("Character: Skipped applying attributes for " + player.getGameProfile().getName() + " (not in combat)");
            return;
        }

        EntityAttributeInstance healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_UUID);
            healthAttr.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_MODIFIER_UUID,
                    "character_health",
                    this.health - 20.0,
                    EntityAttributeModifier.Operation.ADDITION
            ));
        }
        player.setHealth(player.getMaxHealth());

        EntityAttributeInstance damageAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(DAMAGE_MODIFIER_UUID);
            damageAttr.addPersistentModifier(new EntityAttributeModifier(
                    DAMAGE_MODIFIER_UUID,
                    "character_damage",
                    this.damage / 10.0 - 1.0,
                    EntityAttributeModifier.Operation.ADDITION
            ));
        }

        applyBuffs(player);
        System.out.println("Character: Applied attributes for " + player.getGameProfile().getName() + ", health: " + this.health + ", damage: " + this.damage);
    }
    protected abstract void applyBuffs(ServerPlayerEntity player);
    public void applyPathDefaults(PlayerData playerData) { // Задаёт начальные параметры пути
    }

// ----------------------------------------== Getters ==----------------------------------------------------------------
    public PathType getPreferredPath() {
        return this.pathType;
    }
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
    public String getModelPath() {
        return modelPath;
    }
    public String getAnimationPath() {
        return animationPath;
    }
    public String getTexturePath() {
        return texturePath;
    }
    public String getCharacterName() {
        return characterName;
    }

// ---------------------------------------------------------------------------------------------------------------------

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
}