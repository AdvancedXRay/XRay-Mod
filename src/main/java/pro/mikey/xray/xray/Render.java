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
            BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            var opacity = 1F;

            Controller.syncRenderList.forEach(blockProps -> {
                if (blockProps == null) {
                    return;
                }

                final float size = 1.0f;
                final int x = blockProps.getPos().getX(), y = blockProps.getPos().getY(), z = blockProps.getPos().getZ();

                final float red = (blockProps.getColor() >> 16 & 0xff) / 255f;
                final float green = (blockProps.getColor() >> 8 & 0xff) / 255f;
                final float blue = (blockProps.getColor() & 0xff) / 255f;

                buffer.addVertex(x, y + size, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y + size, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y + size, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y + size, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y + size, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y + size, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y + size, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y + size, z).setColor(red, green, blue, opacity);

                // BOTTOM
                buffer.addVertex(x + size, y, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y, z).setColor(red, green, blue, opacity);

                // Edge 1
                buffer.addVertex(x + size, y, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y + size, z + size).setColor(red, green, blue, opacity);

                // Edge 2
                buffer.addVertex(x + size, y, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x + size, y + size, z).setColor(red, green, blue, opacity);

                // Edge 3
                buffer.addVertex(x, y, z + size).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y + size, z + size).setColor(red, green, blue, opacity);

                // Edge 4
                buffer.addVertex(x, y, z).setColor(red, green, blue, opacity);
                buffer.addVertex(x, y + size, z).setColor(red, green, blue, opacity);
            });

            MeshData build = buffer.build();
            if (build == null) {
                vertexBuffer = null;
                return;
            } else {
                vertexBuffer.bind();
                vertexBuffer.upload(build);
                VertexBuffer.unbind();
            }
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
