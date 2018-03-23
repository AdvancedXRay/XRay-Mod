package com.xray.client.render;

import java.util.ArrayList;
import java.util.List;

import com.xray.common.XRay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import com.xray.common.reference.BlockInfo;
import com.xray.common.reference.OreInfo;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

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
                                if ( ore.draw ) // We can handle this condition right here rather than doing it in the big loop
                                {
                                        ores.put( ore, ore.color ); // Using a Map to get the ore color since Set does not have a get() method
                                }
                        }
                        final OreInfo buff = new OreInfo( 0, 0 ); // Search key for the map

                        // Convert world coordinates into chunk coordinates
                        int minX = (px - radius) >> 4;
                        int maxX = (px + radius) >> 4;
                        int minZ = (pz - radius) >> 4;
                        int maxZ = (pz + radius) >> 4;
                        int minY = Math.max(0, (py - 92 >> 4));
                        int maxY = Math.min(15, (py + 32 >> 4));

                        int blockPosX, blockPosY, blockPosZ; // little buffers to avoid useless computations

                        // Loop on chunks (x, z)
                        for ( int x = minX; x <= maxX; x++ )
                        {
                                blockPosX = x << 4;
                                for ( int z = minZ; z <= maxZ; z++ )
                                {
                                        blockPosZ = z << 4;

                                        // Time to get the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
                                        Chunk chunk = mc.world.getChunkFromChunkCoords( x, z );
                                        if ( chunk == null || !chunk.isLoaded() ) {
                                                continue;
                                        }
                                        ExtendedBlockStorage[] extendsList = chunk.getBlockStorageArray();

                                        // Loop on the extends around the player's layer (6 down, 2 up)
                                        for ( int y = minY; y <= maxY; y++ )
                                        {
                                                blockPosY = y << 4;

                                                ExtendedBlockStorage ebs = extendsList[y];
                                                if ( ebs == null ) { // happens quite often!
                                                        continue;
                                                }

                                                // Now that we have an extend, let's check all its blocks
                                                for ( int i = 0; i < 16; i++ )
                                                {
                                                        for ( int j = 0; j < 16; j++ )
                                                        {
                                                                for ( int k = 0; k < 16; k++ )
                                                                {
                                                                        IBlockState state = ebs.get(i, j, k); // this one seems a lot faster than asking the world directly

                                                                        Block block = state.getBlock();
                                                                        buff.id = Block.getIdFromBlock( block );
                                                                        buff.meta = block.getMetaFromState( state );

                                                                        if( block.hasTileEntity( state ) )
                                                                                buff.meta = 0;

                                                                        if ( ores.containsKey(buff) ) // The reason for using Set/Map
                                                                        {
                                                                                temp.add( new BlockInfo( blockPosX + i, blockPosY + j, blockPosZ + k, ores.get(buff)) ); // Add this block to the temp list using world coordinates
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
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
