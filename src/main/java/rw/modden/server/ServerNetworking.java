package rw.modden.server;

import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.character.PlayerData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import rw.modden.combat.BattleKeys;
import rw.modden.combat.CombatState;

/*
    Здесь начинается пляска с бубном...
    ⚠ Рекомендуем сюда нос не совать ⚠
*/

public class ServerNetworking {
    public static final Identifier PLAYER_ACTION_PACKET_ID = new Identifier(Axorunelostworlds.MOD_ID, "player_action");

    public static void initialize() {
        ServerPlayNetworking.registerGlobalReceiver(
                PLAYER_ACTION_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    String action = buf.readString();
                    PlayerData data = PlayerData.getOrCreate(player);
                    BattleKeys keys = new BattleKeys(data,player);
                    server.execute(() -> {
                        switch (action) {
                            case "drop_item","drop_all_items":
                                if (data.getCombatState() != CombatState.NONE) return;
                                break;
                            case "switch_character":
                            case "ult":
                            case "e":
                            case "attack":
                            case "dash":
                                keys.dashKey();
                                break;
                            default:
                                Axorunelostworlds.LOGGER.info("Этого функционала или нет, или не реализован");
                        }
                    });
                });
    }
}