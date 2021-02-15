package pro.mikey.xray.xray;

import pro.mikey.xray.XRay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XRay.MOD_ID, value = Dist.CLIENT)
public class Events
{
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
		if ( Controller.isXRayActive() && Minecraft.getInstance().player != null )
		{
			// this is a world pos of the player
			Render.renderBlocks(event);
		}
	}
}
