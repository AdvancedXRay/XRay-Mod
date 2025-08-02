package pro.mikey.xray.core;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.scanner.ActiveScanTarget;
import pro.mikey.xray.core.scanner.ScanStore;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public enum ScanController {
    INSTANCE;

    // Ensure this thread is shutdown when the game exists.
    private final ExecutorService SCANNER = Executors.newFixedThreadPool(4, new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, "XRay-Scanner");
            thread.setDaemon(true); // Daemon threads do not prevent the JVM from exiting
            return thread;
        }
    });

    private final int maxStepsToScan = 5;

    // Block blackList
    // Todo: move this to a configurable thing
    public static final Set<Block> blackList = new HashSet<>() {{
        add(Blocks.AIR);
        add(Blocks.BEDROCK);
        add(Blocks.STONE);
        add(Blocks.GRASS_BLOCK);
        add(Blocks.DIRT);
    }};


    public final Map<ChunkPos, Set<OutlineRenderTarget>> syncRenderList = Collections.synchronizedMap(new HashMap<>()); // this is accessed by threads
    private ChunkPos lastChunkPos = null;

    public final ScanStore scanStore = new ScanStore();

//    private BlockStore blockStore = new BlockStore();

    // Thread management

    // Draw states
    private boolean xrayActive = false; // Off by default

    public void init() {
        this.scanStore.load();
    }

