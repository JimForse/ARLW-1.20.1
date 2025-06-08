package rw.modden.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.ClientNetworking;

@Mixin(PlayerEntityRenderer.class)
public class PlayerSkinMixin {
    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getCustomTexture(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        Identifier customSkin = ClientNetworking.getPlayerSkin(player.getUuid());
        if (customSkin != null) {
            cir.setReturnValue(customSkin);
        }
    }
}