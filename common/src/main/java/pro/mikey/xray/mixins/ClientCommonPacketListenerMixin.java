package pro.mikey.xray.mixins;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.mikey.xray.core.ScanController;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerMixin {

    @Inject(method = "onDisconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        ScanController.INSTANCE.onDisconnect();
    }
}
