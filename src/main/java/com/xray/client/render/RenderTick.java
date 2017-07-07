package com.xray.client.render;

/* Props goto CJB for the render functions and maths.
 * http://twitter.com/CJBMods
 * I pretty much copied this from his decompiled MoreInfo mod and bitbucket repo.
 */

import com.xray.common.FgtXRay;
import com.xray.common.reference.BlockInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RenderTick
{
	private final Minecraft mc = Minecraft.getMinecraft();
	public static List<BlockInfo> ores = new ArrayList<>();

	@SubscribeEvent
	public void onWorldRenderLast( RenderWorldLastEvent event ) // Called when drawing the world.
	{
		if ( mc.world != null && FgtXRay.drawOres )
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
        if ( mc.world != null && FgtXRay.drawOres )
        {
            ClientTick.blockFinder( true );
        }
    }

    @SubscribeEvent
    public void placeItem(BlockEvent.PlaceEvent event ) {
        if ( mc.world != null && FgtXRay.drawOres )
        {
            ClientTick.blockFinder( true );
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

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellate = tessellator.getBuffer();

		ArrayList<BlockInfo> temp = new ArrayList<>();
		temp.addAll(ores);
		
		for ( BlockInfo b : temp )
		{
			bx = b.x;
			by = b.y;
			bz = b.z;
			float f = 0.0f;
			float f1 = 1.0f;
			int red =  b.color[0], green =  b.color[1], blue =  b.color[2];

			tessellate.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();

			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();

			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();
			tessellate.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();

			tessellator.draw();
		}
		
		GL11.glDepthMask(true);
		GL11.glDisable( GL11.GL_BLEND );
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_CULL_FACE );
	}
}
