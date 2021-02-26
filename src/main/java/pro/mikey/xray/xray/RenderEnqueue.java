package pro.mikey.xray.xray;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.apache.commons.lang3.tuple.Pair;
import pro.mikey.xray.store.BlockStore;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.utils.Region;
import pro.mikey.xray.utils.RenderBlockProps;

import java.util.*;

public class RenderEnqueue implements Runnable {
    private final Region box;

    public RenderEnqueue(Region region) {
        this.box = region;
    }

    /**
     * Use Controller.requestBlockFinder() to trigger a scan.
     */
    @Override
    public void run() // Our thread code for finding syncRenderList near the player.
    {
        HashMap<UUID, BlockData> blocks = Controller.getBlockStore().getStore();
        if (blocks.isEmpty() || !BlockStore.hasActiveBlocks()) {
            if (!Render.syncRenderList.isEmpty()) {
                Render.syncRenderList.clear();
            }
            return; // no need to scan the region if there's nothing to find
        }

        final World world = Minecraft.getInstance().world;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (world == null || player == null) {
            return;
        }

        final List<RenderBlockProps> renderQueue = new ArrayList<>();
        int lowBoundX, highBoundX, lowBoundY, highBoundY, lowBoundZ, highBoundZ;

        // Loop on chunks (x, z)
        for (int chunkX = this.box.minChunkX; chunkX <= this.box.maxChunkX; chunkX++) {
            // Pre-compute the extend bounds on X
            int x = chunkX << 4; // lowest x coord of the chunk in block/world coordinates
            lowBoundX = (x < this.box.minX)
                ? this.box.minX - x
                : 0; // lower bound for x within the extend
            highBoundX = (x + 15 > this.box.maxX)
                ? this.box.maxX - x
                : 15;// and higher bound. Basically, we clamp it to fit the radius.

            for (int chunkZ = this.box.minChunkZ; chunkZ <= this.box.maxChunkZ; chunkZ++) {
                // Time to getStore the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
                if (!world.chunkExists(chunkX, chunkZ)) {
                    continue; // We won't find anything interesting in unloaded chunks
                }

                Chunk chunk = world.getChunk(chunkX, chunkZ);
                ChunkSection[] extendsList = chunk.getSections();

                // Pre-compute the extend bounds on Z
                int z = chunkZ << 4;
                lowBoundZ = (z < this.box.minZ)
                    ? this.box.minZ - z
                    : 0;
                highBoundZ = (z + 15 > this.box.maxZ)
                    ? this.box.maxZ - z
                    : 15;

                // Loop on the extends around the player's layer (6 down, 2 up)
                for (int curExtend = this.box.minChunkY; curExtend <= this.box.maxChunkY; curExtend++) {
                    ChunkSection ebs = extendsList[curExtend];
                    if (ebs == null || ebs.isEmpty()) // happens quite often!
                    {
                        continue;
                    }

                    // Pre-compute the extend bounds on Y
                    int y = curExtend << 4;
                    lowBoundY = (y < this.box.minY)
                        ? this.box.minY - y
                        : 0;
                    highBoundY = (y + 15 > this.box.maxY)
                        ? this.box.maxY - y
                        : 15;

                    // Now that we have an extend, let's check all its blocks
                    BlockPos pos = new BlockPos(x, y, z).toImmutable();
                    renderQueue.addAll(this.scanBounds(world, ebs, pos, lowBoundX, lowBoundY, lowBoundZ, highBoundX, highBoundY, highBoundZ));
                }
            }
        }
        final BlockPos playerPos = player.getPosition();
        renderQueue.sort((t, t1) -> Double.compare(t1.getPos().distanceSq(playerPos), t.getPos().distanceSq((playerPos))));
        Render.syncRenderList.clear();
        Render.syncRenderList.addAll(renderQueue); // Add all our found blocks to the Render.syncRenderList list. To be use by Render when drawing.
    }

    private Set<RenderBlockProps> scanBounds(World world, ChunkSection chunkSection, BlockPos chunkRel, int lowerX, int lowerY, int lowerZ, int higherX, int higherY, int higherZ) {
        BlockState currentState;
        FluidState currentFluid;
        ResourceLocation blockName;
        Pair<BlockData, UUID> dataWithUUID;

        Set<RenderBlockProps> blocks = new HashSet<>();
        for (int i = lowerX; i <= higherX; i++) {
            for (int j = lowerY; j <= higherY; j++) {
                for (int k = lowerZ; k <= higherZ; k++) {
                    currentState = chunkSection.getBlockState(i, j, k);
                    currentFluid = currentState.getFluidState();

                    if (currentState.getMaterial() == Material.AIR) {
                        continue;
                    }

                    // Add Lava
                    if (Controller.isLavaActive() && (currentFluid.getFluid() == Fluids.LAVA || currentFluid.getFluid() == Fluids.FLOWING_LAVA)) {
                        blocks.add(new RenderBlockProps(chunkRel.add(i, j, k), 0xff0000));
                        continue;
                    }

                    // Reject blacklisted blocks
                    if (Controller.blackList.contains(currentState.getBlock())) {
                        continue;
                    }

                    blockName = currentState.getBlock().getRegistryName();
                    if (blockName == null) {
                        continue;
                    }

                    dataWithUUID = Controller.getBlockStore().getStoreByReference(blockName.toString());
                    if (dataWithUUID == null) {
                        continue;
                    }

                    // fail safe
                    if (dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing()) {
                        continue;
                    }

                    // Push the block to the render queue
                    blocks.add(new RenderBlockProps(chunkRel.add(i, j, k), dataWithUUID.getKey().getColor()));
                }
            }
        }

        return blocks;
    }

    /**
     * Single-block version of blockFinder. Can safely be called directly
     * for quick block check.
     *
     * @param pos   the BlockPos to check
     * @param state the current state of the block
     * @param add   true if the block was added to world, false if it was removed
     */
    public static void checkBlock(BlockPos pos, BlockState state, boolean add) {
        if (!Controller.isXRayActive() || Controller.getBlockStore().getStore().isEmpty()) {
            return; // just pass
        }

        // If we're removing then remove :D
        if (!add) {
            Render.syncRenderList.remove(new RenderBlockProps(pos, 0));
            return;
        }

        ResourceLocation block = state.getBlock().getRegistryName();
        if (block == null) {
            return;
        }

        Pair<BlockData, UUID> dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
        if (dataWithUUID == null || dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing()) {
            return;
        }

        // the block was added to the world, let's add it to the drawing buffer
        Render.syncRenderList.add(new RenderBlockProps(pos, dataWithUUID.getKey().getColor()));
    }
}
