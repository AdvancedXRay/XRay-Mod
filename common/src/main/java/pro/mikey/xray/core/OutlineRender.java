package pro.mikey.xray.core;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.io.Closeable;
import java.util.*;

public class OutlineRender {
	private static final Logger LOGGER = LogManager.getLogger();

	// 26.2: Use PrimitiveTopology.LINES for the index buffer
	private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.LINES);
	private static final Map<ChunkPos, VBOHolder> vertexBuffers = new HashMap<>();

	private static final Set<ChunkPos> chunksToRefresh = Collections.synchronizedSet(new HashSet<>());

	// Lazy-initialized pipeline to ensure access widener is applied first
	private static RenderPipeline xrayLinesPipeline = null;

	private static RenderPipeline getXrayPipeline() {
		if (xrayLinesPipeline == null) {
			// 26.2 port: depth test is now a property of the pipeline, not a global GL state.
			// The old code globally disabled GL_DEPTH_TEST around the draw to get the see-through-walls
			// effect; raw GL calls no longer function under the new Vulkan backend, so we bake
			// CompareOp.ALWAYS into a derived LINES pipeline to always pass depth test.
			xrayLinesPipeline = RenderPipelines.register(
					RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
							.withLocation("xray/pipeline/xray_lines")
							.withDepthStencilState(Optional.of(new DepthStencilState(CompareOp.ALWAYS_PASS, false)))
							.build()
			);
		}
		return xrayLinesPipeline;
	}

	// Vertex format for lines: position, color, normal, line width
	private static final int VERTICES_PER_BLOCK = 24; // 12 edges * 2 vertices each
	private static final float LINE_WIDTH = 2.0f;

	public static void renderBlocks(Vec3 cameraPos) {
		try {
			boolean isActive = ScanController.INSTANCE.isXRayActive();
			boolean hasPlayer = Minecraft.getInstance().player != null;

			if (!isActive || !hasPlayer) {
				return;
			}

			// Take a snapshot of the render list to avoid concurrent modification
			Map<ChunkPos, Set<OutlineRenderTarget>> renderListSnapshot;
			synchronized (ScanController.INSTANCE.syncRenderList) {
				if (ScanController.INSTANCE.syncRenderList.isEmpty()) {
					return;
				}
				renderListSnapshot = new HashMap<>(ScanController.INSTANCE.syncRenderList);
			}

	
			if (!chunksToRefresh.isEmpty()) {
				// Clear the vertex buffers for the chunks that need to be refreshed
				List<ChunkPos> toRefresh = new ArrayList<>(chunksToRefresh);
				chunksToRefresh.clear();
				for (ChunkPos pos : toRefresh) {
					VBOHolder holder = vertexBuffers.remove(pos);
					if (holder != null) {
						holder.close();
					}
				}
			}

			for (var chunkWithBlockData : renderListSnapshot.entrySet()) {
				var chunkPos = chunkWithBlockData.getKey();
				var blocksWithProps = chunkWithBlockData.getValue();

				if (blocksWithProps == null || blocksWithProps.isEmpty()) {
					continue;
				}

				VBOHolder holder = vertexBuffers.get(chunkPos);
				if (holder == null) {
					// Take a snapshot of the blocks to avoid concurrent modification
					var blockPropsClone = new ArrayList<>(blocksWithProps);

				// Calculate buffer size: each block needs 24 vertices, each vertex has POSITION_COLOR_NORMAL_LINE_WIDTH format
				// Use 2x buffer size to account for any overhead/alignment requirements
				int vertexSize = DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize();
				int bufferSize = blockPropsClone.size() * VERTICES_PER_BLOCK * vertexSize * 2;

				try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(bufferSize)) {
					BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

					for (var blockProps : blockPropsClone) {
						if (blockProps == null) {
							continue;
						}

						final int x = blockProps.x(), y = blockProps.y(), z = blockProps.z();

						// Render block outline - replaces ShapeRenderer.renderShape which was removed in 26.2
						renderBlockOutline(bufferBuilder, x, y, z, blockProps.color());
					}

					try (MeshData meshData = bufferBuilder.buildOrThrow()) {
						int indexCount = meshData.drawState().indexCount();
						GpuBuffer vertexBuffer = RenderSystem.getDevice()
								.createBuffer(() -> "Xray vertex buffer", GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());

						vertexBuffers.put(chunkPos, new VBOHolder(vertexBuffer, indexCount));
					}
				}
			}

			holder = vertexBuffers.get(chunkPos);
			if (holder == null || holder.vertexBuffer == null || holder.indexCount == 0) {
				continue;
			}

			// Camera position is passed from the render context

			Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
			RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
			GpuTextureView colorTextureView = mainRenderTarget.getColorTextureView();
			GpuTextureView depthTextureView = mainRenderTarget.getDepthTextureView();

			matrix4fStack.pushMatrix();
			// Translate by negative camera position to render blocks in world space
			matrix4fStack.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
			GpuBufferSlice dynamicTransform = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(matrix4fStack));

			GpuBuffer gpuBuffer = indices.getBuffer(holder.indexCount);
			// Clear depth to 0.0 (far plane in reversed depth) so all fragments pass GREATER_THAN_OR_EQUAL test
			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(() -> "xray", colorTextureView, Optional.empty(), depthTextureView, OptionalDouble.of(0.0))) {

				renderPass.setPipeline(RenderPipelines.LINES);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setVertexBuffer(0, holder.vertexBuffer.slice());
				renderPass.setIndexBuffer(gpuBuffer, indices.type());
				renderPass.setUniform("DynamicTransforms", dynamicTransform);
				renderPass.drawIndexed(holder.indexCount, 1, 0, 0, 0);
			}

			matrix4fStack.popMatrix();
		}
		} catch (Exception e) {
			LOGGER.error("Error rendering XRay blocks", e);
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

	/**
	 * Renders a block outline as lines. This replaces ShapeRenderer.renderShape which was removed in MC 26.2.
	 * A block has 12 edges, and for the LINES pipeline, each edge requires 2 vertices with position, color, normal, and line width.
	 */
	private static void renderBlockOutline(BufferBuilder buffer, float x, float y, float z, int color) {
		float minX = x;
		float minY = y;
		float minZ = z;
		float maxX = x + 1.0f;
		float maxY = y + 1.0f;
		float maxZ = z + 1.0f;

		// Bottom face edges (Y = minY)
		addLineVertex(buffer, minX, minY, minZ, color, 1, 0, 0);
		addLineVertex(buffer, maxX, minY, minZ, color, 1, 0, 0);

		addLineVertex(buffer, maxX, minY, minZ, color, 0, 0, 1);
		addLineVertex(buffer, maxX, minY, maxZ, color, 0, 0, 1);

		addLineVertex(buffer, maxX, minY, maxZ, color, -1, 0, 0);
		addLineVertex(buffer, minX, minY, maxZ, color, -1, 0, 0);

		addLineVertex(buffer, minX, minY, maxZ, color, 0, 0, -1);
		addLineVertex(buffer, minX, minY, minZ, color, 0, 0, -1);

		// Top face edges (Y = maxY)
		addLineVertex(buffer, minX, maxY, minZ, color, 1, 0, 0);
		addLineVertex(buffer, maxX, maxY, minZ, color, 1, 0, 0);

		addLineVertex(buffer, maxX, maxY, minZ, color, 0, 0, 1);
		addLineVertex(buffer, maxX, maxY, maxZ, color, 0, 0, 1);

		addLineVertex(buffer, maxX, maxY, maxZ, color, -1, 0, 0);
		addLineVertex(buffer, minX, maxY, maxZ, color, -1, 0, 0);

		addLineVertex(buffer, minX, maxY, maxZ, color, 0, 0, -1);
		addLineVertex(buffer, minX, maxY, minZ, color, 0, 0, -1);

		// Vertical edges connecting top and bottom
		addLineVertex(buffer, minX, minY, minZ, color, 0, 1, 0);
		addLineVertex(buffer, minX, maxY, minZ, color, 0, 1, 0);

		addLineVertex(buffer, maxX, minY, minZ, color, 0, 1, 0);
		addLineVertex(buffer, maxX, maxY, minZ, color, 0, 1, 0);

		addLineVertex(buffer, maxX, minY, maxZ, color, 0, 1, 0);
		addLineVertex(buffer, maxX, maxY, maxZ, color, 0, 1, 0);

		addLineVertex(buffer, minX, minY, maxZ, color, 0, 1, 0);
		addLineVertex(buffer, minX, maxY, maxZ, color, 0, 1, 0);
	}

	private static void addLineVertex(BufferBuilder buffer, float x, float y, float z, int color, float nx, float ny, float nz) {
		buffer.addVertex(x, y, z).setColor(color).setNormal(nx, ny, nz).setLineWidth(LINE_WIDTH);
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
