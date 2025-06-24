package rw.modden.combat;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import rw.modden.Axorunelostworlds;
import net.minecraft.util.Identifier;
import rw.modden.character.PlayerData;


public class CombatMechanics {
    private static boolean battleStatus = false;

    private final StunMechanic stunMechanic = new StunMechanic();

    public void initialize() {
        stunMechanic.initialize();
    }

    public static void setBattleStatus(CombatState state, ServerPlayerEntity player) {
        PlayerData data = PlayerData.getOrCreate(player);
        data.setCombatMode(state, player);
        // TODO: Добавить кнопку "Вступить в бой" в кастомное меню для активации CombatState.NORMAL
    }

    public static boolean isBattleActive() {
        // TODO: сделать проверку боя в зонах или ивентах
        return battleStatus;
    }

    private void sendAction(String action) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(action);
        ClientPlayNetworking.send(new Identifier(Axorunelostworlds.MOD_ID, "player_action"), buf);
    }
}
