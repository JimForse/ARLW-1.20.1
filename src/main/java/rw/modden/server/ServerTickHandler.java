package rw.modden.server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.character.PlayerData;

public class ServerTickHandler implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerData data = PlayerData.getOrCreate(player);
            if (data.getDashCoolDown() > 0) {
                data.setDashCoolDown(data.getDashCoolDown() - 1);
                System.out.println("Player " + player.getName().getString() + " dashCoolDown: " + data.getDashCoolDown());
                data.syncDashCoolDown(player);
                System.out.println("Sent dashCoolDown: " + data.getDashCoolDown());
            }
        }
    }
}