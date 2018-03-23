package com.xray.common.reference;

public class OreInfo
{
	public int id;         // Id of this block
	public String oreName;
	public int meta;       // Metadata value of this block. 0 otherwise.
	public int[] color;	   // Color in 0xRRGGBB to draw.
	public boolean draw;   // Should we draw this ore?
	public String displayName;
	public String catName;

        public OreInfo( int id, int meta)
        {
            this.id = id;
            this.meta = meta;
        }

	public OreInfo( String name, int[] color, boolean draw ) {
		this.oreName = name;
		this.displayName = "";
		this.catName = "";
		this.id = 0;
		this.meta = 0;
		this.color = color;
		this.draw = draw;
	}

	public OreInfo( String displayName, String catName, String name, int id, int meta, int[] color, boolean draw )
	{
		this.oreName = name;
		this.displayName = displayName;
		this.id = id;
		this.meta = meta;
		this.color = color;
		this.draw = draw;
		this.catName = catName;
	}

	public String getCatName() {
		return catName;
	}

	public void setCatName(String catName) {
		this.catName = catName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getOreName() {
		return oreName;
	}

	public int getMeta() {
		return meta;
	}

	public int getId() {
		return id;
	}

	public void disable() // Stop drawing this ore.
	{
		this.draw = false;
	}

	public void enable()  // Start drawing this ore.
	{
		this.draw = true;
	}

        @Override
        public int hashCode()
        {
                return 37 * id + meta;
        }

        @Override
        public boolean equals(Object o)
        {
                if (o == null) return false;
                if (this == o) return true;
                if (!(o instanceof OreInfo)) return false;
                OreInfo t = (OreInfo) o;
                return id == t.id && meta == t.meta;
        }
}
