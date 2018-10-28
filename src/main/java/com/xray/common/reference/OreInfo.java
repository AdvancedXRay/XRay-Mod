package com.xray.common.reference;

import com.xray.common.config.ConfigHandler;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Represents an ore/block for the mod, as seen in GUI and config
 * - a block name,
 * - a metadata
 * - a color
 * - 2 options (drawable and oredict)
 */
@Deprecated
public class OreInfo implements Comparable<OreInfo>
{
	public static final int[] DEFAULT_COLOR = new int[] { 128, 128, 128 };

	private final String name; // the registry name "mod:blockname"
	private final int meta;    // the metadata
	int[] color;               // RGB color (3 bytes)
	boolean draw;
	private boolean useOreDict; // if true, XRayController will use all ore alternatives for scanning chunks

	public OreInfo( String name, int meta, int[] color, boolean draw, boolean useOreDict)
	{
		this.name = name;
		this.meta = meta;
		this.color = color;
		this.draw = draw;
		this.useOreDict = useOreDict;
	}

	public OreInfo( String name, int meta )
	{
		this( name, meta, DEFAULT_COLOR, false, false );
	}

	private OreInfo( ItemStack stack, int[] color, boolean draw, boolean useOreDict)
	{
		this( stack.getItem().getRegistryName().toString(), stack.getItemDamage(), color, draw, useOreDict);
	}

	public OreInfo( ItemStack stack )
	{
		this( stack, DEFAULT_COLOR, true, false );
	}

	public static OreInfo duplicate( OreInfo ore )
	{
		int[] rgb = new int[] { ore.color[0], ore.color[1], ore.color[2] };
		return new OreInfo( ore.getName(), ore.getMeta(), rgb, ore.isDrawable(), ore.useOredict() );
	}

	/**
	 * Helper for creating default config from ore dictionary names.
	 * Given an ore name, tries to find an actual instance of such ore.
	 * @param name OreDictionary name (eg. oreIron)
	 * @param color a color for this ore
	 * @param draw shall we draw it by default?
	 * @return An ore registered with this dictionary name, null if none found
	 */
	public static OreInfo firstOreInDictionary( String name, int[] color, boolean draw )
	{
		NonNullList<ItemStack> ores = OreDictionary.getOres( name );
		if ( ores.isEmpty() || ores.get(0).isEmpty() )
			return null;
		ItemStack stack = ores.get( 0 );

		return new OreInfo( stack, color, draw, true );
	}

	// Public Accessors
	public String getName() { return name; }
	public int getMeta() { return meta; }
	public int[] getColor() { return color; }
	public boolean isDrawable() { return draw; }
	public boolean useOredict() { return useOreDict; }
	public void toggleOredict() { useOreDict = !useOreDict; }
	public Block getBlock() { return Block.getBlockFromName( getName() ); }
	public int getId() { return Block.getIdFromBlock( getBlock() ); }
	public ItemStack getItemStack() { return new ItemStack( getBlock(), 1, getMeta() ); }

	public String getDisplayName()
	{
		ItemStack stack = getItemStack();
		if ( stack.isEmpty() ) // for lava and likes
			return getBlock().getLocalizedName();

		return stack.getDisplayName();
	}

	public void setColor(int[] c)
	{
		if ( c != null && c.length == 3 )
		{
			color[0] = clampColor(c[0]);
			color[1] = clampColor(c[1]);
			color[2] = clampColor(c[2]);
		}
	}

	// Config
	public void addToConfig( ConfigCategory parent )
	{
		ConfigCategory cat = new ConfigCategory( getName() + ":" + meta, parent);
		cat.put("name", new Property("name", name, Property.Type.STRING));
		cat.put("meta", new Property("meta", "" + meta, Property.Type.INTEGER));
		cat.put("red", new Property("red", "" + color[0], Property.Type.INTEGER));
		cat.put("green", new Property("green", "" + color[1], Property.Type.INTEGER));
		cat.put("blue", new Property("blue", "" + color[2], Property.Type.INTEGER));
		cat.put("enabled", new Property("enabled", "" + draw, Property.Type.BOOLEAN));
		cat.put("useoredict", new Property("useoredict", "" + useOreDict, Property.Type.BOOLEAN));
		cat.setPropertyOrder( ConfigHandler.ORDER );
	}

	public static OreInfo fromConfigCategory( ConfigCategory cat ) throws Exception
	{
		Map<String, Property> props = cat.getValues();
		String name = cat.get("name").getString();
		int meta = cat.get("meta").getInt();
		int red =   clampColor( cat.get("red").getInt() );
		int green = clampColor( cat.get("green").getInt() );
		int blue =  clampColor( cat.get("blue").getInt() );
		boolean draw = cat.get("enabled").getBoolean();
		boolean useOredict = cat.get("useoredict").getBoolean();
		return new OreInfo( name, meta, new int[] {red, green, blue}, draw, useOredict );
	}

	private static int clampColor(int c)
	{
		return c < 0 ? 0 : c > 255 ? 255 : c;
	}

	// Comparable Interfaces
	@Override
	public int hashCode()
	{
		return name.hashCode() | ((meta + 1) << 16);
	}

	@Override
	public boolean equals( Object o )
	{
		if ( o == null || !(o instanceof OreInfo) ) return false;
		OreInfo t = (OreInfo) o;
		return name.equals(t.name) && meta == t.meta;
	}

	@Override
	public int compareTo( OreInfo t )
	{
		if ( t == null) return 1;
		int a = name.compareTo(t.name);
		if ( a == 0 ) return meta - t.meta;
		else return a;
	}
}
