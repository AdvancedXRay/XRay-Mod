package com.xray.client.render;

import java.util.ArrayList;
import java.util.List;

import com.xray.common.XRay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import com.xray.common.reference.BlockInfo;
import com.xray.common.reference.OreInfo;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

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
			XRay.localPlyX = MathHelper.floor( mc.player.posX );
			XRay.localPlyY = MathHelper.floor( mc.player.posY );
			XRay.localPlyZ = MathHelper.floor( mc.player.posZ );
            XRay.localPlyXPrev = MathHelper.floor( mc.player.prevPosX );
            XRay.localPlyZPrev = MathHelper.floor( mc.player.prevPosZ );

			if( XRay.drawOres && ((this.thread == null) || !this.thread.isAlive()) &&
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
				if( blockFinder( false ) )
                    Thread.sleep(1000);
                else
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

	public static boolean blockFinder(boolean ForceAdd) {
		if (XRay.drawOres && !XRay.searchList.isEmpty() && mc.world != null && mc.player != null)
		{
			if ( nextTimeMs > System.currentTimeMillis() ) // Delay to avoid spamming ore updates.
				return true;

			final int px = XRay.localPlyX;
			final int py = XRay.localPlyY;
			final int pz = XRay.localPlyZ;
			if( ( ( px == XRay.localPlyXPrev && pz == XRay.localPlyZPrev ) && XrayRenderer.ores.size() > 0 ) && !ForceAdd )
				return true; // Skip the check if the player hasn't moved

			final List<BlockInfo> temp = new ArrayList<>();
			final int radius = XRay.distNumbers[ XRay.currentDist]; // Get the radius around the player to search.

                        final Map<OreInfo, int[]> ores = new HashMap<>(); // Searches in Set/Map are faster than looping on List
                        for( OreInfo ore : XRay.searchList )
                        {
                                if (ore.draw) // We can handle this condition right here rather than doing it in the big loop
                                {
                                        ores.put( ore, ore.color ); // Using a Map to get the ore color since Set does not have a get() method
                                }
                        }

                        // Minecraft already has a method to get a bunch of blocks. Using the mutable version is faster.
                        BlockPos start = new BlockPos(px - radius, Math.max( 0, py - 96 ), pz - radius);
                        BlockPos end = new BlockPos(px + radius, py + 32, pz + radius);
                        Iterator<MutableBlockPos> it = BlockPos.getAllInBoxMutable(start, end).iterator();

                        final OreInfo buff = new OreInfo(0, 0); // Avoids instanciating tons of objects

                        while (it.hasNext())
                        {
                                MutableBlockPos pos = it.next();
                                IBlockState state = mc.world.getBlockState( pos );

                                Block block = state.getBlock();
                                buff.id = Block.getIdFromBlock( block );
                                buff.meta = block.getMetaFromState( state );

                                if( block.hasTileEntity( state ) )
                                        buff.meta = 0;

                                if ( ores.containsKey(buff) ) // The reason for using Set/Map
                                {
                                        temp.add( new BlockInfo( pos.getX(), pos.getY(), pos.getZ(), ores.get(buff)) ); // Add this block to the temp list
                                }
                        }

			XrayRenderer.ores.clear();
			XrayRenderer.ores.addAll(temp); // Add all our found blocks to the XrayRenderer.ores list. To be use by XrayRenderer when drawing.
			nextTimeMs = System.currentTimeMillis() + delayMs; // Update the delay.
		}
		else
		    return false;

		return true;
	}
}
