package rw.modden.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import rw.modden.client.ClientNetworking;
import rw.modden.client.ConcreteDummyGeoPlayerEntity;
import rw.modden.client.CustomPlayerModel;
import rw.modden.client.DummyGeoPlayerEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static rw.modden.Axorunelostworlds.LOGGER;

public class CustomPlayerRender extends GeoEntityRenderer<DummyGeoPlayerEntity> {
    public CustomPlayerRender(EntityRendererFactory.Context context, String modId) {
        super(context, new CustomPlayerModel(modId));
        LOGGER.info("CustomPlayerRender: Инициализирован для modId={}", modId);
    }

    @Override
    public void render(DummyGeoPlayerEntity entity, float entityYaw, float partialTicks, MatrixStack stack, VertexConsumerProvider buffer, int packedLight) {
        if (entity instanceof ConcreteDummyGeoPlayerEntity concrete) {
            super.render(concrete, entityYaw, partialTicks, stack, buffer, packedLight);
        } else {
            LOGGER.warn("Expected ConcreteDummyGeoPlayerEntity, got {}", entity.getClass().getSimpleName());
        }
    }

    @Override
    protected void renderLabelIfPresent(DummyGeoPlayerEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (entity instanceof ConcreteDummyGeoPlayerEntity concrete) {
            PlayerEntity player = concrete.getPlayer();
            String customName = ClientNetworking.getCustomName(player.getGameProfile().getName());
            super.renderLabelIfPresent(entity, Text.literal(customName), matrices, vertexConsumers, light);
        } else {
            LOGGER.warn("Expected ConcreteDummyGeoPlayerEntity, got {}", entity.getClass().getSimpleName());
        }
    }
}