package pro.mikey.xray.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import pro.mikey.xray.XRay;

import java.io.Closeable;
import java.util.*;

public class OutlineRender {
	// Build our own pipeline for no-depth test lines that mimics the vanilla lines pipeline, but with no depth test and no fog.
	// This is used to draw the outlines of blocks that are being scanned for.
	public static final RenderPipeline NO_DEPTH_LINES_PIPELINE = RenderPipeline.builder()
			.withLocation(XRay.id("pipeline/xray_lines"))
			.withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
			.withUniform("Projection", UniformType.UNIFORM_BUFFER)
			.withUniform("Fog", UniformType.UNIFORM_BUFFER)
			.withUniform("Globals", UniformType.UNIFORM_BUFFER)
			.withVertexShader("core/rendertype_lines")
			.withFragmentShader("core/rendertype_lines")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.build();

	// Building one chunk's outlines is quick, but a burst of finished scans can land in the same frame, so spread them out
	private static final int MAX_BUILDS_PER_FRAME = 8;

	private static final Map<ChunkPos, VBOHolder> vertexBuffers = new HashMap<>();

	private static final Set<ChunkPos> chunksToRefresh = Collections.synchronizedSet(new HashSet<>());

	// Vertices are baked relative to the chunk's corner, the camera offset is applied when drawing
	private static final PoseStack IDENTITY = new PoseStack();

	@Nullable
	private static GpuBuffer noFogBuffer = null;

	public static void renderBlocks() {
		if (!ScanController.INSTANCE.isActive() || Minecraft.getInstance().player == null) {
			return;
		}

		processRefreshes();

		// The scanner threads write to this, so copy it under its lock
		List<Map.Entry<ChunkPos, Map<Long, OutlineRenderTarget>>> entries;
		var renderList = ScanController.INSTANCE.syncRenderList;
		synchronized (renderList) {
			if (renderList.isEmpty()) {
				return;
			}

			entries = new ArrayList<>(renderList.entrySet());
		}

		// We use the camera position instead of the player as freecam mods might move the camera but not the player.
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
		buildMissing(entries);

		Frustum frustum = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState.cullFrustum;
		Matrix4fc modelView = RenderSystem.getModelViewStack();

		List<VBOHolder> visible = new ArrayList<>();
		List<DynamicUniforms.Transform> transforms = new ArrayList<>();
		int maxIndexCount = 0;

		for (var entry : entries) {
			ChunkPos chunkPos = entry.getKey();
			VBOHolder holder = vertexBuffers.get(chunkPos);
			if (holder == null || holder.vertexBuffer == null || holder.indexCount == 0) {
				continue;
			}

			// Support for frustum culling.
			if (!frustum.isVisible(holder.bounds)) {
				continue;
			}

			// Translate to the chunk's offset from the camera, worked out in double precision before narrowing to float
			Matrix4f transform = new Matrix4f(modelView).translate(
					(float) (chunkPos.getMinBlockX() - cameraPos.x()),
					(float) -cameraPos.y(),
					(float) (chunkPos.getMinBlockZ() - cameraPos.z())
			);

			visible.add(holder);
			transforms.add(new DynamicUniforms.Transform(transform, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f()));
			maxIndexCount = Math.max(maxIndexCount, holder.indexCount);
		}

		if (visible.isEmpty()) {
			return;
		}

		GpuBufferSlice[] transformSlices = RenderSystem.getDynamicUniforms().writeTransforms(transforms.toArray(DynamicUniforms.Transform[]::new));
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
		GpuBuffer indexBuffer = indices.getBuffer(maxIndexCount);

		GpuTextureView colorTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
		GpuTextureView depthTextureView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

		// One pass for every chunk, only the vertex buffer and transform change between draws
		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "xray", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {

			renderPass.setPipeline(NO_DEPTH_LINES_PIPELINE);
			RenderSystem.bindDefaultUniforms(renderPass);
			// Outlines stay fully visible however far into the fog they are
			renderPass.setUniform("Fog", noFog());
			renderPass.setIndexBuffer(indexBuffer, indices.type());

			for (int i = 0; i < visible.size(); i++) {
				VBOHolder holder = visible.get(i);
				renderPass.setVertexBuffer(0, holder.vertexBuffer);
				renderPass.setUniform("DynamicTransforms", transformSlices[i]);
				renderPass.drawIndexed(0, 0, holder.indexCount, 1);
			}
		}
	}

