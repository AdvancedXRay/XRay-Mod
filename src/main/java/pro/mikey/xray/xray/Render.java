package pro.mikey.xray.xray;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import pro.mikey.xray.Configuration;
import pro.mikey.xray.utils.RenderBlockProps;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Render
{
    public static List<RenderBlockProps> syncRenderList = Collections.synchronizedList( new ArrayList<>() ); // this is accessed by threads

    private static final int GL_FRONT_AND_BACK = 1032;
    private static final int GL_LINE = 6913;
    private static final int GL_FILL = 6914;
    private static final int GL_LINES = 1;

	static void renderBlocks(RenderWorldLastEvent event) {
        Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        PoseStack stack = event.getMatrixStack();
        stack.pushPose();
        stack.translate(-view.x, -view.y, -view.z); // translate

//        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder buffer = tessellator.getBuilder();
//        Profile.BLOCKS.apply(); // Sets GL state for block drawing
//        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
//        RenderSystem.applyModelViewMatrix();

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.LINES);

        syncRenderList.forEach(blockProps -> {
            if (blockProps == null) {
                return;
            }

//            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR );
//            stack.pushPose();
//            stack.translate(blockProps.getPos().getX(), blockProps.getPos().getY(), blockProps.getPos().getZ());
            final float red = (blockProps.getColor() >> 16 & 0xff) / 255f;
            final float green = (blockProps.getColor() >> 8 & 0xff) / 255f;
            final float blue = (blockProps.getColor() & 0xff) / 255f;

            LevelRenderer.renderLineBox(stack, buffer, blockProps.getPos().getX(), blockProps.getPos().getY(), blockProps.getPos().getZ(), blockProps.getPos().getX() + 1, blockProps.getPos().getY() + 1, blockProps.getPos().getZ() + 1, red, green, blue, 1F);
//            renderBlock(stack, buffer, blockProps, 1);
//            stack.popPose();
//            tessellator.end();
        });

        bufferSource.endBatch(RenderTypes.LINES);

//        Profile.BLOCKS.clean();
//        RenderSystem.popMatrix();
        stack.popPose();
//        RenderSystem.applyModelViewMatrix();
//        RenderSystem.applyModelViewMatrix();
	}

	private static void renderBlock(PoseStack stack, VertexConsumer buffer, RenderBlockProps props, float opacity) {
        final float red = (props.getColor() >> 16 & 0xff) / 255f;
        final float green = (props.getColor() >> 8 & 0xff) / 255f;
        final float blue = (props.getColor() & 0xff) / 255f;

        Matrix4f matrix4f = stack.last().pose();
        buffer.vertex(matrix4f, 0, 1, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 1, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 1, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 1, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 1, 0).color(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.vertex(matrix4f, 1, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 0, 0).color(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.vertex(matrix4f, 1, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 1, 1).color(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.vertex(matrix4f, 1, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 1, 1, 0).color(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.vertex(matrix4f, 0, 0, 1).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 1, 1).color(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.vertex(matrix4f, 0, 0, 0).color(red, green, blue, opacity).endVertex();
        buffer.vertex(matrix4f, 0, 1, 0).color(red, green, blue, opacity).endVertex();
    }

    /**
     * OpenGL Profiles used for rendering blocks and entities
     */
    private enum Profile
    {
        BLOCKS {
            @Override
            public void apply()
            {
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask( false );
//                RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_LINE );
//                RenderSystem.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
//                RenderSystem.enableBlend();
                RenderSystem.lineWidth( (float) Configuration.general.outlineThickness.get().doubleValue() );
            }

            @Override
            public void clean()
            {
//                RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_FILL );
//                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask( true );
                RenderSystem.enableTexture();
            }
        },
        // TODO:
        ENTITIES {
            @Override
            public void apply()
            {}

            @Override
            public void clean()
            {}
        };

        private Profile() {}
        public abstract void apply();
        public abstract void clean();
    }
}
