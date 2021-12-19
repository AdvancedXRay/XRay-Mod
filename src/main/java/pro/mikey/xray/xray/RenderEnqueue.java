package pro.mikey.xray.xray;

import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.utils.Region;
import pro.mikey.xray.utils.RenderBlockProps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RenderEnqueue implements Runnable
{
	private final Region box;

	public RenderEnqueue(Region region )
	{
		box = region;
	}

	@Override
	public void run() // Our thread code for finding syncRenderList near the player.
	{
		blockFinder();
	}

	/**
	 * Use Controller.requestBlockFinder() to trigger a scan.
	 */
	private void blockFinder() {
        HashMap<UUID, BlockData> blocks = Controller.getBlockStore().getStore();
        if ( blocks.isEmpty() ) {
		    if( !Render.syncRenderList.isEmpty() )
		        Render.syncRenderList.clear();
            return; // no need to scan the region if there's nothing to find
        }

		final Level world = Minecraft.getInstance().level;
        final Player player = Minecraft.getInstance().player;
        if( world == null || player == null )
        	return;

		final List<RenderBlockProps> renderQueue = new ArrayList<>();
		int lowBoundX, highBoundX, lowBoundY, highBoundY, lowBoundZ, highBoundZ;

		// Used for cleaning up the searching process
		BlockState currentState;
		FluidState currentFluid;

		ResourceLocation block;
		Pair<BlockData, UUID> dataWithUUID;

		// Loop on chunks (x, z)
		for ( int chunkX = box.minChunkX; chunkX <= box.maxChunkX; chunkX++ )
		{
			// Pre-compute the extend bounds on X
			int x = chunkX << 4; // lowest x coord of the chunk in block/world coordinates
			lowBoundX = (x < box.minX) ? box.minX - x : 0; // lower bound for x within the extend
			highBoundX = (x + 15 > box.maxX) ? box.maxX - x : 15;// and higher bound. Basically, we clamp it to fit the radius.

			for ( int chunkZ = box.minChunkZ; chunkZ <= box.maxChunkZ; chunkZ++ )
			{
				// Time to getStore the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
				if (!world.hasChunk(chunkX, chunkZ)) {
					continue; // We won't find anything interesting in unloaded chunks
				}

				LevelChunk chunk = world.getChunk( chunkX, chunkZ );
				LevelChunkSection[] extendsList = chunk.getSections();

				// Pre-compute the extend bounds on Z
				int z = chunkZ << 4;
				lowBoundZ = (z < box.minZ) ? box.minZ - z : 0;
				highBoundZ = (z + 15 > box.maxZ) ? box.maxZ - z : 15;

				// Loop on the extends around the player's layer (6 down, 2 up)
				for ( int curExtend = box.minChunkY; curExtend <= box.maxChunkY; curExtend++ )
				{
					LevelChunkSection ebs = extendsList[curExtend];
					if (ebs == null) // happens quite often!
						continue;

					// Pre-compute the extend bounds on Y
					int y = curExtend << 4;
					lowBoundY = (y < box.minY) ? box.minY - y : 0;
					highBoundY = (y + 15 > box.maxY) ? box.maxY - y : 15;

					// Now that we have an extend, let's check all its blocks
					for ( int i = lowBoundX; i <= highBoundX; i++ ) {
						for ( int j = lowBoundY; j <= highBoundY; j++ ) {
							for ( int k = lowBoundZ; k <= highBoundZ; k++ ) {
								currentState = ebs.getBlockState(i, j, k);
								currentFluid = currentState.getFluidState();

								if( (currentFluid.getType() == Fluids.LAVA || currentFluid.getType() == Fluids.FLOWING_LAVA) && Controller.isLavaActive() ) {
									renderQueue.add(new RenderBlockProps(x + i, y + j + box.worldMinY, z + k, 0xff0000));
									continue;
								}

								// Reject blacklisted blocks
								if( Controller.blackList.contains(currentState.getBlock()) )
									continue;

								block = currentState.getBlock().getRegistryName();
								if( block == null )
									continue;

								dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
								if( dataWithUUID == null )
									continue;

								if( dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() ) // fail safe
									continue;

								// Push the block to the render queue
								renderQueue.add(new RenderBlockProps(x + i, y + j + box.worldMinY, z + k, dataWithUUID.getKey().getColor()));
							}
						}
					}
				}
			}
		}
		final BlockPos playerPos = player.blockPosition();
		renderQueue.sort((t, t1) -> Double.compare(t1.getPos().distSqr(playerPos), t.getPos().distSqr((playerPos))));
		Render.syncRenderList.clear();
		Render.syncRenderList.addAll( renderQueue ); // Add all our found blocks to the Render.syncRenderList list. To be use by Render when drawing.
	}

	/**
	 * Single-block version of blockFinder. Can safely be called directly
	 * for quick block check.
	 * @param pos the BlockPos to check
	 * @param state the current state of the block
	 * @param add true if the block was added to world, false if it was removed
	 */
	public static void checkBlock(BlockPos pos, BlockState state, boolean add )
	{
		if ( !Controller.isXRayActive() || Controller.getBlockStore().getStore().isEmpty() )
		    return; // just pass

		// If we're removing then remove :D
		if( !add ) {
			Render.syncRenderList.remove( new RenderBlockProps(pos,0) );
			return;
		}

		ResourceLocation block = state.getBlock().getRegistryName();
		if( block == null )
			return;

		Pair<BlockData, UUID> dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
		if( dataWithUUID == null || dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() )
			return;

		// the block was added to the world, let's add it to the drawing buffer
		Render.syncRenderList.add(new RenderBlockProps(pos, dataWithUUID.getKey().getColor()) );
	}
}
