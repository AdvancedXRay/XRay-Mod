package fgtXray;

public class OreInfo {
	public String oreName; // Ore dictionary name (oreCopper). Null if custom id:meta
	public int id;
	public int meta;
	public int color;	// Color in 0xRRGGBB to draw.
	public boolean draw;// Should we draw this ore?
	
	public OreInfo( String name, int id, int meta, int color, boolean draw ){
		this.oreName = name;
		this.id = id;
		this.meta = meta;
		this.color = color;
		this.draw = draw;
	}
	
	public void disable(){
		this.draw = false;
	}
	
	public void enable(){
		this.draw = true;
	}
}
