package com.xray.common.utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Created by MiKeY on 29/12/17.
 */
public class Utils {

    public static void renderBlockBounding(Tessellator tessellator, BufferBuilder buffer, float x, float y, float z, int red, int green, int blue, int opacity, boolean isLines) {
        float f = 0.0f;
        float f1 = 1.0f;

        buffer.begin(
                (isLines ? GL11.GL_LINES : GL11.GL_QUADS),
                DefaultVertexFormats.POSITION_COLOR
        );

        // TOP
        buffer.pos(x + f, y + f1, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f1, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f1, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f1, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f1, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f1, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f1, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f1, z + f).color(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.pos(x + f1, y + f, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f, z + f).color(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.pos(x + f1, y + f, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f1, z + f1).color(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.pos(x + f1, y + f, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f1, y + f1, z + f).color(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.pos(x + f, y + f, z + f1).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f1, z + f1).color(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.pos(x + f, y + f, z + f).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + f, y + f1, z + f).color(red, green, blue, opacity).endVertex();

        tessellator.draw();
    }

}
