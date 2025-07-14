package rw.modden.combat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rw.modden.character.PlayerData;

public class BattleKeys {
    PlayerData data;
    ServerPlayerEntity player;

    public BattleKeys(PlayerData data, ServerPlayerEntity player) {
        this.data = data;
        this.player = player;
    }

    public void dashKey() {
        if (data.getCombatState() == CombatState.NORMAL || data.getCombatState() == CombatState.EVENT) {
            // Сохраняем текущий импульс
            double currentMotionX = player.getVelocity().getX();
            double currentMotionZ = player.getVelocity().getZ();
            double motionY = player.isOnGround() ? 0.0 : player.getVelocity().getY(); // Сохраняем высоту
            float yaw = player.getYaw();
            // Отталкивание на ~2.5 блока (сила 0.5 для скорости)
            double dashStrength = 0.5;
            double motionX = currentMotionX + (-Math.sin(Math.toRadians(yaw)) * dashStrength);
            double motionZ = currentMotionZ + (Math.cos(Math.toRadians(yaw)) * dashStrength);
            player.setVelocity(motionX, motionY, motionZ);
            player.velocityModified = true;
            data.setDashCoolDown(60);
            data.syncDashCoolDown(player);
            player.sendMessage(Text.literal("Dash активирован, кулдаун: 60 тиков"), true);
        }
    }
    public void ultKey() {}
}
