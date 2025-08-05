package pro.mikey.xray.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import pro.mikey.xray.XRay;

import java.io.Closeable;
import java.util.*;

public class OutlineRender {
    public static boolean requestedRefresh = false;

	private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
	private static final Map<ChunkPos, VBOHolder> vertexBuffers = new HashMap<>();

	private static final Set<ChunkPos> chunksToRefresh = Collections.synchronizedSet(new HashSet<>());

	public static RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
			.withLocation(XRay.id("pipeline/xray_lines"))
			.withVertexShader("core/rendertype_lines")
			.withFragmentShader(ResourceLocation.fromNamespaceAndPath(XRay.MOD_ID, "frag/constant_color"))
			.withBlend(BlendFunction.TRANSLUCENT)
			.withCull(false)
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withColorLogic(LogicOp.NONE)
			.build();

	public static void renderBlocks(PoseStack poseStack) {
		if (!ScanController.INSTANCE.isXRayActive() || Minecraft.getInstance().player == null) {
			return;
		}

		if (ScanController.INSTANCE.syncRenderList.isEmpty()) {
			return;
		}

		if (!chunksToRefresh.isEmpty()) {
			// Clear the vertex buffers for the chunks that need to be refreshed
			for (ChunkPos pos : chunksToRefresh) {
				VBOHolder holder = vertexBuffers.remove(pos);
				if (holder != null) {
					holder.close();
				}
			}

			chunksToRefresh.clear();
		}

		// Clone the entrySet to avoid concurrent modification exceptions
		var entries = new ArrayList<>(ScanController.INSTANCE.syncRenderList.entrySet());

		for (var chunkWithBlockData : entries) {
			var chunkPos = chunkWithBlockData.getKey();
			var blocksWithProps = chunkWithBlockData.getValue();

			if (blocksWithProps.isEmpty()) {
				continue;
			}

			VBOHolder holder = vertexBuffers.get(chunkPos);
			if (holder == null) {
				BufferBuilder bufferBuilder = Tesselator.getInstance().begin(LINES_NO_DEPTH.getVertexFormatMode(), LINES_NO_DEPTH.getVertexFormat());

				var opacity = 1F;

				// More concurrent modification exceptions can happen here, so we clone the list
				var blockPropsClone = new ArrayList<>(blocksWithProps);

				for (var blockProps : blockPropsClone) {
					if (blockProps == null) {
						continue;
					}

					final float size = 1.0f;
					final int x = blockProps.x(), y = blockProps.y(), z = blockProps.z();

					final float red = (blockProps.color() >> 16 & 0xff) / 255f;
					final float green = (blockProps.color() >> 8 & 0xff) / 255f;
					final float blue = (blockProps.color() & 0xff) / 255f;

					ShapeRenderer.renderLineBox(poseStack, bufferBuilder, x, y, z, x + size, y + size, z + size, red, green, blue, opacity);
				}

				try (MeshData meshData = bufferBuilder.buildOrThrow()) {
					int indexCount = meshData.drawState().indexCount();
					GpuBuffer vertexBuffer = RenderSystem.getDevice()
							.createBuffer(() -> "Xray vertex buffer", GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());

					vertexBuffers.put(chunkPos, new VBOHolder(vertexBuffer, indexCount));
				}
			}

			holder = vertexBuffers.get(chunkPos);
			if (holder == null || holder.vertexBuffer == null || holder.indexCount == 0) {
				continue;
			}

			Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().reverse();

			RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
			if (renderTarget.getColorTexture() == null) {
				return;
			}

			Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
			GpuTextureView colorTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
			GpuTextureView depthTextureView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

			matrix4fStack.pushMatrix();
			matrix4fStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());
			GpuBufferSlice[] gpubufferslice = RenderSystem.getDynamicUniforms().writeTransforms(new DynamicUniforms.Transform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f(), 2.0F));
			matrix4fStack.popMatrix();

			GpuBuffer gpuBuffer = indices.getBuffer(holder.indexCount);
			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(() -> "xray", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {

				renderPass.setPipeline(LINES_NO_DEPTH);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setVertexBuffer(0, holder.vertexBuffer);
				renderPass.setIndexBuffer(gpuBuffer, indices.type());
				renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
				renderPass.setPipeline(LINES_NO_DEPTH);
				renderPass.drawIndexed(0, 0, holder.indexCount, 1);
			}
		}
	}

	public static void clearVBOs() {
		for (VBOHolder holder : vertexBuffers.values()) {
			if (holder != null) {
				holder.close();
			}
		}
		vertexBuffers.clear();
	}

	public static void clearVBOsFor(List<ChunkPos> removedChunks) {
		if (removedChunks.isEmpty()) {
			return;
		}

		chunksToRefresh.addAll(removedChunks);
	}

	public static void refreshVBOForChunk(ChunkPos pos) {
		chunksToRefresh.add(pos);
	}

	private record VBOHolder(GpuBuffer vertexBuffer, int indexCount) implements Closeable {

		@Override
		public void close() {
			if (vertexBuffer != null) {
				vertexBuffer.close();
			}
		}
	}
}
