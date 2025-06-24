package rw.modden.client.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import rw.modden.Axorunelostworlds;
import rw.modden.client.ClientNetworking;

public class CustomModelRenderer extends PlayerEntityRenderer {
    public CustomModelRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, slim);
    }

    @Override
    public void render(AbstractClientPlayerEntity player, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Identifier modelId = ClientNetworking.getPlayerModel(player.getUuid());
        Axorunelostworlds.LOGGER.info("Rendering player {}, modelId: {}", player.getGameProfile().getName(), modelId);

        if (modelId != null && !modelId.getNamespace().equals("minecraft")) {
            Avatar avatar = AvatarManager.getAvatar(player);
            if (avatar != null) {
                Axorunelostworlds.LOGGER.info("Аватар Figura найден для игрока {}, пропускаем стандартный рендеринг", player.getGameProfile().getName());
                return; // Figura сама рендерит аватар
            } else {
                Axorunelostworlds.LOGGER.warn("Аватар Figura не найден для игрока {}, modelId: {}, используем стандартный рендеринг", player.getGameProfile().getName(), modelId);
            }
        } else {
            Axorunelostworlds.LOGGER.info("Нет кастомной модели для игрока {}, modelId: {}, используем стандартный рендеринг", player.getGameProfile().getName(), modelId);
        }

        super.render(player, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}