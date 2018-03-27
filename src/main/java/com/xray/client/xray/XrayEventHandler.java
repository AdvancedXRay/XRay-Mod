package com.xray.client.xray;

import com.xray.client.render.ClientTick;
import com.xray.client.render.XrayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class XrayEventHandler
{
	private static final Minecraft mc = Minecraft.getMinecraft();


	@SubscribeEvent
	public void pickupItem( BlockEvent.BreakEvent event )
	{
		ClientTick.checkBlock( event.getPos(), event.getState(), false);
	}

	@SubscribeEvent
	public void placeItem( BlockEvent.PlaceEvent event )
	{
		ClientTick.checkBlock( event.getPos(), event.getState(), true);
	}

	@SubscribeEvent
	public void chunkLoad( ChunkEvent.Load event )
	{
		XrayController.requestBlockFinder( true );
	}

	@SubscribeEvent
	public void tickEnd( TickEvent.ClientTickEvent event )
	{
		if ( event.phase == TickEvent.Phase.END )
		{
			XrayController.requestBlockFinder( false );
		}
	}

	@SubscribeEvent
	public void onWorldRenderLast( RenderWorldLastEvent event ) // Called when drawing the world.
	{
		if ( XrayController.drawOres() )
		{
			float f = event.getPartialTicks();

			// this is a world pos of the player
			XrayRenderer.drawOres(
				(float)mc.player.prevPosX + ( (float)mc.player.posX - (float)mc.player.prevPosX ) * f,
				(float)mc.player.prevPosY + ( (float)mc.player.posY - (float)mc.player.prevPosY ) * f,
				(float)mc.player.prevPosZ + ( (float)mc.player.posZ - (float)mc.player.prevPosZ ) * f
			);
		}
	}
}
