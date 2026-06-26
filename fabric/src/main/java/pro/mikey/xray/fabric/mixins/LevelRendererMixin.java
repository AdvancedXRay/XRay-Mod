package pro.mikey.xray.fabric.mixins;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.mikey.xray.core.OutlineRender;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "lambda$addWeatherPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;render(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/level/WeatherRenderState;)V"))
    public void onAddWeatherPass(CallbackInfo ci) {
        OutlineRender.renderBlocks();
    }
}
