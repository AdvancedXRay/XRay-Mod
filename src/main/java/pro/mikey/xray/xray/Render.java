package pro.mikey.xray.xray;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import pro.mikey.xray.Configuration;
import pro.mikey.xray.utils.RenderBlockProps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Render {
    public static List<RenderBlockProps> syncRenderList = Collections.synchronizedList(new ArrayList<>()); // this is accessed by threads
    public static List<LivingEntity> entityList = Collections.synchronizedList(new ArrayList<>());

    private static final int GL_FRONT_AND_BACK = 1032;
    private static final int GL_LINE = 6913;
    private static final int GL_FILL = 6914;
    private static final int GL_LINES = 1;

    static void renderBlocks(RenderWorldLastEvent event) {
        Vector3d view = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();

        if (!Render.entityList.isEmpty()) {
            MatrixStack stack = event.getMatrixStack();
            stack.push();
            stack.translate(-view.x, -view.y, -view.z); // translate
            RenderSystem.disableDepthTest();

            IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            IVertexBuilder buffers = bufferSource.getBuffer(RenderType.LINES);

            Render.entityList.forEach(e -> {
                stack.push();
                double d0 = MathHelper.lerp(event.getPartialTicks(), e.lastTickPosX, e.getPosX());
                double d1 = MathHelper.lerp(event.getPartialTicks(), e.lastTickPosY, e.getPosY());
                double d2 = MathHelper.lerp(event.getPartialTicks(), e.lastTickPosZ, e.getPosZ());
                stack.translate(d0, d1, d2);
                AxisAlignedBB axisalignedbb = e.getBoundingBox().offset(-e.getPosX(), -e.getPosY(), -e.getPosZ());
                WorldRenderer.drawBoundingBox(stack, buffers, axisalignedbb, 1F, 1F, 1F, 1.0F);
                //                WorldRenderer.drawBoundingBox(stack, buffers, e.getRenderBoundingBox().offset(-e.getPosX(), -e.getPosY(), -e.getPosZ()), 255, 255, 0, 255);
                stack.pop();
            });
            RenderSystem.enableDepthTest();
            stack.pop();
            bufferSource.finish();
        }

        RenderSystem.pushMatrix();
        RenderSystem.translated(-view.x, -view.y, -view.z);
        //        RenderSystem.multMatrix(stack.getLast().getMatrix());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Profile.BLOCKS.apply(); // Sets GL state for block drawing

        syncRenderList.forEach(blockProps -> {
            if (blockProps == null) {
                return;
            }

            RenderSystem.pushMatrix();
            RenderSystem.translated(blockProps.getPos().getX(), blockProps.getPos().getY(), blockProps.getPos().getZ());
            buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            renderBlock(buffer, blockProps, 1);
            tessellator.draw();
            RenderSystem.popMatrix();
        });

        Profile.BLOCKS.clean();
        RenderSystem.popMatrix();
    }

    private static void renderBlock(IVertexBuilder buffer, RenderBlockProps props, float opacity) {
        final float red = (props.getColor() >> 16 & 0xff) / 255f;
        final float green = (props.getColor() >> 8 & 0xff) / 255f;
        final float blue = (props.getColor() & 0xff) / 255f;

        buffer.pos(0, 1, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 1, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 1, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 1, 0).color(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.pos(1, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 0, 0).color(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.pos(1, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 1, 1).color(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.pos(1, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(1, 1, 0).color(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.pos(0, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 1, 1).color(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.pos(0, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.pos(0, 1, 0).color(red, green, blue, opacity).endVertex();
    }

    /**
     * OpenGL Profiles used for rendering blocks and entities
     */
    private enum Profile {
        BLOCKS {
            @Override
            public void apply() {
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.polygonMode(GL_FRONT_AND_BACK, GL_LINE);
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.enableBlend();
                RenderSystem.lineWidth((float) Configuration.general.outlineThickness.get().doubleValue());
            }

            @Override
            public void clean() {
                RenderSystem.polygonMode(GL_FRONT_AND_BACK, GL_FILL);
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.enableTexture();
            }
        },
        // TODO:
        ENTITIES {
            @Override
            public void apply() {
            }

            @Override
            public void clean() {
            }
        };

        private Profile() {
        }

        public abstract void apply();

        public abstract void clean();
    }
}
