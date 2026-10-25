package pro.mikey.xray.core;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.scanner.ScanStore;
import pro.mikey.xray.core.scanner.ScanType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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

    // Provide a quick, thread-safe lookup for the outline color of blocks that are being scanned for.
    public final Map<ChunkPos, Map<Long, OutlineRenderTarget>> syncRenderList = Collections.synchronizedMap(new HashMap<>()); // this is accessed by threads
    @Nullable
    private ChunkPos lastChunkPos = null;
    @Nullable
    private Level lastLevel = null;

    // Chunks around the camera, nearest first, and the same as a set for quick lookups from the scanner threads
    private volatile List<ChunkPos> chunksInRange = List.of();
    private volatile Set<ChunkPos> chunksInRangeSet = Set.of();
    private final Map<ChunkPos, Integer> pendingChunks = new ConcurrentHashMap<>();
    private final AtomicInteger scanGeneration = new AtomicInteger(); // Holds the generation of the current scan, so scanner threads can tell if their work is still relevant

    public final ScanStore scanStore = new ScanStore();
    private boolean xrayActive = false;

    public void init() {
        this.scanStore.load();
    }

    public boolean isActive() {
        return this.xrayActive && Minecraft.getInstance().level != null && Minecraft.getInstance().player != null;
    }

    public void toggleXRay() {
        if (!xrayActive) {
            syncRenderList.clear(); // first, clear the buffer
            xrayActive = true; // then, enable drawing
            requestBlockFinder(true); // finally, force a refresh

            if (!XRay.config().showOverlay.get() && Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("xray.toggle.activated"));
            }
        } else {
            if (!XRay.config().showOverlay.get() && Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("xray.toggle.deactivated"));
            }

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

    /// Chunk the main camera is in, so freecam style mods scan around where you're looking from rather than the player
    private static ChunkPos scanCentre(Minecraft minecraft) {
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            return ChunkPos.containing(camera.blockPosition());
        }

        return minecraft.player.chunkPosition();
    }

    public synchronized void requestBlockFinder(boolean force) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null || !isActive()) {
            return;
        }

        // Reload everything when the level/dimension changes
        if (level != lastLevel) {
            lastLevel = level;
            force = true;
        }

        // Cleanup for a fresh scan.
        if (force) {
            synchronized (syncRenderList) {
                scanGeneration.incrementAndGet();
                syncRenderList.clear();
            }

            pendingChunks.clear();
            OutlineRender.clearVBOs();
        }

        ChunkPos centre = scanCentre(minecraft);
        if (force || !centre.equals(lastChunkPos)) {
            lastChunkPos = centre;

            // Sorted lookup for chunks in the range, nearest first
            List<ChunkPos> inRange = new ArrayList<>(ChunkPos.rangeClosed(centre, this.getHalfRange()).toList());
            inRange.sort(Comparator.comparingInt(chunk -> chunk.distanceSquared(centre)));
            chunksInRange = inRange;

            List<ChunkPos> removedChunks;
            synchronized (syncRenderList) {
                chunksInRangeSet = new HashSet<>(inRange);
                removedChunks = syncRenderList.keySet().stream().filter(chunk -> !chunksInRangeSet.contains(chunk)).toList();
                removedChunks.forEach(syncRenderList::remove);
            }

            OutlineRender.clearVBOsFor(removedChunks);
        }

        if (this.scanStore.activeScanTargets().isEmpty() && !isLavaActive()) {
            return;
        }

        int generation = scanGeneration.get();
        for (ChunkPos chunk : chunksInRange) {
            if (syncRenderList.containsKey(chunk) || !level.hasChunk(chunk.x(), chunk.z()) || pendingChunks.putIfAbsent(chunk, generation) != null) {
                continue;
            }

            SCANNER.submit(new ChunkScanTask(level, chunk, generation));
        }
    }

    /// False once a forced rescan has started since the scan was queued, or the camera has moved too far away
    boolean isScanWanted(ChunkPos chunk, int generation) {
        return generation == scanGeneration.get() && chunksInRangeSet.contains(chunk);
    }

    /// Called from the scanner threads once a chunk is done
    void completeScan(ChunkPos chunk, int generation, Map<Long, OutlineRenderTarget> found) {
        synchronized (syncRenderList) {
            if (isScanWanted(chunk, generation)) {
                syncRenderList.put(chunk, found);
                OutlineRender.refreshVBOForChunk(chunk);
            }
        }

        pendingChunks.remove(chunk, generation);
    }

    /// Called from the scanner threads when a scan stops early, so the chunk can be queued again if it's still needed
    void abandonScan(ChunkPos chunk, int generation) {
        pendingChunks.remove(chunk, generation);
    }

    /// The outline color for a block, or null if it isn't something we're looking for. Shared by the scanner and
    /// block updates so both agree on what gets outlined.
    @Nullable
    static Integer outlineColor(Level level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (INSTANCE.isLavaActive() && (fluidState.getType() == Fluids.LAVA || fluidState.getType() == Fluids.FLOWING_LAVA)) {
            return 0xFFFF0000;
        }

        if (blackList.contains(state.getBlock())) {
            return null;
        }

        for (ScanType target : INSTANCE.scanStore.activeScanTargets()) {
            if (target.matches(level, pos, state, fluidState)) {
                return target.colorInt();
            }
        }

        return null;
    }

    public static void onBlockChange(Level level, BlockPos pos, BlockState state) {
        if (!ScanController.INSTANCE.isActive()) {
            return;
        }

        var chunkPos = ChunkPos.containing(pos);
        Map<Long, OutlineRenderTarget> outlines = ScanController.INSTANCE.syncRenderList.get(chunkPos);
        if (outlines == null) {
            // It's not being rendered, so we don't care
            return;
        }

        long key = pos.asLong();
        Integer color = outlineColor(level, pos, state);
        OutlineRenderTarget previous = color == null
                ? outlines.remove(key)
                : outlines.put(key, new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), color));

        // Only rebuild the chunk's outlines if this block's outline actually appeared, went or changed color
        boolean changed = color == null ? previous != null : previous == null || previous.color() != color;
        if (changed) {
            OutlineRender.refreshVBOForChunk(chunkPos);
        }
    }
}
