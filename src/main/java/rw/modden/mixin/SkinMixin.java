package rw.modden.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
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
    private void getCustomTexture(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && player.getUuid().equals(client.player.getUuid())) {
            Identifier customSkin = ClientNetworking.getCustomSkin(client.player.getUuid());
            System.out.println("SkinMixin: Attempting to apply skin: " + customSkin + " for player: " + player.getGameProfile().getName());
            if (customSkin != null) {
                cir.setReturnValue(customSkin);
                System.out.println("SkinMixin: Applied skin: " + customSkin);
            } else {
                System.out.println("SkinMixin: No custom skin found, using default");
            }
        }
    }
}