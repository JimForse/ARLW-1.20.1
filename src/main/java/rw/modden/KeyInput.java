package rw.modden;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import rw.modden.client.ClientNetworking;
import rw.modden.combat.CombatState;

/*
    УБРАЛ ОТ СЮДА СВОИ РУКИ!!
    ОНО ТЕБЯ СОЖРЁТ НАХЕР!
    Работает - не трогай!
*/

public class KeyInput {
    private KeyBinding attackKey;
    private KeyBinding dashKey;
    private KeyBinding ultKey;
    private KeyBinding eKey;
    private KeyBinding switchCharacterKey;
    private boolean wasDashKeyPressed = false;
    private boolean wasSwitchCharacterKeyPressed = false;

/*
    🕳️ Код работает по воле Бога...
    Если сломаешь — зови экзорциста.
*/

    public void initialize() {
        Axorunelostworlds.LOGGER.info("Initializing KeyInput KeyBindings");

// -----------------------------------------------------== Keys ==------------------------------------------------------

        attackKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.axorunelostworlds.attack", InputUtil.Type.MOUSE, 0, "category.axorunelostworlds.combat"));
        dashKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.axorunelostworlds.dash", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_LEFT_SHIFT, "category.axorunelostworlds.combat"));
        ultKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.axorunelostworlds.ult", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_Q, "category.axorunelostworlds.combat"));
        eKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.axorunelostworlds.e", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_E, "category.axorunelostworlds.combat"));
        switchCharacterKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.axorunelostworlds.switch_character", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_SPACE, "category.axorunelostworlds.combat"));

// ---------------------------------------------------------------------------------------------------------------------

        Axorunelostworlds.LOGGER.info("KeyBindings initialized successfully");

        // Блокировка разрушения блоков
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            CombatState state = ClientNetworking.getCombatState();
            if (state == CombatState.NORMAL || state == CombatState.EVENT) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // Перехват пакета выброса предметов
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier("minecraft", "player_action"),
                (client, handler, buf, responseSender) -> {
                    PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                            buf.readEnumConstant(PlayerActionC2SPacket.Action.class),
                            buf.readBlockPos(),
                            buf.readEnumConstant(Direction.class)
                    );
                    CombatState state = ClientNetworking.getCombatState();
                    if ((packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM ||
                            packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) &&
                            (state == CombatState.NORMAL || state == CombatState.EVENT)) {
                        // Пакет выброса предметов блокируется
                    } else {
                        handler.sendPacket(new PlayerActionC2SPacket(
                                packet.getAction(),
                                packet.getPos(),
                                packet.getDirection()
                        ));
                    }
                }
        );

        // Обработка тиков клиента
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
            if (networkHandler == null || client.player == null) return;

            CombatState state = ClientNetworking.getCombatState();
            if (state != CombatState.NONE) {
                blockVanillaBinds(client, state);

                // Закрываем только инвентарь
                if (state == CombatState.NORMAL && client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen &&
                        client.player.currentScreenHandler instanceof net.minecraft.screen.PlayerScreenHandler) {
                    client.setScreen(null);
                }
            }

            // Обработка боевых клавиш
            if (state != CombatState.NONE) {
                if (state == CombatState.NORMAL) {
                    if (dashKey.isPressed() && !wasDashKeyPressed) {
                        networkHandler.sendPacket(new CustomPayloadC2SPacket(
                                new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                                new PacketByteBuf(Unpooled.buffer()).writeString("dash")));
                        wasDashKeyPressed = true;
                    } else if (!dashKey.isPressed()) {
                        wasDashKeyPressed = false;
                    }
                    if (switchCharacterKey.isPressed() && !wasSwitchCharacterKeyPressed) {
                        networkHandler.sendPacket(new CustomPayloadC2SPacket(
                                new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                                new PacketByteBuf(Unpooled.buffer()).writeString("switch_character")));
                        wasSwitchCharacterKeyPressed = true;
                    } else if (!switchCharacterKey.isPressed()) {
                        wasSwitchCharacterKeyPressed = false;
                    } else {
                        handleCombatKeys(networkHandler);
                    }
                } else if (state == CombatState.EVENT) {
                    if (dashKey.isPressed() && !wasDashKeyPressed && ClientNetworking.getDashCoolDown(client.player.getUuid()) <= 0) {
                        networkHandler.sendPacket(new CustomPayloadC2SPacket(
                                new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                                new PacketByteBuf(Unpooled.buffer()).writeString("dash")));
                        wasDashKeyPressed = true;
                    } else if (!dashKey.isPressed()) {
                        wasDashKeyPressed = false;
                    } else {
                        handleCombatKeys(networkHandler);
                    }
                }
            }
        });
    }

    private void blockVanillaBinds(MinecraftClient client, CombatState state) {
        GameOptions options = client.options;
        // Блокировка инвентаря и выброса предметов
        if (options.inventoryKey.isPressed()) {
            options.inventoryKey.setPressed(false);
            KeyBinding.setKeyPressed(options.inventoryKey.getDefaultKey(), false);
        }
        if (options.dropKey.isPressed()) {
            options.dropKey.setPressed(false);
            KeyBinding.setKeyPressed(options.dropKey.getDefaultKey(), false);
        }
        // Блокировка прыжка и приседания только в NORMAL
        if (state == CombatState.NORMAL) {
            if (options.jumpKey.isPressed()) {
                options.jumpKey.setPressed(false);
                KeyBinding.setKeyPressed(options.jumpKey.getDefaultKey(), false);
            }
            if (options.sneakKey.isPressed()) {
                options.sneakKey.setPressed(false);
                KeyBinding.setKeyPressed(options.sneakKey.getDefaultKey(), false);
            }
        }
    }

    private void handleCombatKeys(ClientPlayNetworkHandler networkHandler) {
        if (attackKey.isPressed()) {
            networkHandler.sendPacket(new CustomPayloadC2SPacket(
                    new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                    new PacketByteBuf(Unpooled.buffer()).writeString("attack")));
        } else if (ultKey.isPressed()) {
            networkHandler.sendPacket(new CustomPayloadC2SPacket(
                    new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                    new PacketByteBuf(Unpooled.buffer()).writeString("ult")));
        } else if (eKey.isPressed()) {
            networkHandler.sendPacket(new CustomPayloadC2SPacket(
                    new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                    new PacketByteBuf(Unpooled.buffer()).writeString("e")));
        }
    }
}