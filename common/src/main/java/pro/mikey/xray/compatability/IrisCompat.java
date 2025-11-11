package pro.mikey.xray.compatability;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import pro.mikey.xray.core.OutlineRender;

public class IrisCompat {
    public static void init() {
        IrisApi.getInstance().assignPipeline(OutlineRender.LINES_NO_DEPTH, IrisProgram.LINES);
    }
}
