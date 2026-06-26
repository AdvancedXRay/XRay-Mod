//package pro.mikey.xray.core;
//
//import com.mojang.blaze3d.PrimitiveTopology;
//import com.mojang.blaze3d.pipeline.BlendFunction;
//import com.mojang.blaze3d.pipeline.ColorTargetState;
//import com.mojang.blaze3d.pipeline.DepthStencilState;
//import com.mojang.blaze3d.pipeline.RenderPipeline;
//import com.mojang.blaze3d.platform.CompareOp;
//import com.mojang.blaze3d.vertex.*;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.RenderPipelines;
//import net.minecraft.client.renderer.SubmitNodeCollector;
//import net.minecraft.client.renderer.rendertype.LayeringTransform;
//import net.minecraft.client.renderer.rendertype.OutputTarget;
//import net.minecraft.client.renderer.rendertype.RenderSetup;
//import net.minecraft.client.renderer.rendertype.RenderType;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.phys.Vec3;
//import net.minecraft.world.phys.shapes.Shapes;
//import org.joml.Vector3f;
//import pro.mikey.xray.XRay;
//
//import java.util.*;
//
//public class OutlineCustomGeometryRenderer {
//    public static final RenderPipeline NO_DEPTH_LINES_PIPELINE = RenderPipeline.builder(
//            RenderPipelines.MATRICES_FOG_SNIPPET)
//                .withVertexShader("core/rendertype_lines")
//                .withFragmentShader("core/rendertype_lines")
//                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
//                .withCull(false)
//                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
//                .withPrimitiveTopology(PrimitiveTopology.LINES)
//                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)
//            )
//            .withLocation(XRay.id("pipeline/lines_no_depth"))
//            .build();
//
//    // TODO: Tell shaders this acts like the line render type as it uses the same shaders.
//    public static final RenderType NO_DEPTH_LINES_TYPE = RenderType.create("lines_no_depth", RenderSetup.builder(NO_DEPTH_LINES_PIPELINE)
//            .setLayeringTransform(LayeringTransform.NO_LAYERING)
//            .setOutputTarget(OutputTarget.OUTLINE_TARGET)
//            .createRenderSetup()
//    );
//
//    public static final OutlineCustomGeometryRenderer INSTANCE = new OutlineCustomGeometryRenderer();
//
//    private final Map<ChunkPos, List<CachedVertex>> vertexCache = new HashMap<>();
//    private final Set<ChunkPos> chunksToRefresh = Collections.synchronizedSet(new HashSet<>());
//
//    public void render(PoseStack poseStack) {
//        if (!shouldRender()) {
//            return;
//        }
//
//        Vec3 playerPos = Minecraft.getInstance().gameRenderer.mainCamera().position().reverse();
//
//        poseStack.pushPose();
//        poseStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());
//
//        _render(poseStack, Minecraft.getInstance().gameRenderer.renderBuffers().bufferSource().getBuffer(NO_DEPTH_LINES_TYPE));
//
//        poseStack.popPose();
//    }
//
//    public void _render(PoseStack stack, VertexConsumer buffer) {
//        flushInvalidatedChunks();
//
//        var entries = new ArrayList<>(ScanController.INSTANCE.syncRenderList.entrySet());
//
//        PoseStack.Pose pose = stack.last();
//        for (var chunkWithBlockData : entries) {
//            var chunkPos = chunkWithBlockData.getKey();
//            var blocksWithProps = chunkWithBlockData.getValue();
//            if (blocksWithProps.isEmpty()) continue;
//
//            List<CachedVertex> cached = vertexCache.computeIfAbsent(chunkPos, pos -> buildChunkVertices(blocksWithProps));
//
//            for (CachedVertex v : cached) {
//                buffer.addVertex(pose, v.x(), v.y(), v.z())
//                        .setColor(v.color())
//                        .setNormal(pose, v.nx(), v.ny(), v.nz())
//                        .setLineWidth(v.width());
//            }
//        }
//    }
//
//    private List<CachedVertex> buildChunkVertices(Set<OutlineRenderTarget> blocksWithProps) {
//        List<CachedVertex> result = new ArrayList<>();
//        Vector3f normal = new Vector3f();
//
//        for (var blockProps : blocksWithProps) {
//            if (blockProps == null) continue;
//            final int x = blockProps.x(), y = blockProps.y(), z = blockProps.z();
//            int color = blockProps.color();
//            float width = 2.0f;
//
//            Shapes.block().forAllEdges((x1, y1, z1, x2, y2, z2) -> {
//                normal.set((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
//                result.add(new CachedVertex((float)(x + x1), (float)(y + y1), (float)(z + z1), color, normal.x(), normal.y(), normal.z(), width));
//                result.add(new CachedVertex((float)(x + x2), (float)(y + y2), (float)(z + z2), color, normal.x(), normal.y(), normal.z(), width));
//            });
//        }
//        return result;
//    }
//
//    public boolean shouldRender() {
//        if (!ScanController.INSTANCE.isXRayActive() || Minecraft.getInstance().player == null) {
//            return false;
//        }
//
//        return !ScanController.INSTANCE.syncRenderList.isEmpty();
//    }
//
//    private void flushInvalidatedChunks() {
//        if (!chunksToRefresh.isEmpty()) {
//            // Clear the vertex buffers for the chunks that need to be refreshed
//            for (ChunkPos pos : chunksToRefresh) {
//                vertexCache.remove(pos);
//            }
//
//            chunksToRefresh.clear();
//        }
//    }
//
//    public void clearCache() {
//        vertexCache.clear();
//        chunksToRefresh.clear();
//    }
//
//    public void clearCacheFor(List<ChunkPos> removedChunks) {
//        if (removedChunks.isEmpty()) {
//            return;
//        }
//
//        chunksToRefresh.addAll(removedChunks);
//    }
//
//    public void refreshVBOForChunk(ChunkPos pos) {
//        chunksToRefresh.add(pos);
//    }
//
//    private record CachedVertex(float x, float y, float z, int color, float nx, float ny, float nz, float width) {}
//}
