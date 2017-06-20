package com.fgtXray.config;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.fgtXray.FgtXRay;
import com.fgtXray.reference.OreInfo;


public class ConfigHandler
{
	private static Configuration config = null; // Save the config file handle for use later.
	private static Minecraft mc = Minecraft.getMinecraft();

	public static void setup(FMLPreInitializationEvent event )
	{
		config = new Configuration( event.getSuggestedConfigurationFile() );
		config.load();
		FgtXRay.distIndex = config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0).getInt(); // Get our search distance.

		for( String category : config.getCategoryNames() ) // Iterate through each category in our config file.
		{
			ConfigCategory cat = config.getCategory( category );

			if( category.startsWith( "oredict.") ) // Dont iterate over the base category and make sure were on the oredict category.
			{
				String dictName = cat.get("dictname").getString();
				String guiName = cat.get("guiname").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int[] color = {cat.get("red").getInt(), cat.get("green").getInt(), cat.get("blue").getInt()};
				boolean enabled = cat.get("enabled").getBoolean(false);

				FgtXRay.oredictOres.put(dictName, new OreInfo( guiName, id, meta, color, enabled ) );

			}
			else if( category.startsWith("customores.") )
			{
				String name = cat.get("name").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int[] color = {cat.get("red").getInt(), cat.get("green").getInt(), cat.get("blue").getInt()};
				boolean enabled = cat.get("enabled").getBoolean(false);

				FgtXRay.customOres.add( new OreInfo( name, id, meta, color, enabled ) );
			}
		}
		config.save();
	}

	public static void add( String oreName, String ore, int[] color )
	{
		config.load();
		String formattedname = oreName.replace("\\s+", "").toLowerCase();

		// check if entry exists
		for( String category : config.getCategoryNames() )
		{
			if( category.startsWith("customores.") )
			{
				if( config.get("customores."+formattedname, "name", "").getString() == formattedname )
				{
					String notify = String.format( "[XRay] %s already exists. Please enter a different name. ", oreName );
					mc.ingameGUI.getChatGUI().printChatMessage( new TextComponentString(notify));
					return;
				}
			}
		}

		int oreId = Integer.parseInt(ore.split( ":" )[0]);
		// Don't do this if it does not exist... Stupid me
		int oreMeta = ore.contains(":") ? Integer.parseInt(ore.split( ":" )[1]) : 0;

		for( String category : config.getCategoryNames() )
		{
			if( category.startsWith("customores.") )
			{
				config.get("customores."+formattedname, "red", "").set( color[0] );
				config.get("customores."+formattedname, "green", "").set( color[1] );
				config.get("customores."+formattedname, "blue", "").set( color[2] );
				config.get("customores."+formattedname, "enabled", "false").set( true );
				config.get("customores."+formattedname, "id", "").set( oreId );
				config.get("customores."+formattedname, "meta", "").set( oreMeta );
				config.get("customores." + formattedname, "name", "").set(oreName);

			}
		}
		config.save();
	}

	// For updating single options
	public static void update(String string, boolean draw){
		if( string.equals("searchdist") ) // Save the new render distance.
		{
			config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0).set( FgtXRay.distIndex );
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

	// TODO: add remove option - AoKMiKeY
	public static void remove( String name )
	{

	}
}