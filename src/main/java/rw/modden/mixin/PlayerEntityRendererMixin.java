package rw.modden.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.character.CharacterInitializer;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getCustomSkin(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        String playerName = player.getGameProfile().getName();
        Identifier customSkin = CharacterInitializer.getSkin(playerName);
        if (customSkin != null) {
            cir.setReturnValue(customSkin);
            System.out.println("PlayerEntityRendererMixin: Applied custom skin for " + playerName);
        }
    }
}