package com.xray.xray;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.xray.Configuration;
import com.xray.XRay;
import com.xray.utils.RenderBlockProps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.debug.ChunkBorderDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.tileentity.PistonTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.realms.Tezzelator;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.animation.TileEntityRendererAnimation;

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

	static void renderBlocks(RenderWorldLastEvent event, float playerX, float playerY, float playerZ) {
        MatrixStack stack = event.getMatrixStack();
        stack.func_227861_a_(-playerX, -playerY - (XRay.mc.player.getEyeHeight()), -playerZ); // translate

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(stack.func_227866_c_().func_227870_a_());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Profile.BLOCKS.apply(); // Sets GL state for block drawing

        syncRenderList.forEach(blockProps -> {
            buffer.begin( GL_LINES, DefaultVertexFormats.POSITION_COLOR );
            renderBlockBounding( buffer, blockProps, 1);
            tessellator.draw();
        } );

        Profile.BLOCKS.clean();
        RenderSystem.popMatrix();
	}

    private static void renderBlockBounding(BufferBuilder buffer, RenderBlockProps b, int opacity) {
        if( b == null )
            return;

        final float size = 1.0f;

        int red = b.getColor().getRed();
        int green = b.getColor().getGreen();
        int blue = b.getColor().getBlue();

        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();

        // TOP
        // func_225582_a_ = POS
        // func_227885_a_ = COLOR
        buffer.func_225582_a_(x, y + size, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y + size, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y + size, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y + size, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y + size, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y + size, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y + size, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y + size, z).func_227885_a_(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.func_225582_a_(x + size, y, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y, z).func_227885_a_(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.func_225582_a_(x + size, y, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y + size, z + size).func_227885_a_(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.func_225582_a_(x + size, y, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x + size, y + size, z).func_227885_a_(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.func_225582_a_(x, y, z + size).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y + size, z + size).func_227885_a_(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.func_225582_a_(x, y, z).func_227885_a_(red, green, blue, opacity).endVertex();
        buffer.func_225582_a_(x, y + size, z).func_227885_a_(red, green, blue, opacity).endVertex();
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
                RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_LINE );
                RenderSystem.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
                RenderSystem.enableBlend();
                RenderSystem.lineWidth( (float) Configuration.general.outlineThickness.get().doubleValue() );
            }

            @Override
            public void clean()
            {
                RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_FILL );
                RenderSystem.disableBlend();
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
