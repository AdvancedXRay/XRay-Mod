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
import pro.mikey.xray.core.scanner.ScanStore;
import pro.mikey.xray.core.scanner.ScanType;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    private final int maxStepsToScan = 8;

    // Block blackList
    // Todo: move this to a configurable thing
    public static final Set<Block> blackList = new HashSet<>() {{
        add(Blocks.AIR);
        add(Blocks.BEDROCK);
        add(Blocks.STONE);
        add(Blocks.GRASS_BLOCK);
        add(Blocks.DIRT);
    }};


    public final Map<ChunkPos, Set<OutlineRenderTarget>> syncRenderList = new ConcurrentHashMap<>(); // this is accessed by threads
    public final Map<ChunkPos, Set<OutlineRenderTarget>> syncRenderLista = new ConcurrentHashMap<>(); // this is accessed by threads
    private ChunkPos lastChunkPos = null;
    private BlockPos lastPos = null;
    private Set<ChunkPos> last3x3 = ConcurrentHashMap.newKeySet();
    private Set<ChunkPos> lastxxx = ConcurrentHashMap.newKeySet();
    private List<BlockPos> lastshi = new ArrayList<>();
    private static final Object RENDER_SET_LOCK = new Object();

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
            syncRenderLista.clear(); // first, clear the buffer
            last3x3.clear();
            lastxxx.clear();
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

    public void incrementCurrentDist(int value) {
            XRay.config().radius.set(value);
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

    private boolean playerHasMoveda() {
        if (Minecraft.getInstance().player == null)
            return false;
        BlockPos plyChunkPos = Minecraft.getInstance().player.blockPosition();
        return lastPos == null || !lastPos.equals(plyChunkPos);
    }

    private void updatePlayerPosition() {
        lastChunkPos = Minecraft.getInstance().player.chunkPosition();
    }

    private void updatePlayerPositiona() {
        lastPos = Minecraft.getInstance().player.blockPosition();
    }

    private List<ChunkPos> getCore3x3Chunks(ChunkPos playerChunk,int range) {
    List<ChunkPos> coreChunks = new ArrayList<>();
    if (range==0){
        coreChunks.add(new ChunkPos(playerChunk.x, playerChunk.z));
    } else {
        for (int x = playerChunk.x - 1; x <= playerChunk.x + 1; x++) {
            for (int z = playerChunk.z - 1; z <= playerChunk.z + 1; z++) {
                coreChunks.add(new ChunkPos(x, z));
            }
        }
    }
        return coreChunks;
    }

    private List<ChunkPos> getOuterChunks(List<ChunkPos> allChunks, List<ChunkPos> core3x3Chunks) {
        return allChunks.stream()
            .filter(chunk -> !core3x3Chunks.contains(chunk))
            .collect(Collectors.toList());
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
                syncRenderLista.clear();
                last3x3.clear();
                lastxxx.clear();
                OutlineRender.clearVBOs(); // Clear the VBOs as well
            }

            if (this.scanStore.activeScanTargets().isEmpty() && !isLavaActive()) {
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

            List<ChunkPos> core3x3Chunks = getCore3x3Chunks(playerChunkPos,range);
            List<ChunkPos> outerChunks = getOuterChunks(chunksToScan, core3x3Chunks);

            var knownChunksa = last3x3;

            var newChunksa = core3x3Chunks.stream().filter(chunk -> !knownChunksa.contains(chunk)).toList();
            var removedChunksa = knownChunksa.stream().filter(chunk -> !core3x3Chunks.contains(chunk)).toList();

            if (!removedChunksa.isEmpty()) {
                OutlineRender.clearVBOsFor(removedChunksa);
            }
            for (ChunkPos chunk : removedChunksa) {
                last3x3.remove(chunk);
                syncRenderList.remove(chunk);
            }

            for (ChunkPos chunk : newChunksa) {
                last3x3.add(chunk);
                SCANNER.submit(new ChunkScanTask(player.level(), chunk, true));
            }

            var knownChunks = lastxxx;

            // New chunks
            var newChunks = outerChunks.stream().filter(chunk -> !knownChunks.contains(chunk)).toList();
            var removedChunks = knownChunks.stream().filter(chunk -> !outerChunks.contains(chunk)).toList();

            if (!removedChunks.isEmpty()) {
                OutlineRender.clearVBOsFor(removedChunks);
            }

            // Push the new chunks to the scanner, remove the old ones from the render list
            for (ChunkPos chunk : removedChunks) {
                lastxxx.remove(chunk);
                syncRenderList.remove(chunk);
            }

            for (ChunkPos chunk : newChunks) {
                lastxxx.add(chunk);
                SCANNER.submit(new ChunkScanTask(player.level(), chunk, false));
            }
        }

        if (isXRayActive() && (force || playerHasMoveda())) {
            updatePlayerPositiona();
            clean();
            add();
        }
    }

    public synchronized void clean(){
        if (lastshi != null && !lastshi.isEmpty()) {
            Set<OutlineRenderTarget> renderQueuea = new HashSet<>();
            for (BlockPos chunk : lastshi){
                renderQueuea.add(new OutlineRenderTarget(chunk.getX(), chunk.getY(), chunk.getZ(), 0xffff00ff));
            }
            var re=ScanController.INSTANCE.syncRenderLista.get(new ChunkPos(1, 1));
            if (re != null){
                re.removeAll(renderQueuea);
            }
            OutlineRender.refreshVBOForChunka(new ChunkPos(1, 1));
        }
    }

    public synchronized void add(){
        BlockPos playerChunkPos = Minecraft.getInstance().player.blockPosition();
        List<BlockPos> chunksToScan = new ArrayList<>();
        for (int z = playerChunkPos.getZ() - 5; z <= playerChunkPos.getZ() + 5; z++) {
            if (z != playerChunkPos.getZ()) {
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()-1, z));
            } else {
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()+7, z));
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()+6, z));
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()+5, z));
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()+4, z));
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()+3, z));
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()-2, z));
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()-3, z));
                chunksToScan.add(new BlockPos(playerChunkPos.getX(),playerChunkPos.getY()-4, z));
            }
        };
        for (int x = playerChunkPos.getX() - 5; x <= playerChunkPos.getX() + 5; x++) {
            if (x != playerChunkPos.getX()) {
                chunksToScan.add(new BlockPos(x,playerChunkPos.getY()-1 ,playerChunkPos.getZ()));
            }
        };
        Set<OutlineRenderTarget> renderQueue = new HashSet<>();
        for (BlockPos chunk : chunksToScan){
            BlockState state = Minecraft.getInstance().player.level().getBlockState(chunk);
            if (chunk.getY()>=playerChunkPos.getY()+3) {
                if ((state.getBlock() ==  Blocks.SAND || state.getBlock() ==  Blocks.GRAVEL) && ScanController.INSTANCE.isLavaActive()) {
                    renderQueue.add(new OutlineRenderTarget(chunk.getX(), chunk.getY(), chunk.getZ(), 0xffff00ff));
                    ScanController.INSTANCE.syncRenderLista.put(new ChunkPos(1, 1), renderQueue);
                }
            } else {
                if ((state.getBlock() ==  Blocks.AIR || state.getBlock() ==  Blocks.CAVE_AIR|| state.getBlock() ==  Blocks.POWDER_SNOW) && ScanController.INSTANCE.isLavaActive()) {
                    renderQueue.add(new OutlineRenderTarget(chunk.getX(), chunk.getY(), chunk.getZ(), 0xffff00ff));
                    ScanController.INSTANCE.syncRenderLista.put(new ChunkPos(1, 1), renderQueue);
                }
            }
        }
        lastshi=new ArrayList<>(chunksToScan);
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
            synchronized (RENDER_SET_LOCK) {
                var removed = outlineRenderTargets.removeIf(target -> target.x() == pos.getX() && target.y() == pos.getY() && target.z() == pos.getZ());
                if (removed) {
                    // We need to tell the outline render to refresh the VBO for this chunk
                    OutlineRender.refreshVBOForChunk(chunkPos);
                }
            }
            INSTANCE.clean();
            INSTANCE.add();
            return;
        }

        if (ScanController.INSTANCE.isLavaActive() && state.is(Blocks.LAVA)) {
            synchronized (RENDER_SET_LOCK) {
                // We're actively looking at this chunk so let's inject this block
                outlineRenderTargets.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), 0xffff0000));
            }
            
            // Tell the VBO to refresh for this chunk
            OutlineRender.refreshVBOForChunk(chunkPos);
            
            INSTANCE.clean();
            INSTANCE.add();
            return;
        }

        // Otherwise, do we have scantarget in the active list of things to find?
        var noMatchesFound = true;

        Set<ScanType> activeScanTargets = ScanController.INSTANCE.scanStore.activeScanTargets();
        for (var scanType : activeScanTargets) {
            if (scanType.matches(level, pos, state, state.getFluidState())) {
                // We need to tell the render system to refresh. We should manually add this black to the renderlist
                // We're actively looking at this chunk so let's inject this block
                synchronized (RENDER_SET_LOCK) {
                    outlineRenderTargets.add(new OutlineRenderTarget(pos.getX(), pos.getY(), pos.getZ(), scanType.colorInt()));
                }

                // Tell the VBO to refresh for this chunk
                OutlineRender.refreshVBOForChunk(chunkPos);
                noMatchesFound = false; // We found a match, so we can stop checking
                
                INSTANCE.clean();
                INSTANCE.add();
                break; // We found a match, so we can stop checking
            }
        }

        // If no matches are found AND the block pos is currently in the render list, we need to remove it and ask the chunk to refresh
        var blockFromRenderList = outlineRenderTargets.stream()
                .filter(target -> target.x() == pos.getX() && target.y() == pos.getY() && target.z() == pos.getZ())
                .findFirst();

        if (blockFromRenderList.isEmpty()) {
            INSTANCE.clean();
            INSTANCE.add();
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
