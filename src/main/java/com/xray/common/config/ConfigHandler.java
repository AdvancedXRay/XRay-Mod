package com.xray.common.config;

import com.xray.common.XRay;
import com.xray.common.reference.OreInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigHandler
{
	private static Configuration config = null; // Save the config file handle for use later.
	private static Minecraft mc = Minecraft.getMinecraft();

	public static void setup(FMLPreInitializationEvent event )
	{
		config = new Configuration( event.getSuggestedConfigurationFile() );
		config.load();
		XRay.currentDist = config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0).getInt(); // Get our search distance.

		for( String category : config.getCategoryNames() ) // Iterate through each category in our config file.
		{
			ConfigCategory cat = config.getCategory( category );

			if( category.startsWith( "oredict.") ) // Dont iterate over the base category and make sure were on the oredict category.
			{
				String dictName = cat.get("dictname").getString();
				String guiName = cat.get("name").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int[] color = {cat.get("red").getInt(), cat.get("green").getInt(), cat.get("blue").getInt()};
				boolean enabled = cat.get("enabled").getBoolean(false);

				XRay.oredictOres.put(dictName, new OreInfo( guiName, guiName.replaceAll("\\s+", ""), id, meta, color, enabled ) );

			}
			else if( category.startsWith("customores.") )
			{
				String name = cat.get("name").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int[] color = {cat.get("red").getInt(), cat.get("green").getInt(), cat.get("blue").getInt()};
				boolean enabled = cat.get("enabled").getBoolean(false);

				XRay.customOres.add( new OreInfo( name, name.replaceAll("\\s+", ""), id, meta, color, enabled ) );
			}
		}
		config.save();
	}

	public static void add( String oreName, Integer id, Integer meta, int[] color )
	{
		config.load();
		String formattedname = oreName.replaceAll("\\s+", "").toLowerCase();

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

		for( String category : config.getCategoryNames() )
		{
			if( category.startsWith("customores.") )
			{
				config.get("customores."+formattedname, "red", "").set( color[0] );
				config.get("customores."+formattedname, "green", "").set( color[1] );
				config.get("customores."+formattedname, "blue", "").set( color[2] );
				config.get("customores."+formattedname, "enabled", "false").set( true );
				config.get("customores."+formattedname, "id", "").set( id );
				config.get("customores."+formattedname, "meta", "").set( meta );
				config.get("customores." + formattedname, "name", "").set(oreName);

			}
		}
		config.save();
	}

	// For updating single options
	public static void update(String string, boolean draw){
		if( string.equals("searchdist") ) // Save the new render distance.
		{
			config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0).set( XRay.currentDist);
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

	public static void updateInfo( OreInfo original, OreInfo newInfo )
	{
		String tmpCategory = "";
		for( String category : config.getCategoryNames() ) {
			String cleanStr = original.getOreName().toLowerCase();
			String[] splitCat = category.split("\\.");

			if( splitCat.length == 2 && splitCat[1].equals( cleanStr ) ) {
				if( splitCat[0].equals( "oredict" ) )
					tmpCategory = "oredict."+cleanStr;
				if( splitCat[0].equals( "customores" ) )
					tmpCategory = "customores."+cleanStr;

				if( !tmpCategory.isEmpty() ) {
					config.get(tmpCategory, "red", "").set( newInfo.color[0] );
					config.get(tmpCategory, "green", "").set( newInfo.color[1] );
					config.get(tmpCategory, "blue", "").set( newInfo.color[2] );
					config.get(tmpCategory, "name", "").set( newInfo.displayName );
					break;
				}
			}
		}
		config.save();
	}

	public static void remove( OreInfo original ) {
		for( String category : config.getCategoryNames() ) {
			String cleanStr = original.getOreName().toLowerCase();
			String[] splitCat = category.split("\\.");
			if( splitCat.length == 2 && splitCat[1].equals( cleanStr ) ) {
				System.out.println(cleanStr);

				if( splitCat[0].equals( "oredict" ) )
					config.removeCategory( config.getCategory("oredict."+cleanStr) );
				if( splitCat[0].equals( "customores" ) )
					config.removeCategory( config.getCategory("customores."+cleanStr) );
				break;
			}
		}
		config.save();
	}
}
