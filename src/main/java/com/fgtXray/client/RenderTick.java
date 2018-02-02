package com.fgtXray.client;

/* Props goto CJB for the render functions and maths.
 * http://twitter.com/CJBMods
 * I pretty much copied this from his decompiled MoreInfo mod and bitbucket repo.
 */

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import com.fgtXray.reference.BlockInfo;
import com.fgtXray.FgtXRay;

import org.lwjgl.opengl.GL11;

public class RenderTick
{
	private final Minecraft mc = Minecraft.getMinecraft();
	public static List<BlockInfo> ores = new ArrayList();
	World world = mc.theWorld;

	@SubscribeEvent
	public void onRenderEvent( RenderWorldLastEvent event ) // Called when drawing the world.
	{
		if ( mc.theWorld != null && FgtXRay.drawOres )
		{
			float f = event.partialTicks; // I still dont know what this is for.
			float px = (float)mc.thePlayer.posX;
			float py = (float)mc.thePlayer.posY;
			float pz = (float)mc.thePlayer.posZ;
			float mx = (float)mc.thePlayer.prevPosX;
			float my = (float)mc.thePlayer.prevPosY;
			float mz = (float)mc.thePlayer.prevPosZ;
			float dx = mx + ( px - mx ) * f;
			float dy = my + ( py - my ) * f;
			float dz = mz + ( pz - mz ) * f;
			drawOres( dx, dy, dz ); // this is a world pos of the player
		}
	}
	
	private void drawOres( float px, float py, float pz )
	{
		int bx, by, bz;
		
		GL11.glDisable( GL11.GL_TEXTURE_2D );
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		GL11.glDisable( GL11.GL_CULL_FACE );
		GL11.glDepthMask(false);
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GL11.glLineWidth( 1f );
		Tessellator tes = Tessellator.instance;
		
		List<BlockInfo> temp = new ArrayList();
		temp.addAll(this.ores);	// If we dont make a copy then the thread in ClientTick will ConcurrentModificationException.
		
		for ( BlockInfo b : temp )
		{
			bx = b.x;
			by = b.y;
			bz = b.z;
			float f = 0.0f;
			float f1 = 1.0f;
			
			tes.startDrawing( GL11.GL_LINES );
			tes.setColorRGBA_I(b.color, 255);
			tes.setBrightness( 200 );
			
			// Bottom
			tes.addVertex( bx-px + f, by-py + f1, bz-pz + f); tes.addVertex( bx-px + f1,  by-py + f1, bz-pz + f);
			tes.addVertex( bx-px + f1,  by-py + f1, bz-pz + f); tes.addVertex( bx-px + f1,  by-py + f1, bz-pz + f1); 
			tes.addVertex( bx-px + f1,  by-py + f1, bz-pz + f1); tes.addVertex( bx-px + f,  by-py + f1,  bz-pz + f1);
			tes.addVertex( bx-px + f,  by-py + f1,  bz-pz + f1); tes.addVertex( bx-px + f,  by-py + f1,  bz-pz + f);
	
			// Top
			tes.addVertex( bx-px + f1,  by-py + f,  bz-pz + f); tes.addVertex( bx-px + f1,  by-py + f,  bz-pz + f1);
			tes.addVertex( bx-px + f1,  by-py + f,  bz-pz + f1); tes.addVertex( bx-px + f,  by-py + f,  bz-pz + f1);
			tes.addVertex( bx-px + f,  by-py + f,  bz-pz + f1); tes.addVertex( bx-px + f,  by-py + f,  bz-pz + f);
			tes.addVertex( bx-px + f,  by-py + f,  bz-pz + f); tes.addVertex( bx-px + f1,  by-py + f,  bz-pz + f);
			
			// Corners
			tes.addVertex( bx-px + f1,  by-py + f,  bz-pz + f1); tes.addVertex( bx-px + f1,  by-py + f1,  bz-pz + f1); // Top Left
			tes.addVertex( bx-px + f1,  by-py + f,  bz-pz + f); tes.addVertex( bx-px + f1,  by-py + f1,  bz-pz + f); // Bottom Left
			tes.addVertex( bx-px + f,  by-py + f,  bz-pz + f1); tes.addVertex( bx-px + f,  by-py + f1,  bz-pz + f1); // Top Right
			tes.addVertex( bx-px + f,  by-py + f,  bz-pz + f); tes.addVertex( bx-px + f,  by-py + f1,  bz-pz + f); // Bottom Right
			
			tes.draw();
		}
		
		GL11.glDepthMask(true);
		GL11.glDisable( GL11.GL_BLEND );
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_CULL_FACE );
	}
}
