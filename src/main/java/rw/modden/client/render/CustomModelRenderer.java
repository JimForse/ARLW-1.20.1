package rw.modden.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;

public class CustomModelRenderer extends PlayerEntityRenderer {
    public CustomModelRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, slim);
    }
}
