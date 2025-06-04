package rw.modden.combat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import rw.modden.Axorunelostworlds;
import net.minecraft.util.Identifier;
import rw.modden.character.PlayerData;


public class CombatMechanics implements ClientModInitializer {
    private static boolean battleStatus = false;

    private static final StunMechanic stunMechanic = new StunMechanic();

    public static void initialize() {
        stunMechanic.initialize();
    }

    @Override
    public void onInitializeClient() {
    }

    public static void setBattleStatus(boolean status, ServerPlayerEntity player) {
        PlayerData data = PlayerData.getOrCreate(player);
        data.setCombatMode(status, false, player);
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
