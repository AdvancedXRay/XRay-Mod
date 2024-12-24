package pro.mikey.xray;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Utils {
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(XRay.MOD_ID, path);
    }

    public static ResourceLocation rlFull(String namespaceAndPath) {
        return ResourceLocation.tryParse(namespaceAndPath);
    }

    public static Component safeItemStackName(ItemStack stack) {
        try {
            @Nullable var hoverName = stack.getHoverName();
            if (hoverName != null) {
                return hoverName;
            }

            var displayName = stack.getDisplayName();
            if (displayName != null) {
                return displayName;
            }

            return Component.translatable(stack.getItem().getDescriptionId());
        } catch (Exception e) {
            return Component.literal("Unknown...");
        }
    }

    private static int packColorWithAlpha(int red, int green, int blue, int alpha) {
        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }

    private static int packColor(int red, int green, int blue) {
        return packColorWithAlpha(red, green, blue, 255);
    }

    public static int packColorWithAlpha(float red, float green, float blue, float alpha) {
        return packColorWithAlpha((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    public static int packColor(float red, float green, float blue) {
        return packColorWithAlpha(red, green, blue, 1.0f);
    }

    public static int addAlphaToPackedColor(int packedColor, float alpha) {
        return (packedColor & 0x00FFFFFF) | ((int) (alpha * 255) << 24);
    }
}
