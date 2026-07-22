package pro.mikey.xray.mixins;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.ScanController;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
  
    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", at = @At("HEAD"))
    private void onCustomPayload(CustomPacketPayload payload, CallbackInfo ci) {
        if (XRay.SERVER_DISABLE_CHANNEL.equals(payload.type().id())) {
            ScanController.INSTANCE.disableOnServer();
        }
    }
}
