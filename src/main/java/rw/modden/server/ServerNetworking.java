package rw.modden.server;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.character.PlayerData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import rw.modden.combat.CombatState;

/*
    Здесь начинается пляска с бубном...
    ⚠ Рекомендуем сюда нос не совать ⚠
*/

public class ServerNetworking implements ModInitializer {
    public static final Identifier PLAYER_ACTION_PACKET_ID = new Identifier("axorunelostworlds", "player_action");

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(PLAYER_ACTION_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            // 1. Прочитать действие
            String action = buf.readString();
            System.out.println("Received action: " + action + " from " + player.getEntityName());

            // 2. Проверить действие
            if (action.equals("dash")) {
                // 3. Обновить PlayerData
                server.execute(() -> {
                    PlayerData data = PlayerData.getOrCreate(player);
                    data.setDashStatus(false);
                    data.setDashCoolDown(60);

                    // 4. Синхронизировать cooldown с клиентом
                    data.syncDashCoolDown(player);

                    System.out.println("Updated dashCoolDown for player: " + player.getEntityName());
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                (server, player, handler, buf, responseSender) -> {
                    String action = buf.readString();
                    PlayerData data = PlayerData.getOrCreate(player);
                    if (action.equals("drop") && data.getCombatState() != CombatState.NONE) {
                        return; // Блокируем выброс
                    }
                    // Обработка других действий
                }
        );
    }
}
