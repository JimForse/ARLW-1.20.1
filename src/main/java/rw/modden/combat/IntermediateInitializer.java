package rw.modden.combat;

import rw.modden.character.CharacterInitializer;

public class IntermediateInitializer {
    public static void initialize() {
        System.out.println("IntermediateInitializer: Initializing mechanics...");
        CharacterInitializer.initialize();
        // TODO: Добавить StunMechanic
    }
}