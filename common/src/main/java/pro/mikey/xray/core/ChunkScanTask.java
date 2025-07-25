package pro.mikey.xray.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.HashSet;
import java.util.Set;

public class ChunkScanTask implements Runnable {
    private final Level level;

    private final int startX;
    private final int startZ;

    public ChunkScanTask(Level level, ChunkPos pos) {
        // Move the chunk pos to block pos by multiplying by 16
        this.startX = pos.x << 4;
        this.startZ = pos.z << 4;

        this.level = level;
    }

    @Override
    public void run() {
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
                        renderQueue.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), 0xff0000));
                        continue;
                    }

                    // Reject blacklisted blocks
                    if (ScanController.blackList.contains(state.getBlock()))
                        continue;

                    for (var target : ScanController.INSTANCE.scanStore.activeScanTargets()) {
                        if (target.type().matches(level, pos, state, fluidState)) {
                            renderQueue.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), target.color()));
                        }
                    }
                }
            }
        }

        ScanController.INSTANCE.syncRenderList.put(new ChunkPos(startX >> 4, startZ >> 4), renderQueue);
    }
}