//    public static BlockStore getBlockStore() {
//        return blockStore;
//    }

    // Public accessors
    public boolean isXRayActive() {
        return this.xrayActive && Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
    }

    public void toggleXRay() {
        if (!xrayActive) // enable drawing
        {
            syncRenderList.clear(); // first, clear the buffer
            xrayActive = true; // then, enable drawing
            requestBlockFinder(true); // finally, force a refresh

            if (!XRay.config().showOverlay.get() && Minecraft.getInstance().player != null)
                Minecraft.getInstance().player.displayClientMessage(Component.translatable("xray.toggle.activated"), false);
        } else // disable drawing
        {
            if (!XRay.config().showOverlay.get() && Minecraft.getInstance().player != null)
                Minecraft.getInstance().player.displayClientMessage(Component.translatable("xray.toggle.deactivated"), false);

            xrayActive = false;
        }
    }

    public boolean isLavaActive() {
        return XRay.config().lavaActive.get();
    }

    public void toggleLava() {
        XRay.config().lavaActive.set(!XRay.config().lavaActive.get());
    }

    public int getRadius() {
        return Mth.clamp(XRay.config().radius.get(), 0, maxStepsToScan) * 3;
    }

    public int getHalfRange() {
        return Math.max(0, getRadius() / 2);
    }

    public int getVisualRadius() {
        return Math.max(1, getRadius());
    }

    public void incrementCurrentDist() {
        if (XRay.config().radius.get() < maxStepsToScan)
            XRay.config().radius.set(XRay.config().radius.get() + 1);
        else
            XRay.config().radius.set(0);
    }

    public void decrementCurrentDist() {
        if (XRay.config().radius.get() > 0)
            XRay.config().radius.set(XRay.config().radius.get() - 1);
        else
            XRay.config().radius.set(maxStepsToScan);
    }

    private boolean playerHasMoved() {
        if (Minecraft.getInstance().player == null)
            return false;

        ChunkPos plyChunkPos = Minecraft.getInstance().player.chunkPosition();

        return lastChunkPos == null || !lastChunkPos.equals(plyChunkPos);
    }

    private void updatePlayerPosition() {
        lastChunkPos = Minecraft.getInstance().player.chunkPosition();
    }

    public synchronized void requestBlockFinder(boolean force) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (isXRayActive() && (force || playerHasMoved())) {
            updatePlayerPosition(); // since we're about to run, update the last known position

            if (force) {
                // Clear the render list if we are forcing a scan
                syncRenderList.clear();
                OutlineRender.clearVBOs(); // Clear the VBOs as well
            }

            if (this.scanStore.activeScanTargets().isEmpty()) {
                return;
            }

            int range = this.getHalfRange();

            List<ChunkPos> chunksToScan = new ArrayList<>();
            var playerChunkPos = player.chunkPosition();
            for (int i = playerChunkPos.x - range; i <= playerChunkPos.x + range; i++) {
                for (int j = playerChunkPos.z - range; j <= playerChunkPos.z + range; j++) {
                    chunksToScan.add(new ChunkPos(i, j));
                }
            }

            // Sort the chunks by distance to the player
            chunksToScan.sort(Comparator.comparingDouble(chunk -> chunk.distanceSquared(playerChunkPos)));

            var knownChunks = syncRenderList.keySet();

            // New chunks
            var newChunks = chunksToScan.stream().filter(chunk -> !knownChunks.contains(chunk)).toList();
            var removedChunks = knownChunks.stream().filter(chunk -> !chunksToScan.contains(chunk)).toList();

            if (!removedChunks.isEmpty()) {
                OutlineRender.clearVBOsFor(removedChunks);
            }

            // Push the new chunks to the scanner, remove the old ones from the render list
            for (ChunkPos chunk : removedChunks) {
                syncRenderList.remove(chunk);
            }

            for (ChunkPos chunk : newChunks) {
                SCANNER.submit(new ChunkScanTask(player.level(), chunk));
            }
        }
    }

    public static void onBlockChange(Level level, BlockPos pos, BlockState state) {
        if (!ScanController.INSTANCE.isXRayActive()) {
            return;
        }

        var chunkPos = new ChunkPos(pos);
        Set<OutlineRenderTarget> outlineRenderTargets = ScanController.INSTANCE.syncRenderList.get(chunkPos);
        if (outlineRenderTargets == null) {
            // It's not being rendered, so we don't care
            return;
        }

        // We're now air baby!
        if (state.isAir()) {
            // Remove the block from the render list
            var removed = outlineRenderTargets.removeIf(target -> target.x() == pos.getX() && target.y() == pos.getY() && target.z() == pos.getZ());
            if (removed) {
                // We need to tell the outline render to refresh the VBO for this chunk
                OutlineRender.refreshVBOForChunk(chunkPos);
            }

            return;
        }

        if (ScanController.INSTANCE.isLavaActive() && state.is(Blocks.LAVA)) {
            // We're actively looking at this chunk so let's inject this block
            outlineRenderTargets.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), 0xff0000));

            // Tell the VBO to refresh for this chunk
            OutlineRender.refreshVBOForChunk(chunkPos);
        }

        // Otherwise, do we have scantarget in the active list of things to find?
        var noMatchesFound = true;

        Set<ActiveScanTarget> activeScanTargets = ScanController.INSTANCE.scanStore.activeScanTargets();
        for (var scanType : activeScanTargets) {
            if (scanType.type().matches(level, pos, state, state.getFluidState())) {
                // We need to tell the render system to refresh. We should manually add this black to the renderlist
                // We're actively looking at this chunk so let's inject this block
                outlineRenderTargets.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), scanType.color()));

                // Tell the VBO to refresh for this chunk
                OutlineRender.refreshVBOForChunk(chunkPos);
                noMatchesFound = false; // We found a match, so we can stop checking
                break; // We found a match, so we can stop checking
            }
        }

        // If no matches are found AND the block pos is currently in the render list, we need to remove it and ask the chunk to refresh
        var blockFromRenderList = outlineRenderTargets.stream()
                .filter(target -> target.x() == pos.getX() && target.y() == pos.getY() && target.z() == pos.getZ())
                .findFirst();

        if (blockFromRenderList.isEmpty()) {
            return;
        }

        if (noMatchesFound) {
            // We didn't find any matches, so we need to remove the block from the render list
            outlineRenderTargets.remove(blockFromRenderList.get());

            // Tell the VBO to refresh for this chunk
            OutlineRender.refreshVBOForChunk(chunkPos);
        }
    }
}
