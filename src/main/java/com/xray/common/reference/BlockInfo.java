package com.xray.common.reference;

import net.minecraft.util.math.Vec3i;

public class BlockInfo extends Vec3i // so we benefit from Vec3i's hashCode() and equals()
{
	public int[] color;

	public BlockInfo( int bx, int by, int bz, int[] c )
	{
		super( bx, by, bz );
		this.color = c;
	}

	public BlockInfo( Vec3i pos, int[] c )
	{
		this( pos.getX(), pos.getY(), pos.getZ(), c );
	}

}
