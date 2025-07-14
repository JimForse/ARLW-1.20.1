package rw.modden.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.PlayerData;

public class ServerTickHandler {
    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerData data = PlayerData.getOrCreate(player);
                if (data.getDashCoolDown() > 0) {
                    data.setDashCoolDown(data.getDashCoolDown() - 1);
                    data.syncDashCoolDown(player);
                }
            }
        });
    }
}