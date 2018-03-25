package com.xray.client;

import com.xray.client.render.XrayRenderer;
import com.xray.common.XRay;
import com.xray.common.reference.BlockInfo;
import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Just a helper class to move away events from ClientTick
 * (which is instantiated often due to threads)
 */
public class XRayEventHandler
{
	private static final Minecraft mc = Minecraft.getMinecraft();

	@SubscribeEvent
	public void pickupItem( BlockEvent.BreakEvent event )
	{
		checkBlock( event.getPos(), event.getState(), false);
	}

	@SubscribeEvent
	public void placeItem( BlockEvent.PlaceEvent event )
	{
		checkBlock( event.getPos(), event.getState(), true);
	}

	private static void checkBlock( BlockPos pos, IBlockState state, boolean add )
	{
		if ( !XRay.drawOres() ) return; // just pass

		// Let's start with getting data (id, meta)
		Block block = state.getBlock();
		int id = Block.getIdFromBlock( block );
		int meta = block.getMetaFromState( state );

		// Let's see if the block to check is an ore we monitor
		OreInfo key = new OreInfo(id, meta);
		OreInfo ore = null;
		for ( OreInfo o : XRay.searchList ) {
			if ( key.equals(o) && o.draw )
			{
				ore = o;
				break;
			}
		}
		if ( ore != null ) // it's a block we are monitoring
		{
			if ( add )	// the block was added to the world, let's add it to the drawing buffer
				XrayRenderer.ores.add( new BlockInfo(pos, ore.color) );
			else		// it was removed from the world, let's remove it from the buffer as well
				XrayRenderer.ores.remove( new BlockInfo(pos, null) );
		}
	}
}
