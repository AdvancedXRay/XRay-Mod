package com.xray.xray;

import com.xray.XRay;
import com.xray.utils.Reference;
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
	public static void pickupItem( BlockEvent.BreakEvent event )
	{
		RenderEnqueue.checkBlock( event.getPos(), event.getState(), false);
	}

	@SubscribeEvent
	public static void placeItem( BlockEvent.EntityPlaceEvent event )
	{
		RenderEnqueue.checkBlock( event.getPos(), event.getState(), true);
	}

	@SubscribeEvent
	public static void chunkLoad( ChunkEvent.Load event )
	{
		Controller.requestBlockFinder( true );
	}

	@SubscribeEvent
	public static void tickEnd( TickEvent.ClientTickEvent event )
	{
		if ( event.phase == TickEvent.Phase.END )
		{
			Controller.requestBlockFinder( false );
		}
	}

	@SubscribeEvent
	public static void onWorldRenderLast( RenderWorldLastEvent event ) // Called when drawing the world.
	{
		if ( Controller.isXRayActive() )
		{
			float f = event.getPartialTicks();

			// this is a world pos of the player
			Render.renderBlocks(
				(float)XRay.mc.player.prevPosX + ( (float)XRay.mc.player.posX - (float)XRay.mc.player.prevPosX ) * f,
				(float)XRay.mc.player.prevPosY + ( (float)XRay.mc.player.posY - (float)XRay.mc.player.prevPosY ) * f,
				(float)XRay.mc.player.prevPosZ + ( (float)XRay.mc.player.posZ - (float)XRay.mc.player.prevPosZ ) * f
			);
		}
	}
}
