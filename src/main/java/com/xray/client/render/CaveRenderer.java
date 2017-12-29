package com.xray.client.render;

import com.xray.common.XRay;
import com.xray.common.reference.BlockInfo;
import com.xray.common.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by MiKeY on 29/12/17.
 */
public class CaveRenderer {

    private static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void worldRenderTick(RenderWorldLastEvent event) {

        if ( mc.world == null || !XRay.drawCaves )
            return;

        float f = event.getPartialTicks();

        // this is a world pos of the player
        pulseRender(
                (float)mc.player.prevPosX + ( (float)mc.player.posX - (float)mc.player.prevPosX ) * f,
                (float)mc.player.prevPosY + ( (float)mc.player.posY - (float)mc.player.prevPosY ) * f,
                (float)mc.player.prevPosZ + ( (float)mc.player.posZ - (float)mc.player.prevPosZ ) * f
        );

       // XRay.drawCaves = !XRay.drawCaves;
    }

    private void pulseRender(float px, float py, float pz) {

        GL11.glDisable( GL11.GL_TEXTURE_2D );
        GL11.glDisable( GL11.GL_DEPTH_TEST );
        GL11.glDisable( GL11.GL_CULL_FACE );
        GL11.glDepthMask(false);
        GL11.glEnable( GL11.GL_BLEND );
        GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
        GL11.glLineWidth( XRay.outlineThickness );

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if( CaveFinder.caveBlocks.size() != 0 ) {
            for (BlockInfo block : CaveFinder.caveBlocks
                    ) {

                if (block == null)
                    continue;

                Utils.renderBlockBounding(
                        tessellator,
                        buffer,
                        block.x - px,
                        block.y - py,
                        block.z - pz,
                        0,
                        0,
                        255,
                        50,
                        false
                );
            }
        }

        System.out.println("Rendering");

        GL11.glDepthMask(true);
        GL11.glDisable( GL11.GL_BLEND );
        GL11.glEnable( GL11.GL_TEXTURE_2D );
        GL11.glEnable( GL11.GL_DEPTH_TEST );
        GL11.glEnable( GL11.GL_CULL_FACE );
    }

}
