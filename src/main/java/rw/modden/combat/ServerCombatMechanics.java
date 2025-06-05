package rw.modden.combat;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.character.PlayerData;

public class ServerCombatMechanics {
    public static void initialize() {
        ServerPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                (server, player, handler, buf, responseSender) -> {
                    String action = buf.readString();
                    PlayerData data = PlayerData.getOrCreate(player);

                    switch (action) {
                        case "attack":
                            // TODO: Реализовать атаку
                            System.out.println("Player " + player.getName().getString() + " performed attack");
                            break;
                        case "dash":
                            // TODO: Реализовать дэш
                            break;
                        case "ult":
                            // TODO: Реализовать ульту
                            break;
                        case "e":
                            // TODO: Реализовать E-способность
                            break;
                        case "switch_character":
                            if (data.isCombatMode() && !data.isEventCombatMode()) {
                                int currentIndex = data.getCombatCharacters().indexOf(data.getActiveCharacter());
                                int nextIndex = (currentIndex + 1) % data.getCombatCharacters().size();
                                data.switchCharacter(nextIndex, player);
                            }
                            break;
                        case "toggle_battle":
                            // TODO: Добавить логику для ивентового боя (CombatState.EVENT)
                            CombatState newState = data.isCombatMode() ? CombatState.NONE : CombatState.NORMAL;
                            data.setCombatMode(newState, player);
                            System.out.println("ServerCombatMechanics: Toggled battle mode for " + player.getName().getString() + " to " + newState);
                            break;
                    }
                }
        );
    }
}