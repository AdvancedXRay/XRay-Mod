package com.xray.client.render;

import com.xray.common.XRay;
import com.xray.common.reference.BlockInfo;
import com.xray.common.utils.Utils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Please refactor of all this file :heart:
public class XrayRenderer
{
	public static List<BlockInfo> ores = Collections.synchronizedList( new ArrayList<>() ); // this is accessed by threads

	// Opacity is weird as all hell. We save it as 0 - 1 / off -> full so we need to convert that value
	private static float opacity = ( XRay.outlineOpacity > 1 ? 255 : XRay.outlineOpacity < 0 ? 1 : ( XRay.outlineOpacity  * 255 ) ); // Pretty simple :D

	public static void drawOres( float playerX, float playerY, float playerZ )
	{
		GL11.glDisable( GL11.GL_TEXTURE_2D );
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		GL11.glDisable( GL11.GL_CULL_FACE );
		GL11.glDepthMask(false);
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GL11.glLineWidth( XRay.outlineThickness );

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		ArrayList<BlockInfo> temp = new ArrayList<>();
		temp.addAll(ores);

		for ( BlockInfo b : temp )
		{
		    if( b == null )
		        continue;

			Utils.renderBlockBounding(
				tessellator,
				buffer,
				b.getX()-playerX,
				b.getY()-playerY,
				b.getZ()-playerZ,
				b.color[0],
				b.color[1],
				b.color[2],
				(int)opacity,
				true
			);

		}

		GL11.glDepthMask(true);
		GL11.glDisable( GL11.GL_BLEND );
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_CULL_FACE );
	}
}
