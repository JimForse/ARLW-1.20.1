package rw.modden.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rw.modden.client.ConcreteDummyGeoPlayerEntity;
import rw.modden.client.CustomPlayerModel;

import static rw.modden.Axorunelostworlds.LOGGER;

public class PlayerRendererAdapter extends EntityRenderer<PlayerEntity> {
    private final CustomPlayerRender geoRenderer;
    private final CustomPlayerModel model;

    public PlayerRendererAdapter(EntityRendererFactory.Context context, String modId) {
        super(context);
        this.geoRenderer = new CustomPlayerRender(context, modId);
        this.model = new CustomPlayerModel(modId);
        LOGGER.info("PlayerRendererAdapter: Инициализирован для modId={}", modId);
    }

    @Override
    public void render(PlayerEntity player, float entityYaw, float partialTicks, MatrixStack stack, VertexConsumerProvider buffer, int packedLight) {
        ConcreteDummyGeoPlayerEntity dummy = new ConcreteDummyGeoPlayerEntity(EntityType.MARKER, player.getWorld(), player);
        geoRenderer.render(dummy, entityYaw, partialTicks, stack, buffer, packedLight);
    }

    @Override
    protected void renderLabelIfPresent(PlayerEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        ConcreteDummyGeoPlayerEntity dummy = new ConcreteDummyGeoPlayerEntity(EntityType.MARKER, entity.getWorld(), entity);
        geoRenderer.renderLabelIfPresent(dummy, text, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(PlayerEntity entity) {
        ConcreteDummyGeoPlayerEntity dummy = new ConcreteDummyGeoPlayerEntity(EntityType.MARKER, entity.getWorld(), entity);
        return model.getTextureResource(dummy);
    }

    public static EntityRendererFactory<PlayerEntity> createAdapter(String modId) {
        return context -> new PlayerRendererAdapter(context, modId);
    }
}