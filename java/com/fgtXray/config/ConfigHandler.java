package com.fgtXray.config;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.fgtXray.FgtXRay;
import com.fgtXray.reference.OreInfo;
import org.lwjgl.Sys;

public class ConfigHandler
{
	public static Configuration config = null; // Save the config file handle for use later.

	public static void setup(FMLPreInitializationEvent event )
	{
		config = new Configuration( event.getSuggestedConfigurationFile() );
		config.load();
		FgtXRay.distIndex = config.get(config.CATEGORY_GENERAL, "searchdist", 0).getInt(); // Get our search distance.
		
		for( String category : config.getCategoryNames() ) // Iterate through each category in our config file.
		{
			ConfigCategory cat = config.getCategory( category );
			
			if( category.startsWith( "oredict.") ) // Dont iterate over the base category and make sure were on the oredict category.
			{
				String dictName = cat.get("dictname").getString();
				String guiName = cat.get("guiname").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int color = cat.get("color").getInt();
				boolean enabled = cat.get("enabled").getBoolean(false);
				
				FgtXRay.oredictOres.put(dictName, new OreInfo( guiName, id, meta, color, enabled ) );
				
			}
			else if( category.startsWith("customores.") )
			{
				String name = cat.get("name").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int color = cat.get("color").getInt();
				boolean enabled = cat.get("enabled").getBoolean(false);
				
				FgtXRay.customOres.add( new OreInfo( name, id, meta, color, enabled ) );
			}
		}
		config.save();
	}
	
	public static void add( String oreName )
    {
	}
	
	// For updating single options
	public static void update(String string, boolean draw){
		if( string.equals("searchdist") ) // Save the new render distance.
		{
			config.get(config.CATEGORY_GENERAL, "searchdist", 0).set( FgtXRay.distIndex );
			config.save();
			return;
		}
		
		for( String category : config.getCategoryNames() ) // Figure out if this is a custom or dictionary ore.
		{
			String cleanStr = string.replaceAll("\\s+", "").toLowerCase(); // No whitespace or capitals in the config file categories.
			String[] splitCat = category.split("\\.");
			
			if( splitCat.length == 2 )
			{
				if( splitCat[0].equals( "oredict" ) && splitCat[1].equals( cleanStr ) ) // Check if the current iteration is the correct category (oredict.emerald)
				{
					config.get("oredict."+cleanStr, "enabled", false).set( draw );
					
				}
				else if ( splitCat[0].equals( "customores" ) && splitCat[1].equals( cleanStr ) )
				{
					config.get("customores."+cleanStr, "enabled", false).set( draw );
				}
			}
		}
		config.save();
	}
}