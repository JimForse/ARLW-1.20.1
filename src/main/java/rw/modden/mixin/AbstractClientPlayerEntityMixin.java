package rw.modden.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.ClientNetworking;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void injectGetSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        Identifier customSkin = ClientNetworking.getPlayerSkin(player.getUuid());
        if (customSkin != null) {
            cir.setReturnValue(customSkin);
            cir.cancel();
        }
    }

    @Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
    private void injectGetModel(CallbackInfoReturnable<String> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        Identifier modelId = ClientNetworking.getPlayerModel(player.getUuid());
        if (modelId != null && modelId.equals(new Identifier("minecraft", "entity/player/wide"))) {
            cir.setReturnValue("default"); // Steve
        } else {
            cir.setReturnValue("slim"); // Эксклюзивная модель или Alex
        }
    }
}