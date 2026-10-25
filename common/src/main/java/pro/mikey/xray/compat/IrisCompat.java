package pro.mikey.xray.compat;

import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import pro.mikey.xray.core.OutlineRender;

/// Only loaded when Iris is installed, as it references Iris classes directly
public final class IrisCompat {
    static void init() {
        // Tells Iris to draw our pipeline with the shader pack's lines program, the same as vanilla's lines
        IrisPipelines.assignPipeline(OutlineRender.NO_DEPTH_LINES_PIPELINE, ShaderKey.LINES);
    }
}
