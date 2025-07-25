package pro.mikey.xray.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Miscellaneous utility methods for the mod
 */
public class Utils {
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
}
