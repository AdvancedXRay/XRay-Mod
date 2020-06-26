package com.xray.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

// so we benefit from Vec3i's hashCode() and equals()
public class RenderBlockProps extends Vector3i {
	private int color;

	public RenderBlockProps(double x, double y, double z, int color) {
		super( x, y, z );
		this.color = color;
	}

	public RenderBlockProps(BlockPos vec, int color) {
		this( vec.getX(), vec.getY(), vec.getZ(), color );
	}

	public int getColor() {
		return color;
	}
}
