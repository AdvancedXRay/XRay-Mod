package fgtXray.client;

/* Props goto CJB for the render functions and maths.
 * http://twitter.com/CJBMods
 * */

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.NetworkRegistry;
import fgtXray.BlockInfo;
import fgtXray.FgtXRay;

import org.lwjgl.opengl.GL11;
import net.minecraftforge.event.ForgeSubscribe;

public class RenderTick implements ITickHandler {
	private final Minecraft mc = Minecraft.getMinecraft();
	public static List<BlockInfo> ores = new ArrayList();
	World world = mc.theWorld;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@ForgeSubscribe
	public void onRenderEvent( RenderWorldLastEvent event ){
		if ( mc.theWorld != null && FgtXRay.drawOres ) {
			float f = event.partialTicks;
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
	
	private void drawOres( float px, float py, float pz ){// blockX - playerPos + offset ...
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
		
		for ( BlockInfo b : temp ){
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
	
	/*private void renderText( String str ) {
		ScaledResolution res = new ScaledResolution( this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
		FontRenderer fontRend = mc.fontRenderer;
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();
		mc.entityRenderer.setupOverlayRendering();
		fontRend.drawString( str, 1, 140, 0xFFFFFF);
	}*/
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		/*if ( mc.theWorld != null && FindIt.drawText ) {
			renderText("Test");
		}*/
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return "rendertick";
	}

}
