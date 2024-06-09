package pro.mikey.xray.xray;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;

public class Render {
    private static VertexBuffer vertexBuffer;
    public static boolean requestedRefresh = false;

	static void renderBlocks(RenderLevelStageEvent event) {
        if (vertexBuffer == null || requestedRefresh) {
            requestedRefresh = false;
            vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

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
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);

            poseStack.mulPose(event.getModelViewMatrix());
            poseStack.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());

            vertexBuffer.bind();
            vertexBuffer.drawWithShader(poseStack.last().pose(), event.getProjectionMatrix(), RenderSystem.getShader());
            VertexBuffer.unbind();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
	}
}
