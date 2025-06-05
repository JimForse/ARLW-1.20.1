package rw.modden;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import rw.modden.combat.CombatState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientNetworking implements ClientModInitializer {
    private static final Map<UUID, Identifier> playerSkins = new HashMap<>();
    private static CombatState combatState = CombatState.NONE;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"),
                (client, handler, buf, responseSender) -> {
                    Identifier skinId = buf.readIdentifier();
                    System.out.println("ClientNetworking: Received sync_skin packet for skin: " + skinId);
                    if (client.player != null) {
                        playerSkins.put(client.player.getUuid(), skinId);
                    }
                });

        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"),
                (client, handler, buf, responseSender) -> {
                    int stateOrdinal = buf.readInt();
                    combatState = CombatState.values()[stateOrdinal];
                    System.out.println("ClientNetworking: Updated combatState to: " + combatState);
                });
    }

    public static Identifier getCustomSkin(UUID playerUuid) {
        return playerSkins.getOrDefault(playerUuid, new Identifier("minecraft", "textures/entity/player/steve.png"));
    }

    public static CombatState getCombatState() {
        return combatState;
    }
}
