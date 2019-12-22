package com.xray.xray;

import com.xray.XRay;
import com.xray.utils.Reference;
import com.xray.utils.TempMapping;
import net.minecraft.client.Minecraft;
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
	private static float renderPartialTicksPaused = 0f;
	private static boolean gameWasPauseFlag = false;

	@SubscribeEvent
	public static void pickupItem( BlockEvent.BreakEvent event ) {
		RenderEnqueue.checkBlock( event.getPos(), event.getState(), false);
	}

	@SubscribeEvent
	public static void placeItem( BlockEvent.EntityPlaceEvent event ) {
		RenderEnqueue.checkBlock( event.getPos(), event.getState(), true);
	}

	@SubscribeEvent
	public static void chunkLoad( ChunkEvent.Load event )
	{
		Controller.requestBlockFinder( true );
	}

	@SubscribeEvent
	public static void tickEnd( TickEvent.ClientTickEvent event ) {
		if ( event.phase == TickEvent.Phase.END ) {
			Controller.requestBlockFinder( false );
		}
	}

	@SubscribeEvent
	public static void onWorldRenderLast( RenderWorldLastEvent event ) // Called when drawing the world.
	{
		if ( Controller.isXRayActive() && XRay.mc.player != null )
		{
			// Replicate how the game naturally handles pause and partial ticks.
			// todo: remove when https://github.com/MinecraftForge/MinecraftForge/issues/6380 has been resolved
			if (XRay.mc.isGamePaused()) {
				if( !gameWasPauseFlag ) {
					renderPartialTicksPaused = XRay.mc.getRenderPartialTicks();
					System.out.println(renderPartialTicksPaused);
					gameWasPauseFlag = true;
				}
			}
			else
				gameWasPauseFlag = false;

			// Used to use the event but it's broken as of 30.0.15 ^
			float f = XRay.mc.isGamePaused() ? renderPartialTicksPaused : XRay.mc.getRenderPartialTicks();

			// this is a world pos of the player
			Render.renderBlocks(
					event,
				(float)XRay.mc.player.prevPosX + ( (float)TempMapping.Player.getPosX(XRay.mc.player) - (float)XRay.mc.player.prevPosX ) * f,
				(float)XRay.mc.player.prevPosY + ( (float)TempMapping.Player.getPosY(XRay.mc.player) - (float)XRay.mc.player.prevPosY ) * f,
				(float)XRay.mc.player.prevPosZ + ( (float)TempMapping.Player.getPosZ(XRay.mc.player) - (float)XRay.mc.player.prevPosZ ) * f
			);
		}
	}
}
