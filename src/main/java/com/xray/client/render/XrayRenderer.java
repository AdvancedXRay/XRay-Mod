package com.xray.client.render;

import com.xray.common.XRay;
import com.xray.common.reference.BlockInfo;
import com.xray.common.utils.Utils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

// TODO: Please refactor of all this file :heart:
public class XrayRenderer
{
    public static List<BlockInfo> ores = Collections.synchronizedList( new ArrayList<>() ); // this is accessed by threads
    // Opacity is weird as all hell. We save it as 0 - 1 / off -> full so we need to convert that value
    private final static int opacity = XRay.outlineOpacity >= 1 ? 255 : XRay.outlineOpacity <= 0 ? 0 : (int) (XRay.outlineOpacity * 255); // Pretty simple :D

    public static final int GL_FRONT_AND_BACK = 1032;
    public static final int GL_LINE = 6913;
    public static final int GL_FILL = 6914;
    public static final int GL_LINES = 1;

	public static void drawOres( float playerX, float playerY, float playerZ )
	{
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Profile.BLOCKS.apply(); // Sets GL state for block drawing

        BlockInfo[] temp = new BlockInfo[0];
        ores.toArray(temp);

        buffer.setTranslation( -playerX, -playerY, -playerZ );
        for ( BlockInfo b : temp )
        {
            buffer.begin( GL_LINES, DefaultVertexFormats.POSITION_COLOR );
            {
                Utils.renderBlockBounding( buffer, b, opacity );
            }
            tessellator.draw();
        }
        buffer.setTranslation( 0, 0, 0 );

        Profile.BLOCKS.clean();
	}

    /**
     * OpenGL Profiles used for rendering blocks and entities
     */
    private static enum Profile
    {
        BLOCKS {
            @Override
            public void apply()
            {
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.depthMask( false );
                GlStateManager.glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
                GlStateManager.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
                GlStateManager.enableBlend();
                GlStateManager.glLineWidth( XRay.outlineThickness );
            }

            @Override
            public void clean()
            {
                GlStateManager.glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();
                GlStateManager.depthMask( true );
                GlStateManager.enableTexture2D();
            }
        },
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