	private static void processRefreshes() {
		if (chunksToRefresh.isEmpty()) {
			return;
		}

		// The scanner threads add to this, so take a copy and clear it in one go under the lock
		List<ChunkPos> refreshing;
		synchronized (chunksToRefresh) {
			refreshing = new ArrayList<>(chunksToRefresh);
			chunksToRefresh.clear();
		}

		// Clear the vertex buffers for the chunks that need to be refreshed
		for (ChunkPos pos : refreshing) {
			VBOHolder holder = vertexBuffers.remove(pos);
			if (holder != null) {
				holder.close();
			}
		}
	}

	/// Builds vertex buffers for chunks that don't have one yet, nearest the camera first, up to the per frame budget
	private static void buildMissing(List<Map.Entry<ChunkPos, Map<Long, OutlineRenderTarget>>> entries) {
		List<Map.Entry<ChunkPos, Map<Long, OutlineRenderTarget>>> missing = new ArrayList<>();
		for (var entry : entries) {
			if (!entry.getValue().isEmpty() && !vertexBuffers.containsKey(entry.getKey())) {
				missing.add(entry);
			}
		}

		if (missing.size() > MAX_BUILDS_PER_FRAME) {
			ChunkPos cameraChunk = ChunkPos.containing(Minecraft.getInstance().gameRenderer.getMainCamera().blockPosition());
			missing.sort(Comparator.comparingInt(entry -> entry.getKey().distanceSquared(cameraChunk)));
		}

		for (int i = 0; i < Math.min(missing.size(), MAX_BUILDS_PER_FRAME); i++) {
			var entry = missing.get(i);
			vertexBuffers.put(entry.getKey(), build(entry.getKey(), entry.getValue().values()));
		}
	}

	private static VBOHolder build(ChunkPos chunkPos, Collection<OutlineRenderTarget> outlines) {
		BufferBuilder bufferBuilder = Tesselator.getInstance().begin(NO_DEPTH_LINES_PIPELINE.getVertexFormatMode(), NO_DEPTH_LINES_PIPELINE.getVertexFormat());

		final int originX = chunkPos.getMinBlockX(), originZ = chunkPos.getMinBlockZ();
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

		for (var outline : new ArrayList<>(outlines)) {
			ShapeRenderer.renderShape(IDENTITY, bufferBuilder, Shapes.block(), outline.x() - originX, outline.y(), outline.z() - originZ, outline.color(), 1f);
			minY = Math.min(minY, outline.y());
			maxY = Math.max(maxY, outline.y());
		}

		// Only as tall as the outlines inside it, so culling can skip it when they're all off screen
		AABB bounds = new AABB(originX, minY, originZ, originX + 16, maxY + 1, originZ + 16);

		MeshData meshData = bufferBuilder.build();
		if (meshData == null) {
			return new VBOHolder(null, 0, bounds);
		}

		try (meshData) {
			GpuBuffer vertexBuffer = RenderSystem.getDevice()
					.createBuffer(() -> "Xray vertex buffer", GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());

			return new VBOHolder(vertexBuffer, meshData.drawState().indexCount(), bounds);
		}
	}

	/// A fog uniform that never fogs anything
	private static GpuBufferSlice noFog() {
		if (noFogBuffer == null) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				Std140Builder builder = Std140Builder.onStack(stack, FogRenderer.FOG_UBO_SIZE).putVec4(0, 0, 0, 0);
				for (int i = 0; i < 6; i++) {
					builder.putFloat(Float.MAX_VALUE);
				}

				noFogBuffer = RenderSystem.getDevice().createBuffer(() -> "Xray no fog", GpuBuffer.USAGE_UNIFORM, builder.get());
			}
		}

		return noFogBuffer.slice(0, FogRenderer.FOG_UBO_SIZE);
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

	private record VBOHolder(@Nullable GpuBuffer vertexBuffer, int indexCount, AABB bounds) implements Closeable {

		@Override
		public void close() {
			if (vertexBuffer != null) {
				vertexBuffer.close();
			}
		}
	}
}
