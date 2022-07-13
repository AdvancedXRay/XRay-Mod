package pro.mikey.xray.xray;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pro.mikey.xray.XRay;

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
	public static void tickEnd( TickEvent.ClientTickEvent event ) {
		if ( event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null && Minecraft.getInstance().level != null ) {
			Controller.requestBlockFinder( false );
		}
	}

	@SubscribeEvent
	public static void onWorldRenderLast( RenderLevelStageEvent event ) // Called when drawing the world.
	{
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			return;
		}

		if ( Controller.isXRayActive() && Minecraft.getInstance().player != null )
		{
			// this is a world pos of the player
			Render.renderBlocks(event);
		}
	}
}
