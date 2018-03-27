package com.xray.client.xray;

import com.xray.client.render.ClientTick;
import com.xray.client.render.XrayRenderer;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.OreInfo;
import com.xray.common.utils.WorldRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;

public class XrayController
{
	static Minecraft mc = Minecraft.getMinecraft();

	// Data
	public static List<OreInfo> searchList = new ArrayList<>(); // List of ores/blocks to search for.
	private static Vec3i lastPlayerPos = null;
	//private static int lastPlayerDim = 0;

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

	/**
	 * TODO Map is rebuilt on each call. If we block direct access to
	 * searchList, we can buffer this map. Not a real big deal tho.
	 * @return searchList as a filtered map containing only drawable ores
	 */
	public static Map<OreInfo, OreInfo> getDrawableOres()
	{
		Map<OreInfo, OreInfo> ores = new HashMap<>();
		for ( OreInfo ore : searchList )
			if ( ore.draw )
				ores.put( ore, ore );
		return ores;
	}

	/**
	 * Precondition: world and player must be not null
	 * Has player moved since the last region scan?
	 * This method does not update the last player location so consecutive
	 * calls yield the same result.
	 * @return true if the player has moved since the last blockFinder call
	 */
	public static boolean playerHasMoved()
	{
		return lastPlayerPos == null
			|| lastPlayerPos.getX() != mc.player.getPosition().getX()
			|| lastPlayerPos.getZ() != mc.player.getPosition().getZ();
			//|| lastPlayerDim != mc.player.dimension;
	}

	public static void updatePlayerPosition()
	{
		lastPlayerPos = mc.player.getPosition();
		//lastPlayerDim = mc.player.dimension;
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
			WorldRegion region = new WorldRegion(lastPlayerPos, XRay.distNumbers[ XRay.currentDist ]); // the region to scan for ores
			task = executor.submit( new ClientTick(region) );
		}
	}

	/**
	 * Add's the ore to the Config and adds to the Searchlist
	 * @param oreId
	 * @param oreMeta
	 * @param name
	 * @param color
	 */
	public static void add( int oreId, int oreMeta, String name, int[] color ) // Takes a string of id:meta or oreName to add to our search list.
	{
		if( searchList.contains( new OreInfo(oreId, oreMeta) ) ) {
			mc.player.sendMessage(new TextComponentString("[XRay] "+ I18n.format("xray.message.already_exists")));
			return;
		}

		searchList.add( new OreInfo( name, name.replaceAll("\\s+", "").toLowerCase(), name.replaceAll("\\s+", ""), oreId, oreMeta, color, true ) );
		ConfigHandler.add(name, oreId, oreMeta, color);

		mc.player.sendMessage(new TextComponentString("[XRay] "+I18n.format( "xray.message.added_block", name )));
	}

	public static void update( OreInfo original, String name, int[] color, int meta ) {
		if( !searchList.contains( original ) )
			return;

		OreInfo current = searchList.get(searchList.indexOf(original));
		current.displayName = name;
		current.color = color;
		current.meta = meta;

		ConfigHandler.updateInfo(original, current);
		mc.player.sendMessage(new TextComponentString("[XRay] "+I18n.format( "xray.message.updated_block" )));
	}

	public static void remove( OreInfo original ) {
		if( !searchList.contains( original ) )
			return;

		searchList.remove( original );
		ConfigHandler.remove(original);

		mc.player.sendMessage(new TextComponentString("[XRay] "+I18n.format( "xray.message.remove_block", original.getOreName() )));
	}
}
