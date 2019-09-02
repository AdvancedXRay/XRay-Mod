package com.xray.xray;

import com.mojang.blaze3d.platform.GlStateManager;
import com.xray.Configuration;
import com.xray.XRay;
import com.xray.reference.block.BlockInfo;
import com.xray.utils.Utils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Render
{
    public static List<BlockInfo> ores = Collections.synchronizedList( new ArrayList<>() ); // this is accessed by threads

    private static final int GL_FRONT_AND_BACK = 1032;
    private static final int GL_LINE = 6913;
    private static final int GL_FILL = 6914;
    private static final int GL_LINES = 1;

	public static void drawOres( float playerX, float playerY, float playerZ )
	{
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Profile.BLOCKS.apply(); // Sets GL state for block drawing

        buffer.setTranslation( -playerX, -playerY - (XRay.mc.player.getEyeHeight()), -playerZ );

        ores.forEach( b -> {
            buffer.begin( GL_LINES, DefaultVertexFormats.POSITION_COLOR );
            Utils.renderBlockBounding( buffer, b, (int) b.alpha );
            tessellator.draw();
        } );

        buffer.setTranslation( 0, 0, 0 );

        Profile.BLOCKS.clean();
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
                GlStateManager.disableTexture();
                GlStateManager.disableDepthTest();
                GlStateManager.depthMask( false );
                GlStateManager.polygonMode( GL_FRONT_AND_BACK, GL_LINE );
                GlStateManager.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
                GlStateManager.enableBlend();
                GlStateManager.lineWidth( (float) Configuration.outlineThickness );
            }

            @Override
            public void clean()
            {
                GlStateManager.polygonMode( GL_FRONT_AND_BACK, GL_FILL );
                GlStateManager.disableBlend();
                GlStateManager.enableDepthTest();
                GlStateManager.depthMask( true );
                GlStateManager.enableTexture();
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
