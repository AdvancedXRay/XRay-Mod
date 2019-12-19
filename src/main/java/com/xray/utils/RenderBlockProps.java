package com.xray.utils;

import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

// so we benefit from Vec3i's hashCode() and equals()
public class RenderBlockProps extends Vec3i {
	private Color color;
	private double alpha;

	// This is only used for rendering.
	private Block block;
	private boolean ignored = false;

	public RenderBlockProps(int x, int y, int z, Block block, Color color, double alpha) {
		super( x, y, z );
		this.color = color;
		this.alpha = alpha;
		this.block = block;
	}

	public RenderBlockProps(int x, int y, int z, Block block, Color color, double alpha, boolean ignored) {
		this(x, y, z, block, color, alpha);
		this.ignored = ignored;
	}

	public RenderBlockProps(Vec3i pos, Block block, Color color, double alpha ) {
		this( pos.getX(), pos.getY(), pos.getZ(), block, color, alpha );
	}

	public Color getColor() {
		return color;
	}

	public double getAlpha() {
		return alpha;
	}

	public Block getBlock() {
		return block;
	}

	public boolean isIgnored() {
		return ignored;
	}
}
