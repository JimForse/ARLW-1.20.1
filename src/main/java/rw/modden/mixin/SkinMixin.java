package rw.modden.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rw.modden.Axorunelostworlds;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class SkinMixin {
    private static Identifier customSkin = null;

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void injectGetSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        if (customSkin != null) {
            cir.setReturnValue(customSkin);
        }
    }

    @Unique
    public static void setCustomSkin(Identifier skin) {
        customSkin = skin;
    }
}