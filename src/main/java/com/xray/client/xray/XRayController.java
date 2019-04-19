package com.xray.client.xray;

import com.xray.client.render.ClientTick;
import com.xray.client.render.XrayRenderer;
import com.xray.common.Configuration;
import com.xray.common.XRay;
import com.xray.common.reference.block.BlockStore;
import com.xray.common.utils.WorldRegion;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class XRayController
{
	// Block blackList
	public static ArrayList blackList = new ArrayList<Block>() {{
		add(Blocks.AIR);
		add(Blocks.BEDROCK);
		add(Blocks.STONE);
		add(Blocks.GRASS);
		add(Blocks.DIRT);
	}};

    /**
     * Index for {@link XRay#distanceList}
     */
	private static int currentDist = 0;
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
			XrayRenderer.ores.clear(); // first, clear the buffer
			executor = Executors.newSingleThreadExecutor();
			drawOres = true; // then, enable drawing
			requestBlockFinder( true ); // finally, force a refresh

			if( !Configuration.showOverlay )
				XRay.mc.player.sendMessage( new TextComponentString(I18n.format("xray.toggle.activated")) );
		}
		else // disable drawing
		{
			if( !Configuration.showOverlay )
				XRay.mc.player.sendMessage( new TextComponentString(I18n.format("xray.toggle.deactivated")) );

			shutdownExecutor();
		}
	}

	public static int getRadius() { return XRay.distanceList[Configuration.radius]; }

	public static void incrementCurrentDist()
	{
		if ( currentDist < XRay.distanceList.length - 1 )
			currentDist++;
		else
			currentDist = 0;

		Configuration.radius = currentDist;
	}

	public static void decrementCurrentDist()
	{
		if ( currentDist > 0 )
			currentDist--;
		else
			currentDist = XRay.distanceList.length - 1;

		Configuration.radius = currentDist;
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
			task = executor.submit( new ClientTick(region) );
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
