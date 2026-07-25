package pro.mikey.xray.fabric;

import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import pro.mikey.xray.core.OutlineRender;

/**
 * Isolated in its own class so the JVM never has to resolve Iris's classes unless this class is
 * actually loaded, which only happens behind an "is Iris installed" check in XRayFabric.
 */
final class IrisCompat {
    private IrisCompat() {
    }

    static void register() {
        IrisPipelines.assignPipeline(OutlineRender.NO_DEPTH_LINES_PIPELINE, ShaderKey.LINES);
    }
}
