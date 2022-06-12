package pro.mikey.xray.xray;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.utils.RenderBlockProps;

import java.util.*;

public class RenderEnqueue {
	/**
	 * Use Controller.requestBlockFinder() to trigger a scan.
	 */
	public static Set<RenderBlockProps> blockFinder() {
        HashMap<UUID, BlockData> blocks = Controller.getBlockStore().getStore();
        if ( blocks.isEmpty() ) {
            return new HashSet<>(); // no need to scan the region if there's nothing to find
        }

		final Level world = Minecraft.getInstance().level;
        final Player player = Minecraft.getInstance().player;

		// Something is fatally wrong
        if( world == null || player == null ) {
			return new HashSet<>();
		}

		final Set<RenderBlockProps> renderQueue = new HashSet<>();

		int range = Controller.getHalfRange();

		int cX = player.chunkPosition().x;
		int cZ = player.chunkPosition().z;

		BlockState currentState;
		FluidState currentFluid;

		Pair<BlockData, UUID> dataWithUUID;
		ResourceLocation block;

		for (int i = cX - range; i <= cX + range; i++) {
			int chunkStartX = i << 4;
			for (int j = cZ - range; j <= cZ + range; j++) {
				int chunkStartZ = j << 4;

				int height =
						Arrays.stream(world.getChunk(i, j).getSections())
								.filter(Objects::nonNull)
								.mapToInt(LevelChunkSection::bottomBlockY)
								.max()
								.orElse(0);

				for (int k = chunkStartX; k < chunkStartX + 16; k++) {
					for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
						for (int m = world.getMinBuildHeight(); m < height + (1 << 4); m++) {
							BlockPos pos = new BlockPos(k, m, l);

							currentState = world.getBlockState(pos);
							currentFluid = currentState.getFluidState();

							if( (currentFluid.getType() == Fluids.LAVA || currentFluid.getType() == Fluids.FLOWING_LAVA) && Controller.isLavaActive() ) {
								renderQueue.add(new RenderBlockProps(pos.getX(), pos.getY(), pos.getZ(), 0xff0000));
								continue;
							}

							// Reject blacklisted blocks
							if( Controller.blackList.contains(currentState.getBlock()) )
								continue;

							block = ForgeRegistries.BLOCKS.getKey(currentState.getBlock());
							if( block == null )
								continue;

							dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
							if( dataWithUUID == null )
								continue;

							if( dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() ) // fail safe
								continue;

							// Push the block to the render queue
							renderQueue.add(new RenderBlockProps(pos.getX(), pos.getY(), pos.getZ(), dataWithUUID.getKey().getColor()));
						}
					}
				}
			}
		}

		return renderQueue;
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
			boolean removed = Controller.syncRenderList.remove(new RenderBlockProps(pos, 0));
			if (removed) {
				Render.requestedRefresh = true;
			}
			return;
		}

		ResourceLocation block = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		if( block == null )
			return;

		Pair<BlockData, UUID> dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
		if( dataWithUUID == null || dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() )
			return;

		// the block was added to the world, let's add it to the drawing buffer
		Controller.syncRenderList.add(new RenderBlockProps(pos, dataWithUUID.getKey().getColor()) );
		Render.requestedRefresh = true;
	}
}
