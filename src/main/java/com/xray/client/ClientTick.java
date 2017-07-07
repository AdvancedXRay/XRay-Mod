package com.xray.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import com.xray.common.reference.BlockInfo;
import com.xray.common.FgtXRay;
import com.xray.common.reference.OreInfo;

public class ClientTick implements Runnable
{
    private static final Minecraft mc = Minecraft.getMinecraft();
	private static long nextTimeMs = System.currentTimeMillis();
	private static final int delayMs = 200;
	private Thread thread = null;

	@SubscribeEvent
	public void tickEnd( TickEvent.ClientTickEvent event )
	{
		if ( (event.phase == TickEvent.Phase.END) && (mc.player != null) )
		{
			FgtXRay.localPlyX = MathHelper.floor( mc.player.posX );
			FgtXRay.localPlyY = MathHelper.floor( mc.player.posY );
			FgtXRay.localPlyZ = MathHelper.floor( mc.player.posZ );
            FgtXRay.localPlyXPrev = MathHelper.floor( mc.player.prevPosX );
            FgtXRay.localPlyZPrev = MathHelper.floor( mc.player.prevPosZ );

			if( FgtXRay.drawOres && ((this.thread == null) || !this.thread.isAlive()) &&
			( (mc.world != null) && (mc.player != null) ) ) // If we're in a world and want to start drawing create the thread.
			{
				this.thread = new Thread( this );
				this.thread.setDaemon( false );
				this.thread.setPriority( Thread.MAX_PRIORITY );
				this.thread.start();
			}
		}
	}


	@Override
	public void run() // Our thread code for finding ores near the player.
	{
		try
		{
			while( !this.thread.isInterrupted() ) // Check the internal interrupt flag. Exit thread if set.
			{
				if( !blockFinder( false ) )
                    this.thread.interrupt(); // Kill the thread if we turn off xray or the player/world object becomes null.
			}
			//System.out.println(" --- Thread Exited Cleanly! ");
			this.thread = null;
		}
		catch ( Exception exc )
		{
			System.out.println(" ClientTick Thread Interrupted!!! " + exc.toString() ); // This shouldnt get called.
		}
	}

	static boolean blockFinder(boolean ForceAdd) {
		if (FgtXRay.drawOres && !OresSearch.searchList.isEmpty() && mc.world != null && mc.player != null)
		{
			if ( nextTimeMs > System.currentTimeMillis() ) // Delay to avoid spamming ore updates.
				return true;

			int px = FgtXRay.localPlyX;
			int py = FgtXRay.localPlyY;
			int pz = FgtXRay.localPlyZ;
			if( ( ( px == FgtXRay.localPlyXPrev && pz == FgtXRay.localPlyZPrev ) && RenderTick.ores.size() > 0 ) && !ForceAdd )
				return true; // Skip the check if the player hasn't moved

			List<BlockInfo> temp = new ArrayList<>();
			int radius = FgtXRay.distNumbers[ FgtXRay.distIndex ]; // Get the radius around the player to search.

			for (int y = Math.max( 0, py - 96 ); y < py + 32; y++) // Check the y axis. from 0 or the players y-96 to the players y + 32
			{
				for (int x = px - radius; x < px + radius; x++) // Iterate the x axis around the player in radius.
				{
					for (int z = pz - radius; z < pz + radius; z++) // z axis.
					{
						IBlockState state = mc.world.getBlockState(  new BlockPos(x, y, z) );

						int id = Block.getIdFromBlock( state.getBlock() );
						int meta = state.getBlock().getMetaFromState(state);

						if( state.getBlock().hasTileEntity( state ) )
							meta = 0;

						for( OreInfo ore : OresSearch.searchList ) // Now we're actually checking if the current x,y,z block is in our searchList.
						{
							if ( (ore.draw) && (id == ore.id) && (meta == ore.meta) ) // Dont check meta if its -1 (custom)
							{
								temp.add( new BlockInfo( x, y, z, ore.color) ); // Add this block to the temp list
								break; // Found a match, move on to the next block.
							}
						}
					}
				}
			}
			RenderTick.ores.clear();
			RenderTick.ores.addAll(temp); // Add all our found blocks to the RenderTick.ores list. To be use by RenderTick when drawing.
			nextTimeMs = System.currentTimeMillis() + delayMs; // Update the delay.
		}
		else
		    return false;

		return true;
	}
}
