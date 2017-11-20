package com.xray.client.render;

import com.xray.common.XRay;
import com.xray.common.reference.BlockInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// TODO: Please refactor of all this file :heart:
public class RenderTick
{
	private final Minecraft mc = Minecraft.getMinecraft();
	public static List<BlockInfo> ores = new ArrayList<>();

	// Opacity is weird as all hell. We save it as 0 - 1 / off -> full so we need to convert that value
	private static float opacity = ( XRay.outlineOpacity > 1 ? 255 : XRay.outlineOpacity < 0 ? 1 : ( XRay.outlineOpacity  * 255 ) ); // Pretty simple :D

	@SubscribeEvent
	public void onWorldRenderLast( RenderWorldLastEvent event ) // Called when drawing the world.
	{
		if ( mc.world != null && XRay.drawOres )
		{
			float f = event.getPartialTicks();
			float px = (float)mc.player.posX;
			float py = (float)mc.player.posY;
			float pz = (float)mc.player.posZ;
			float mx = (float)mc.player.prevPosX;
			float my = (float)mc.player.prevPosY;
			float mz = (float)mc.player.prevPosZ;
			float dx = mx + ( px - mx ) * f;
			float dy = my + ( py - my ) * f;
			float dz = mz + ( pz - mz ) * f;
			drawOres( dx, dy, dz ); // this is a world pos of the player
		}
	}

    @SubscribeEvent
    public void pickupItem( BlockEvent.BreakEvent event ) {
        if ( mc.world != null && XRay.drawOres )
        {
            ClientTick.blockFinder( true );
        }
    }

    @SubscribeEvent
    public void placeItem(BlockEvent.PlaceEvent event ) {
        if ( mc.world != null && XRay.drawOres )
        {
            ClientTick.blockFinder( true );
        }
    }

	private void drawOres( float px, float py, float pz )
	{
		int bx, by, bz;
		float f = 0.0f;
		float f1 = 1.0f;

		int opac = (int)opacity;

		GL11.glDisable( GL11.GL_TEXTURE_2D );
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		GL11.glDisable( GL11.GL_CULL_FACE );
		GL11.glDepthMask(false);
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GL11.glLineWidth( XRay.outlineThickness );

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellate = tessellator.getBuffer();

		ArrayList<BlockInfo> temp = new ArrayList<>();
		temp.addAll(ores);
		
		for ( BlockInfo b : temp )
		{
		    if( b == null )
		        continue;

			bx = b.x;
			by = b.y;
			bz = b.z;
			int red =  b.color[0], green =  b.color[1], blue =  b.color[2];

			tessellate.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

			// TOP
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, opac).endVertex();

			// BOTTOM
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, opac).endVertex();

			// Edge 1
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, opac).endVertex();

			// Edge 2
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, opac).endVertex();

			// Edge 3
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, opac).endVertex();

			// Edge 4
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, opac).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, opac).endVertex();

			tessellator.draw();
		}
		
		GL11.glDepthMask(true);
		GL11.glDisable( GL11.GL_BLEND );
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_CULL_FACE );
	}
}
