package com.xray.common.utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Created by MiKeY on 29/12/17.
 */
public class Utils {

    public static void renderBlockBounding(BufferBuilder buffer, float x, float y, float z, int red, int green, int blue, int opacity, boolean isLines) {
        final float size = 1.0f;

        buffer.begin(
                (isLines ? GL11.GL_LINES : GL11.GL_QUADS),
                DefaultVertexFormats.POSITION_COLOR
        );

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

        Tessellator.getInstance().draw();
    }

}
