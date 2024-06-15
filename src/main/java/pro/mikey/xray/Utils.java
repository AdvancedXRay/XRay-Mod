package pro.mikey.xray;

import net.minecraft.resources.ResourceLocation;

public class Utils {
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(XRay.MOD_ID, path);
    }

    public static ResourceLocation rlFull(String namespaceAndPath) {
        return ResourceLocation.tryParse(namespaceAndPath);
    }
}
