package rw.modden.combat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import rw.modden.Axorunelostworlds;


public class CombatMechanics implements ClientModInitializer {
    private static boolean battleStatus = false;
    private static KeyBinding battleKey;

    private static final StunMechanic stunMechanic = new StunMechanic();

    public static void initialize() {
        stunMechanic.initialize();
    }

    @Override
    public void onInitializeClient() {
        battleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key."+Axorunelostworlds.MOD_ID+".toggle_battle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category."+Axorunelostworlds.MOD_ID+".combat"
        ));

        // Обработчик тиков для проверки нажатия клавиши
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (battleKey.wasPressed() && client.player != null) {
                battleStatus = !battleStatus;
                client.player.sendMessage(
                        Text.literal("Battle Mode "+(battleStatus?"Enable":"Disabled")),
                        true
                );
            }
        });
    }

    public static boolean isBattleActive() {
        // TODO: сделать проверку боя в зонах или ивентах
        return battleStatus;
    }
}
