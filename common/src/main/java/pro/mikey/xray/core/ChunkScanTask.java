package pro.mikey.xray.core;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class ChunkScanTask implements Runnable {
    private final Level level;

    private final int startX;
    private final int startZ;
    private final ChunkPos Cpos;
    private final boolean iscore;

    public ChunkScanTask(Level level, ChunkPos pos, boolean core) {
        // Move the chunk pos to block pos by multiplying by 16
        this.startX = pos.x << 4;
        this.startZ = pos.z << 4;
        this.Cpos=pos;

        this.level = level;
        this.iscore = core;
    }

    private boolean isNetherDimension() {
        ResourceKey<Level> dimensionKey = level.dimension();
        return dimensionKey == Level.NETHER;
    }

    @Override
    public void run() {
        final Set<OutlineRenderTarget> renderQueue = ConcurrentHashMap.newKeySet();

        BlockState state;
        FluidState fluidState;

        for (int k = startX; k < startX + 16; k++) {
            for (int l = startZ; l < startZ + 16; l++) {
                for (int m = level.getMinY(); m < level.getMaxY(); m++) {
                    BlockPos pos = new BlockPos(k, m, l);

                    state = level.getBlockState(pos);
                    fluidState = state.getFluidState();

                    if (iscore){
                        if (( fluidState.getType() == Fluids.LAVA || fluidState.getType() == Fluids.FLOWING_LAVA) && ScanController.INSTANCE.isLavaActive()) {
                            if (isNetherDimension()) {
                                if (pos.getY() > 31){
                                    renderQueue.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), 0xffff0000));
                                }
                            } else {
                                renderQueue.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), 0xffff0000));
                            }
                            continue;
                        }
    
                    }

                    // Reject blacklisted blocks
                    if (ScanController.blackList.contains(state.getBlock()))
                        continue;

                    for (var target : ScanController.INSTANCE.scanStore.activeScanTargets()) {
                        if (target.matches(level, pos, state, fluidState)) {
                            renderQueue.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), target.colorInt()));
                            break;
                        }
                    }
                }
            }
        }

        ScanController.INSTANCE.syncRenderList.put(new ChunkPos(startX >> 4, startZ >> 4), renderQueue);
    }
}
