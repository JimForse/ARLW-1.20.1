package rw.modden;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
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

        // Обработка тиков клиента
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
            if (networkHandler == null || client.player == null) return;

            GameOptions options = client.options;
            CombatState state = ClientNetworking.getCombatState();
            int dashCoolDown = ClientNetworking.getDashCoolDown(client.player.getEntityName());

            // Обработка боевых клавиш
            if (state != CombatState.NONE) {
                blockVanillaBinds(client, state);

                // Закрываем инвентарь
//                if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen &&
//                        client.player.currentScreenHandler instanceof net.minecraft.screen.PlayerScreenHandler) {
//                    client.setScreen(null);
//                }

                if (state == CombatState.NORMAL) {
                    if (options.jumpKey.isPressed()||switchCharacterKey.isPressed()) {
                        options.jumpKey.setPressed(false);
                        KeyBinding.setKeyPressed(options.jumpKey.getDefaultKey(), false);
                        networkHandler.sendPacket(new CustomPayloadC2SPacket(
                                new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                                new PacketByteBuf(Unpooled.buffer()).writeString("switch_character")));
                        // TODO: смена персонажей
                    } else if (options.sneakKey.isPressed()||dashKey.isPressed()) {

                    }
                }
            }
        });
    }

    private void blockVanillaBinds(MinecraftClient client, CombatState state) {
        GameOptions options = client.options;
        if (state == CombatState.NORMAL || state == CombatState.EVENT) {
            if (options.inventoryKey.isPressed()) {
                options.inventoryKey.setPressed(false);
                KeyBinding.setKeyPressed(options.inventoryKey.getDefaultKey(), false);
            }

            if (options.dropKey.isPressed()) {
                options.dropKey.setPressed(false);
                KeyBinding.setKeyPressed(options.dropKey.getDefaultKey(), false);
            }
        }

        if (state == CombatState.NORMAL) {
            if (options.jumpKey.isPressed()) {
                options.jumpKey.setPressed(false);
                KeyBinding.setKeyPressed(options.jumpKey.getDefaultKey(), false);
            }
        }
    }

    private void handleCombatKeys(ClientPlayNetworkHandler networkHandler) {
        CombatState state = ClientNetworking.getCombatState();

        if (state != CombatState.NONE) {
            if (attackKey.isPressed()) {
                networkHandler.sendPacket(new CustomPayloadC2SPacket(
                        new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                        new PacketByteBuf(Unpooled.buffer()).writeString("attack")));
            }
            if (ultKey.isPressed()) {
                networkHandler.sendPacket(new CustomPayloadC2SPacket(
                        new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                        new PacketByteBuf(Unpooled.buffer()).writeString("ult")));
            }
            if (eKey.isPressed()) {
                networkHandler.sendPacket(new CustomPayloadC2SPacket(
                        new Identifier(Axorunelostworlds.MOD_ID, "player_action"),
                        new PacketByteBuf(Unpooled.buffer()).writeString("e")));
            }
        }
    }

    private void dashRealize(MinecraftClient client) {

    }
}