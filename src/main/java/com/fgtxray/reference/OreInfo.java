// Structure for damn near everything.
package com.fgtXray.reference;

public class OreInfo
{
	public int id;         // Id of this block
	public String oreName;
	public int meta;       // Metadata value of this block. 0 otherwise.
	public int[] color;	   // Color in 0xRRGGBB to draw.
	public boolean draw;   // Should we draw this ore?
	
	public OreInfo( String name, int id, int meta, int[] color, boolean draw )
	{
		this.oreName = name;
		this.id = id;
		this.meta = meta;
		this.color = color;
		this.draw = draw;
	}
	
	public void disable() // Stop drawing this ore.
	{
		this.draw = false;
	}
	
	public void enable()  // Start drawing this ore.
	{
		this.draw = true;
	}
}
