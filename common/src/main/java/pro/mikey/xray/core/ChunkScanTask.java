package pro.mikey.xray.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.HashMap;
import java.util.Map;

public class ChunkScanTask implements Runnable {
    private final Level level;
    private final ChunkPos chunkPos;
    private final int generation;

    public ChunkScanTask(Level level, ChunkPos pos, int generation) {
        this.level = level;
        this.chunkPos = pos;
        this.generation = generation;
    }

    @Override
    public void run() {
        ScanController controller = ScanController.INSTANCE;

        ChunkAccess chunk = this.level.getChunkSource().getChunk(this.chunkPos.x(), this.chunkPos.z(), ChunkStatus.FULL, false);
        if (chunk == null) {
            // Unloaded since it was queued, it'll be queued again once it's back
            controller.abandonScan(this.chunkPos, this.generation);
            return;
        }

        final Map<Long, OutlineRenderTarget> found = new HashMap<>();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final int startX = this.chunkPos.getMinBlockX();
        final int startZ = this.chunkPos.getMinBlockZ();

        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            // Stop early if the camera has moved away or a rescan started, so stale work doesn't hold up the chunks that matter
            if (!controller.isScanWanted(this.chunkPos, this.generation)) {
                controller.abandonScan(this.chunkPos, this.generation);
                return;
            }

            LevelChunkSection section = sections[i];
            if (section.hasOnlyAir()) {
                continue;
            }

            int sectionMinY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        pos.set(startX + x, sectionMinY + y, startZ + z);

                        Integer color = ScanController.outlineColor(this.level, pos, state);
                        if (color != null) {
                            found.put(pos.asLong(), new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), color));
                        }
                    }
                }
            }
        }

        controller.completeScan(this.chunkPos, this.generation, found);
    }
}
