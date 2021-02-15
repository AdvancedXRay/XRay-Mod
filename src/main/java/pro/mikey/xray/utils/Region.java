package pro.mikey.xray.utils;

import net.minecraft.util.math.vector.Vector3i;

/**
 * A bounding box representing a world 3D area in both world and chunk coords.
 */
public class Region {

	public int minX, minY, minZ, maxX, maxY, maxZ;
	public int minChunkX, minChunkY, minChunkZ, maxChunkX, maxChunkY, maxChunkZ;

	/**
	 * Constructs a world region from a player location and a radius.
	 * Vertical extend is 92 blocks down and 32 blocks up
	 * @param pos a world position
	 * @param radius a block radius
	 */
	public Region(Vector3i pos, int radius)
	{
		minX = pos.getX() - radius;
		maxX = pos.getX() + radius;
		minY = Math.max(0, pos.getY() - 92);
		maxY = Math.min(255, pos.getY() + 32);
		minZ = pos.getZ() - radius;
		maxZ = pos.getZ() + radius;
		minChunkX = minX >> 4;
		maxChunkX = maxX >> 4;
		minChunkY = minY >> 4;
		maxChunkY = maxY >> 4;
		minChunkZ = minZ >> 4;
		maxChunkZ = maxZ >> 4;
	}
}
