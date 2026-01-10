package pro.mikey.xray.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.io.Closeable;
import java.util.*;

public class OutlineRender {
	private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
	private static final Map<ChunkPos, VBOHolder> vertexBuffers = new HashMap<>();
	private static final Map<ChunkPos, VBOHolder> vertexBuffersa = new HashMap<>();

	private static final Set<ChunkPos> chunksToRefresh = Collections.synchronizedSet(new HashSet<>());
	private static final Set<ChunkPos> chunksToRefresha = Collections.synchronizedSet(new HashSet<>());
    private static final Object CHUNKS_TO_REFRESH_LOCK = new Object();
    private static final Object CHUNKS_TO_REFRESH_LOCKA = new Object();
	private static final VoxelShape BLOCK = Block.box(0.1D, 0.1D, 0.1D, 15.9D, 15.9D, 15.9D);

	public static void renderBlocks(PoseStack poseStack) {
		if (!ScanController.INSTANCE.isXRayActive() || Minecraft.getInstance().player == null) {
			return;
		}

		if (ScanController.INSTANCE.syncRenderList.isEmpty()) {
			//return;
		} else {
			if (!chunksToRefresh.isEmpty()) {
				synchronized (CHUNKS_TO_REFRESH_LOCK) {
				// Clear the vertex buffers for the chunks that need to be refreshed
					for (ChunkPos pos : chunksToRefresh) {
						VBOHolder holder = vertexBuffers.remove(pos);
						if (holder != null) {
							holder.close();
						}
					}
		
					chunksToRefresh.clear();
				}
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
					BufferBuilder bufferBuilder = Tesselator.getInstance().begin(RenderPipelines.LINES.getVertexFormatMode(), RenderPipelines.LINES.getVertexFormat());
	
					// More concurrent modification exceptions can happen here, so we clone the list
					var blockPropsClone = new ArrayList<>(blocksWithProps);
	
					for (var blockProps : blockPropsClone) {
						if (blockProps == null) {
							continue;
						}
	
						final int x = blockProps.x(), y = blockProps.y(), z = blockProps.z();
	
						ShapeRenderer.renderShape(poseStack, bufferBuilder, BLOCK, x, y, z, blockProps.color(), 1f);
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
	
				Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().position().reverse();
	
				Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
				GpuTextureView colorTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
				GpuTextureView depthTextureView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
	
				matrix4fStack.pushMatrix();
				matrix4fStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());
				GpuBufferSlice[] gpubufferslice = RenderSystem.getDynamicUniforms().writeTransforms(new DynamicUniforms.Transform(new Matrix4f(matrix4fStack), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f()));
	
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				RenderSystem.setShaderFog(gpubufferslice[0]);
	
				GpuBuffer gpuBuffer = indices.getBuffer(holder.indexCount);
				try (RenderPass renderPass = RenderSystem.getDevice()
						.createCommandEncoder()
						.createRenderPass(() -> "xray", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {
	
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setVertexBuffer(0, holder.vertexBuffer);
					renderPass.setIndexBuffer(gpuBuffer, indices.type());
					renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
					renderPass.setPipeline(RenderPipelines.LINES);
					renderPass.drawIndexed(0, 0, holder.indexCount, 1);
				}
	
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				matrix4fStack.popMatrix();
			}
		}

		//
		if (ScanController.INSTANCE.syncRenderLista.isEmpty()) {
			return;
		} else {
			if (!chunksToRefresha.isEmpty()) {
				synchronized (CHUNKS_TO_REFRESH_LOCKA) {
					// Clear the vertex buffers for the chunks that need to be refreshed
					for (ChunkPos pos : chunksToRefresha) {
						VBOHolder holder = vertexBuffersa.remove(pos);
						if (holder != null) {
							holder.close();
						}
					}
		
					chunksToRefresha.clear();
				}
			}
	
			// Clone the entrySet to avoid concurrent modification exceptions
			var entries = new ArrayList<>(ScanController.INSTANCE.syncRenderLista.entrySet());
	
			for (var chunkWithBlockData : entries) {
				var chunkPos = chunkWithBlockData.getKey();
				var blocksWithProps = chunkWithBlockData.getValue();
	
				if (blocksWithProps.isEmpty()) {
					continue;
				}
	
				VBOHolder holder = vertexBuffersa.get(chunkPos);
				if (holder == null) {
					BufferBuilder bufferBuilder = Tesselator.getInstance().begin(RenderPipelines.LINES.getVertexFormatMode(), RenderPipelines.LINES.getVertexFormat());
	
					// More concurrent modification exceptions can happen here, so we clone the list
					var blockPropsClone = new ArrayList<>(blocksWithProps);
	
					for (var blockProps : blockPropsClone) {
						if (blockProps == null) {
							continue;
						}
	
						final int x = blockProps.x(), y = blockProps.y(), z = blockProps.z();
	
						ShapeRenderer.renderShape(poseStack, bufferBuilder, BLOCK, x, y, z, blockProps.color(), 1f);
					}
	
					try (MeshData meshData = bufferBuilder.buildOrThrow()) {
						int indexCount = meshData.drawState().indexCount();
						GpuBuffer vertexBuffer = RenderSystem.getDevice()
								.createBuffer(() -> "Xray vertex buffer", GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());
	
						vertexBuffersa.put(chunkPos, new VBOHolder(vertexBuffer, indexCount));
					} catch (Exception e) {
					}
				}
	
				holder = vertexBuffersa.get(chunkPos);
				if (holder == null || holder.vertexBuffer == null || holder.indexCount == 0) {
					continue;
				}
	
				Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().position().reverse();
	
				Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
				GpuTextureView colorTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
				GpuTextureView depthTextureView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
	
				matrix4fStack.pushMatrix();
				matrix4fStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());
				GpuBufferSlice[] gpubufferslice = RenderSystem.getDynamicUniforms().writeTransforms(new DynamicUniforms.Transform(new Matrix4f(matrix4fStack), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f()));
	
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				RenderSystem.setShaderFog(gpubufferslice[0]);
	
				GpuBuffer gpuBuffer = indices.getBuffer(holder.indexCount);
				try (RenderPass renderPass = RenderSystem.getDevice()
						.createCommandEncoder()
						.createRenderPass(() -> "xray", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {
	
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setVertexBuffer(0, holder.vertexBuffer);
					renderPass.setIndexBuffer(gpuBuffer, indices.type());
					renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
					renderPass.setPipeline(RenderPipelines.LINES);
					renderPass.drawIndexed(0, 0, holder.indexCount, 1);
				}
	
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				matrix4fStack.popMatrix();
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
		for (VBOHolder holder : vertexBuffersa.values()) {
			if (holder != null) {
				holder.close();
			}
		}
		vertexBuffersa.clear();
	}

	public static void clearVBOsFor(List<ChunkPos> removedChunks) {
		if (removedChunks.isEmpty()) {
			return;
		}

        synchronized (CHUNKS_TO_REFRESH_LOCK) {
			chunksToRefresh.addAll(removedChunks);
		}
	}

	public static void refreshVBOForChunk(ChunkPos pos) {
        synchronized (CHUNKS_TO_REFRESH_LOCK) {
			chunksToRefresh.add(pos);
		}
	}

	public static void refreshVBOForChunka(ChunkPos pos) {
		synchronized (CHUNKS_TO_REFRESH_LOCKA) {
			chunksToRefresha.add(pos);
		}
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
