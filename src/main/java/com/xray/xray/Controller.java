package com.xray.xray;

import com.xray.Configuration;
import com.xray.XRay;
import com.xray.store.BlockStore;
import com.xray.utils.WorldRegion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Controller
{
    // Radius +/- around the player to search. So 8 is 8 on left and right of player plus under the player. So 17x17 area.
    private static final int[] distanceList = new int[] {8, 16, 32, 48, 64, 80, 128, 256};

    // Block blackList
	// Todo: move this to a configurable thing
	public static ArrayList blackList = new ArrayList<Block>() {{
		add(Blocks.AIR);
		add(Blocks.BEDROCK);
		add(Blocks.STONE);
		add(Blocks.GRASS);
		add(Blocks.DIRT);
	}};

	private static Vec3i lastPlayerPos = null;

	/**
     * Global blockStore used for:
     * [Rendering, GUI, Configuration Handling]
     */
	private static BlockStore blockStore = new BlockStore();

	// Thread management
	private static Future task;
    private static ExecutorService executor;

	// Draw states
	private static boolean drawOres = false; // Off by default

    public static BlockStore getBlockStore() {
        return blockStore;
    }

    // Public accessors
	public static boolean drawOres() { return drawOres && XRay.mc.world != null && XRay.mc.player != null; }
	public static void toggleDrawOres()
	{
		if ( !drawOres ) // enable drawing
		{
			Render.ores.clear(); // first, clear the buffer
			executor = Executors.newSingleThreadExecutor();
			drawOres = true; // then, enable drawing
			requestBlockFinder( true ); // finally, force a refresh

			if( !Configuration.general.showOverlay.get() )
				XRay.mc.player.sendMessage( new StringTextComponent(I18n.format("xray.toggle.activated")) );
		}
		else // disable drawing
		{
			if( !Configuration.general.showOverlay.get() )
				XRay.mc.player.sendMessage( new StringTextComponent(I18n.format("xray.toggle.deactivated")) );

			shutdownExecutor();
		}
	}

	public static int getRadius() { return distanceList[Configuration.general.radius.get()]; }

	public static void incrementCurrentDist()
	{
		if ( Configuration.general.radius.get() < distanceList.length - 1 )
			Configuration.general.radius.set(Configuration.general.radius.get() + 1);
		else
			Configuration.general.radius.set(0);
	}

	public static void decrementCurrentDist()
	{
		if ( Configuration.general.radius.get() > 0 )
			Configuration.general.radius.set(Configuration.general.radius.get() - 1);
		else
			Configuration.general.radius.set( distanceList.length - 1 );
	}

	/**
	 * Precondition: world and player must be not null
	 * Has player moved since the last region scan?
	 * This method does not update the last player location so consecutive
	 * calls yield the same result.
	 * @return true if the player has moved since the last blockFinder call
	 */
	private static boolean playerHasMoved()
	{
		return lastPlayerPos == null
			|| lastPlayerPos.getX() != XRay.mc.player.getPosition().getX()
			|| lastPlayerPos.getZ() != XRay.mc.player.getPosition().getZ();
	}

	private static void updatePlayerPosition()
	{
		lastPlayerPos = XRay.mc.player.getPosition();
	}

	/**
	 * Starts a region scan thread if possible, that is if:
	 * - we actually want to draw ores
	 * - we are not already scanning an area
	 * - either the player has moved since the last call
	 * - or we want to (and can) force a scan
	 *
	 * @param force should we force a block scan even if the player hasn't moved?
	 */
	public static synchronized void requestBlockFinder( boolean force )
	{
		if ( drawOres() && (task == null || task.isDone()) && (force || playerHasMoved()) ) // world/player check done by drawOres()
		{
			updatePlayerPosition(); // since we're about to run, update the last known position
			WorldRegion region = new WorldRegion( lastPlayerPos, getRadius() ); // the region to scan for ores
			task = executor.submit( new RenderEnqueue(region) );
		}
	}

	/**
	 * To be called at least when the game shutsdown
	 */
	public static void shutdownExecutor()
	{
		// Important. If drawOres is true when a player logs out then logs back in, the next requestBlockFinder will crash
		drawOres = false;
		try { executor.shutdownNow(); }
		catch (Throwable ignore) {}
	}
}
