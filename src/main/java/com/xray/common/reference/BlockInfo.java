package com.xray.common.reference;

import net.minecraft.util.math.Vec3i;

public class BlockInfo extends Vec3i // so we benefit from Vec3i's hashCode() and equals()
{
	public int[] color;

	public BlockInfo( int x, int y, int z, int[] c )
	{
		super( x, y, z );
		this.color = c;
	}

	public BlockInfo( Vec3i pos, int[] c )
	{
		this( pos.getX(), pos.getY(), pos.getZ(), c );
	}

}
