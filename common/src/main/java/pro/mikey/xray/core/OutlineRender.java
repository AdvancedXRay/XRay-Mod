package pro.mikey.xray.core;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import pro.mikey.xray.XRay;

import java.io.Closeable;
import java.util.*;

public class OutlineRender {
    public static final RenderPipeline NO_DEPTH_LINES_PIPELINE = RenderPipeline.builder(
        RenderPipelines.MATRICES_FOG_SNIPPET)
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader("core/rendertype_lines")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
            .withPrimitiveTopology(PrimitiveTopology.LINES)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)
        )
        .withLocation(XRay.id("pipeline/lines_no_depth"))
        .build();

	private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.LINES);

	private static final Map<ChunkPos, VBOHolder> vertexBuffers = new HashMap<>();

	private static final Set<ChunkPos> chunksToRefresh = Collections.synchronizedSet(new HashSet<>());

	public static void renderBlocks() {
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
				try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(NO_DEPTH_LINES_PIPELINE.getVertexFormatBinding(0).getVertexSize() * 1024)) {
					BufferBuilder bufferBuilder = new BufferBuilder(
							byteBufferBuilder,
							NO_DEPTH_LINES_PIPELINE.getPrimitiveTopology(),
							NO_DEPTH_LINES_PIPELINE.getVertexFormatBinding(0)
					);

					var blockPropsClone = new ArrayList<>(blocksWithProps);
					for (var blockProps : blockPropsClone) {
                        Vector3f normal = new Vector3f();
                        if (blockProps == null) continue;
                        final int x = blockProps.x(), y = blockProps.y(), z = blockProps.z();
                        int color = blockProps.color();
                        float width = 2.0f;

                        Shapes.block().forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                            normal.set((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
                            bufferBuilder.addVertex((float)(x + x1), (float)(y + y1), (float)(z + z1)).setColor(color).setNormal(normal.x(), normal.y(), normal.z()).setLineWidth(width);
                            bufferBuilder.addVertex((float)(x + x2), (float)(y + y2), (float)(z + z2)).setColor(color).setNormal(normal.x(), normal.y(), normal.z()).setLineWidth(width);
                        });
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

			Vec3 playerPos = Minecraft.getInstance().gameRenderer.mainCamera().position().reverse();

			Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
			GpuTextureView colorTextureView = Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView();
			GpuTextureView depthTextureView = Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTextureView();

			matrix4fStack.pushMatrix();
			matrix4fStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());
			GpuBufferSlice[] gpubufferslice = RenderSystem.getDynamicUniforms().writeTransforms(new DynamicUniforms.Transform(new Matrix4f(matrix4fStack), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f()));

            RenderSystem.setShaderFog(gpubufferslice[0]);

			GpuBuffer gpuBuffer = indices.getBuffer(holder.indexCount);
			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(() -> "xray", colorTextureView, Optional.empty(), depthTextureView, OptionalDouble.empty())) {

				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setVertexBuffer(0, holder.vertexBuffer.slice());
				renderPass.setIndexBuffer(gpuBuffer, indices.type());
				renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
				renderPass.setPipeline(NO_DEPTH_LINES_PIPELINE);
				renderPass.drawIndexed(holder.indexCount, 1, 0, 0, 0);
			}

            matrix4fStack.popMatrix();
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
