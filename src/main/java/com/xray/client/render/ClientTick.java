package com.xray.client.render;

import java.util.ArrayList;
import java.util.List;

import com.xray.common.XRay;
import net.minecraft.block.state.IBlockState;

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

        @Override
	public void run() // Our thread code for finding ores near the player.
	{
                blockFinder();
                XRay.instance.doneFindingBlocks(); // Inform the mod that we're done finding blocks
	}

	/**
	 * Should only be called by requestBlockFinder() as it manages the
	 * threads and checks the preconditions (drawOres, player not null ...)
	 */
	private void blockFinder() {

		final int px = XRay.localPlyX;
		final int py = XRay.localPlyY;
		final int pz = XRay.localPlyZ;
		if ( px == XRay.localPlyXPrev && pz == XRay.localPlyZPrev && XrayRenderer.ores.size() > 0 ) {
			return; // Skip the check if the player hasn't moved
		}
		final List<BlockInfo> temp = new ArrayList<>();
		final int radius = XRay.distNumbers[ XRay.currentDist ]; // Get the radius around the player to search.

		final Map<OreInfo, int[]> ores = new HashMap<>(); // Searches in Set/Map are faster than looping on List
		for ( OreInfo ore : XRay.searchList ) {
			if ( ore.draw ) // We can handle this condition right here rather than doing it in the big loop
			{
				ores.put( ore, ore.color ); // Using a Map to get the ore color since Set does not have a get() method
			}
		}
		final OreInfo buff = new OreInfo( 0, 0 ); // Search key for the map

		final int minX = px - radius;
		final int maxX = px + radius;
		final int minY = Math.max(0, py - 92);
		final int maxY = Math.min(255, py + 32);
		final int minZ = pz - radius;
		final int maxZ = pz + radius;
		int lowBoundX, highBoundX, lowBoundY, highBoundY, lowBoundZ, highBoundZ;

		// Loop on chunks (x, z)
		for ( int chunkX = (minX >> 4); chunkX <= (maxX >> 4); chunkX++ ) // Using bitshift because negative numbers divided by 16 will give a wrong chunk
		{
			// Pre-compute the extend bounds on X
			int x = chunkX << 4; // lowest x coord of the chunk in block/world coordinates
			lowBoundX = (x < minX) ? minX - x : 0; // lower bound for x within the extend
			highBoundX = (x + 15 > maxX) ? maxX - x : 15;// and higher bound. Basically, we clamp it to fit the radius.

			for ( int chunkZ = (minZ >> 4); chunkZ <= (maxZ >> 4); chunkZ++ )
			{
				// Time to get the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
				Chunk chunk = mc.world.getChunkFromChunkCoords( chunkX, chunkZ );
				if ( chunk == null || !chunk.isLoaded() ) {
					continue; // We won't find anything interesting in unloaded chunks
				}
				ExtendedBlockStorage[] extendsList = chunk.getBlockStorageArray();

				// Pre-compute the extend bounds on Z
				int z = chunkZ << 4;
				lowBoundZ = (z < minZ) ? minZ - z : 0;
				highBoundZ = (z + 15 > maxZ) ? maxZ - z : 15;

				// Loop on the extends around the player's layer (6 down, 2 up)
				for ( int curExtend = (minY >> 4); curExtend <= (maxY >> 4); curExtend++ )
				{
					ExtendedBlockStorage ebs = extendsList[curExtend];
					if (ebs == null) // happens quite often!
						continue;

					// Pre-compute the extend bounds on Y
					int y = curExtend << 4;
					lowBoundY = (y < minY) ? minY - y : 0;
					highBoundY = (y + 15 > maxY) ? maxY - y : 15;

					// Now that we have an extend, let's check all its blocks
					for ( int i = lowBoundX; i <= highBoundX; i++ ) {
						for ( int j = lowBoundY; j <= highBoundY; j++ ) {
							for ( int k = lowBoundZ; k <= highBoundZ; k++ ) {
								IBlockState state = ebs.get( i, j, k ); // this one seems a lot faster than asking the world directly

								Block block = state.getBlock();
								buff.id = Block.getIdFromBlock( block );      // prepare the search key according to OreInfo.equals()
								buff.meta = block.getMetaFromState( state );  // and OreInfo.hashCode()

								if (block.hasTileEntity( state )) {
									buff.meta = 0;
								}

								if (ores.containsKey( buff )) // The reason for using Set/Map
								{
									temp.add( new BlockInfo(x + i, y + j, z + k, ores.get(buff)) ); // Add this block to the temp list using world coordinates
								}
							}
						}
					}
				}
			}
		}

		XrayRenderer.ores.clear();
		XrayRenderer.ores.addAll( temp ); // Add all our found blocks to the XrayRenderer.ores list. To be use by XrayRenderer when drawing.

	}
}
