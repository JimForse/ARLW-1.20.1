package rw.modden;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import rw.modden.mixin.SkinMixin;

public class KeyInput implements ClientModInitializer {
    private static KeyBinding attackKey;
    private static KeyBinding dashKey;
    private static KeyBinding ultKey;
    private static KeyBinding eKey;
    private static KeyBinding switchCharacterKey;
    private static final boolean[] combatState = new boolean[2]; // [0] = combatMode, [1] = eventCombatMode

    @Override
    public void onInitializeClient() {
        String category = "key.categories.axorune_lostworlds";

        attackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.axorunelostworlds.attack",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                category
        ));
        dashKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.axorunelostworlds.dash",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_SHIFT,
                category
        ));
        ultKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.axorunelostworlds.ult",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Q,
                category
        ));
        eKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.axorunelostworlds.e",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                category
        ));
        switchCharacterKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.axorunelostworlds.switch_character",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SPACE,
                category
        ));

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"), (client, handler, buf, responseSender) -> {
            Identifier skinId = buf.readIdentifier();
            SkinMixin.setCustomSkin(skinId);
            System.out.println("KeyInput: Received sync_skin packet, applied skin: " + skinId);
        });

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"), (client, handler, buf, responseSender) -> {
            combatState[0] = buf.readBoolean();
            combatState[1] = buf.readBoolean();
            System.out.println("KeyInput: Received sync_combat_mode packet, combatMode: " + combatState[0] + ", eventCombatMode: " + combatState[1]);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean[] combatState = new boolean[2]; // inCombat, isEventCombat
            ClientPlayNetworking.registerGlobalReceiver(new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"), (client_, handler, buf, responseSender) -> {
                Identifier skinId = buf.readIdentifier();
                SkinMixin.setCustomSkin(skinId);
                System.out.println("KeyInput: Received sync_skin packet, applied skin: " + skinId);
            });

            if (combatState[0]) {
                if (attackKey.wasPressed()) {
                    sendAction("attack");
                }
                if (dashKey.wasPressed()) {
                    sendAction("dash");
                }
                if (ultKey.wasPressed()) {
                    sendAction("ult");
                }
                if (eKey.wasPressed()) {
                    sendAction("e");
                }
            }

            if (combatState[0] && !combatState[1] && switchCharacterKey.wasPressed()) {
                // TODO: Добавить проверку очков помощи
                sendAction("switch_character");
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"), (client, handler, buf, responseSender) -> {
            Identifier skinId = buf.readIdentifier();
            SkinMixin.setCustomSkin(skinId);
        });
    }

    private void sendAction(String action) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(action);
        ClientPlayNetworking.send(new Identifier(Axorunelostworlds.MOD_ID, "player_action"), buf);
    }
}