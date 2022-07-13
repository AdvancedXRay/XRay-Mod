package pro.mikey.xray.xray;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;

public class Render {
    private static VertexBuffer vertexBuffer;
    public static boolean requestedRefresh = false;

	static void renderBlocks(RenderLevelStageEvent event) {
        if (vertexBuffer == null || requestedRefresh) {
            requestedRefresh = false;
            vertexBuffer = new VertexBuffer();

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();

            var opacity = 1F;

            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            Controller.syncRenderList.forEach(blockProps -> {
                if (blockProps == null) {
                    return;
                }

                final float size = 1.0f;
                final double x = blockProps.getPos().getX(), y = blockProps.getPos().getY(), z = blockProps.getPos().getZ();

                final float red = (blockProps.getColor() >> 16 & 0xff) / 255f;
                final float green = (blockProps.getColor() >> 8 & 0xff) / 255f;
                final float blue = (blockProps.getColor() & 0xff) / 255f;

                buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();

                // BOTTOM
                buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();

                // Edge 1
                buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();

                // Edge 2
                buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();

                // Edge 3
                buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();

                // Edge 4
                buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
                buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
            });

            vertexBuffer.bind();
            vertexBuffer.upload(buffer.end());
            VertexBuffer.unbind();
        }

        if (vertexBuffer != null) {
            Vec3 view = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            PoseStack matrix = event.getPoseStack();
            matrix.pushPose();
            matrix.translate(-view.x, -view.y, -view.z);

            vertexBuffer.bind();
            vertexBuffer.drawWithShader(matrix.last().pose(), event.getProjectionMatrix().copy(), RenderSystem.getShader());
            VertexBuffer.unbind();
            matrix.popPose();

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
	}
}
