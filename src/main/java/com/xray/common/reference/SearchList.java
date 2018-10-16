package com.xray.common.reference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Data structure to hold the monitored objects (blocks, entities)
 */
@Deprecated
public class SearchList
{

	private final Set<OreInfo> ores = new HashSet<>();
	private final Map<BlockId, int[]> drawableBlocks = new HashMap<>();
	//private final Map<String, Set<EntityInfo>> entityMap = new HashMap<>();

	public boolean contains( OreInfo ore )
	{
		return ores.contains( ore );
	}

	public OreInfo getOre( OreInfo ore )
	{
		if ( !ores.contains( ore ) )
			return null;
		for (OreInfo o : ores ) // Set.get()
		{
			if ( ore.equals( o ) )
				return o;
		}
		return null; // Cannot happen
	}

	public Collection<OreInfo> getOres()
	{
		return ores;
	}

	public boolean addOre( OreInfo oreInfo )
	{
		if ( oreInfo == null || ores.contains( oreInfo ) )
			return false;
		ores.add( oreInfo );
		refreshDrawableBlocks();
		return true;
	}

	public boolean addOres( Collection<OreInfo> oresToAdd )
	{
		boolean added = false;
		for ( OreInfo ore : oresToAdd ) // don't use addAll() here, we need to make sure we are not adding null ores
			if ( ore != null )
				added |= ores.add( ore );
		if ( added )
			refreshDrawableBlocks();
		return added;
	}

	public boolean removeOre( OreInfo ore )
	{
		boolean ret = ores.remove( ore );
		if ( ret )
			refreshDrawableBlocks();
		return ret;
	}

	public void toggleOreDrawable( OreInfo ore )
	{
		if ( ores.contains( ore ) )
		{
			for ( OreInfo o : ores ) // Set.get()
			{
				if ( o.equals(ore) )
				{
					o.draw = !o.draw;
					break;
				}
			}
			refreshDrawableBlocks();
		}
	}

	public void updateOre( OreInfo ore )
	{
		if ( ores.contains( ore ) )
		{
			ores.remove( ore );
			ores.add( ore );
			refreshDrawableBlocks();
		}
	}

	public Map<BlockId, int[]> getDrawableBlocks()
	{
		return drawableBlocks;
	}

	/**
	 * To be called each time the ores map is altered.
	 * This can be optimized as there is no real need to cycle through the
	 * whole map everytime, but the number of ores is so small it's not
	 * worth it. Code is much simpler like this.
	 */
	private void refreshDrawableBlocks()
	{
		drawableBlocks.clear();
		for ( OreInfo ore : ores )
		{
			Set<BlockId> blocks = BlockId.drawablesFromOreInfo( ore );
			for ( BlockId b : blocks )
				drawableBlocks.put( b, ore.color );
		}
	}
}
