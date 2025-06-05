package rw.modden;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import rw.modden.combat.CombatState;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class KeyInput implements ClientModInitializer {
    private static final KeyBinding attackKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.axorunelostworlds.attack", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_R, "category.axorunelostworlds.combat"));
    private static final KeyBinding dashKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.axorunelostworlds.dash", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_LEFT_SHIFT, "category.axorunelostworlds.combat"));
    private static final KeyBinding ultKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.axorunelostworlds.ult", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_Q, "category.axorunelostworlds.combat"));
    private static final KeyBinding eKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.axorunelostworlds.e", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_E, "category.axorunelostworlds.combat"));
    private static final KeyBinding switchCharacterKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.axorunelostworlds.switch_character", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_SPACE, "category.axorunelostworlds.combat"));
    private static final KeyBinding toggleBattleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.axorunelostworlds.toggle_battle", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_B, "category.axorunelostworlds.combat"));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
            if (networkHandler == null) return;

            if (ClientNetworking.getCombatState() != CombatState.NONE) {
                if (attackKey.wasPressed()) {
                    networkHandler.sendPacket(new CustomPayloadC2SPacket(
                            new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                            new PacketByteBuf(Unpooled.buffer()).writeString("attack")));
                }
                if (dashKey.wasPressed()) {
                    networkHandler.sendPacket(new CustomPayloadC2SPacket(
                            new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                            new PacketByteBuf(Unpooled.buffer()).writeString("dash")));
                }
                if (ultKey.wasPressed()) {
                    networkHandler.sendPacket(new CustomPayloadC2SPacket(
                            new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                            new PacketByteBuf(Unpooled.buffer()).writeString("ult")));
                }
                if (eKey.wasPressed()) {
                    networkHandler.sendPacket(new CustomPayloadC2SPacket(
                            new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                            new PacketByteBuf(Unpooled.buffer()).writeString("e")));
                }
            }

            if (ClientNetworking.getCombatState() == CombatState.NORMAL && switchCharacterKey.wasPressed()) {
                networkHandler.sendPacket(new CustomPayloadC2SPacket(
                        new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                        new PacketByteBuf(Unpooled.buffer()).writeString("switch_character")));
            }

            if (toggleBattleKey.wasPressed()) {
                networkHandler.sendPacket(new CustomPayloadC2SPacket(
                        new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                        new PacketByteBuf(Unpooled.buffer()).writeString("toggle_battle")));
            }
        });
    }
}