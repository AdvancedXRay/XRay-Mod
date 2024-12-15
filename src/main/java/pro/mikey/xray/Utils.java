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

            return Component.translatable(stack.getDescriptionId());
        } catch (Exception e) {
            return Component.literal("Unknown...");
        }
    }
}
