package rw.modden.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import rw.modden.Axorunelostworlds;
import rw.modden.KeyInput;
import rw.modden.combat.CombatMechanics;
import rw.modden.combat.StuntMechanicClient;

@Environment(EnvType.CLIENT)
public class ClientClassesInitializer {
    public static void initialize(){
        new StuntMechanicClient().initialize();
        new ClientNetworking().initialize();
        new KeyInput().initialize();
        new CombatMechanics().initialize();

        // Регистрация рендерера для PlayerEntity
        EntityRendererRegistry.register(() -> EntityType.PLAYER, CustomPlayerRenderer.createAdapter(Axorunelostworlds.MOD_ID));
    }
}
