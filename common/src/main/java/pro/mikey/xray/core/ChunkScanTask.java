package pro.mikey.xray.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ChunkScanTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkScanTask.class);

    private final Level level;

    private final int startX;
    private final int startZ;

    public ChunkScanTask(Level level, ChunkPos pos) {
        // Move the chunk pos to block pos by multiplying by 16
        this.startX = pos.x() << 4;
        this.startZ = pos.z() << 4;

        this.level = level;
    }

    @Override
    public void run() {
        // Note: this runs via ExecutorService#submit, so an uncaught exception here would otherwise vanish
        // silently (submit() captures it in a discarded Future instead of the thread's uncaught handler).
        try {
            final Set<OutlineRenderTarget> renderQueue = new HashSet<>();

            BlockState state;
            FluidState fluidState;

            for (int k = startX; k < startX + 16; k++) {
                for (int l = startZ; l < startZ + 16; l++) {
                    for (int m = level.getMinY(); m < level.getMaxY(); m++) {
                        BlockPos pos = new BlockPos(k, m, l);

                        state = level.getBlockState(pos);
                        fluidState = state.getFluidState();

                        if ((fluidState.getType() == Fluids.LAVA || fluidState.getType() == Fluids.FLOWING_LAVA) && ScanController.INSTANCE.isLavaActive()) {
                            renderQueue.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), 0xffff0000));
                            continue;
                        }

                        // Reject blacklisted blocks
                        if (ScanController.blackList.contains(state.getBlock()))
                            continue;

                        for (var target : ScanController.INSTANCE.scanStore.activeScanTargets()) {
                            if (target.matches(level, pos, state, fluidState)) {
                                renderQueue.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), target.colorInt()));
                            }
                        }
                    }
                }
            }

            ChunkPos chunkPos = new ChunkPos(startX >> 4, startZ >> 4);
            LOGGER.debug("Scanned chunk {} - found {} matching block(s)", chunkPos, renderQueue.size());
            ScanController.INSTANCE.syncRenderList.put(chunkPos, renderQueue);
        } catch (Exception e) {
            LOGGER.error("Chunk scan failed for chunk ({}, {}) - this chunk will have no outlines", startX >> 4, startZ >> 4, e);
        }
    }
}
