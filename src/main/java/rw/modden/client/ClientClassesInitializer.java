package rw.modden.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import rw.modden.KeyInput;
import rw.modden.client.render.ModClientRender;
import rw.modden.combat.CombatMechanics;
import rw.modden.combat.StuntMechanicClient;

@Environment(EnvType.CLIENT)
public class ClientClassesInitializer {
    public static void initialize(){
        new StuntMechanicClient().initialize();
        new ClientNetworking().initialize();
        new KeyInput().initialize();
        new CombatMechanics().initialize();
        new ModClientRender().initialize();
    }
}
