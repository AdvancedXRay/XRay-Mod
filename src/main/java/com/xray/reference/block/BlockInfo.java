package com.xray.reference.block;

import net.minecraft.util.math.Vec3i;

public class BlockInfo extends Vec3i // so we benefit from Vec3i's hashCode() and equals()
{
	public int[] color;
	public double alpha;

	public BlockInfo( int x, int y, int z, int[] color, double alpha )
	{
		super( x, y, z );
		this.color = color;
		this.alpha = alpha;
	}

	public BlockInfo( Vec3i pos, int[] c, double alpha )
	{
		this( pos.getX(), pos.getY(), pos.getZ(), c, alpha );
	}

}
