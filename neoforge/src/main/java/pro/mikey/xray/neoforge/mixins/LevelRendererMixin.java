package pro.mikey.xray.neoforge.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.mikey.xray.core.OutlineRender;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addWeatherPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/world/phys/Vec3;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    private void onRenderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
        LevelRenderer levelRenderer = (LevelRenderer) (Object) this;
        FramePass framePass = frameGraphBuilder.addPass("xray:outline");

        if (levelRenderer.targets.weather != null) {
            levelRenderer.targets.weather = framePass.readsAndWrites(levelRenderer.targets.weather);
        } else {
            levelRenderer.targets.main = framePass.readsAndWrites(levelRenderer.targets.main);
        }

        framePass.executes(() -> {
            PoseStack poseStack = new PoseStack();
            OutlineRender.renderBlocks(poseStack);
        });
    }
}
