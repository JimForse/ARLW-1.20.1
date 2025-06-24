package rw.modden.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.EntityType;

public class ModClientRender {
    @SuppressWarnings("unchecked")
    public void initialize() {
        EntityRendererRegistry.register((EntityType) EntityType.PLAYER, (EntityRendererFactory) (ctx -> new CustomModelRenderer(ctx, false)));
    }

}