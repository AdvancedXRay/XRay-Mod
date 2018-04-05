package com.xray.common.utils;

import com.xray.common.reference.BlockInfo;
import net.minecraft.client.renderer.BufferBuilder;

/**
 * Created by MiKeY on 29/12/17.
 */
public class Utils {

    public static void renderBlockBounding(BufferBuilder buffer, BlockInfo b, int opacity) {
        final float size = 1.0f;
        int red = b.color[0];
        int green = b.color[1];
        int blue = b.color[2];
        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();

        // TOP
        buffer.pos(x, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z).color(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.pos(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y, z).color(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.pos(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.pos(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z).color(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.pos(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.pos(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z).color(red, green, blue, opacity).endVertex();
    }

}
