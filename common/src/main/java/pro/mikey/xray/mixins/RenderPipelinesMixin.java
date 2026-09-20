package pro.mikey.xray.mixins;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesMixin {
    @Accessor("MATRICES_FOG_SNIPPET")
    static RenderPipeline.Snippet getMatricesFogSnippet() {
        throw new AssertionError();
    }
}
