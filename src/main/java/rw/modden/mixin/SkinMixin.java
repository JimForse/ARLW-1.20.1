package rw.modden.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.ClientNetworking;

@Mixin(PlayerEntityRenderer.class)
public class SkinMixin {
    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getCustomTexture(CallbackInfoReturnable<Identifier> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Identifier customSkin = ClientNetworking.getCustomSkin(client.player.getUuid());
            if (customSkin != null) {
                cir.setReturnValue(customSkin);
            }
        }
    }
}