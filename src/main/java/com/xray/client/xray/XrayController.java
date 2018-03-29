package com.xray.client.xray;

import com.xray.client.render.ClientTick;
import com.xray.client.render.XrayRenderer;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.SearchList;
import com.xray.common.utils.WorldRegion;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3i;

public class XrayController
{
	private static Minecraft mc = Minecraft.getMinecraft();

	// Data
	private static int currentDist = 0; // Index for the distNumers array. Default search distance.
	public static final SearchList searchList = new SearchList();
	private static Vec3i lastPlayerPos = null;

	// Thread management
	private static Future task;
    private static ExecutorService executor;

	// Mod state, cannot be made public because we need to know when the values change
	private static boolean drawOres = false; // Off by default
	private static boolean drawCaves = false;

	// Public accesors
	public static boolean drawOres() { return drawOres && mc.world != null && mc.player != null; }
	public static boolean drawCaves() { return drawCaves; }
	public static void toggleDrawCaves() { drawCaves = !drawCaves; }
	public static void toggleDrawOres()
	{
		if ( !drawOres ) // enable drawing
		{
			XrayRenderer.ores.clear(); // first, clear the buffer
			drawOres = true; // then, enable drawing
			executor = Executors.newSingleThreadExecutor();
			requestBlockFinder( true ); // finally, force a refresh
		}
		else // disable drawing
		{
			drawOres = false;
			executor.shutdownNow(); // no need to have a thread pool running if we don't draw ores
		}
	}
	public static int getCurrentDist() { return currentDist; }
	public static int getRadius() { return XRay.distNumbers[currentDist]; }
	public static void setCurrentDist( int dist )
	{
		currentDist = dist;
		ConfigHandler.storeCurrentDist();
	}
	public static void incrementCurrentDist()
	{
		if ( currentDist < XRay.distNumbers.length - 1 )
			currentDist++;
		else
			currentDist = 0;
		ConfigHandler.storeCurrentDist();
	}
	public static void decrementCurrentDist()
	{
		if ( currentDist > 0 )
			currentDist--;
		else
			currentDist = XRay.distNumbers.length - 1;
		ConfigHandler.storeCurrentDist();
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
			|| lastPlayerPos.getX() != mc.player.getPosition().getX()
			|| lastPlayerPos.getZ() != mc.player.getPosition().getZ();
	}

	private static void updatePlayerPosition()
	{
		lastPlayerPos = mc.player.getPosition();
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
			task = executor.submit( new ClientTick(region) );
		}
	}
}
