package com.xray.xray;

import com.xray.XRay;
import com.xray.reference.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class Events
{
	@SubscribeEvent
	public void pickupItem( BlockEvent.BreakEvent event )
	{
		RenderEnqueue.checkBlock( event.getPos(), event.getState(), false);
	}

	@SubscribeEvent
	public void placeItem( BlockEvent.EntityPlaceEvent event )
	{
		RenderEnqueue.checkBlock( event.getPos(), event.getState(), true);
	}

	@SubscribeEvent
	public void chunkLoad( ChunkEvent.Load event )
	{
		Controller.requestBlockFinder( true );
	}

	@SubscribeEvent
	public void tickEnd( TickEvent.ClientTickEvent event )
	{
		if ( event.phase == TickEvent.Phase.END )
		{
			Controller.requestBlockFinder( false );
		}
	}

	@SubscribeEvent
	public void onWorldRenderLast( RenderWorldLastEvent event ) // Called when drawing the world.
	{
		if ( Controller.drawOres() )
		{
			float f = event.getPartialTicks();

			// this is a world pos of the player
			Render.drawOres(
				(float)XRay.mc.player.prevPosX + ( (float)XRay.mc.player.posX - (float)XRay.mc.player.prevPosX ) * f,
				(float)XRay.mc.player.prevPosY + ( (float)XRay.mc.player.posY - (float)XRay.mc.player.prevPosY ) * f,
				(float)XRay.mc.player.prevPosZ + ( (float)XRay.mc.player.posZ - (float)XRay.mc.player.prevPosZ ) * f
			);
		}
	}
}
