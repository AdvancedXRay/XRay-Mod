package pro.mikey.xray.xray;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;
import pro.mikey.xray.XRay;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Render {
    public static boolean requestedRefresh = false;
	private static GpuBuffer vertexBuffer = null;
	private static int indexCount = 0;
	private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);

	public static RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
			.withLocation("pipeline/xray_lines")
			.withVertexShader("core/rendertype_lines")
			.withFragmentShader(ResourceLocation.fromNamespaceAndPath(XRay.MOD_ID, "frag/constant_color"))
			.withUniform("LineWidth", UniformType.FLOAT)
			.withUniform("ScreenSize", UniformType.VEC2)
			.withBlend(BlendFunction.TRANSLUCENT)
			.withCull(false)
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.build();

	static void renderBlocks(RenderLevelStageEvent event) {
		if (Controller.syncRenderList.isEmpty()) {
			return;
		}

		RenderPipeline pipeline = LINES_NO_DEPTH;
        if (vertexBuffer == null || requestedRefresh) {
            requestedRefresh = false;

			if (vertexBuffer != null) {
				vertexBuffer.close();
			}

			BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
					pipeline.getVertexFormatMode(), pipeline.getVertexFormat()
			);

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

				ShapeRenderer.renderLineBox(event.getPoseStack(), bufferBuilder, x, y, z, x + size, y + size, z + size, red, green, blue, opacity);
			});

			try (MeshData meshData = bufferBuilder.buildOrThrow()) {
				vertexBuffer = RenderSystem.getDevice()
						.createBuffer(() -> "Xray vertex buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshData.vertexBuffer());

				indexCount = meshData.drawState().indexCount();
			}
        }

        if (indexCount != 0) {
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().reverse();

			RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
			if (renderTarget.getColorTexture() == null) {
				return;
			}


			GpuBuffer gpuBuffer = indices.getBuffer(indexCount);
			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(renderTarget.getColorTexture(), OptionalInt.empty(), renderTarget.getDepthTexture(), OptionalDouble.empty())) {

				Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
				matrix4fStack.pushMatrix();
				matrix4fStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());

				renderPass.setPipeline(pipeline);
				renderPass.setIndexBuffer(gpuBuffer, indices.type());
				renderPass.setVertexBuffer(0, vertexBuffer);
				renderPass.drawIndexed(0, indexCount);

				matrix4fStack.popMatrix();
			}
        }
	}
}
