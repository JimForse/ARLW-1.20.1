package rw.modden.weapon;

import rw.modden.character.Character;

public abstract class Weapon {
    public enum WeaponType {
        CLUB(1.0f, "physical"),
        SWORD(1.0f, "physical"),
        GUN(0.8f, "fire");

        public final float stunModifier;
        public final String element;

        WeaponType(float stunModifier, String element) {
            this.stunModifier = stunModifier;
            this.element = element;
        }
    }

    protected final String name;
    protected final WeaponType type;
    protected final int baseDamage;
    protected final boolean isSignature;

    public Weapon(String name, WeaponType type, int baseDamage, boolean isSignature) {
        this.name = name;
        this.type = type;
        this.baseDamage = baseDamage;
        this.isSignature = isSignature;
    }

    public int getDamage(Character character) {
        float multiplier = isSignature ? 1.2f : 1.0f;
        return (int) (baseDamage * multiplier * (character.getDamage() / 100.0f));
    }

    public float getStunModifier() {
        return type.stunModifier;
    }

    public String getElement() {
        return type.element;
    }

    public String getName() {
        return name;
    }
}