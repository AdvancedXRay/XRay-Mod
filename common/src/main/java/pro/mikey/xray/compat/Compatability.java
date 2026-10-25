package pro.mikey.xray.compat;

import pro.mikey.xray.XRay;

public class Compatability {
    public static void init() {
        if (XRay.XPLAT.isModLoaded("iris")) {
            IrisCompat.init();
        }
    }
}
